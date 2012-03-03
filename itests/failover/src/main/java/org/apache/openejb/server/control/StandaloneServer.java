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
public class StandaloneServer {

    private final File home;
    private final File base;
    private final File java;
    private final File openejbJar;
    private boolean debug;
    private boolean profile;
    private volatile Process process;
    private final List<String> jvmOpts = new ArrayList<String>();
    private final Properties properties = new Properties();
    private boolean verbose = false;
    private OutputStream out = System.out;
    private Options options = new Options(properties);

    public StandaloneServer(File home) {
        this(home, home);
    }

    public StandaloneServer(File home, File base) {
        this.home = home;
        this.base = base;

        final File lib = readable(dir(exists(new File(home, "lib"))));

        openejbJar = readable(file(select(lib, "openejb-core.*.jar")));
        final File javaagentJar = readable(file(select(lib, "openejb-javaagent.*.jar")));

        final File javaHome = readable(dir(exists(new File(System.getProperty("java.home")))));

        java = readable(file(Files.path(javaHome, "bin", "java")));

        jvmOpts.add("-XX:+HeapDumpOnOutOfMemoryError");
        jvmOpts.add("-javaagent:" + javaagentJar.getAbsolutePath());
    }

    public ServerService getServerService(String string) {
        return new ServerService(string);
    }

    public class ServerService {

        private final String name;

        public ServerService(String name) {
            this.name = name;
        }

        public int getPort() {
            return options.get(name + ".port", -1);
        }

        public void setPort(int i) {
            properties.put(name + ".port", i + "");
        }

        public boolean isDisabled() {
            return options.get(name + ".disabled", true);
        }

        public boolean isEnabled() {
            return !isDisabled();
        }

        public void setDisabled(boolean b) {
            properties.put(name + ".disabled", b + "");
        }

        public void setEnabled(boolean b) {
            setDisabled(!b);
        }

        public String getBind() {
            return options.get(name + ".bind", "");
        }

        public void setBind(String bind) {
            properties.put(name + ".bind", bind);
        }

        public int getThreads() {
            return options.get(name + ".threads", -1);
        }

        public void setThreads(int threads) {
            properties.put(name + ".threads", threads + "");
        }

        public ServerService set(String name, String value) {
            properties.put(this.name + "." + name, value);
            return this;
        }

        public ServerService threads(int threads) {
            setThreads(threads);
            return this;
        }

        public ServerService port(int port) {
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

        public ServerService bind(String host) {
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

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isProfile() {
        return profile;
    }

    public void setProfile(boolean profile) {
        this.profile = profile;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
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

    public Object setProperty(String key, String value) {
        return getProperties().setProperty(key, value);
    }

    public void start() {
        start(0, TimeUnit.MILLISECONDS);
    }

    public void start(int timeout, TimeUnit minutes) {
        if (process != null) throw new ServerRunningException(home, "Server already running");

        try {
            List<String> args = new ArrayList<String>();
            args.add(java.getAbsolutePath());
            args.addAll(jvmOpts);
            final Set<Map.Entry<Object, Object>> collection = properties.entrySet();
            args.addAll(Join.strings(collection, new SystemPropertiesCallback()));

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
        } catch (InterruptedException e) {
            Thread.interrupted();
            throw new IllegalStateException("Server failed to start in the expected time");
        } catch (IOException e) {
            throw new IllegalStateException("Server did not start correctly", e);
        }
    }

    private void edit() {
        if (process != null) throw new ServerRunningException(home, "Cannot change settings while server is running");
    }

    public static class DevNull extends OutputStream {

        @Override
        public void write(int b) throws IOException {
        }
    }

    public void kill() {
        if (process == null) return;

        process.destroy();

        waitForExit();
    }

    private void waitForExit() {
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        process = null;
    }

    private int command(String... strings) {
        return command(Arrays.asList(strings));
    }

    private int command(List<String> strings) {
        if (process == null) throw new ServerNotRunningException(home);

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
        } catch (IOException e) {
            throw new ServerException(home, Join.join(" ", strings), e);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
        return -1;
    }

    public void deploy(String path) {
        final int code = command("deploy", getServerUrl(), path);
        if (code != 0) {
            throw new DeployException(home, code, path);
        }
    }

    public void undeploy(String path) {
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
        if (kill.contains(this)) return;
        kill.add(this);
    }

    // Shutdown hook for recursive delete on tmp directories
    static final List<StandaloneServer> kill = new ArrayList<StandaloneServer>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (StandaloneServer server : kill) {
                    try {
                        if (server.process != null) {
                            server.process.destroy();
                        }
                    } catch (Throwable e) {
                    }
                }
            }
        });
    }

    public static class ServerException extends RuntimeException {
        private final File home;

        public ServerException(File home) {
            this.home = home;
        }

        public ServerException(File home, String message) {
            super(message);
            this.home = home;
        }

        public ServerException(File home, String message, Throwable cause) {
            super(message, cause);
            this.home = home;
        }

        @Override
        public String getMessage() {
            return super.getMessage() + String.format("server path `%s`", home.getAbsolutePath());
        }
    }

    public static class ServerNotRunningException extends ServerException {
        public ServerNotRunningException(File home) {
            super(home);
        }
    }

    public static class ServerRunningException extends ServerException {
        public ServerRunningException(File home) {
            super(home);
        }

        public ServerRunningException(File home, String message) {
            super(home, message);
        }
    }

    public static class ServerCommandException extends ServerException {
        private final int returnCode;
        private final String[] args;

        public ServerCommandException(File home, int returnCode, String... args) {
            super(home);
            this.returnCode = returnCode;
            this.args = args;
        }
    }

    public static class DeployException extends ServerCommandException {
        public DeployException(File home, int returnCode, String... args) {
            super(home, returnCode, args);
        }
    }

    public static class UndeployException extends ServerCommandException {
        public UndeployException(File home, int returnCode, String... args) {
            super(home, returnCode, args);
        }
    }

    public static class StopException extends ServerCommandException {
        public StopException(File home, int returnCode, String... args) {
            super(home, returnCode, args);
        }
    }

    private static class SystemPropertiesCallback implements Join.NameCallback<Map.Entry<Object, Object>> {
        @Override
        public String getName(Map.Entry<Object, Object> e) {
            return String.format("-D%s=%s", e.getKey().toString(), e.getValue().toString());
        }
    }

    private String getServerUrl() {
        final ServerService ejbd = getServerService("ejbd");

        int port = ejbd.getPort();
        if (port == 0) port = 4201;

        String host = ejbd.getBind();
        if (host == null || host.length() == 0) host = "localhost";

        return String.format("--server-url=ejbd://%s:%s", host, port);
    }
}
