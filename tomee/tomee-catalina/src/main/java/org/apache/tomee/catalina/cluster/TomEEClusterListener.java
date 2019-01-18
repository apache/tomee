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
package org.apache.tomee.catalina.cluster;

import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.DaemonThreadFactory;
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
    @Override
    public void messageReceived(final ClusterMessage clusterMessage) {
        final Class<?> type = clusterMessage.getClass();

        if (DeployMessage.class.equals(type)) {
            final DeployMessage msg = (DeployMessage) clusterMessage;
            String file = msg.getFile();

            final boolean alreadyDeployed = isDeployed(file);
            File ioFile = new File(file);

            if (!alreadyDeployed) {
                if (msg.getArchive() != null) {
                    final File deployed = deployedDir();
                    try {
                        if (!deployed.exists()) {
                            Files.mkdirs(deployed); // can throw runtime exceptions
                        }

                        final File dump = new File(deployed, ioFile.getName());
                        IO.copy(msg.getArchive(), dump);
                        file = dump.getAbsolutePath();
                        ioFile = new File(file);

                        Static.LOGGER.info("dumped archive: " + msg.getFile() + " to " + file);
                    } catch (final Exception e) {
                        Static.LOGGER.error("can't dump archive: "+ file, e);
                    }
                }

                if (ioFile.exists()) {
                    Static.SERVICE.submit(new DeployTask(file));
                } else {
                    Static.LOGGER.warning("can't find '" + file);
                }
            } else {
                Static.LOGGER.info("application already deployed: " + file);
            }
        } else if (UndeployMessage.class.equals(type)) {
            final String file = ((UndeployMessage) clusterMessage).getFile();
            if (isDeployed(file)) {
                Static.SERVICE.submit(new UndeployTask(file));
            } else {
                final File alternativeFile = new File(deployedDir(), new File(file).getName());
                if (isDeployed(alternativeFile.getAbsolutePath())) {
                    Static.SERVICE.submit(new UndeployTask(alternativeFile.getAbsolutePath()));
                }
                Static.LOGGER.info("app '" + file + "' was not deployed");
            }
        } else {
            Static.LOGGER.warning("message type not supported: " + type);
        }
    }

    private File deployedDir() {
        return new File(SystemInstance.get().getHome().getDirectory(), "deployed");
    }

    private static boolean isDeployed(final String file) {
        return SystemInstance.get().getComponent(Assembler.class).isDeployed(file);
    }

    private static Deployer deployer() throws NamingException {
        return (Deployer) new InitialContext(Static.IC_PROPS).lookup("openejb/DeployerBusinessRemote");
    }

    @Override
    public boolean accept(final ClusterMessage clusterMessage) {
        return clusterMessage != null
            && (DeployMessage.class.equals(clusterMessage.getClass())
                || UndeployMessage.class.equals(clusterMessage.getClass()));
    }

    public static void stop() {
        Static.SERVICE.shutdown();
        try {
            Static.SERVICE.awaitTermination(1, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            Static.SERVICE.shutdownNow();
        }
    }

    private static class DeployTask implements Runnable {
        private final String app;
        private static final Properties REMOTE_DEPLOY_PROPERTIES = new Properties();
        static {
            REMOTE_DEPLOY_PROPERTIES.setProperty("openejb.app.autodeploy", "true"); // avoid to send deployment again
        }

        public DeployTask(final String ioFile) {
            app = ioFile;
        }

        @Override
        public void run() {
            if (!isDeployed(app)) {
                try {
                    deployer().deploy(app, REMOTE_DEPLOY_PROPERTIES);
                } catch (final OpenEJBException e) {
                    Static.LOGGER.warning("can't deploy: " + app, e);
                } catch (final NamingException e) {
                    Static.LOGGER.warning("can't find deployer", e);
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
                } catch (final UndeployException e) {
                    Static.LOGGER.error("can't undeploy app", e);
                } catch (final NoSuchApplicationException e) {
                    Static.LOGGER.warning("no app toi deploy", e);
                } catch (final NamingException e) {
                    Static.LOGGER.warning("can't find deployer", e);
                }
            }
        }
    }

    // lazy init of logger (can fail with shutdown hooks to kill the container) and executor
    private static final class Static {
        private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, TomEEClusterListener.class);
        private static final Properties IC_PROPS = new Properties();

        // async processing to avoid to make the cluster hanging
        private static final ExecutorService SERVICE = Executors.newSingleThreadExecutor(new DaemonThreadFactory("TomEE-Cluster-Listener-thread-"));

        static {
            IC_PROPS.setProperty(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
        }

        private Static() {
            // no-op
        }
    }
}
