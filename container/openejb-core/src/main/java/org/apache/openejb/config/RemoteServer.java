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

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Options;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Pipe;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
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
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class RemoteServer {

    private static final Options options = new Options(System.getProperties());
    public static final String SERVER_DEBUG_PORT = "server.debug.port";
    public static final String SERVER_SHUTDOWN_PORT = "server.shutdown.port";
    public static final String SERVER_SHUTDOWN_HOST = "server.shutdown.host";
    public static final String SERVER_SHUTDOWN_COMMAND = "server.shutdown.command";
    public static final String OPENEJB_SERVER_DEBUG = "openejb.server.debug";

    private boolean debug = options.get(OPENEJB_SERVER_DEBUG, false);
    private final boolean profile = options.get("openejb.server.profile", false);
    private final boolean tomcat;
    private final String javaOpts = System.getProperty("java.opts");
    private String additionalClasspath = null;

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private Properties properties;
    private Process server;
    private final int tries;
    private final boolean verbose;
    private final int shutdownPort;
    private final String host;
    private final String command;
    private File home = null;

    public RemoteServer() {
        this(options.get("connect.tries", 60), options.get("verbose", false));
    }

    public RemoteServer(final int tries, final boolean verbose) {
        this.tries = tries;
        this.verbose = verbose;
        home = getHome();
        tomcat = (home != null) && (new File(new File(home, "bin"), "catalina.sh").exists());

        shutdownPort = options.get(SERVER_SHUTDOWN_PORT, tomcat ? 8005 : 4200);
        command = options.get(SERVER_SHUTDOWN_COMMAND, "SHUTDOWN");
        host = options.get(SERVER_SHUTDOWN_HOST, "localhost");
    }

    public void init(final Properties props) {
        properties = props;

        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        final int port = options.get("ejbd.port", 4201);
        props.put("java.naming.provider.url", options.get("java.naming.provider.url", "127.0.0.1:" + port));
        props.put("java.naming.security.principal", "testuser");
        props.put("java.naming.security.credentials", "testpassword");
    }

    public static void main(final String[] args) {
        assert args.length > 0 : "no arguments supplied: valid argumen -efts are 'start' or 'stop'";
        if (args[0].equalsIgnoreCase("start")) {
            new RemoteServer().start();
        } else if (args[0].equalsIgnoreCase("stop")) {
            final RemoteServer remoteServer = new RemoteServer();
            remoteServer.serverHasAlreadyBeenStarted = false;
            remoteServer.stop();
        } else {
            throw new OpenEJBRuntimeException("valid arguments are 'start' or 'stop'");
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public void destroy() {
        stop();
        if (server != null) {
            try {
                server.waitFor();
            } catch (InterruptedException e) {
                // no-op
            }
        }
    }

    public void start() {
        start(Collections.EMPTY_LIST, "start", true);
    }

    public void start(final List<String> additionalArgs, final String cmd, final boolean checkPortAvailable) {
        cmd(additionalArgs, cmd, checkPortAvailable);
    }

    private void cmd(final List<String> additionalArgs, final String cmd, final boolean checkPortAvailable) {
        boolean ok = true;
        if (checkPortAvailable) {
            ok = !connect();
        }
        if (ok) {
            try {
                if (verbose) {
                    System.out.println("[] " + cmd.toUpperCase() + " SERVER");
                }

                final File home = getHome();
                final String javaVersion = System.getProperty("java.version");
                if (verbose) {
                    System.out.println("OPENEJB_HOME = " + home.getAbsolutePath());
                    final String systemInfo = "Java " + javaVersion + "; " + System.getProperty("os.name") + "/" + System.getProperty("os.version");
                    System.out.println("SYSTEM_INFO  = " + systemInfo);
                }

                serverHasAlreadyBeenStarted = false;

                final File lib = new File(home, "lib");
                final File webapplib = new File(new File(new File(home, "webapps"), "tomee"), "lib");

                final File openejbJar = lib("openejb-core", lib, webapplib);
                final File javaagentJar = lib("openejb-javaagent", lib, webapplib);

                //File openejbJar = new File(lib, "openejb-core-" + version + ".jar");

                final String java;
                final boolean isWindows = System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows");
                if (isWindows && "start".equals(cmd) && options.get("server.windows.fork", false)) {
                    // run and forget
                    java = new File(System.getProperty("java.home"), "bin/javaw").getAbsolutePath();
                } else {
                    java = new File(System.getProperty("java.home"), "bin/java").getAbsolutePath();
                }

                //DMB: If you don't use an array, you get problems with jar paths containing spaces
                // the command won't parse correctly
                final String[] args;
                final int debugPort = options.get(SERVER_DEBUG_PORT, 5005);
                if (!tomcat) {
                    if (debug) {
                        args = new String[]{java,
                                            "-XX:+HeapDumpOnOutOfMemoryError",
                                            "-Xdebug",
                                            "-Xnoagent",
                                            "-Djava.compiler=NONE",
                                            "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + debugPort,

                                            "-javaagent:" + javaagentJar.getAbsolutePath(),

                                            "-jar", openejbJar.getAbsolutePath(), "start"
                        };
                    } else {
                        args = new String[]{java,
                                            "-XX:+HeapDumpOnOutOfMemoryError",
                                            "-javaagent:" + javaagentJar.getAbsolutePath(),
                                            "-jar", openejbJar.getAbsolutePath(), "start"
                        };
                    }
                } else {
                    final File bin = new File(home, "bin");
                    final File tlib = new File(home, "lib");
                    final File bootstrapJar = new File(bin, "bootstrap.jar");
                    final File juliJar = new File(bin, "tomcat-juli.jar");
                    final File commonsLoggingJar = new File(bin, "commons-logging-api.jar");

                    final File conf = new File(home, "conf");
                    final File loggingProperties = new File(conf, "logging.properties");

                    File endorsed = new File(home, "endorsed");
                    if (javaVersion != null && javaVersion.startsWith("1.7.")) { // java 7
                        endorsed = new File(home, "endorsed7"); // doesn't exist but just to ignore it with j7
                    }
                    final File temp = new File(home, "temp");

                    final List<String> argsList = new ArrayList<String>() {
                    };
                    argsList.add(java);
                    argsList.add("-XX:+HeapDumpOnOutOfMemoryError");

                    if (debug) {
                        argsList.add("-Xdebug");
                        argsList.add("-Xnoagent");
                        argsList.add("-Djava.compiler=NONE");
                        argsList.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + debugPort);
                    }

                    if (profile) {
                        String yourkitHome = options.get("yourkit.home", "/Applications/YourKit_Java_Profiler_9.5.6.app/bin/mac/");
                        if (!yourkitHome.endsWith("/"))
                            yourkitHome += "/";
                        final String yourkitOpts = options.get("yourkit.opts", "disablestacktelemetry,disableexceptiontelemetry,builtinprobes=none,delay=10000,sessionname=Tomcat");
                        argsList.add("-agentpath:" + yourkitHome + "libyjpagent.jnilib=" + yourkitOpts);
                    }

                    if (javaOpts != null) {
                        final String[] strings = javaOpts.split(" +");
                        for (final String string : strings) {
                            argsList.add(string);
                        }
                    }

                    final Map<String, String> addedArgs = new HashMap<String, String>();
                    if (additionalArgs != null) {
                        for (final String arg : additionalArgs) {
                            final String[] values = arg.split("=");
                            if (values.length == 1) {
                                addedArgs.put(values[0], "null");
                            } else {
                                addedArgs.put(values[0], values[1]);
                            }
                            argsList.add(arg);
                        }
                    }

                    argsList.add("-javaagent:" + javaagentJar.getAbsolutePath());
                    if (!addedArgs.containsKey("-Dcom.sun.management.jmxremote")) {
                        argsList.add("-Dcom.sun.management.jmxremote");
                    }
                    if (!addedArgs.containsKey("-Djava.util.logging.manager")) {
                        argsList.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");
                    }
                    if (!addedArgs.containsKey("-Djava.util.logging.config.file") && loggingProperties.exists()) {
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
                    final String ps = File.pathSeparator;
                    final StringBuilder cp = new StringBuilder(bootstrapJar.getAbsolutePath()).append(ps).append(juliJar.getAbsolutePath());
                    if (commonsLoggingJar.exists()) {
                        cp.append(ps).append(commonsLoggingJar.getAbsolutePath());
                    }
                    if (additionalClasspath != null) {
                        cp.append(ps).append(additionalClasspath);
                    }
                    argsList.add(cp.toString());

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

                // kill3UNIXDebug();

                final Process process = Runtime.getRuntime().exec(args);
                Pipe.pipe(process);

                if ("start".equals(cmd)) {
                    server = process;
                } else if ("stop".equals(cmd) && server != null) {
                    server.waitFor();
                }

            } catch (Exception e) {
                throw (RuntimeException) new OpenEJBRuntimeException("Cannot start the server.  Exception: " + e.getClass().getName() + ": " + e.getMessage()).initCause(e);
            }
            if (checkPortAvailable) {
                if (debug) {
                    if (!connect(Integer.MAX_VALUE))
                        throw new OpenEJBRuntimeException("Could not connect to server");
                } else {
                    if (!connect(tries))
                        throw new OpenEJBRuntimeException("Could not connect to server");
                }
            }
        } else {
            if (verbose)
                System.out.println("[] FOUND STARTED SERVER");
        }
    }

    // debugging method (mainly for buildbot), don't let it activated when all is fine
    private void kill3UNIXDebug() {
        final Thread t = new Thread() {
            @Override
            public void run() {
                setName("[DEBUG] Dump Observer");

                boolean end = false;
                int i = 0;
                while (!end) {
                    try {
                        if (server == null) {
                            throw new IllegalThreadStateException();
                        }
                        server.exitValue();
                        end = true;
                    } catch (IllegalThreadStateException e) {
                        i++;
                        try {
                            Thread.sleep(Integer.getInteger("sleep", 5000 * 60));
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        if (i == 5) {
                            kill3UNIX();
                            i = 0;
                        }
                    }
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    public void kill3UNIX() { // debug purpose only
        if (System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows")) {
            return;
        }

        try {
            final Field f = server.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            final int pid = (Integer) f.get(server);
            Pipe.pipe(Runtime.getRuntime().exec("kill -3 " + pid));
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private File lib(final String name, final File... dirs) {
        for (final File dir : dirs) {
            final File[] files = dir.listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (!file.isFile())
                        continue;
                    if (!file.getName().endsWith(".jar"))
                        continue;
                    if (file.getName().startsWith(name))
                        return file;
                }
            }
        }

        for (final File dir : dirs) {
            dumpLibs(dir);
        }
        throw new IllegalStateException("Cannot find the " + name + " jar");
    }

    // for debug purpose
    private static void dumpLibs(final File dir) {
        if (!dir.exists()) {
            System.out.println("lib dir doesn't exist");
            return;
        }
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File lib : files) {
                System.out.println(lib.getAbsolutePath());
            }
        }
    }

    public Process getServer() {
        return server;
    }

    private void addIfSet(final List<String> argsList, final String key) {
        if (System.getProperties().containsKey(key)) {
            argsList.add("-D" + key + "=" + System.getProperty(key));
        }
    }

    private File getHome() {
        if (home != null) {
            return home;
        }

        final String openejbHome = System.getProperty("openejb.home");

        if (openejbHome != null) {
            home = new File(openejbHome);
        }
        return home;
    }

    public void stop() {
        if (!serverHasAlreadyBeenStarted) {
            try {
                shutdown();
            } catch (Exception e) {
                if (verbose) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    public void forceStop() throws Exception {
        shutdown();

        // check tomcat was effectively shutted down
        // we can have some concurrent shutdown commands (catalina shutdown hook for instance)
        // so we can have to wait here since it is important to be synchronous in this method
        waitForServerShutdown();
    }

    private void waitForServerShutdown() throws InterruptedException {
        if (verbose) {
            System.out.print("Waiting for TomEE shutdown.");
        }
        while (connect()) {
            Thread.sleep(1000);
            if (verbose) {
                System.out.print(".");
            }
        }
        if (verbose) {
            System.out.println();
        }
    }

    // same as catalina.sh stop {@see org.apache.catalina.startup.Catalina#stopServer}
    private void shutdown() throws Exception {
        Socket socket = null;
        OutputStream stream = null;
        try {
            socket = new Socket(host, shutdownPort);
            stream = socket.getOutputStream();
            final String shutdown = command + Character.toString((char) 0);
            for (int i = 0; i < shutdown.length(); i++) {
                stream.write(shutdown.charAt(i));
            }
            stream.flush();
        } finally {
            IO.close(stream);
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private boolean connect() {
        return connect(1);
    }

    private boolean connect(int tries) {
        if (verbose)
            System.out.println("[] CONNECT ATTEMPT " + (this.tries - tries));

        Socket s = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(host, shutdownPort), 1000);
            s.getOutputStream().close();
            if (verbose) {
                System.out.println("[] CONNECTED IN " + (this.tries - tries));
            }
        } catch (Exception e) {
            if (tries < 2) {
                if (verbose) {
                    System.out.println("[] CONNECT ATTEMPTS FAILED ( " + (this.tries - tries) + " tries)");
                }
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
            if (s != null) {
                try {
                    s.close();
                } catch (Exception ignored) {
                    // no-op
                }
            }
        }

        return true;
    }

    public void setAdditionalClasspath(final String additionalClasspath) {
        this.additionalClasspath = additionalClasspath;
    }

    public void killOnExit() {
        if (!serverHasAlreadyBeenStarted && kill.contains(this.server))
            return;
        kill.add(this.server);
    }

    // Shutdown hook for recursive delete on tmp directories
    static final List<Process> kill = new ArrayList<Process>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (final Process server : kill) {
                    try {
                        if (server != null) {
                            server.destroy();
                            server.waitFor();
                        }
                    } catch (Throwable e) {
                        //Ignore
                    }
                }
            }
        });
    }
}
