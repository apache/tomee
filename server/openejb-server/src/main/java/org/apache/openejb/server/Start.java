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

import java.io.File;

import java.io.InputStream;

import java.io.OutputStream;

import java.lang.reflect.Method;

import java.net.Socket;

import java.util.ArrayList;

import java.util.Iterator;

import java.util.Map;

import java.util.Set;

public class Start {

    public static void main(String[] args) {

//        System.exit(new Start().start()?0:1);

        new Start().start();

    }

    public boolean start() {

        if (!connect()) {

            forkServerProcess();

            return connect(10);

        } else {

            System.out.println(":: server already started ::");

            return true;

        }

    }

    private void forkServerProcess() {

        try {

            ArrayList cmd = new ArrayList();

            String s = java.io.File.separator;

            String java = System.getProperty("java.home") + s + "bin" + s + "java";

            cmd.add(java);

            addSystemProperties(cmd);

            cmd.add("-classpath");

            cmd.add(getClasspath());

            cmd.add("org.apache.openejb.server.Main");

            String[] command = (String[]) cmd.toArray(new String[0]);

            Runtime runtime = Runtime.getRuntime();

            Process server = runtime.exec(command);

            InputStream out = server.getInputStream();

            Thread serverOut = new Thread(new Pipe(out, System.out));

            serverOut.setDaemon(true);

            serverOut.start();

            InputStream err = server.getErrorStream();

            Thread serverErr = new Thread(new Pipe(err, System.err));

            serverErr.setDaemon(true);

            serverErr.start();

        } catch (Exception e) {

            throw new RuntimeException("Cannot start the server.");

        }

    }

    private void addSystemProperties(ArrayList cmd) {

        Set set = System.getProperties().entrySet();

        for (Iterator iter = set.iterator(); iter.hasNext();) {

            Map.Entry entry = (Map.Entry) iter.next();

            String key = (String) entry.getKey();

            String value = (String) entry.getValue();

            if (key.matches("^-X.*")) {

                cmd.add(key + value);

            } else if (!key.matches("^(java|javax|os|sun|user|file|awt|line|path)\\..*")) {

                cmd.add("-D" + key + "=" + value);

            }

        }

    }

    private String getClasspath() {

        String classpath = System.getProperty("java.class.path");

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        String antLoader = "org.apache.tools.ant.AntClassLoader";

        if (cl.getClass().getName().equals(antLoader)) {

            try {

                Class ant = cl.getClass();

                Method getClasspath = ant.getMethod("getClasspath", new Class[0]);

                classpath += File.pathSeparator + getClasspath.invoke(cl, new Object[0]);

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

        return classpath;

    }

    private boolean connect() {

        return connect(1);

    }

    private boolean connect(int tries) {

        try {

            Socket socket = new Socket("localhost", 4201);

            OutputStream out = socket.getOutputStream();

        } catch (Exception e) {

            if (tries < 2) {

                return false;

            } else {

                try {

                    Thread.sleep(2000);

                } catch (Exception e2) {

                    e.printStackTrace();

                }

                return connect(--tries);

            }

        }

        return true;

    }

    private static final class Pipe implements Runnable {

        private final InputStream is;

        private final OutputStream out;

        private Pipe(InputStream is, OutputStream out) {

            super();

            this.is = is;

            this.out = out;

        }

        public void run() {

            try {

                int i = is.read();

                out.write(i);

                while (i != -1) {

                    i = is.read();

                    out.write(i);

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }

    }

}
