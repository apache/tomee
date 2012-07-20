package org.apache.tomee.catalina.cluster;

import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TomEEClusterListener extends ClusterListener {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomEEClusterListener.class);
    private static final Properties IC_PROPS = new Properties();

    // async processing to avoid to make the cluster hanging
    private static final ExecutorService SERVICE = Executors.newFixedThreadPool(1);

    static {
        IC_PROPS.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
    }

    @Override
    public void messageReceived(final ClusterMessage clusterMessage) {
        final Class<?> type = clusterMessage.getClass();

        if (DeployMessage.class.equals(type)) {
            final DeployMessage msg = (DeployMessage) clusterMessage;
            final String file = msg.getFile();
            final boolean alreadyDeployed = isDeployed(file);
            final File ioFile = new File(file);
            if (ioFile.exists() && !alreadyDeployed) {
                SERVICE.submit(new DeployTask(file));
            } else if (!alreadyDeployed) {
                LOGGER.warning("file is remote, can't deploy it: " + ioFile.getPath());
            } else {
                LOGGER.info("application already deployed: " + file);
            }
        } else if (UndeployMessage.class.equals(type)) {
            final String file = ((UndeployMessage) clusterMessage).getFile();
            if (isDeployed(file)) {
                SERVICE.submit(new UndeployTask(file));
            } else {
                LOGGER.info("app '" + file + "' was not deployed");
            }
        } else {
            LOGGER.warning("message type not supported: " + type);
        }
    }

    private static boolean isDeployed(final String file) {
        return SystemInstance.get().getComponent(Assembler.class).isDeployed(file);
    }

    private static Deployer deployer() throws NamingException {
        return (Deployer) new InitialContext(IC_PROPS).lookup("openejb/DeployerBusinessRemote");
    }

    @Override
    public boolean accept(final ClusterMessage clusterMessage) {
        return clusterMessage != null
            && (DeployMessage.class.equals(clusterMessage.getClass())
                || UndeployMessage.class.equals(clusterMessage.getClass()));
    }

    public static void stop() {
        SERVICE.shutdown();
        try {
            SERVICE.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            SERVICE.shutdownNow();
        }
    }

    private static class DeployTask implements Runnable {
        private final String app;

        public DeployTask(final String ioFile) {
            app = ioFile;
        }

        @Override
        public void run() {
            if (!isDeployed(app)) {
                try {
                    deployer().deploy(app);
                } catch (OpenEJBException e) {
                    LOGGER.warning("can't deploy: " + app, e);
                } catch (NamingException e) {
                    LOGGER.warning("can't find deployer", e);
                }
            }
        }
    }

    private static class UndeployTask implements Runnable {
        private final String app;

        public UndeployTask(final String ioFile) {
            app = ioFile;
        }

        @Override
        public void run() {
            if (isDeployed(app)) {
                try {
                    deployer().undeploy(app);
                } catch (UndeployException e) {
                    LOGGER.error("can't undeploy app", e);
                } catch (NoSuchApplicationException e) {
                    LOGGER.warning("no app toi deploy", e);
                } catch (NamingException e) {
                    LOGGER.warning("can't find deployer", e);
                }
            }
        }
    }
}
