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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.control;

import org.apache.openejb.client.Options;
import org.apache.openejb.loader.Files;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.OutputScanner;
import org.apache.openejb.util.Pipe;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.openejb.loader.Files.dir;
import static org.apache.openejb.loader.Files.exists;
import static org.apache.openejb.loader.Files.file;
import static org.apache.openejb.loader.Files.readable;
import static org.apache.openejb.loader.Files.select;

/**
 * @version $Rev$ $Date$
 */
@SuppressWarnings({"UnusedDeclaration", "UseOfSystemOutOrSystemErr"})
public class StandaloneServer {

    private final File home;
    private final File base;
    private final File java;
    private final File openejbJar;
    private boolean debug;
    private int debugPort = 5005;
    private boolean profile;
    private volatile Process process;
    private final List<String> jvmOpts = new ArrayList<String>();
    private final Properties properties = new Properties();
    private boolean verbose = false;
    private OutputStream out = System.out;
    private Options options = new Options(properties);
    private Context context = new Context();

    public StandaloneServer(final File home) {
        this(home, home);
    }

    public StandaloneServer(final File home, final File base) {
        this.home = home;
        this.base = base;

        final File lib = readable(dir(exists(new File(home, "lib"))));

        openejbJar = readable(file(select(lib, "openejb-core.*.jar")));
        final File javaagentJar = readable(file(select(lib, "openejb-javaagent.*.jar")));

        final File javaHome = readable(dir(exists(new File(System.getProperty("java.home")))));

        final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

        java = readable(file(Files.path(javaHome, "bin", isWindows ? "java.exe" : "java")));

        jvmOpts.add("-XX:+HeapDumpOnOutOfMemoryError");
        jvmOpts.add("-javaagent:" + javaagentJar.getAbsolutePath());
    }

    /**
     * Used as a convenience for tracking objects associated
     * with this server.  Does not affect the running server
     * and none of these objects are in any way sent or part
     * of the server itself.
     *
     * @return Context
     */
    public Context getContext() {
        return context;
    }

    public ServerService getServerService(final String string) {
        return new ServerService(string);
    }

    public class ServerService {

        private final String name;

        public ServerService(final String name) {
            this.name = name;
        }

        public int getPort() {
            return options.get(name + ".port", -1);
        }

        public void setPort(final int i) {
            properties.put(name + ".port", i + "");
        }

        public boolean isDisabled() {
            return options.get(name + ".disabled", true);
        }

        public boolean isEnabled() {
            return !isDisabled();
        }

        public void setDisabled(final boolean b) {
            properties.put(name + ".disabled", b + "");
        }

        public void setEnabled(final boolean b) {
            setDisabled(!b);
        }

        public String getBind() {
            return options.get(name + ".bind", "");
        }

        public void setBind(final String bind) {
            properties.put(name + ".bind", bind);
        }

        public int getThreads() {
            return options.get(name + ".threads", -1);
        }

        public void setThreads(final int threads) {
            properties.put(name + ".threads", threads + "");
        }

        public ServerService set(final String name, final String value) {
            properties.put(this.name + "." + name, value);
            return this;
        }

        public Object get(final String name) {
            return properties.get(this.name + "." + name);
        }

        public ServerService threads(final int threads) {
            setThreads(threads);
            return this;
        }

        public ServerService port(final int port) {
            setPort(port);
            return this;
        }

        public ServerService enable() {
            setEnabled(true);
            return this;
        }

        public ServerService disable() {
            setDisabled(true);
            return this;
        }

        public ServerService bind(final String host) {
            setBind(host);
            return this;
        }

    }

    public File getHome() {
        return home;
    }

    public File getBase() {
        return base;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(final int debugPort) {
        this.debugPort = debugPort;
    }

    public boolean isProfile() {
        return profile;
    }

    public void setProfile(final boolean profile) {
        this.profile = profile;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(final Process process) {
        this.process = process;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(final OutputStream out) {
        this.out = out;
    }

    public void ignoreOut() {
        setOut(new DevNull());
    }

    public List<String> getJvmOpts() {
        return jvmOpts;
    }

    public Properties getProperties() {
        return properties;
    }

    public Object setProperty(final String key, final String value) {
        return getProperties().setProperty(key, value);
    }

    public void start() {
        start(0, TimeUnit.MILLISECONDS);
    }

    public void start(final int timeout, final TimeUnit minutes) {
        if (process != null) {
            throw new ServerRunningException(home, "Server already running");
        }

        try {
            final List<String> args = new ArrayList<String>();
            args.add(java.getAbsolutePath());
            args.addAll(jvmOpts);
            final Set<Map.Entry<Object, Object>> collection = properties.entrySet();
            args.addAll(Join.strings(collection, new SystemPropertiesCallback()));
            if (debug) {
                args.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=" + debugPort);
            }

            args.add("-jar");
            args.add(openejbJar.getAbsolutePath());
            args.add("start");

            final ProcessBuilder builder = new ProcessBuilder(args);
            builder.redirectErrorStream(true);

            if (verbose) {
                System.out.println(Join.join("\n", args));
            }

            process = builder.start();

            if (timeout > 0) {
                final OutputScanner scanner = new OutputScanner(out, "Ready!");
                Pipe.pipe(process.getInputStream(), scanner);
                scanner.await(timeout, minutes);
            } else {
                out = System.out;
                Pipe.pipe(process.getInputStream(), out);
            }
        } catch (final InterruptedException e) {
            Thread.interrupted();
            throw new IllegalStateException("Server failed to start in the expected time");
        } catch (final IOException e) {
            throw new IllegalStateException("Server did not start correctly", e);
        }
    }

    private void edit() {
        if (process != null) {
            throw new ServerRunningException(home, "Cannot change settings while server is running");
        }
    }

    public static class DevNull extends OutputStream {

        @Override
        public void write(final int b) throws IOException {
        }
    }

    public void kill() {
        if (process == null) {
            return;
        }

        process.destroy();

        waitForExit();
    }

    private void waitForExit() {
        try {
            process.waitFor();
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }
        process = null;
    }

    private int command(final String... strings) {
        return command(Arrays.asList(strings));
    }

    private int command(final List<String> strings) {
        if (process == null) {
            throw new ServerNotRunningException(home);
        }

        try {
            final List<String> args = new ArrayList<String>();
            args.add(java.getAbsolutePath());
            args.add("-jar");
            args.add(openejbJar.getAbsolutePath());
            args.addAll(strings);

            final ProcessBuilder builder = new ProcessBuilder(args);
            final Process command = builder.start();
            Pipe.read(command);
            return command.waitFor();
        } catch (final IOException e) {
            throw new ServerException(home, Join.join(" ", strings), e);
        } catch (final InterruptedException e) {
            Thread.interrupted();
        }
        return -1;
    }

    public void deploy(final String path) {
        final int code = command("deploy", getServerUrl(), path);
        if (code != 0) {
            throw new DeployException(home, code, path);
        }
    }

    public void undeploy(final String path) {
        final int code = command("undeploy", getServerUrl(), path);
        if (code != 0) {
            throw new UndeployException(home, code, path);
        }
    }

    public void stop() {
        final int code = command("stop");
        if (code != 0) {
            throw new StopException(home, code);
        }
        waitForExit();
    }

    public void killOnExit() {
        if (kill.contains(this)) {
            return;
        }
        kill.add(this);
    }

    // Shutdown hook for recursive delete on tmp directories
    static final List<StandaloneServer> kill = new ArrayList<StandaloneServer>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (final StandaloneServer server : kill) {
                    try {
                        if (server.process != null) {
                            server.process.destroy();
                        }
                    } catch (final Throwable e) {
                        //Ignore
                    }
                }
            }
        });
    }

    public static class ServerException extends RuntimeException {

        private final File home;

        public ServerException(final File home) {
            this.home = home;
        }

        public ServerException(final File home, final String message) {
            super(message);
            this.home = home;
        }

        public ServerException(final File home, final String message, final Throwable cause) {
            super(message, cause);
            this.home = home;
        }

        @Override
        public String getMessage() {
            return super.getMessage() + String.format("server path `%s`", home.getAbsolutePath());
        }
    }

    public static class ServerNotRunningException extends ServerException {

        public ServerNotRunningException(final File home) {
            super(home);
        }
    }

    public static class ServerRunningException extends ServerException {

        public ServerRunningException(final File home) {
            super(home);
        }

        public ServerRunningException(final File home, final String message) {
            super(home, message);
        }
    }

    public static class ServerCommandException extends ServerException {

        private final int returnCode;
        private final String[] args;

        public ServerCommandException(final File home, final int returnCode, final String... args) {
            super(home);
            this.returnCode = returnCode;
            this.args = args;
        }
    }

    public static class DeployException extends ServerCommandException {

        public DeployException(final File home, final int returnCode, final String... args) {
            super(home, returnCode, args);
        }
    }

    public static class UndeployException extends ServerCommandException {

        public UndeployException(final File home, final int returnCode, final String... args) {
            super(home, returnCode, args);
        }
    }

    public static class StopException extends ServerCommandException {

        public StopException(final File home, final int returnCode, final String... args) {
            super(home, returnCode, args);
        }
    }

    private static class SystemPropertiesCallback implements Join.NameCallback<Map.Entry<Object, Object>> {

        @Override
        public String getName(final Map.Entry<Object, Object> e) {
            return String.format("-D%s=%s", e.getKey().toString(), e.getValue().toString());
        }
    }

    private String getServerUrl() {
        final ServerService ejbd = getServerService("ejbd");

        int port = ejbd.getPort();
        if (port == 0) {
            port = Integer.parseInt(System.getProperty("ejbd.port", "4201"));
        }

        String host = ejbd.getBind();
        if (host == null || host.length() == 0) {
            host = "localhost";
        }

        return String.format("--server-url=ejbd://%s:%s", host, port);
    }
}
