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
package org.apache.openejb.config;

import org.apache.openejb.loader.Options;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Pipe;

import java.io.File;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class RemoteServer {
    private static final Options options = new Options(System.getProperties());

    private final boolean debug = options.get("openejb.server.debug", false);
    private final boolean profile = options.get("openejb.server.profile", false);
    private final boolean tomcat;
    private final String javaOpts = System.getProperty("java.opts");

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private Properties properties;
    private Process server;
    private final int tries;
    private final boolean verbose;
    private final int shutdownPort;

    public RemoteServer() {
        this(options.get("connect.tries", 10), options.get("verbose", false));
    }

    public RemoteServer(int tries, boolean verbose) {
        this.tries = tries;
        this.verbose = verbose;
        File home = getHome();
        tomcat = (home != null) && (new File(new File(home, "bin"), "catalina.sh").exists());

        shutdownPort = options.get("server.shutdown.port", tomcat ? 8005 : 4200);
    }

    public void init(Properties props) {
        properties = props;

        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", options.get("java.naming.provider.url", "127.0.0.1:4201"));
        props.put("java.naming.security.principal", "testuser");
        props.put("java.naming.security.credentials", "testpassword");
    }

    public static void main(String[] args) {
        assert args.length > 0 : "no arguments supplied: valid argumen -efts are 'start' or 'stop'";
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
        start(Collections.EMPTY_LIST, "start", true);
    }

    public void start(final List<String> additionalArgs, final String cmd, boolean checkPortAvailable) {
        boolean ok = true;
        if (checkPortAvailable) {
            ok = !connect();
        }
        if (ok) {
            try {
                System.out.println("[] " + cmd.toUpperCase() + " SERVER");

                File home = getHome();
                System.out.println("OPENEJB_HOME = "+ home.getAbsolutePath());
                String systemInfo = "Java " + System.getProperty("java.version") + "; " + System.getProperty("os.name") + "/" + System.getProperty("os.version");
                System.out.println("SYSTEM_INFO  = "+systemInfo);

                serverHasAlreadyBeenStarted = false;

                File openejbJar = null;
                File javaagentJar = null;

                File lib;
                if (!tomcat) {
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

                String java = new File(System.getProperty("java.home"), "bin/java").getAbsolutePath();

                //DMB: If you don't use an array, you get problems with jar paths containing spaces
                // the command won't parse correctly
                String[] args;
                final int debugPort = options.get("server.debug.port", 5005);
                if (!tomcat) {
                    if (debug) {
                        args = new String[] { java,
                                "-XX:+HeapDumpOnOutOfMemoryError",
                                "-Xdebug",
                                "-Xnoagent",
                                "-Djava.compiler=NONE",
                                "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + debugPort,

                                "-javaagent:" + javaagentJar.getAbsolutePath(),

                                "-jar", openejbJar.getAbsolutePath(), "start"
                        };
                    } else {
                        args = new String[] { java,
                                "-XX:+HeapDumpOnOutOfMemoryError",
                                "-javaagent:" + javaagentJar.getAbsolutePath(),
                                "-jar", openejbJar.getAbsolutePath(), "start"
                        };
                    }
                } else {
                    File bin = new File(home, "bin");
                    File tlib = new File(home, "lib");
                    File bootstrapJar = new File(bin, "bootstrap.jar");
                    File juliJar = new File(bin, "tomcat-juli.jar");
                    File commonsLoggingJar = new File(bin, "commons-logging-api.jar");

                    File conf = new File(home, "conf");
                    File loggingProperties = new File(conf, "logging.properties");


                    File endorsed = new File(home, "endorsed");
                    File temp = new File(home, "temp");

                    List<String> argsList = new ArrayList<String>() {};
                    argsList.add(java);
                    argsList.add("-XX:+HeapDumpOnOutOfMemoryError");

                    if (debug) {
                        argsList.add("-Xdebug");
                        argsList.add("-Xnoagent");
                        argsList.add("-Djava.compiler=NONE");
                        argsList.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + debugPort);
                    }

                    if (profile) {
                        String yourkitHome = options.get("yourkit.home","/Applications/YourKit_Java_Profiler_9.5.6.app/bin/mac/");
                        if (!yourkitHome.endsWith("/")) yourkitHome += "/";
                        final String yourkitOpts = options.get("yourkit.opts", "disablestacktelemetry,disableexceptiontelemetry,builtinprobes=none,delay=10000,sessionname=Tomcat");
                        argsList.add("-agentpath:" + yourkitHome + "libyjpagent.jnilib=" + yourkitOpts);
                    }

                    if (javaOpts != null) {
                        final String[] strings = javaOpts.split(" +");
                        for (String string : strings) {
                            argsList.add(string);
                        }
                    }

                    final Map<String, String> addedArgs = new HashMap<String, String>();
                    if (additionalArgs != null) {
                        for (String arg : additionalArgs) {
                            argsList.add("-Dorg.apache.tomcat.util.http.ServerCookie.ALLOW_HTTP_SEPARATORS_IN_V0=true");
                            String[] values = arg.split("=");
                            if (values.length == 1) {
                                addedArgs.put(values[0], "null");
                            } else {
                                addedArgs.put(values[0], values[1]);
                            }
                        }
                    }

                    argsList.add("-javaagent:" + javaagentJar.getAbsolutePath());
                    if (!addedArgs.containsKey("-Dcom.sun.management.jmxremote")) {
                        argsList.add("-Dcom.sun.management.jmxremote");
                    }
                    if (!addedArgs.containsKey("-Djava.util.logging.manager")) {
                        argsList.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");
                    }
                    if (!addedArgs.containsKey("-Djava.util.logging.config.file")) {
                        argsList.add("-Djava.util.logging.config.file=" + loggingProperties.getAbsolutePath());
                    }
                    if (!addedArgs.containsKey("-Djava.io.tmpdir")) {
                        argsList.add("-Djava.io.tmpdir=" + temp.getAbsolutePath());
                    }
                    if (!addedArgs.containsKey("-Djava.endorsed.dirs")) {
                        argsList.add("-Djava.endorsed.dirs=" + endorsed.getAbsolutePath());
                    }
                    if (!addedArgs.containsKey("-Dcatalina.base")) {
                        argsList.add("-Dcatalina.base=" + home.getAbsolutePath());
                    }
                    if (!addedArgs.containsKey("-Dcatalina.home")) {
                        argsList.add("-Dcatalina.home=" + home.getAbsolutePath());
                    }
                    if (!addedArgs.containsKey("-Dcatalina.ext.dirs")) {
                        argsList.add("-Dcatalina.ext.dirs=" + tlib.getAbsolutePath());
                    }
                    if (!addedArgs.containsKey("-Dopenejb.servicemanager.enabled")) {
                        argsList.add("-Dopenejb.servicemanager.enabled=" + Boolean.getBoolean("openejb.servicemanager.enabled"));
                    }
                    if (!addedArgs.containsKey("-Dorg.apache.catalina.STRICT_SERVLET_COMPLIANCE")) {
                        argsList.add("-Dorg.apache.catalina.STRICT_SERVLET_COMPLIANCE=true");
                    }
                    if (!addedArgs.containsKey("-Dorg.apache.tomcat.util.http.ServerCookie.ALLOW_HTTP_SEPARATORS_IN_V0")) {
                        argsList.add("-Dorg.apache.tomcat.util.http.ServerCookie.ALLOW_HTTP_SEPARATORS_IN_V0=true");
                    }

                    if (addedArgs.isEmpty()) { // default case
                        addIfSet(argsList, "javax.net.ssl.keyStore");
                        addIfSet(argsList, "javax.net.ssl.keyStorePassword");
                        addIfSet(argsList, "javax.net.ssl.trustStore");
                        addIfSet(argsList, "java.protocol.handler.pkgs");
                    }

                    argsList.add("-ea");
                    argsList.add("-classpath");
                    String ps = File.pathSeparator;
                    if (commonsLoggingJar.exists()) {
                        argsList.add(bootstrapJar.getAbsolutePath() + ps + juliJar.getAbsolutePath() + ps + commonsLoggingJar.getAbsolutePath());

                    } else {
                        argsList.add(bootstrapJar.getAbsolutePath() + ps + juliJar.getAbsolutePath());
                    }

                    argsList.add("org.apache.catalina.startup.Bootstrap");
                    if (cmd == null) {
                        argsList.add("start");
                    } else {
                        argsList.add(cmd);
                    }
                    
                    args = argsList.toArray(new String[argsList.size()]);
                }


                if (verbose) {
                    System.out.println(Join.join("\n", args));
                }
                server = Runtime.getRuntime().exec(args);

                Pipe.pipe(server);

            } catch (Exception e) {
                throw (RuntimeException)new RuntimeException("Cannot start the server.  Exception: "+e.getClass().getName()+": "+e.getMessage()).initCause(e);
            }
            if (checkPortAvailable) {
                if (debug) {
                    if (!connect(Integer.MAX_VALUE)) throw new RuntimeException("Could not connect to server");
                } else {
                    if (!connect(tries)) throw new RuntimeException("Could not connect to server");
                }
            }
        } else {
            if (verbose) System.out.println("[] FOUND STARTED SERVER");
        }
    }

    public Process getServer() {
        return server;
    }

    private void addIfSet(List<String> argsList, String key) {
        if (System.getProperties().containsKey(key)) {
            argsList.add("-D" + key + "=" + System.getProperty(key));
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

                String command = "SHUTDOWN" + Character.toString((char) 0); // SHUTDOWN + EOF

                Socket socket = new Socket("localhost", shutdownPort);
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
        if (verbose) System.out.println("[] CONNECT ATTEMPT " + (this.tries - tries));
        //System.out.println("CONNECT "+ tries);
        try {

            Socket socket = new Socket("localhost", shutdownPort);
            OutputStream out = socket.getOutputStream();
            out.close();
            if (verbose) System.out.println("[] CONNECTED IN " + (this.tries - tries));
        } catch (Exception e) {
            if (tries < 2) {
                if (verbose) System.out.println("[] CONNECT ATTEMPTS FAILED ( " + (this.tries - tries) + " tries)");
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
}
