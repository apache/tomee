/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class RemoteServer {
    private static final boolean DEBUG = System.getProperty("openejb.server.debug","false").equalsIgnoreCase("TRUE");
    private static final boolean TOMCAT;
    static {
        File home = getHome();
        TOMCAT = (home != null) && (new File(new File(home, "bin"), "catalina.sh").exists());
    }

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private Properties properties;
    private Process server;

    public void init(Properties props) {
        properties = props;

        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", "127.0.0.1:4201");
        props.put("java.naming.security.principal", "testuser");
        props.put("java.naming.security.credentials", "testpassword");
    }

    public static void main(String[] args) {
        assert args.length > 0 : "no arguments supplied: valid arguments are 'start' or 'stop'";
        if (args[0].equalsIgnoreCase("start")){
            new RemoteServer().start();
        } else if (args[0].equalsIgnoreCase("stop")) {
            RemoteServer remoteServer = new RemoteServer();
            remoteServer.serverHasAlreadyBeenStarted = false;
            remoteServer.stop();
        } else {
            throw new RuntimeException("valid arguments are 'start' or 'stop'");
        }
    }
    public Properties getProperties() {
        return properties;
    }

    public void destroy() {
        stop();
    }

    public void start() {
        if (!connect()) {
            try {
                System.out.println("[] START SERVER");

                File home = getHome();
                System.out.println("OPENEJB_HOME = "+home.getAbsolutePath());
                String systemInfo = "Java " + System.getProperty("java.version") + "; " + System.getProperty("os.name") + "/" + System.getProperty("os.version");
                System.out.println("SYSTEM_INFO  = "+systemInfo);

                serverHasAlreadyBeenStarted = false;

                File openejbJar = null;
                File javaagentJar = null;

                File lib;
                if (!TOMCAT) {
                    lib = new File(home, "lib");
                } else {
                    lib = new File(new File(new File(home, "webapps"), "openejb"), "lib");
                }
                
                for (File file : lib.listFiles()) {
                    if (file.getName().startsWith("openejb-core") && file.getName().endsWith("jar")){
                        openejbJar = file;
                    }
                    if (file.getName().startsWith("openejb-javaagent") && file.getName().endsWith("jar")){
                        javaagentJar = file;
                    }
                }

                if (openejbJar == null){
                    throw new IllegalStateException("Cannot find the openejb-core jar in "+lib.getAbsolutePath());
                }
                if (javaagentJar == null){
                    throw new IllegalStateException("Cannot find the openejb-javaagent jar in "+lib.getAbsolutePath());
                }

                //File openejbJar = new File(lib, "openejb-core-" + version + ".jar");

                //DMB: If you don't use an array, you get problems with jar paths containing spaces
                // the command won't parse correctly
                String[] args;
                if (!TOMCAT) {
                    if (DEBUG) {
                        args = new String[]{"java",
                                "-Xdebug",
                                "-Xnoagent",
                                "-Djava.compiler=NONE",
                                "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005",

                                "-javaagent:" + javaagentJar.getAbsolutePath(),

                                "-jar", openejbJar.getAbsolutePath(), "start"
                        };
                    } else {
                        args = new String[]{"java",
                                "-javaagent:" + javaagentJar.getAbsolutePath(),
                                "-jar", openejbJar.getAbsolutePath(), "start"
                        };
                    }
                } else {
                    File bin = new File(home, "bin");
                    File bootstrapJar = new File(bin, "bootstrap.jar");
                    File commonsLoggingJar = new File(bin, "commons-logging-api.jar");

                    File conf = new File(home, "conf");
                    File loggingProperties = new File(conf, "logging.properties");

                    File endorsed = new File(home, "endorsed");
                    File temp = new File(home, "temp");

                    if (DEBUG) {
                        args = new String[] { "java",
                                "-Xdebug",
                                "-Xnoagent",
                                "-Djava.compiler=NONE",
                                "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005",

                                "-javaagent:" + javaagentJar.getAbsolutePath(),

                                "-Dcom.sun.management.jmxremote",

                                "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager",
                                "-Djava.util.logging.config.file=" + loggingProperties.getAbsolutePath(),

                                "-Djava.io.tmpdir=" + temp.getAbsolutePath(),
                                "-Djava.endorsed.dirs=" + endorsed.getAbsolutePath(),
                                "-Dcatalina.base=" + home.getAbsolutePath(),
                                "-Dcatalina.home=" + home.getAbsolutePath(),
                                "-Dopenejb.servicemanager.enabled=" + Boolean.getBoolean("openejb.servicemanager.enabled"),

                                "-classpath", bootstrapJar.getAbsolutePath() + ":" + commonsLoggingJar.getAbsolutePath(),

                                "org.apache.catalina.startup.Bootstrap", "start"
                        };
                    } else {
                        args = new String[] { "java",
                                "-javaagent:" + javaagentJar.getAbsolutePath(),

                                "-Dcom.sun.management.jmxremote",

                                "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager",
                                "-Djava.util.logging.config.file=" + loggingProperties.getAbsolutePath(),

                                "-Djava.io.tmpdir=" + temp.getAbsolutePath(),
                                "-Djava.endorsed.dirs=" + endorsed.getAbsolutePath(),
                                "-Dcatalina.base=" + home.getAbsolutePath(),
                                "-Dcatalina.home=" + home.getAbsolutePath(),
                                "-Dopenejb.servicemanager.enabled=" + Boolean.getBoolean("openejb.servicemanager.enabled"),

                                "-classpath", bootstrapJar.getAbsolutePath() + ":" + commonsLoggingJar.getAbsolutePath(),

                                "org.apache.catalina.startup.Bootstrap", "start"
                        };
                    }
                }
                server = Runtime.getRuntime().exec(args);

                // Pipe the processes STDOUT to ours
                InputStream out = server.getInputStream();
                Thread serverOut = new Thread(new Pipe(out, System.out));

                serverOut.setDaemon(true);
                serverOut.start();

                // Pipe the processes STDERR to ours
                InputStream err = server.getErrorStream();
                Thread serverErr = new Thread(new Pipe(err, System.err));

                serverErr.setDaemon(true);
                serverErr.start();
            } catch (Exception e) {
                throw (RuntimeException)new RuntimeException("Cannot start the server.  Exception: "+e.getClass().getName()+": "+e.getMessage()).initCause(e);
            }
            if (DEBUG) {
                connect(Integer.MAX_VALUE);
            } else {
                connect(10);
            }
        } else {
            //System.out.println("[] SERVER STARTED");
        }
    }

    private static File getHome() {
        String openejbHome = System.getProperty("openejb.home");

        if (openejbHome != null) {
            return new File(openejbHome);
        } else {
            return null;
        }
    }

    public void stop() {
        if (!serverHasAlreadyBeenStarted) {
            try {
                System.out.println("[] STOP SERVER");

                int port;
                String command;
                if (!TOMCAT) {
                    port = 4200;
                    command = "Stop";
                } else {
                    port = 8005;
                    command = "SHUTDOWN";
                }

                Socket socket = new Socket("localhost", port);
                OutputStream out = socket.getOutputStream();
                out.write(command.getBytes());

                if (server != null) {
                    server.waitFor();
                    server = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean connect() {
        return connect(1);
    }

    private boolean connect(int tries) {
        //System.out.println("CONNECT "+ tries);
        try {
            int port;
            if (!TOMCAT) {
                port = 4200;
            } else {
                port = 8005;
            }

            Socket socket = new Socket("localhost", port);
            OutputStream out = socket.getOutputStream();
            out.close();
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
