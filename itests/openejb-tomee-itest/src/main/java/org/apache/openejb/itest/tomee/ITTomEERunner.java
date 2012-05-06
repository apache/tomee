package org.apache.openejb.itest.tomee;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.arquillian.common.Setup;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.Zips;
import org.apache.openejb.util.NetworkUtil;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class ITTomEERunner extends BlockJUnit4ClassRunner {
    public static final String TOMEE_TEST_IT_WORKING_DIR = "tomee.test.it.working-dir";
    public static final String TOMEE_TEST_IT_RETRIES = "tomee.test.it.retries";
    public static final String HTTP_PROP_PREFIX = "tomee.it.";
    public static final String HTTP_PROP_SUFFIX = ".http";

    protected static final String DEFAULT_SERVER = "_DEFAULT_";

    private static final String WORKING_DIR = System.getProperty(TOMEE_TEST_IT_WORKING_DIR, "target/it-working-dir");
    private static final String OPENEJB_HOME = "openejb.home";
    private static final String SERVER_SHUTDOWN_PORT = "server.shutdown.port";

    private final Map<String, ServerInfo> servers;
    private final RemoteServer[] remoteServers;

    public ITTomEERunner(final Class<?> klass) throws InitializationError {
        super(klass);

        servers = new HashMap<String, ServerInfo>();

        final Servers serversAnnotation = klass.getAnnotation(Servers.class);
        if (serversAnnotation != null) {
            for (Server server : serversAnnotation.value()) {
                addServer(server);
            }
        }

        final Server serverAnnotation = klass.getAnnotation(Server.class);
        if (serverAnnotation != null) {
            addServer(serverAnnotation);
        }

        remoteServers = new RemoteServer[servers.size()];

        for (Method method : klass.getMethods()) {
            if (isProducerMethod(method)) {
                {
                    final Archive webapp = method.getAnnotation(Archive.class);
                    if (webapp != null && org.jboss.shrinkwrap.api.Archive.class.isAssignableFrom(method.getReturnType())) {
                        final String server = webapp.value();
                        final ServerInfo info = servers.get(server);
                        if (info == null) {
                            throw new OpenEJBRuntimeException("can't find server '" + server + "'");
                        }

                        final org.jboss.shrinkwrap.api.Archive<?> archive;
                        try {
                            archive = (org.jboss.shrinkwrap.api.Archive<?>) method.invoke(null);
                        } catch (Exception e) {
                            throw new OpenEJBRuntimeException("can't create archive from '" + method.toGenericString() + "'", e);
                        }
                        info.tweaker = new ShrinkWrapEnhancedTweaker(info.tweaker, archive);
                    }
                }

                {
                    final Library library = method.getAnnotation(Library.class);
                    if (library != null && File.class.isAssignableFrom(method.getReturnType())) {
                        final String server = library.value();
                        final ServerInfo info = servers.get(server);
                        if (info == null) {
                            throw new OpenEJBRuntimeException("can't find server '" + server + "'");
                        }

                        final File lib;
                        try {
                            lib = (File) method.invoke(null);
                        } catch (Exception e) {
                            throw new OpenEJBRuntimeException("can't create archive from '" + method.toGenericString() + "'", e);
                        }
                        info.tweaker = new LibraryEnhancedTweaker(info.tweaker, lib);
                    }
                }
            }
        }
    }

    private boolean isProducerMethod(final Method method) {
        final int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && method.getParameterTypes().length == 0;
    }

    private void addServer(final Server server) throws InitializationError {
        final ServerInfo info = new ServerInfo();

        info.name = server.name();
        if (info.name == null || info.name.isEmpty()) {
            info.name = getTestClass().getJavaClass().getSimpleName();
        }

        info.configurationDir = server.configurationDir();
        if (info.configurationDir == null || info.configurationDir.isEmpty()) {
            info.configurationDir = info.name;
        }

        info.clean = server.cleanWebapp();
        info.httpPort = server.http();
        info.shutdownPort = server.shutdown();
        info.ajpPort = server.ajp();

        final Class<? extends  ServerTweaker> tweaker = server.tweaker();
        try {
            info.tweaker = tweaker.newInstance();
        } catch (InstantiationException e) {
            throw new InitializationError(e);
        } catch (IllegalAccessException e) {
            throw new InitializationError(e);
        }

        final Artifact art = server.artifact();
        info.server = Repository.getArtifact(art.groupId(), art.artifactId(), art.version(), art.type(), art.classifier());

        servers.put(info.name, info);
    }

    @Override
    protected Statement withBeforeClasses(final Statement statement) {
        return new BeforeClassStatement(super.withBeforeClasses(statement));
    }

    @Override
    protected Statement withAfterClasses(final Statement statement) {
        return new AfterClassStatement(super.withAfterClasses(statement));
    }

    private class BeforeClassStatement extends Statement {
        private final Statement delegate;

        public BeforeClassStatement(final Statement statement) {
            delegate = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            final String oldOpenEJBHome = System.getProperty(OPENEJB_HOME);
            final String oldShutdownPort = System.getProperty(SERVER_SHUTDOWN_PORT);

            int i = 0;
            for (ServerInfo info : servers.values()) {
                final File tomee = new File(WORKING_DIR, info.name);
                Files.mkdirs(tomee);
                Zips.unzip(info.server, tomee, true);

                if (info.httpPort <= 0) {
                    info.httpPort = NetworkUtil.getNextAvailablePort();
                }
                if (info.shutdownPort <= 0) {
                    info.shutdownPort = info.httpPort + 1;
                }
                if (info.ajpPort <= 0) {
                    info.ajpPort = info.shutdownPort + 1;
                }

                Setup.updateServerXml(tomee, info.httpPort, info.shutdownPort, info.ajpPort);

                if (info.clean) { // speed up a bit
                    Setup.removeUselessWebapps(tomee);
                }

                final File conf = new File(info.configurationDir);
                if (conf.exists() && conf.isDirectory()) {
                    IO.copyDirectory(conf, new File(tomee, "conf"));
                }

                info.tweaker.tweak(tomee);

                // start the server
                System.setProperty(OPENEJB_HOME, tomee.getCanonicalPath());
                System.setProperty(SERVER_SHUTDOWN_PORT, Integer.toString(info.shutdownPort));
                remoteServers[i] = new RemoteServer(Integer.getInteger(TOMEE_TEST_IT_RETRIES, 100), true);
                remoteServers[i].start();

                System.setProperty(HTTP_PROP_PREFIX + info.name + HTTP_PROP_SUFFIX, Integer.toString(info.httpPort));

                i++;
            }
            try {
                delegate.evaluate();
            } finally {
                resetSystemProperty(OPENEJB_HOME, oldOpenEJBHome);
                resetSystemProperty(SERVER_SHUTDOWN_PORT, oldShutdownPort);
            }
        }

        private void resetSystemProperty(final String key, final String old) {
            if (old != null) {
                System.setProperty(key, old);
            } else {
                System.clearProperty(key);
            }
        }
    }

    private class AfterClassStatement extends Statement {
        private final Statement delegate;

        public AfterClassStatement(final Statement statement) {
            delegate = statement;
        }

        @Override
        public void evaluate() throws Throwable {
            delegate.evaluate();

            if (servers != null) {
                for (RemoteServer server : remoteServers) {
                    if (server != null) {
                        server.stop();
                    }
                }
            }
            Files.delete(new File(WORKING_DIR));
        }
    }

    private static class ServerInfo {
        private String name;
        private String configurationDir;
        private int httpPort;
        private int shutdownPort;
        private int ajpPort;
        private boolean clean;
        private ServerTweaker tweaker;
        private File server;
    }
}
