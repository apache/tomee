/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.openejb.core.TemporaryClassLoader;
import org.apache.openejb.core.cmp.cmp2.Cmp2Generator;
import org.apache.openejb.core.cmp.cmp2.CmrField;
import org.apache.openejb.core.cmp.cmp2.Cmp1Generator;
import org.apache.openejb.core.cmp.CmpUtil;

/**
 * Creates a jar file which contains the CMP implementation classes and the cmp entity mappings xml file.
 */
public class CmpJarBuilder {
    private final ClassLoader tempClassLoader;

    private File jarFile;
    private final Set<String> entries = new TreeSet<String>();
    private final AppInfo appInfo;

    public CmpJarBuilder(AppInfo appInfo, ClassLoader classLoader) {
        this.appInfo = appInfo;
        tempClassLoader = new TemporaryClassLoader(classLoader);
    }

    public File getJarFile() throws IOException {
        if (jarFile == null) {
            generate();
        }
        return jarFile;
    }

    private void generate() throws IOException {
        // Don't generate an empty jar
        if (!hasCmpBeans()) return;

        boolean threwException = false;
        JarOutputStream jarOutputStream = openJarFile();
        try {
            // Generate CMP implementation classes
            for (EjbJarInfo ejbJar : appInfo.ejbJars) {
                for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
                    if (beanInfo instanceof EntityBeanInfo) {
                        EntityBeanInfo entityBeanInfo = (EntityBeanInfo) beanInfo;
                        if ("CONTAINER".equalsIgnoreCase(entityBeanInfo.persistenceType)) {
                            generateClass(jarOutputStream, entityBeanInfo);
                        }
                    }
                }
            }
            if (appInfo.cmpMappingsXml != null) {
                // System.out.println(appInfo.cmpMappingsXml);
                addJarEntry(jarOutputStream, "META-INF/openejb-cmp-generated-orm.xml", appInfo.cmpMappingsXml.getBytes());
            }
        } catch (IOException e) {
            threwException = true;
            throw e;
        } finally {
            close(jarOutputStream);
            if (threwException) {
                jarFile.delete();
                jarFile = null;
            }
        }
    }

    private boolean hasCmpBeans() {
        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
                if (beanInfo instanceof EntityBeanInfo) {
                    EntityBeanInfo entityBeanInfo = (EntityBeanInfo) beanInfo;
                    if ("CONTAINER".equalsIgnoreCase(entityBeanInfo.persistenceType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void generateClass(JarOutputStream jarOutputStream, EntityBeanInfo entityBeanInfo) throws IOException {
        // don't generate if there is aleady an implementation class
        String cmpImplClass = CmpUtil.getCmpImplClassName(entityBeanInfo.abstractSchemaName, entityBeanInfo.ejbClass);
        String entryName = cmpImplClass.replace(".", "/") + ".class";
        if (entries.contains(entryName) || tempClassLoader.getResource(entryName) != null) {
            return;
        }

        // load the bean class, which is used by the generator
        Class<?> beanClass = null;
        try {
            beanClass = tempClassLoader.loadClass(entityBeanInfo.ejbClass);
        } catch (ClassNotFoundException e) {
            throw (IOException)new IOException("Could not find entity bean class " + beanClass).initCause(e);
        }

        Class<?> primKeyClass = null;
        if (entityBeanInfo.primKeyClass != null) {
            try {
                primKeyClass = tempClassLoader.loadClass(entityBeanInfo.primKeyClass);
            } catch (ClassNotFoundException e) {
                throw (IOException)new IOException("Could not find entity primary key class " + entityBeanInfo.primKeyClass).initCause(e);
            }
        }

        byte[] bytes = null;
        if (entityBeanInfo.cmpVersion != 2) {
            Cmp1Generator cmp1Generator = new Cmp1Generator(cmpImplClass, beanClass);
            if ("java.lang.Object".equals(entityBeanInfo.primKeyClass)) {
                cmp1Generator.setUnknownPk(true);
            }
            bytes = cmp1Generator.generate();
        } else {

            // generte the implementation class
            Cmp2Generator cmp2Generator = new Cmp2Generator(cmpImplClass,
                    beanClass,
                    entityBeanInfo.primKeyField,
                    primKeyClass,
                    entityBeanInfo.cmpFieldNames.toArray(new String[entityBeanInfo.cmpFieldNames.size()]));

            for (CmrFieldInfo cmrFieldInfo : entityBeanInfo.cmrFields) {
                EntityBeanInfo roleSource = cmrFieldInfo.mappedBy.roleSource;
                CmrField cmrField = new CmrField(cmrFieldInfo.fieldName,
                        cmrFieldInfo.fieldType,
                        CmpUtil.getCmpImplClassName(roleSource.abstractSchemaName, roleSource.ejbClass),
                        roleSource.local,
                        cmrFieldInfo.mappedBy.fieldName,
                        cmrFieldInfo.synthetic);
                cmp2Generator.addCmrField(cmrField);
            }
            bytes = cmp2Generator.generate();
        }

        // add the generated class to the jar
        addJarEntry(jarOutputStream, entryName, bytes);
    }

    private void addJarEntry(JarOutputStream jarOutputStream, String fileName, byte[] bytes) throws IOException {
        // add all missing directory entried
        String path = "";
        for (StringTokenizer tokenizer = new StringTokenizer(fileName, "/"); tokenizer.hasMoreTokens();) {
            String part = tokenizer.nextToken();
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

    private JarOutputStream openJarFile() throws IOException {
        if (jarFile != null) {
            throw new IllegalStateException("Jar file is closed");
        }
        jarFile = File.createTempFile("OpenEJB_Generated_", ".jar");
        jarFile.deleteOnExit();
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(jarFile));
        return jarOutputStream;
    }

    private void close(JarOutputStream jarOutputStream) {
        if (jarOutputStream != null) {
            try {
                jarOutputStream.close();
            } catch (IOException ignored) {
            }
        }
    }
}
