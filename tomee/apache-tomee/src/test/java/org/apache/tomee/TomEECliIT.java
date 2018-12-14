/**
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
package org.apache.tomee;

import org.apache.openejb.cli.Bootstrap;
import org.apache.openejb.loader.IO;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TomEECliIT {

    @Test
    public void testTomEECliWithJar() throws IOException, InterruptedException {
        final File file = Files.createTempDirectory("tomee-test").toFile();
        final File jar = new File(file.getAbsolutePath() + "/test.jar");

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClasses(org.apache.tomee.Test.class)
                .add(new StringAsset("main.class = org.apache.tomee.Test\n" +
                        "name = classloadertest\n" +
                        "description = Show a simple msg"), "META-INF/org.apache.openejb.cli/classloadertest");

        archive.as(ZipExporter.class).exportTo(jar);

        File work = new File("target/webprofile-work-dir/").getAbsoluteFile();
        if (!work.exists()) {
            work = new File("apache-tomee/target/webprofile-work-dir/").getAbsoluteFile();
        }

        final File[] files = work.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith("apache-tomcat-");
            }
        });

        final File tomee = (null != files ? files[0] : null);
        if (tomee == null) {
            fail("Failed to find Tomcat directory required for this test - Ensure you have run at least the maven phase: mvn process-resources");
        }

        final ProcessBuilder builder = new ProcessBuilder()
            .command("java", "-cp", jar.getAbsolutePath() + File.pathSeparator +
                            tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "openejb-core-8.0.0-SNAPSHOT.jar" + File.pathSeparator +
                            tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "commons-cli-1.2.jar" + File.pathSeparator +
                            tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "commons-lang3-3.8.1.jar",
                    "org.apache.openejb.cli.Bootstrap", "classloadertest");

        final Process start = builder.start();
        start.waitFor();

        final String result = IO.slurp(start.getInputStream());
        assertTrue(result.contains("TESTING CLASSLOADER!!"));
    }

    @Test
    public void testTomEECliWithJarDir() throws IOException, InterruptedException {
        final File file = Files.createTempDirectory("tomee-test").toFile();
        final File jar = new File(file.getAbsolutePath() + "/test.jar");

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClasses(org.apache.tomee.Test.class)
                .add(new StringAsset("main.class = org.apache.tomee.Test\n" +
                        "name = classloadertest\n" +
                        "description = Show a simple msg"), "META-INF/org.apache.openejb.cli/classloadertest");

        archive.as(ZipExporter.class).exportTo(jar);

        File work = new File("target/webprofile-work-dir/").getAbsoluteFile();
        if (!work.exists()) {
            work = new File("apache-tomee/target/webprofile-work-dir/").getAbsoluteFile();
        }

        final File[] files = work.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith("apache-tomcat-");
            }
        });

        final File tomee = (null != files ? files[0] : null);
        if (tomee == null) {
            fail("Failed to find Tomcat directory required for this test - Ensure you have run at least the maven phase: mvn process-resources");
        }

        final ProcessBuilder builder = new ProcessBuilder()
                .command("java", "-cp", file.getAbsolutePath() + File.separator + "*" + File.pathSeparator +
                                tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "*",
                        "org.apache.openejb.cli.Bootstrap", "classloadertest");

        final Process start = builder.start();
        start.waitFor();

        final String result = IO.slurp(start.getInputStream());
        assertTrue(result.contains("TESTING CLASSLOADER!!"));
    }

    @Test
    public void testTomEECliWithJarCallingAnotherMain() throws IOException, InterruptedException {
        final File file = Files.createTempDirectory("tomee-test").toFile();
        final File jar = new File(file.getAbsolutePath() + "/test.jar");
        final File jar2 = new File(file.getAbsolutePath() + "/test2.jar");

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClasses(org.apache.tomee.Test.class)
                .add(new StringAsset("main.class = org.apache.tomee.Test\n" +
                        "name = classloadertest\n" +
                        "description = Show a simple msg"), "META-INF/org.apache.openejb.cli/classloadertest");

        final JavaArchive archive2 = ShrinkWrap.create(JavaArchive.class, "test2.jar")
                .addClasses(org.apache.tomee.TestCommand1.class)
                .add(new StringAsset("main.class = org.apache.tomee.TestCommand1\n" +
                        "name = classloadertest2\n" +
                        "description = Show a simple msg"), "META-INF/org.apache.openejb.cli/classloadertest2");

        archive.as(ZipExporter.class).exportTo(jar);
        archive2.as(ZipExporter.class).exportTo(jar2);

        File work = new File("target/webprofile-work-dir/").getAbsoluteFile();
        if (!work.exists()) {
            work = new File("apache-tomee/target/webprofile-work-dir/").getAbsoluteFile();
        }

        final File[] files = work.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith("apache-tomcat-");
            }
        });

        final File tomee = (null != files ? files[0] : null);
        if (tomee == null) {
            fail("Failed to find Tomcat directory required for this test - Ensure you have run at least the maven phase: mvn process-resources");
        }

        final ProcessBuilder builder = new ProcessBuilder()
                .command("java", "-cp", jar.getAbsolutePath() + File.pathSeparator +
                                jar2.getAbsolutePath() + File.pathSeparator +
                                tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "openejb-core-8.0.0-SNAPSHOT.jar" + File.pathSeparator +
                                tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "commons-cli-1.2.jar" + File.pathSeparator +
                                tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "commons-lang3-3.8.1.jar",
                        "org.apache.openejb.cli.Bootstrap", "classloadertest2");

        final Process start = builder.start();
        start.waitFor();

        final String result = IO.slurp(start.getInputStream());
        assertTrue(result.contains("TESTING CLASSLOADER!!"));
    }

    @Test
    public void testTomEECliWithJarDirCallingAnotherMain() throws IOException, InterruptedException {
        final File file = Files.createTempDirectory("tomee-test").toFile();
        final File jar = new File(file.getAbsolutePath() + "/test.jar");
        final File jar2 = new File(file.getAbsolutePath() + "/test2.jar");

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClasses(org.apache.tomee.Test.class)
                .add(new StringAsset("main.class = org.apache.tomee.Test\n" +
                        "name = classloadertest\n" +
                        "description = Show a simple msg"), "META-INF/org.apache.openejb.cli/classloadertest");

        final JavaArchive archive2 = ShrinkWrap.create(JavaArchive.class, "test2.jar")
                .addClasses(org.apache.tomee.TestCommand1.class)
                .add(new StringAsset("main.class = org.apache.tomee.TestCommand1\n" +
                        "name = classloadertest2\n" +
                        "description = Show a simple msg"), "META-INF/org.apache.openejb.cli/classloadertest2");

        archive.as(ZipExporter.class).exportTo(jar);
        archive2.as(ZipExporter.class).exportTo(jar2);

        File work = new File("target/webprofile-work-dir/").getAbsoluteFile();
        if (!work.exists()) {
            work = new File("apache-tomee/target/webprofile-work-dir/").getAbsoluteFile();
        }

        final File[] files = work.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith("apache-tomcat-");
            }
        });

        final File tomee = (null != files ? files[0] : null);
        if (tomee == null) {
            fail("Failed to find Tomcat directory required for this test - Ensure you have run at least the maven phase: mvn process-resources");
        }

        final ProcessBuilder builder = new ProcessBuilder()
                .command("java", "-cp", file.getAbsolutePath() + File.separator + "*" + File.pathSeparator +
                                tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "*",
                        "org.apache.openejb.cli.Bootstrap", "classloadertest2");

        final Process start = builder.start();
        start.waitFor();

        final String result = IO.slurp(start.getInputStream());
        assertTrue(result.contains("TESTING CLASSLOADER!!"));
    }

    @Test
    public void testDelegatesToCLIMain() throws IOException, InterruptedException {
        final File file = Files.createTempDirectory("tomee-test").toFile();
        final File jar = new File(file.getAbsolutePath() + "/test.jar");
        final File jar2 = new File(file.getAbsolutePath() + "/test2.jar");

        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addClasses(org.apache.tomee.Test.class)
                .add(new StringAsset("main.class = org.apache.tomee.Test\n" +
                        "name = classloadertest\n" +
                        "description = Show a simple msg"), "META-INF/org.apache.openejb.cli/classloadertest");

        final JavaArchive archive2 = ShrinkWrap.create(JavaArchive.class, "test2.jar")
                .addClasses(org.apache.tomee.TestCommand2.class)
                .add(new StringAsset("main.class = org.apache.tomee.TestCommand2\n" +
                        "name = classloadertest2\n" +
                        "description = Show a simple msg"), "META-INF/org.apache.openejb.cli/classloadertest2");

        archive.as(ZipExporter.class).exportTo(jar);
        archive2.as(ZipExporter.class).exportTo(jar2);

        File work = new File("target/webprofile-work-dir/").getAbsoluteFile();
        if (!work.exists()) {
            work = new File("apache-tomee/target/webprofile-work-dir/").getAbsoluteFile();
        }

        final File[] files = work.listFiles(new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pathname.isDirectory() && pathname.getName().startsWith("apache-tomcat-");
            }
        });

        final File tomee = (null != files ? files[0] : null);
        if (tomee == null) {
            fail("Failed to find Tomcat directory required for this test - Ensure you have run at least the maven phase: mvn process-resources");
        }

        final ProcessBuilder builder = new ProcessBuilder()
                .command("java", "-cp", file.getAbsolutePath() + File.separator + "*" + File.pathSeparator +
                                tomee.getAbsolutePath() + File.separator + "lib" + File.separator + "*",
                        "org.apache.openejb.cli.Bootstrap", "classloadertest2");

        final Process start = builder.start();
        start.waitFor();

        final String result = IO.slurp(start.getInputStream());
        assertTrue(result.contains("TESTING CLASSLOADER!!"));
    }

    @Test
    public void testIfClassloaderNotChange() throws Exception {
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        new Bootstrap().main(new String[]{"cipher"});
        assertEquals(originalClassLoader, Thread.currentThread().getContextClassLoader());
    }
}
