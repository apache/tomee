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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Connect;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class Start {

    public static void main(final String[] args) {

        //        System.exit(new Start().start()?0:1);

        new Start().start();

    }

    public static boolean connect() {
        final int port = SystemInstance.get().getOptions().get("ejbd.port", 4201);
        return Connect.connect(1, "localhost", port);

    }

    public boolean start() {

        if (!connect()) {

            forkServerProcess();
            final int port = SystemInstance.get().getOptions().get("ejbd.port", 4201);
            return Connect.connect(10, "localhost", port);

        } else {

            System.out.println(":: server already started ::");

            return true;

        }

    }

    private void forkServerProcess() {

        try {

            final ArrayList<String> cmd = new ArrayList<>();

            final String s = java.io.File.separator;

            //Not really required here for exec, but as a reminder that we run on all platforms
            final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

            String java = System.getProperty("java.home") + s + "bin" + s + (isWindows ? "java.exe" : "java");

            cmd.add(java);

            addSystemProperties(cmd);

            cmd.add("-classpath");

            cmd.add(getClasspath());

            cmd.add("org.apache.openejb.server.Main");

            final String[] command = cmd.toArray(new String[cmd.size()]);

            final Runtime runtime = Runtime.getRuntime();

            Process server = runtime.exec(command);

            InputStream out = server.getInputStream();

            Thread serverOut = new Thread(new Pipe(out, System.out));

            serverOut.setDaemon(true);

            serverOut.start();

            final InputStream err = server.getErrorStream();

            Thread serverErr = new Thread(new Pipe(err, System.err));

            serverErr.setDaemon(true);

            serverErr.start();

        } catch (Exception e) {

            throw new ServerRuntimeException("Cannot start the server.");

        }

    }

    private void addSystemProperties(final ArrayList<String> cmd) {

        final Set set = System.getProperties().entrySet();

        for (final Object aSet : set) {

            final Map.Entry entry = (Map.Entry) aSet;

            String key = (String) entry.getKey();

            String value = (String) entry.getValue();

            if (key.matches("^-X.*")) {

                cmd.add(key + value);

            } else if (!key.matches("^(java|javax|os|sun|user|file|awt|line|path)\\..*")) {

                cmd.add("-D" + key + "=" + value);

            }

        }

    }

    @SuppressWarnings("unchecked")
    private String getClasspath() {

        String classpath = System.getProperty("java.class.path");

        final ClassLoader cl = Thread.currentThread().getContextClassLoader();

        String antLoader = "org.apache.tools.ant.AntClassLoader";

        if (cl.getClass().getName().equals(antLoader)) {

            try {

                final Class ant = cl.getClass();

                Method getClasspath = ant.getMethod("getClasspath", new Class[0]);

                classpath += File.pathSeparator + getClasspath.invoke(cl, new Object[0]);

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

        return classpath;

    }

    private static final class Pipe implements Runnable {

        private final InputStream is;
        private final OutputStream out;

        private Pipe(final InputStream is, final OutputStream out) {

            super();

            this.is = is;

            this.out = out;

        }

        @Override
        public void run() {

            try {

                int i = is.read();

                out.write(i);

                while (i != -1) {

                    i = is.read();

                    out.write(i);

                }

            } catch (final Exception e) {

                e.printStackTrace();

            }

        }

    }

}
