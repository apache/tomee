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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Creates a jar file which contains the CMP implementation classes and the cmp entity mappings xml file.
 */
public class CmpJarBuilder {

    private final ClassLoader tempClassLoader;

    private File jarFile;
    private final Set<String> entries = new TreeSet<>();
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

            for (final Entry e : classes.values()) {
                addJarEntry(jarOutputStream, e.name, e.bytes);
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

    private static final class Entry {
        private final String clazz;
        private final String name;
        private final byte[] bytes;

        private Entry(final String clazz, final String name, final byte[] bytes) {
            this.clazz = clazz;
            this.name = name;
            this.bytes = bytes;
        }
    }
}
