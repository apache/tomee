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
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.openejb.util.Join;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Locale;

/**
 * NOTE: Do not add inner or anonymous classes or a dependency without updating ExecMojo
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class RemoteServer {

    private static final Options systemPropertiesOptions = new Options(JavaSecurityManagers.getSystemProperties());
    public static final String SERVER_DEBUG_PORT = "server.debug.port";
    public static final String SERVER_SHUTDOWN_PORT = "server.shutdown.port";
    public static final String SERVER_SHUTDOWN_HOST = "server.shutdown.host";
    public static final String SERVER_SHUTDOWN_COMMAND = "server.shutdown.command";
    public static final String SOCKET_TIMEOUT = "server.socket.timeout";
    public static final String OPENEJB_SERVER_DEBUG = "openejb.server.debug";
    public static final String START = "start";
    public static final String STOP = "stop";

    // instance variables in constructors
    private final Options options;
    private final boolean profile;
    private final String javaOpts;
    private final boolean tomcat;
    private final int ejbPort;
    private final String ejbPproviderUrl;

    // mutable configuration
    private boolean debug;
    private String additionalClasspath;

    /**
     * Has the remote server's instance been already running ?
     */
    private boolean serverHasAlreadyBeenStarted = true;

    private final AtomicReference<Process> server = new AtomicReference<>();
    private final int tries;
    private final boolean verbose;
    private final int portShutdown;
    private final String host;
    private final String command;
    private File home;
    private int portStartup;
    private final int connectTimeout;

    public RemoteServer() {
        this(systemPropertiesOptions.get("connect.tries", 60), systemPropertiesOptions.get("verbose", false));
    }

    public RemoteServer(final int tries, final boolean verbose) {
        this (new Properties(), tries, verbose);
    }

    public RemoteServer(final Properties overrides, final int tries, final boolean verbose) {
        this.tries = (tries < 1 ? 1 : (tries > 3600 ? 3600 : tries)); //Wait at least 1 second to start or stop, but not more than an hour.
        this.verbose = verbose;

        // makes it possible to override default and static system properties
        options = new Options(overrides, RemoteServer.systemPropertiesOptions);

        home = getHome();
        tomcat = (home != null) && (new File(new File(home, "bin"), "catalina.sh").exists());

        portShutdown = options.get(SERVER_SHUTDOWN_PORT, tomcat ? 8005 : 4200);
        portStartup = portShutdown;
        command = options.get(SERVER_SHUTDOWN_COMMAND, "SHUTDOWN");
        host = options.get(SERVER_SHUTDOWN_HOST, "localhost");
        connectTimeout = options.get(SOCKET_TIMEOUT, 1000);
        debug = options.get(OPENEJB_SERVER_DEBUG, false);
        profile = options.get("openejb.server.profile", false);
        javaOpts = options.get("java.opts", (String) null);
        ejbPort = options.get("ejbd.port", 4201);
        ejbPproviderUrl = options.get("java.naming.provider.url", "127.0.0.1:" + ejbPort);
    }

    public void init(final Properties props) {
        props.put("java.naming.factory.initial", "org.apache.openejb.client.RemoteInitialContextFactory");
        props.put("java.naming.provider.url", ejbPproviderUrl);
        props.put("java.naming.security.principal", "testuser");
        props.put("java.naming.security.credentials", "testpassword");
    }

    public static void main(final String[] args) {
        assert args.length > 0 : "no arguments supplied: valid arguments are 'start' or 'stop'";
        if (args[0].equalsIgnoreCase(START)) {
            final RemoteServer remoteServer = new RemoteServer();
            try {
                remoteServer.start();
            } catch (final Exception e) {
                remoteServer.destroy();
                throw e;
            }
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

    public void destroy() {

        try {
            final boolean stopSent = stop();

            final Process p = server.get();
            if (p != null) {

                if (stopSent) {
                    waitFor(p);
                } else {
                    p.destroy();
                }
            }
        } catch (final Exception e) {
            Logger.getLogger(RemoteServer.class.getName()).log(Level.WARNING, "Failed to destroy server", e);
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
        final int port = START.equals(cmd) && portStartup > 0 ? portStartup : portShutdown;

        if (checkPortAvailable) {
            ok = !connect(port, 1);
        }

        if (ok) {
            try {
                if (verbose) {
                    System.out.println("[] " + cmd.toUpperCase(Locale.ENGLISH) + " SERVER");
                }

                final File home = getHome();
                final String javaVersion = options.get("java.version", (String) null);
                final String javaHome = options.get("java.home", (String) null);
                if (verbose) {
                    System.out.println("OPENEJB_HOME = " + home.getAbsolutePath());
                    final String systemInfo = "Java " + javaVersion + "; "
                            + options.get("os.name", (String) null) + "/"
                            + options.get("os.version", (String) null);
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

                final String java;
                final boolean isWindows = options.get("os.name", "unknown").toLowerCase(Locale.ENGLISH).startsWith("windows");
                if (isWindows && START.equals(cmd) && options.get("server.windows.fork", false)) {
                    // run and forget
                    java = new File(javaHome, "bin/javaw").getAbsolutePath();
                } else {
                    java = new File(javaHome, "bin/java").getAbsolutePath();
                }

                final List<String> argsList = new ArrayList<>(20);
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
                    argsList.addAll(parse(javaOpts.replace("${openejb.base}", home.getAbsolutePath())));
                }

                final Map<String, String> addedArgs = new HashMap<>();
                if (additionalArgs != null) {
                    for (final String arg : additionalArgs) {
                        final int equal = arg.indexOf('=');
                        if (equal < 0) {
                            addedArgs.put(arg, "null");
                        } else {
                            addedArgs.put(arg.substring(0, equal), arg.substring(equal + 1).replace("${openejb.base}", home.getAbsolutePath()));
                        }
                        argsList.add(arg.replace("${openejb.base}", home.getAbsolutePath()));
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
                        cp.append(ps).append(additionalClasspath.replace("${openejb.base}", home.getAbsolutePath()));
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

                    //if (!addedArgs.containsKey("-Dcom.sun.management.jmxremote")) {
                    //    argsList.add("-Dcom.sun.management.jmxremote");
                    //}
                    if (!addedArgs.containsKey("-Djava.util.logging.manager")) {
                        argsList.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager");
                    }
                    if (!addedArgs.containsKey("-Djava.io.tmpdir")) {
                        argsList.add("-Djava.io.tmpdir=" + temp.getAbsolutePath());
                    }
                    if ((javaVersion.startsWith("1.7") || javaVersion.startsWith("1.8")) && // java 9 dropped endorsed folder
                            !addedArgs.containsKey("-Djava.endorsed.dirs") && endorsed.exists()) {
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

                    if(!addedArgs.containsKey("-da")) {
                        argsList.add("-ea");
                    }

                    argsList.add("-classpath");

                    final StringBuilder cp = new StringBuilder(bootstrapJar.getAbsolutePath()).append(ps).append(juliJar.getAbsolutePath());
                    if (commonsLoggingJar.exists()) {
                        cp.append(ps).append(commonsLoggingJar.getAbsolutePath());
                    }
                    if (additionalClasspath != null) {
                        cp.append(ps).append(additionalClasspath.replace("${openejb.base}", home.getAbsolutePath()));
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
                final ProcessBuilder pb = new ProcessBuilder(args)
                        .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .directory(home.getAbsoluteFile());

                Process p = pb.start();

                if (START.equals(cmd)) {
                    server.set(p);
                } else if (STOP.equals(cmd)) {
                    waitFor(p);
                    p = server.get();
                    if (p != null) {
                        waitFor(p);
                    }
                }
            } catch (final Exception e) {
                throw (RuntimeException) new OpenEJBRuntimeException("Cannot start the server.  Exception: " + e.getClass().getName() + ": " + e.getMessage()).initCause(e);
            }

            if (port > 0) {
                if (debug) {
                    if (!connect(port, Integer.MAX_VALUE)) {
                        throw new OpenEJBRuntimeException("Could not connect to server: " + this.host + ":" + port);
                    }
                } else {
                    if (!connect(port, tries)) {
                        throw new OpenEJBRuntimeException("Could not connect to server: " + this.host + ":" + port);
                    }
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
                    synchronized (kill) {
                        kill.remove(p);
                    }
                } catch (final InterruptedException e) {
                    Thread.interrupted();
                } finally {
                    latch.countDown();
                }
            }
        }, "process-waitFor");

        t.start();

        try {
            if (!latch.await(Integer.getInteger("openejb.server.waitFor.seconds", 10), TimeUnit.SECONDS)) {
                killOnExit(p);
                throw new RuntimeException("Timeout waiting for process");
            }
        } catch (final InterruptedException e) {
            Thread.interrupted();
            killOnExit(p);
        }
    }

    public void kill3UNIX() { // debug purpose only
        if (options.get("os.name", "unknown").toLowerCase(Locale.ENGLISH).startsWith("windows")) {
            return;
        }

        try {
            final Field f = server.get().getClass().getDeclaredField("pid");
            f.setAccessible(true);
            final int pid = (Integer) f.get(server.get());
            new ProcessBuilder("kill", "-3", Integer.toString(pid))
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
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
        final String systemProperty = options.get(key, (String) null);
        if (systemProperty != null) {
            argsList.add("-D" + key + "=" + systemProperty);
        }
    }

    private File getHome() {
        if (home != null) {
            return home;
        }

        final String openejbHome = options.get("openejb.home", (String) null);

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
        OutputStream stream = null;
        try (Socket socket = new Socket(host, portShutdown)) {
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
            // Ignore
        }

        return true;
    }

    private boolean connect(final int port, int tries) {
        if (verbose) {
            System.out.println("[] CONNECT ATTEMPT " + (this.tries - tries) + " on port: " + port);
        }

        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(this.host, port), connectTimeout);
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
                } catch (final InterruptedException e2) {
                    Thread.interrupted();
                }
                return connect(port, --tries);
            }
        }
        // no-op

        return true;
    }

    private boolean disconnect(final int port, int tries) {
        if (verbose) {
            System.out.println("[] DISCONNECT ATTEMPT " + (this.tries - tries) + " on port: " + port);
        }

        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(this.host, port), connectTimeout);
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
                } catch (final InterruptedException e2) {
                    Thread.interrupted();
                }

                return disconnect(port, --tries);
            }

        } catch (final IOException e) {
            //This is what we want
        }
        // no-op

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
        synchronized (kill) {
            kill.add(p);
        }
    }

    // Shutdown hook for processes
    private static final List<Process> kill = new ArrayList<Process>();

    static {
        Runtime.getRuntime().addShutdownHook(new CleanUpThread());
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
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

    private static Collection<String> parse(final String raw) {
        final Collection<String> result = new LinkedList<>();

        Character end = null;
        boolean escaped = false;
        final StringBuilder current = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            final char c = raw.charAt(i);
            if (escaped) {
                escaped = false;
                current.append(c);
            } else if ((end != null && end == c) || (c == ' ' && end == null)) {
                if (current.length() > 0) {
                    result.add(current.toString());
                    current.setLength(0);
                }
                end = null;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"' || c == '\'') {
                end = c;
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }
}
