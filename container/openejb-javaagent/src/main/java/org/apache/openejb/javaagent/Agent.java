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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.Closeable;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Agent {
    private static final Permission ACCESS_PERMISSION = new ReflectPermission("suppressAccessChecks");
    private static String agentArgs;
    private static Instrumentation instrumentation;
    private static boolean initialized = false;

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        if (Agent.instrumentation != null) return;

        Agent.agentArgs = agentArgs;
        Agent.instrumentation = instrumentation;
        initialized = true;
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        if (Agent.instrumentation != null) return;

        Agent.agentArgs = agentArgs;
        Agent.instrumentation = instrumentation;
        initialized = true;
    }

    public static synchronized String getAgentArgs() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
        checkInitialization();
        return agentArgs;
    }

    /**
     * Gets the instrumentation instance.
     * You must have java.lang.ReflectPermission(suppressAccessChecks) to call this method
     * @return the instrumentation instance
     */
    public static synchronized Instrumentation getInstrumentation() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(ACCESS_PERMISSION);
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
        if (instrumentation != null) return;

        Class<?> systemAgentClass = null;
        try {
            ClassLoader systemCl = ClassLoader.getSystemClassLoader();
            systemAgentClass = systemCl.loadClass(Agent.class.getName());
        } catch (ClassNotFoundException e) {
            // java-agent jar was not on the system class path
            return;
        }

        Field instrumentationField = systemAgentClass.getDeclaredField("instrumentation");
        instrumentationField.setAccessible(true);
        instrumentation = (Instrumentation) instrumentationField.get(null);

        Field agentArgsField = systemAgentClass.getDeclaredField("agentArgs");
        agentArgsField.setAccessible(true);
        agentArgs = (String) agentArgsField.get(null);
    }

    private static void dynamicLoadAgent() throws Exception{
        if (instrumentation != null) return;

        try {
            Class<?> vmClass = Class.forName("com.sun.tools.attach.VirtualMachine");
            Method attachMethod = vmClass.getMethod("attach", String.class);
            Method loadAgentMethod = vmClass.getMethod("loadAgent", String.class);

            // find the agentJar
            String agentPath = getAgentJar();

            // get the pid of the current process (for attach command)
            String pid = getPid();

            // attach to the vm
            Object vm = attachMethod.invoke(null, new String[] { pid });

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
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        String pid = bean.getName();
        if (pid.indexOf("@") != -1) {
            pid = pid.substring(0, pid.indexOf("@"));
        }
        return pid;
    }

    /**
     * Try to find the openejb-javaagent jar, and if not found create a new jar
     * file for the sole purpose of specifying an Agent-Class to load into the JVM.
     */
    private static String getAgentJar() throws IOException {
        URL resource = Agent.class.getClassLoader().getResource(Agent.class.getName().replace('.', '/') + ".class");
        if (resource == null) {
            throw new IllegalStateException("Could not find Agent class file in class path");
        }

        URLConnection urlConnection = resource.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            JarURLConnection jarURLConnection = (JarURLConnection) urlConnection;
            return jarURLConnection.getJarFile().getName();
        }

        InputStream in = urlConnection.getInputStream();
        ZipOutputStream out = null;
        File file = null;
        try {
            file = File.createTempFile(Agent.class.getName(), ".jar");
            file.deleteOnExit();

            out = new ZipOutputStream(new FileOutputStream(file));

            // write manifest
            out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
            try {
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
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
                byte[] buffer = new byte[4096];
                for (int count = in.read(buffer); count >= 0; count = in.read(buffer)) {
                    out.write(buffer, 0, count);
                }
            } finally {
                out.closeEntry();
            }

            return file.getAbsolutePath();
        } catch (IOException e) {
            if (file != null) {
                file.delete();
            }
            throw e;
        } finally {
            close(in);
            close(out);
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }
}
