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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ResourceInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.PropertyPlaceHolderHelper;
import org.apache.xbean.asm4.ClassWriter;
import org.apache.xbean.asm4.MethodVisitor;
import org.apache.xbean.asm4.Opcodes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.InitialContext;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.xbean.asm4.Opcodes.ACC_PUBLIC;
import static org.apache.xbean.asm4.Opcodes.ACC_SUPER;
import static org.apache.xbean.asm4.Opcodes.ALOAD;
import static org.apache.xbean.asm4.Opcodes.INVOKESPECIAL;
import static org.apache.xbean.asm4.Opcodes.RETURN;

/**
 * @version $Rev$ $Date$
 */
public class ServiceClasspathTest extends Assert {
    @After
    @Before
    public void reset() {
        SystemInstance.reset();
        PropertyPlaceHolderHelper.reset();
    }


    @Test
    public void test() throws Exception {

        final String className = "org.superbiz.foo.Orange";
        final File jar = subclass(Color.class, className);

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final Openejb openejb = new Openejb();
        final Resource resource = new Resource();
        openejb.getResource().add(resource);

        resource.setClassName(className);
        resource.setType(className);
        resource.setId("Orange");
        resource.getProperties().put("red", "FF");
        resource.getProperties().put("green", "99");
        resource.getProperties().put("blue", "00");
        resource.setClasspath(jar.getAbsolutePath());

        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        final ResourceInfo serviceInfo = config.configureService(resource, ResourceInfo.class);
//        serviceInfo.classpath = new URI[]{jar.toURI()};
        assembler.createResource(serviceInfo);

        final InitialContext initialContext = new InitialContext();
        final Color color = (Color) initialContext.lookup("openejb:Resource/Orange");

        assertNotNull(color);
        assertEquals("Orange.FF", color.getRed());
        assertEquals("Orange.99", color.getGreen());
        assertEquals("Orange.00", color.getBlue());
    }

    @Test
    public void testXml() throws Exception {

        final String className = "org.superbiz.foo.Orange";
        final File jar = subclass(Color.class, className);

        final File xml = File.createTempFile("config-", ".xml");
        xml.deleteOnExit();

        final PrintStream out = new PrintStream(IO.write(xml));
        out.println("<openejb>\n" +
                "  <Resource id=\"Orange\" type=\"org.superbiz.foo.Orange\" class-name=\"org.superbiz.foo.Orange\" classpath=\"" + jar.getAbsolutePath() + "\">\n" +
                "    red = FF\n" +
                "    green = 99\n" +
                "    blue = 00\n" +
                "  </Resource>\n" +
                "</openejb>");
        out.close();


        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());


        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.buildContainerSystem(config.getOpenEjbConfiguration(xml));

        final InitialContext initialContext = new InitialContext();
        final Color color = (Color) initialContext.lookup("openejb:Resource/Orange");

        assertNotNull(color);
        assertEquals("Orange.FF", color.getRed());
        assertEquals("Orange.99", color.getGreen());
        assertEquals("Orange.00", color.getBlue());
    }

    @Test
    public void testRelativePath() throws Exception {

        final String className = "org.superbiz.foo.Orange";
        final File jar = subclass(Color.class, className);

        final File xml = File.createTempFile("config-", ".xml");
        xml.deleteOnExit();

        final PrintStream out = new PrintStream(IO.write(xml));
        out.println("<openejb>\n" +
                "  <Resource id=\"Orange\" type=\"org.superbiz.foo.Orange\"" +
                "           class-name=\"org.superbiz.foo.Orange\"" +
                "           classpath=\"${openejb.home}/" + jar.getName() + "\">\n" +
                "    red = FF\n" +
                "    green = 99\n" +
                "    blue = 00\n" +
                "  </Resource>\n" +
                "</openejb>");
        out.close();


        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        final Properties properties = new Properties();
        properties.setProperty("openejb.home", jar.getParentFile().getAbsolutePath());
        SystemInstance.init(properties);
        PropertyPlaceHolderHelper.reset();
        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.buildContainerSystem(config.getOpenEjbConfiguration(xml));

        final InitialContext initialContext = new InitialContext();
        final Color color = (Color) initialContext.lookup("openejb:Resource/Orange");

        assertNotNull(color);
        assertEquals("Orange.FF", color.getRed());
        assertEquals("Orange.99", color.getGreen());
        assertEquals("Orange.00", color.getBlue());
    }

    @Test
    public void testJson() throws Exception {

        final String className = "org.superbiz.foo.Orange";
        final File jar = subclass(Color.class, className);

        final File json = File.createTempFile("config-", ".json");
        json.deleteOnExit();

        final PrintStream out = new PrintStream(IO.write(json));
        out.println("{\n" +
                "    \"resources\":{\n" +
                "        \"Orange\":{\n" +
                "            \"type\":\"org.superbiz.foo.Orange\",\n" +
                "            \"class-name\":\"org.superbiz.foo.Orange\",\n" +
                "            \"classpath\":\"" + jar.getAbsolutePath() + "\",\n" +
                "            \"properties\":{\n" +
                "                \"red\":\"FF\",\n" +
                "                \"green\":\"99\",\n" +
                "                \"blue\":\"00\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n");
        out.close();


        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());


        final ConfigurationFactory config = new ConfigurationFactory();
        final Assembler assembler = new Assembler();

        assembler.buildContainerSystem(config.getOpenEjbConfiguration(json));

        final InitialContext initialContext = new InitialContext();
        final Color color = (Color) initialContext.lookup("openejb:Resource/Orange");

        assertNotNull(color);
        assertEquals("Orange.FF", color.getRed());
        assertEquals("Orange.99", color.getGreen());
        assertEquals("Orange.00", color.getBlue());
    }

    public static class Color {

        private String red;
        private String green;
        private String blue;

        public String getRed() {
            return wrap(red);
        }

        public String getGreen() {
            return wrap(green);
        }

        public String getBlue() {
            return wrap(blue);
        }

        public void setRed(String red) {
            this.red = red;
        }

        public void setGreen(String green) {
            this.green = green;
        }

        public void setBlue(String blue) {
            this.blue = blue;
        }

        private String wrap(final String value) {
            return this.getClass().getSimpleName() + "." + value;
        }
    }

    public static File subclass(Class<?> parent, String subclassName) throws Exception {
        final String subclassNameInternal = subclassName.replace('.', '/');

        final byte[] bytes;
        {
            final ClassWriter cw = new ClassWriter(0);
            final String parentClassNameInternal = parent.getName().replace('.', '/');

            cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + ACC_SUPER, subclassNameInternal, null, parentClassNameInternal, null);

            final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, parentClassNameInternal, "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
            cw.visitEnd();

            bytes = cw.toByteArray();
        }

        return Archive.archive().add(subclassNameInternal + ".class", bytes).asJar();
    }

    /**
     * @version $Revision$ $Date$
     */
    public static class Archive {

        final Map<String, String> manifest = new HashMap<String, String>();
        final Map<String, byte[]> entries = new HashMap<String, byte[]>();

        public static Archive archive() {
            return new Archive();
        }

        public Archive manifest(String key, Object value) {
            manifest.put(key, value.toString());
            return this;
        }

        public Archive manifest(String key, Class value) {
            manifest.put(key, value.getName());
            return this;
        }

        public Archive add(Class<?> clazz) {
            try {
                final String name = clazz.getName().replace('.', '/') + ".class";

                final URL resource = this.getClass().getClassLoader().getResource(name);

                final InputStream from = new BufferedInputStream(resource.openStream());
                final ByteArrayOutputStream to = new ByteArrayOutputStream();

                final byte[] buffer = new byte[1024];
                int length;
                while ((length = from.read(buffer)) != -1) {
                    to.write(buffer, 0, length);
                }
                to.flush();

                final byte[] bytes = to.toByteArray();
                return add(name, bytes);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        public Archive add(String name, byte[] bytes) {
            entries.put(name, bytes);
            return this;
        }

        public File toJar() throws IOException {
            final File file = File.createTempFile("archive-", ".jar");
            file.deleteOnExit();

            // Create the ZIP file
            final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

            for (Map.Entry<String, byte[]> entry : entries().entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue());
            }

            // Complete the ZIP file
            out.close();
            return file;
        }

        public File asJar() {
            try {
                return toJar();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public File toDir() throws IOException {

            File classpath = Files.tmpdir();

            for (Map.Entry<String, byte[]> entry : entries().entrySet()) {

                final String key = entry.getKey().replace('/', File.separatorChar);

                final File file = new File(classpath, key);

                File d = file.getParentFile();

                if (!d.exists()) assertTrue(d.getAbsolutePath(), d.mkdirs());

                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                out.write(entry.getValue());

                out.close();
            }

            return classpath;
        }

        public File asDir() {
            try {
                return toDir();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private HashMap<String, byte[]> entries() {
            final HashMap<String, byte[]> entries = new HashMap<String, byte[]>(this.entries);
            entries.put("META-INF/MANIFEST.MF", buildManifest().getBytes());
            return entries;
        }

        private String buildManifest() {
            return Join.join("\r\n", new Join.NameCallback<Map.Entry<String, String>>() {
                @Override
                public String getName(Map.Entry<String, String> entry) {
                    return entry.getKey() + ": " + entry.getValue();
                }
            }, manifest.entrySet());
        }

    }

}
