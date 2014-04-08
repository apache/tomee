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
package org.apache.openejb.javaagent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Agent {

    private static final Permission ACCESS_PERMISSION = new ReflectPermission("suppressAccessChecks");
    private static String agentArgs;
    private static Instrumentation instrumentation;
    private static boolean initialized = false;

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        if (Agent.instrumentation != null)
            return;

        Agent.agentArgs = agentArgs;
        Agent.instrumentation = instrumentation;
        initialized = true;

        instrumentation.addTransformer(new BootstrapTransformer());
    }

    public static void agentmain(final String agentArgs, final Instrumentation instrumentation) {
        if (Agent.instrumentation != null)
            return;

        Agent.agentArgs = agentArgs;
        Agent.instrumentation = instrumentation;
        initialized = true;
    }

    public static synchronized String getAgentArgs() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(ACCESS_PERMISSION);
        checkInitialization();
        return agentArgs;
    }

    /**
     * Gets the instrumentation instance.
     * You must have java.lang.ReflectPermission(suppressAccessChecks) to call this method
     *
     * @return the instrumentation instance
     */
    public static synchronized Instrumentation getInstrumentation() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(ACCESS_PERMISSION);
        checkInitialization();
        return instrumentation;
    }

    private static synchronized void checkInitialization() {
        if (!initialized) {
            try {
                checkSystemClassPath();
                dynamicLoadAgent();
            } catch (Exception e) {
                new IllegalStateException("Unable to initialize agent", e).printStackTrace();
            } finally {
                initialized = true;
            }
        }
    }

    private static void checkSystemClassPath() throws NoSuchFieldException, IllegalAccessException {
        if (instrumentation != null)
            return;

        final Class<?> systemAgentClass;
        try {
            final ClassLoader systemCl = ClassLoader.getSystemClassLoader();
            systemAgentClass = systemCl.loadClass(Agent.class.getName());
        } catch (ClassNotFoundException e) {
            // java-agent jar was not on the system class path
            return;
        }

        final Field instrumentationField = systemAgentClass.getDeclaredField("instrumentation");
        instrumentationField.setAccessible(true);
        instrumentation = (Instrumentation) instrumentationField.get(null);

        final Field agentArgsField = systemAgentClass.getDeclaredField("agentArgs");
        agentArgsField.setAccessible(true);
        agentArgs = (String) agentArgsField.get(null);
    }

    private static void dynamicLoadAgent() throws Exception {
        if (instrumentation != null)
            return;

        try {
            final Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            final Method attachMethod = vmClass.getMethod("attach", String.class);
            final Method loadAgentMethod = vmClass.getMethod("loadAgent", String.class);

            // find the agentJar
            final String agentPath = getAgentJar();

            // get the pid of the current process (for attach command)
            final String pid = getPid();

            // attach to the vm
            final Object vm = attachMethod.invoke(null, new String[]{pid});

            // load our agent
            loadAgentMethod.invoke(vm, agentPath);

            // The AgentJar is loaded into the system classpath, and this class could
            // be in a child classloader, so we need to double check the system classpath
            checkSystemClassPath();
        } catch (ClassNotFoundException e) {
            // not a Sun VM
        } catch (NoSuchMethodException e) {
            // not a Sun VM
        }
    }

    private static String getPid() {
        // This relies on the undocumented convention of the
        // RuntimeMXBean's name starting with the PID, but
        // there appears to be no other way to obtain the
        // current process' id, which we need for the attach
        // process
        final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String pid = bean.getName();
        if (pid.contains("@")) {
            pid = pid.substring(0, pid.indexOf("@"));
        }
        return pid;
    }

    /**
     * Try to find the openejb-javaagent jar, and if not found create a new jar
     * file for the sole purpose of specifying an Agent-Class to load into the JVM.
     */
    private static String getAgentJar() throws IOException {
        final URL resource = Agent.class.getClassLoader().getResource(Agent.class.getName().replace('.', '/') + ".class");
        if (resource == null) {
            throw new IllegalStateException("Could not find Agent class file in class path");
        }

        final URLConnection urlConnection = resource.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            final JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
            return jarURLConnection.getJarFile().getName();
        }

        final InputStream in = urlConnection.getInputStream();
        ZipOutputStream out = null;
        File file = null;
        try {
            try {
                file = File.createTempFile(Agent.class.getName(), ".jar");
            } catch (Throwable e) {
                final File tmp = new File("tmp");
                if (!tmp.exists() && !tmp.mkdirs()) {
                    throw new IOException("Failed to create local tmp directory: " + tmp.getAbsolutePath());
                }

                file = File.createTempFile(Agent.class.getName(), ".jar", tmp);
            }

            file.deleteOnExit();

            out = new ZipOutputStream(new FileOutputStream(file));

            // write manifest
            out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            try {
                final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
                writer.println("Agent-Class: " + Agent.class.getName());
                writer.println("Can-Redefine-Classes: true");
                writer.println("Can-Retransform-Classes: true");
                writer.flush();
            } finally {
                out.closeEntry();
            }

            // write agent class
            out.putNextEntry(new ZipEntry(Agent.class.getName().replace('.', '/') + ".class"));
            try {
                final byte[] buffer = new byte[4096];
                for (int count = in.read(buffer); count >= 0; count = in.read(buffer)) {
                    out.write(buffer, 0, count);
                }
            } finally {
                out.closeEntry();
            }

            return file.getAbsolutePath();
        } catch (IOException e) {
            if (file != null) {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
            throw e;
        } finally {
            close(in);
            close(out);
        }
    }

    private static void close(final Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    private static class BootstrapTransformer implements ClassFileTransformer {

        private boolean done;

        @Override
        public byte[] transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final byte[] classfileBuffer) throws IllegalClassFormatException {

            try {
                bootstrap(loader);
            } catch (Throwable e) {
                done = true;
            }

            return classfileBuffer;
        }

        private void bootstrap(final ClassLoader loader) {
            if (loader == null || done)
                return;

            final String bootstrapClassName = "org.apache.openejb.persistence.PersistenceBootstrap";
            final String bootstrapClassFile = "org/apache/openejb/persistence/PersistenceBootstrap.class";

            if (loader.getResource(bootstrapClassFile) == null) {
                return;
            }

            // We found the classloader that has the openejb-core jar
            // we need to mark ourselves as "done" so that when we attempt to load
            // the PersistenceBootstrap class it doesn't cause an infinite loop
            done = true;

            try {
                final Class<?> bootstrapClass = loader.loadClass(bootstrapClassName);
                final Method bootstrap = bootstrapClass.getMethod("bootstrap", ClassLoader.class);
                bootstrap.invoke(null, loader);
            } catch (Throwable e) {
                Logger.getLogger(Agent.class.getName()).log(Level.WARNING, "Failed to invoke bootstrap: " + e.getMessage());
            }
        }
    }
}
