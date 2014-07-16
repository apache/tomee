/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.assembler.classic;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.apache.openejb.ClassLoaderUtil;
import org.apache.openejb.core.cmp.CmpUtil;
import org.apache.openejb.core.cmp.cmp2.Cmp1Generator;
import org.apache.openejb.core.cmp.cmp2.Cmp2Generator;
import org.apache.openejb.core.cmp.cmp2.CmrField;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.UrlCache;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;
import org.apache.openjpa.enhance.PCEnhancer;
import org.apache.openjpa.jdbc.conf.JDBCConfigurationImpl;
import org.apache.openjpa.jdbc.meta.MappingRepository;
import org.apache.openjpa.jdbc.meta.NoneMappingDefaults;
import org.apache.openjpa.jdbc.sql.HSQLDictionary;
import org.apache.openjpa.lib.util.BytecodeWriter;
import org.apache.openjpa.meta.AccessCode;
import org.apache.openjpa.meta.MetaDataModes;
import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.jdbc.PersistenceMappingFactory;
import org.apache.xbean.asm5.ClassReader;
import org.apache.xbean.asm5.ClassWriter;
import serp.bytecode.BCClass;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static java.util.Arrays.asList;

/**
 * Creates a jar file which contains the CMP implementation classes and the cmp entity mappings xml file.
 */
public class CmpJarBuilder {

    private final ClassLoader tempClassLoader;

    private File jarFile;
    private final Set<String> entries = new TreeSet<String>();
    private final AppInfo appInfo;

    public CmpJarBuilder(final AppInfo appInfo, final ClassLoader classLoader) {
        this.appInfo = appInfo;
        tempClassLoader = ClassLoaderUtil.createTempClassLoader(classLoader);
    }

    public File getJarFile() throws IOException {
        if (jarFile == null) {
            generate();
        }
        return jarFile;
    }

    /**
     * Generate the CMP jar file associated with this
     * deployed application.  The generated jar file will
     * contain generated classes and metadata that will
     * allow the JPA engine to manage the bean persistence.
     *
     * @throws IOException
     */
    private void generate() throws IOException {
        // Don't generate an empty jar.  If there are no container-managed beans defined in this 
        // application deployment, there's nothing to do. 
        if (!hasCmpBeans()) {
            return;
        }

        JarOutputStream jarOutputStream = null;

        try {
            jarOutputStream = openJarFile(this);

            // Generate CMP implementation classes
            final Map<String, Entry> classes = new HashMap<>();
            for (final EjbJarInfo ejbJar : appInfo.ejbJars) {
                for (final EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
                    if (beanInfo instanceof EntityBeanInfo) {
                        final EntityBeanInfo entityBeanInfo = (EntityBeanInfo) beanInfo;
                        if ("CONTAINER".equalsIgnoreCase(entityBeanInfo.persistenceType)) {
                            final Entry entry = generateClass(jarOutputStream, entityBeanInfo);
                            classes.put(entry.clazz, entry);
                        }
                    }
                }
            }

            final URLClassLoaderFirst thisClassLoader = new EnhancingClassLoader(tempClassLoader, classes);
            final StoringBytecodeBytecodeWriter writer = new StoringBytecodeBytecodeWriter(thisClassLoader);
            doEnhanceWithOpenJPA(classes, thisClassLoader, writer, appInfo.cmpMappingsXml);

            for (final Entry e : classes.values()) {
                // add the generated class to the jar
                final byte[] bytes = writer.bytecodes.get(e.clazz);
                final byte[] bytecode = bytes != null ? bytes : e.bytes;
                final File f = new File("/tmp/dump/" + e.name + ".class");
                f.getParentFile().mkdirs();
                final FileOutputStream w = new FileOutputStream(f);
                w.write(bytecode);
                w.close();
                addJarEntry(jarOutputStream, e.name, bytecode);
            }
            if (appInfo.cmpMappingsXml != null) {
                // System.out.println(appInfo.cmpMappingsXml);
                addJarEntry(jarOutputStream, "META-INF/openejb-cmp-generated-orm.xml", appInfo.cmpMappingsXml.getBytes());
            }
        } catch (final Throwable e) {

            if (null != jarFile && !jarFile.delete()) {
                jarFile.deleteOnExit();
            }
            jarFile = null;

            throw new IOException("CmpJarBuilder.generate()", e);
        } finally {
            close(jarOutputStream);
        }
    }

    private void doEnhanceWithOpenJPA(final Map<String, Entry> classes, final ClassLoader tmpLoader,
                                      final StoringBytecodeBytecodeWriter writer,
                                      final String cmpMappingsXml) throws ClassNotFoundException, IOException {
        final Thread th = Thread.currentThread();
        final ClassLoader old = th.getContextClassLoader();
        th.setContextClassLoader(tmpLoader);
        try {
            final JDBCConfigurationImpl conf = new JDBCConfigurationImpl();
            conf.setDBDictionary(new HSQLDictionary());
            final MappingRepository repos = new MappingRepository();

            final Set<Class<?>> tmpClasses = new HashSet<>();
            for (final Entry e : classes.values()) {
                tmpClasses.add(tmpLoader.loadClass(e.clazz));
            }

            final PersistenceMappingFactory factory = new PersistenceMappingFactory() {
                @Override
                public Set getPersistentTypeNames(final boolean devpath, final ClassLoader envLoader) {
                    getXMLParser().setValidating(false);
                    try { // xml only
                        return parsePersistentTypeNames(tmpLoader);
                    } catch (final IOException e) {
                        // no-op
                    }
                    return super.getPersistentTypeNames(devpath, envLoader);
                }
            };

            final File tempFile = File.createTempFile("OpenEJBGenerated.", ".xml", tmpDir());
            tempFile.deleteOnExit();
            final FileWriter tmpMapping = new FileWriter(tempFile);
            try {
                tmpMapping.write(cmpMappingsXml);
            } finally {
                tmpMapping.close();
            }
            factory.setFiles(asList(tempFile));

            repos.setConfiguration(conf);
            repos.setMetaDataFactory(factory);
            repos.setMappingDefaults(NoneMappingDefaults.getInstance());
            repos.setResolve(MetaDataModes.MODE_NONE);
            repos.setValidate(MetaDataRepository.VALIDATE_NONE);
            for (final Class<?> tmpClass : tmpClasses) {
                repos.addMetaData(tmpClass);
            }

            final PCEnhancer.Flags flags = new PCEnhancer.Flags();
            flags.tmpClassLoader = false;

            PCEnhancer.run(conf, null, flags, repos, writer, tmpLoader);

            tempFile.delete(); // try to delete it now, not a big deal otherwise,deleteOnExit will do it
        } catch (final Throwable thr) {
            // shouldn't be created in normal case
            Logger.getInstance(LogCategory.OPENEJB, CmpJarBuilder.class).error(thr.getMessage(), thr);
        } finally {
            th.setContextClassLoader(old);
        }
    }

    /**
     * Test if an application contains and CMP beans that
     * need to be mapped to the JPA persistence engine.  This
     * will search all of the ejb jars contained within
     * the application looking for Entity beans with
     * a CONTAINER persistence type.
     *
     * @return true if the application uses container managed beans,
     * false if none are found.
     */
    private boolean hasCmpBeans() {
        for (final EjbJarInfo ejbJar : appInfo.ejbJars) {
            for (final EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
                if (beanInfo instanceof EntityBeanInfo) {
                    final EntityBeanInfo entityBeanInfo = (EntityBeanInfo) beanInfo;
                    if ("CONTAINER".equalsIgnoreCase(entityBeanInfo.persistenceType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Generate a class file for a CMP bean, writing the
     * byte data for the generated class into the jar file
     * we're constructing.
     *
     * @param jarOutputStream The target jarfile.
     * @param entityBeanInfo  The descriptor for the entity bean we need to wrapper.
     * @throws IOException
     */
    private Entry generateClass(final JarOutputStream jarOutputStream, final EntityBeanInfo entityBeanInfo) throws IOException {
        // don't generate if there is aleady an implementation class
        final String cmpImplClass = CmpUtil.getCmpImplClassName(entityBeanInfo.abstractSchemaName, entityBeanInfo.ejbClass);
        final String entryName = cmpImplClass.replace(".", "/") + ".class";
        if (entries.contains(entryName) || tempClassLoader.getResource(entryName) != null) {
            return null;
        }

        // load the bean class, which is used by the generator
        Class<?> beanClass = null;
        try {
            beanClass = tempClassLoader.loadClass(entityBeanInfo.ejbClass);
        } catch (final ClassNotFoundException e) {
            throw (IOException) new IOException("Could not find entity bean class " + beanClass).initCause(e);
        }

        // and the primary key class, if defined.  
        Class<?> primKeyClass = null;
        if (entityBeanInfo.primKeyClass != null) {
            try {
                primKeyClass = tempClassLoader.loadClass(entityBeanInfo.primKeyClass);
            } catch (final ClassNotFoundException e) {
                throw (IOException) new IOException("Could not find entity primary key class " + entityBeanInfo.primKeyClass).initCause(e);
            }
        }

        // now generate a class file using the appropriate level of CMP generator.  
        final byte[] bytes;
        // NB:  We'll need to change this test of CMP 3 is ever defined!
        if (entityBeanInfo.cmpVersion != 2) {
            final Cmp1Generator cmp1Generator = new Cmp1Generator(cmpImplClass, beanClass);
            // A primary key class defined as Object is an unknown key.  Mark it that 
            // way so the generator will create the automatically generated key. 
            if ("java.lang.Object".equals(entityBeanInfo.primKeyClass)) {
                cmp1Generator.setUnknownPk(true);
            }
            bytes = cmp1Generator.generate();
        } else {

            // generate the implementation class
            final Cmp2Generator cmp2Generator = new Cmp2Generator(cmpImplClass,
                beanClass,
                entityBeanInfo.primKeyField,
                primKeyClass,
                entityBeanInfo.cmpFieldNames.toArray(new String[entityBeanInfo.cmpFieldNames.size()]));

            // we need to have a complete set of the defined CMR fields available for the 
            // generation process as well. 
            for (final CmrFieldInfo cmrFieldInfo : entityBeanInfo.cmrFields) {
                final EntityBeanInfo roleSource = cmrFieldInfo.mappedBy.roleSource;
                final CmrField cmrField = new CmrField(cmrFieldInfo.fieldName,
                    cmrFieldInfo.fieldType,
                    CmpUtil.getCmpImplClassName(roleSource.abstractSchemaName, roleSource.ejbClass),
                    roleSource.local,
                    cmrFieldInfo.mappedBy.fieldName,
                    cmrFieldInfo.synthetic);
                cmp2Generator.addCmrField(cmrField);
            }
            bytes = cmp2Generator.generate();
        }

        return new Entry(cmpImplClass, entryName, bytes);
    }

    /**
     * Insert a file resource into the generated jar file.
     *
     * @param jarOutputStream The target jar file.
     * @param fileName        The name we're inserting.
     * @param bytes           The file byte data.
     * @throws IOException
     */
    private void addJarEntry(final JarOutputStream jarOutputStream, String fileName, final byte[] bytes) throws IOException {
        // add all missing directory entries
        fileName = fileName.replace('\\', '/');
        String path = "";
        for (final StringTokenizer tokenizer = new StringTokenizer(fileName, "/"); tokenizer.hasMoreTokens(); ) {
            final String part = tokenizer.nextToken();
            if (tokenizer.hasMoreTokens()) {
                path += part + "/";
                if (!entries.contains(path)) {
                    jarOutputStream.putNextEntry(new JarEntry(path));
                    jarOutputStream.closeEntry();
                    entries.add(path);
                }
            }
        }

        // write the bytes
        jarOutputStream.putNextEntry(new JarEntry(fileName));
        try {
            jarOutputStream.write(bytes);
        } finally {
            jarOutputStream.closeEntry();
            entries.add(fileName);
        }
    }

    private static synchronized JarOutputStream openJarFile(final CmpJarBuilder instance) throws IOException {

        if (instance.jarFile != null) {
            throw new IllegalStateException("Jar file exists already");
        }

        final File dir = tmpDir();

        // if url caching is enabled, generate the file directly in the cache dir, so it doesn't have to be recoppied
        try {
            instance.jarFile = File.createTempFile("OpenEJBGenerated.", ".jar", dir).getAbsoluteFile();
        } catch (final Throwable e) {

            Logger.getInstance(LogCategory.OPENEJB_STARTUP, CmpJarBuilder.class).warning("Failed to create temp jar file in: " + dir, e);

            //Try
            try {
                Thread.sleep(50);
            } catch (final InterruptedException ie) {
                //Ignore
            }

            instance.jarFile = File.createTempFile("OpenEJBGenerated.", ".jar", dir).getAbsoluteFile();
        }

        Thread.yield();

        instance.jarFile.deleteOnExit();

        Logger.getInstance(LogCategory.OPENEJB_STARTUP, CmpJarBuilder.class).debug("Using temp jar file: " + instance.jarFile);

        return new JarOutputStream(IO.write(instance.jarFile));
    }

    private static File tmpDir() throws IOException {
        File dir = UrlCache.cacheDir;

        if (null == dir) {
            dir = SystemInstance.get().getBase().getDirectory("tmp", true);
        }
        return dir;
    }

    private void close(final JarOutputStream jarOutputStream) {
        if (jarOutputStream != null) {
            try {
                jarOutputStream.close();
            } catch (final Throwable ignored) {
                // no-op
            }
        }
    }

    private static class Entry {
        private final String clazz;
        private final String name;
        private final byte[] bytes;

        private Entry(final String clazz, final String name, final byte[] bytes) {
            this.clazz = clazz;
            this.name = name;
            this.bytes = bytes;
        }
    }

    private static class StoringBytecodeBytecodeWriter implements BytecodeWriter {
        private final Map<String, byte[]> bytecodes = new HashMap<>();
        private final ClassLoader loader;

        private StoringBytecodeBytecodeWriter(final ClassLoader loader) {
            this.loader = loader;
        }

        @Override
        public void write(final BCClass type) throws IOException {
            bytecodes.put(type.getName(), type.toByteArray());

            final byte[] b = type.toByteArray();
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_1_7)) {
                final ByteArrayInputStream bais = new ByteArrayInputStream(b);
                final BufferedInputStream bis = new BufferedInputStream(bais);
                final ClassWriter cw = new CommonClassWriterHack(loader);
                final ClassReader cr = new ClassReader(bis);
                cr.accept(cw, 0);
                baos.write(cw.toByteArray());
            } else {
                baos.write(b);
            }
        }
    }

    private static class CommonClassWriterHack extends ClassWriter {
        private final ClassLoader loader;

        private CommonClassWriterHack(final ClassLoader loader) {
            super(ClassWriter.COMPUTE_FRAMES);
            this.loader = loader;
        }

        @Override
        protected String getCommonSuperClass(final String type1, final String type2) {
            Class<?> class1;
            Class<?> class2;
            try {
                class1 = loader.loadClass(type1.replace('/', '.'));
                class2 = loader.loadClass(type2.replace('/', '.'));
            } catch (final ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            if (class1.isAssignableFrom(class2)) {
                return type1;
            }
            if (class2.isAssignableFrom(class1)) {
                return type2;
            }
            if (class1.isInterface() || class2.isInterface()) {
                return "java/lang/Object";
            }
            do {
                class1 = class1.getSuperclass();
            } while (!class1.isAssignableFrom(class2));
            return class1.getName().replace('.', '/');
        }
    }

    private static class EnhancingClassLoader extends URLClassLoaderFirst {
        private final Map<String, Entry> classes;

        public EnhancingClassLoader(final ClassLoader tempClassLoader, Map<String, Entry> classes) {
            super(new URL[0], tempClassLoader);
            this.classes = classes;
        }

        @Override
        public Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            final Entry e = classes.get(name);
            if (e != null) {
                final Class<?> alreadyLoaded = findLoadedClass(name);
                if (alreadyLoaded != null) {
                    if (resolve) {
                        resolveClass(alreadyLoaded);
                    }
                    return alreadyLoaded;
                }

                final Class<?> c = defineClass(e.clazz, e.bytes, 0, e.bytes.length);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
            return super.loadClass(name, resolve);
        }

        @Override
        public InputStream getResourceAsStream(final String name) {
            final String key = name.replace('/', '.').replace(".class", "");
            final Entry e = classes.get(key);
            return e != null ? new ByteArrayInputStream(e.bytes) : super.getResourceAsStream(name);
        }
    }
}
