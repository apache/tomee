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

public class TomEEClusterListener extends ClusterListener {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomEEClusterListener.class);
    private static final Properties IC_PROPS = new Properties();

    static {
        IC_PROPS.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
    }

    @Override
    public void messageReceived(final ClusterMessage clusterMessage) {
        final Class<?> type = clusterMessage.getClass();

        if (DeployMessage.class.equals(type)) {
            final DeployMessage msg = (DeployMessage) clusterMessage;
            final String file = msg.getFile();
            final boolean alreadyDeployed = SystemInstance.get().getComponent(Assembler.class).isDeployed(file);
            final File ioFile = new File(file);
            if (ioFile.exists() && !alreadyDeployed) {
                try {
                    deployer().deploy(file);
                } catch (OpenEJBException e) {
                    LOGGER.warning("can't deploy: " + ioFile.getPath(), e);
                } catch (NamingException e) {
                    LOGGER.warning("can't find deployer", e);
                }
            } else if (!alreadyDeployed) {
                LOGGER.warning("file is remote, can't deploy it: " + ioFile.getPath());
            } else {
                LOGGER.info("application already deployed: " + file);
            }
        } else if (UndeployMessage.class.equals(type)) {
            final String file = ((UndeployMessage) clusterMessage).getFile();
            if (SystemInstance.get().getComponent(Assembler.class).isDeployed(file)) {
                try {
                    deployer().undeploy(file);
                } catch (UndeployException e) {
                    LOGGER.error("can't undeploy app", e);
                } catch (NoSuchApplicationException e) {
                    LOGGER.warning("no app toi deploy", e);
                } catch (NamingException e) {
                    LOGGER.warning("can't find deployer", e);
                }
            }
        } else {
            LOGGER.warning("message type not supported: " + type);
        }
    }

    private Deployer deployer() throws NamingException {
        return (Deployer) new InitialContext(IC_PROPS).lookup("openejb/DeployerBusinessRemote");
    }

    @Override
    public boolean accept(final ClusterMessage clusterMessage) {
        return DeployMessage.class.equals(clusterMessage.getClass()) || UndeployMessage.class.equals(clusterMessage.getClass());
    }
}
