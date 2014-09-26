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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * NOTE: Do not add inner or anonymous classes or a dependency without updating ExecMojo
 *
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
    public static final String START = "start";
    public static final String STOP = "stop";

    private final boolean debug = options.get(OPENEJB_SERVER_DEBUG, false);
    private final boolean profile = options.get("openejb.server.profile", false);
    private final boolean tomcat;
    private final String javaOpts = System.getProperty("java.opts");
    private String additionalClasspath;

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private Properties properties;
    private final AtomicReference<Process> server = new AtomicReference<Process>();
    private final int tries;
    private final boolean verbose;
    private final int portShutdown;
    private final String host;
    private final String command;
    private File home;
    private int portStartup;

    public RemoteServer() {
        this(options.get("connect.tries", 60), options.get("verbose", false));
    }

    public RemoteServer(final int tries, final boolean verbose) {
        this.tries = (tries < 1 ? 1 : (tries > 3600 ? 3600 : tries)); //Wait at least 1 second to start or stop, but not more than an hour.
        this.verbose = verbose;
        home = getHome();
        tomcat = (home != null) && (new File(new File(home, "bin"), "catalina.sh").exists());

        portShutdown = options.get(SERVER_SHUTDOWN_PORT, tomcat ? 8005 : 4200);
        portStartup = portShutdown;
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
        assert args.length > 0 : "no arguments supplied: valid arguments are 'start' or 'stop'";
        if (args[0].equalsIgnoreCase(START)) {
            new RemoteServer().start();
        } else if (args[0].equalsIgnoreCase(STOP)) {
            final RemoteServer remoteServer = new RemoteServer();
            remoteServer.serverHasAlreadyBeenStarted = false;
            try {
                remoteServer.forceStop();
            } catch (final Exception e) {
                e.printStackTrace(System.err);
            }
        } else {
            throw new OpenEJBRuntimeException("valid arguments are 'start' or 'stop'");
        }
    }

    public int getPortStartup() {
        return this.portStartup;
    }

    public void setPortStartup(final int portStartup) {
        this.portStartup = portStartup;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void destroy() {

        final boolean stopSent = stop();

        final Process p = server.get();
        if (p != null) {

            if (stopSent) {
                waitFor(p);
            } else {
                p.destroy();
            }
        }
    }

    public void start() {
        start(Collections.<String>emptyList(), START, true);
    }

    public void start(final List<String> additionalArgs, final String cmd, final boolean checkPortAvailable) {
        cmd(additionalArgs, cmd, checkPortAvailable);
    }

    private void cmd(final List<String> additionalArgs, final String cmd, final boolean checkPortAvailable) {
        boolean ok = true;
        final int port = START.equals(cmd) ? portStartup : portShutdown;

        if (checkPortAvailable) {
            ok = !connect(port, 1);
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

                File javaagentJar = null;
                try {
                    javaagentJar = lib("openejb-javaagent", lib, webapplib);
                } catch (final IllegalStateException ise) {
                    // no-op
                }

                final File conf = new File(home, "conf");
                final File loggingProperties = new File(conf, "logging.properties");

                //File openejbJar = new File(lib, "openejb-core-" + version + ".jar");

                final String java;
                final boolean isWindows = System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows");
                if (isWindows && START.equals(cmd) && options.get("server.windows.fork", false)) {
                    // run and forget
                    java = new File(System.getProperty("java.home"), "bin/javaw").getAbsolutePath();
                } else {
                    java = new File(System.getProperty("java.home"), "bin/java").getAbsolutePath();
                }

                final List<String> argsList = new ArrayList<String>(20);
                argsList.add(java);
                argsList.add("-XX:+HeapDumpOnOutOfMemoryError");

                if (debug) {
                    argsList.add("-Xdebug");
                    argsList.add("-Xnoagent");
                    argsList.add("-Djava.compiler=NONE");
                    argsList.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + options.get(SERVER_DEBUG_PORT, 5005));
                }

                if (profile) {
                    String yourkitHome = options.get("yourkit.home", "/Applications/YourKit_Java_Profiler_9.5.6.app/bin/mac/");
                    if (!yourkitHome.endsWith("/")) {
                        yourkitHome += "/";
                    }
                    final String yourkitOpts = options.get("yourkit.opts", "disablestacktelemetry,disableexceptiontelemetry,builtinprobes=none,delay=10000,sessionname=Tomcat");
                    argsList.add("-agentpath:" + yourkitHome + "libyjpagent.jnilib=" + yourkitOpts);
                }

                if (javaOpts != null) {
                    Collections.addAll(argsList, javaOpts.split(" +"));
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

                if (!addedArgs.containsKey("-Djava.util.logging.config.file") && loggingProperties.exists()) {
                    argsList.add("-Djava.util.logging.config.file=" + loggingProperties.getAbsolutePath());
                }

                if (javaagentJar != null && javaagentJar.exists()) {
                    argsList.add("-javaagent:" + javaagentJar.getAbsolutePath());
                }

                //DMB: If you don't use an array, you get problems with jar paths containing spaces
                // the command won't parse correctly
                final String ps = File.pathSeparator;

                final String[] args;
                if (!tomcat) {
                    final File openejbJar = lib("openejb-core", lib, webapplib);
                    final StringBuilder cp = new StringBuilder(openejbJar.getAbsolutePath());
                    if (additionalClasspath != null) {
                        cp.append(ps).append(additionalClasspath);
                    }

                    argsList.add("-cp");
                    argsList.add(cp.toString());
                    argsList.add("org.apache.openejb.cli.Bootstrap");
                } else {
                    final File bin = new File(home, "bin");
                    final File tlib = new File(home, "lib");
                    final File bootstrapJar = new File(bin, "bootstrap.jar");
                    final File juliJar = new File(bin, "tomcat-juli.jar");
                    final File commonsLoggingJar = new File(bin, "commons-logging-api.jar");

                    final File endorsed = new File(home, "endorsed");
                    final File temp = new File(home, "temp");

//                    if (!addedArgs.containsKey("-Dcom.sun.management.jmxremote")) {
//                        argsList.add("-Dcom.sun.management.jmxremote");
//                    }
                    if (!addedArgs.containsKey("-Djava.util.logging.manager")) {
                        argsList.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");
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

                    final StringBuilder cp = new StringBuilder(bootstrapJar.getAbsolutePath()).append(ps).append(juliJar.getAbsolutePath());
                    if (commonsLoggingJar.exists()) {
                        cp.append(ps).append(commonsLoggingJar.getAbsolutePath());
                    }
                    if (additionalClasspath != null) {
                        cp.append(ps).append(additionalClasspath);
                    }
                    argsList.add(cp.toString());

                    argsList.add("org.apache.catalina.startup.Bootstrap");
                }

                if (cmd == null) {
                    argsList.add(START);
                } else {
                    argsList.add(cmd);
                }
                args = argsList.toArray(new String[argsList.size()]);

                if (verbose) {
                    System.out.println(Join.join("\n", args));
                }

                // kill3UNIXDebug();
                final ProcessBuilder pb = new ProcessBuilder(args);
                pb.directory(home.getAbsoluteFile());
                Process p = pb.start();

                //Process p = Runtime.getRuntime().exec(args);
                Pipe.pipeOut(p); // why would we need to redirect System.in to the process, TomEE doesn't use it

                if (START.equals(cmd)) {
                    server.set(p);
                } else if (STOP.equals(cmd)) {
                    waitFor(p);
                    p = server.get();
                    if (p != null) {
                        waitFor(p);
                    }
                }

                System.out.println("Started server process on port: " + port);

            } catch (final Exception e) {
                throw (RuntimeException) new OpenEJBRuntimeException("Cannot start the server.  Exception: " + e.getClass().getName() + ": " + e.getMessage()).initCause(e);
            }

            if (debug) {
                if (!connect(port, Integer.MAX_VALUE)) {
                    destroy();
                    throw new OpenEJBRuntimeException("Could not connect to server");
                }
            } else {
                if (!connect(port, tries)) {
                    destroy();
                    throw new OpenEJBRuntimeException("Could not connect to server");
                }
            }

        } else {
            if (verbose) {
                System.out.println("[] FOUND STARTED SERVER");
            }
        }
    }

    private void waitFor(final Process p) {
        final CountDownLatch latch = new CountDownLatch(1);
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    p.waitFor();
                } catch (final InterruptedException e) {
                    //Ignore
                }

                latch.countDown();
            }
        }, "process-waitFor");

        t.start();

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                killOnExit(p);
                throw new RuntimeException("Timeout waiting for process");
            }
        } catch (final InterruptedException e) {
            killOnExit(p);
        }
    }

    public void kill3UNIX() { // debug purpose only
        if (System.getProperty("os.name", "unknown").toLowerCase().startsWith("windows")) {
            return;
        }

        try {
            final Field f = server.get().getClass().getDeclaredField("pid");
            f.setAccessible(true);
            final int pid = (Integer) f.get(server.get());
            Pipe.pipe(Runtime.getRuntime().exec("kill -3 " + pid));
        } catch (final Exception e1) {
            e1.printStackTrace();
        }
    }

    private File lib(final String name, final File... dirs) {
        for (final File dir : dirs) {
            final File[] files = dir.listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }
                    if (!file.getName().endsWith(".jar")) {
                        continue;
                    }
                    if (file.getName().startsWith(name)) {
                        return file;
                    }
                }
            }
        }

        if (debug) {
            for (final File dir : dirs) {
                dumpLibs(dir);
            }
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
        return server.get();
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

    public boolean stop() {
        if (sendShutdown(5)) {
            return true;
        } else {
            if (verbose) {
                notSent();
            }

            return false;
        }
    }

    private void notSent() {
        System.out.println("Failed to send the shutdown notification - TomEE is likely shut down already");
    }

    public void forceStop() throws Exception {
        if (sendShutdown(5)) {

            // Check TomEE was effectively shut down after getting the message
            // There can be concurrent shutdown operations (catalina shutdown hook for instance),
            // so we have to wait here since it is important to be synchronous in this method
            waitForServerShutdown();
        } else {
            if (verbose) {
                notSent();
            }
        }
    }

    private void waitForServerShutdown() throws Exception {

        if (verbose) {
            System.out.print("Waiting for TomEE shutdown.");
        }

        final boolean b = disconnect(portShutdown, tries);

        if (verbose) {
            System.out.println();
        }

        if (!b) {
            //We need to know about this
            System.out.println("SEVERE: Failed to shutdown TomEE running on port " + portStartup + " using shutdown port: " + portShutdown);
        }
    }

    /**
     * Send the shutdown message to the running server
     *
     * @param attempts How many times to try to send the message before giving up
     * @return True is the message was sent, else false if unable to connect after the defined number of attempts
     */
    private boolean sendShutdown(int attempts) {
        Socket socket = null;
        OutputStream stream = null;
        try {
            socket = new Socket(host, portShutdown);
            stream = socket.getOutputStream();
            final String shutdown = command + Character.toString((char) 0);
            for (int i = 0; i < shutdown.length(); i++) {
                stream.write(shutdown.charAt(i));
            }
            stream.flush();
        } catch (final Exception e) {
            if (attempts > 0) {
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException ie) {
                    return false;
                }
                return sendShutdown(--attempts);
            } else {
                return false;
            }
        } finally {
            IO.close(stream);
            if (socket != null) {
                try {
                    socket.close();
                } catch (final Exception e) {
                    // Ignore
                }
            }
        }

        return true;
    }

    private boolean connect(final int port, int tries) {
        if (verbose) {
            System.out.println("[] CONNECT ATTEMPT " + (this.tries - tries) + " on port: " + port);
        }

        Socket s = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(this.host, port), 1000);
            s.getOutputStream().close();
            if (verbose) {
                System.out.println("[] CONNECTED IN " + (this.tries - tries));
            }
        } catch (final Exception e) {
            if (tries < 2) {
                if (verbose) {
                    System.out.println("[] CONNECT ATTEMPTS FAILED ( " + (this.tries - tries) + " ATTEMPTS)");
                }
                return false;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (final Exception e2) {
                    e2.printStackTrace();
                }
                return connect(port, --tries);
            }
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (final Exception ignored) {
                    // no-op
                }
            }
        }

        return true;
    }

    private boolean disconnect(final int port, int tries) {
        if (verbose) {
            System.out.println("[] DISCONNECT ATTEMPT " + (this.tries - tries) + " on port: " + port);
        }

        Socket s = null;
        try {
            s = new Socket();
            s.connect(new InetSocketAddress(this.host, port), 500);
            s.getOutputStream().close();

            if (verbose) {
                System.out.println("[] NOT DISCONNECTED AFTER ( " + (this.tries - tries) + " ATTEMPTS)");
            }

            if (tries < 2) {
                //Give up
                return false;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (final Exception e2) {
                    e2.printStackTrace();
                }

                return disconnect(port, --tries);
            }

        } catch (final IOException e) {
            //This is what we want
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (final Exception ignored) {
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
        final Process p = this.server.get();
        if (!serverHasAlreadyBeenStarted && kill.contains(p)) {
            return;
        }

        killOnExit(p);
    }

    private static void killOnExit(final Process p) {
        kill.add(p);
    }

    // Shutdown hook for processes
    private static final List<Process> kill = new ArrayList<Process>();

    static {
        Runtime.getRuntime().addShutdownHook(new CleanUpThread());
    }

    public static class CleanUpThread extends Thread {
        @Override
        public void run() {
            for (final Process server : kill) {
                try {
                    if (server != null) {
                        server.destroy();
                        server.waitFor();
                    }
                } catch (final Throwable e) {
                    //Ignore
                }
            }
        }
    }
}
