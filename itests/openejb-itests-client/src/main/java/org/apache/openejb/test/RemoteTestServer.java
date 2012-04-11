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
package org.apache.openejb.test;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

public class RemoteTestServer implements org.apache.openejb.test.TestServer {

    static {
        System.setProperty("noBanner", "true");
    }

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private Properties properties;

    @Override
    public void init(final Properties props) {
        properties = props;
        if (props.contains("java.naming.security.principal")) throw new IllegalArgumentException("Not allowed 'java.naming.security.principal'");
//        props.put("test.server.class","org.apache.openejb.test.RemoteTestServer");
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "127.0.0.1:4201");
//        props.put("java.naming.security.principal", "testuser");
//        props.put("java.naming.security.credentials", "testpassword");
    }

    @Override
    public void start() {
        if (!connect()) {
            try {
                System.out.println("[] START SERVER");

                final String openejbHome = System.getProperty("openejb.home");

                final File home = new File(openejbHome);
                System.out.println("OPENEJB_HOME = " + home.getAbsolutePath());
                final String systemInfo = "Java " + System.getProperty("java.version") + "; " + System.getProperty("os.name") + "/" + System.getProperty("os.version");
                System.out.println("SYSTEM_INFO  = " + systemInfo);

                serverHasAlreadyBeenStarted = false;

                File openejbJar = null;
                final File lib = new File(home, "lib");
                final File[] files = lib.listFiles();
                for (int i = 0; i < files.length && openejbJar == null; i++) {
                    final File file = files[i];
                    if (file.getName().startsWith("openejb-core") && file.getName().endsWith("jar")) {
                        openejbJar = file;
                    }
                }

                if (openejbJar == null) {
                    throw new IllegalStateException("Cannot find the openejb-core jar in " + lib.getAbsolutePath());
                }

                //File openejbJar = new File(lib, "openejb-core-" + version + ".jar");

                //Not really required here for exec, but as a reminder that we run on all platforms
                final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

                //DMB: If you don't use an array, you get problems with jar paths containing spaces
                // the command won't parse correctly
                final String[] args = {(isWindows ? "java.exe" : "java"), "-jar", openejbJar.getAbsolutePath(), "start"};
                final Process server = Runtime.getRuntime().exec(args);

                // Pipe the processes STDOUT to ours
                final InputStream out = server.getInputStream();
                final Thread serverOut = new Thread(new Pipe(out, System.out));

                serverOut.setDaemon(true);
                serverOut.start();

                // Pipe the processes STDERR to ours
                final InputStream err = server.getErrorStream();
                final Thread serverErr = new Thread(new Pipe(err, System.err));

                serverErr.setDaemon(true);
                serverErr.start();
            } catch (Exception e) {
                throw (RuntimeException) new RuntimeException("Cannot start the server.").initCause(e);
            }
            connect(10);
        } else {
            //System.out.println("[] SERVER STARTED");
        }
    }

    @Override
    public void stop() {
        if (!serverHasAlreadyBeenStarted) {
            try {
                System.out.println("[] STOP SERVER");

                final Socket socket = new Socket("localhost", 4200);
                final OutputStream out = socket.getOutputStream();

                out.write("Stop".getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Properties getContextEnvironment() {
        return (Properties) properties.clone();
    }

    private boolean connect() {
        return connect(1);
    }

    private boolean connect(int tries) {
        //System.out.println("CONNECT "+ tries);
        OutputStream out = null;
        try {
            final Socket socket = new Socket("localhost", 4200);
            out = socket.getOutputStream();
        } catch (Exception e) {
            //System.out.println(e.getMessage());
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
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (Throwable e) {
                    //Ignore
                }
            }
        }

        return true;
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
