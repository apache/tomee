/**
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * </p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */
package org.apache.tomee;

import org.apache.geronimo.osgi.locator.ProviderLocator;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.assembler.DeployerEjb;
import org.apache.openejb.client.EjbObjectInputStream;
import org.apache.openejb.client.RemoteInitialContextFactory;
import org.apache.openejb.config.RemoteServer;
import org.apache.openejb.loader.IO;
import org.apache.tomee.util.QuickServerXmlParser;

import jakarta.ejb.EJBException;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.validation.ValidationException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class RemoteTomEEEJBContainer extends EJBContainer {
    private static RemoteTomEEEJBContainer instance;
    private RemoteServer container;
    private InitialContext context;

    @Override
    public void close() {
        instance.container.destroy();
        instance.container = null;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public static class Provider implements EJBContainerProvider {
        private static final List<String> CONTAINER_NAMES = Arrays.asList(RemoteTomEEEJBContainer.class.getName(), "tomee-remote", "remote-tomee");

        @Override
        public EJBContainer createEJBContainer(final Map<?, ?> rawProperties) {
            final Map<?, ?> properties = rawProperties == null ? new HashMap<>() : rawProperties;
            final Object provider = properties.get(EJBContainer.PROVIDER);
            int ejbContainerProviders = 1;
            try {
                ejbContainerProviders = ProviderLocator.getServices(EJBContainerProvider.class.getName(), EJBContainer.class, Thread.currentThread().getContextClassLoader()).size();
            } catch (final Exception e) {
                // no-op
            }

            if ((provider == null && ejbContainerProviders > 1)
                    || (!RemoteTomEEEJBContainer.class.equals(provider)
                    && !CONTAINER_NAMES.contains(String.valueOf(provider)))) {
                return null;
            }

            if (instance != null) {
                return instance;
            }

            final Object modules = properties.get(EJBContainer.MODULES);

            System.getProperties().putAll(properties);
            final File home = new File(System.getProperty("openejb.home", "doesn't exist"));
            if (!home.exists()) {
                throw new IllegalArgumentException("You need to set openejb.home");
            }

            final QuickServerXmlParser parser = QuickServerXmlParser.parse(new File(home, "conf/server.xml"));
            final String remoteEjb = System.getProperty(Context.PROVIDER_URL, "http://" + parser.host() + ":" + parser.http() + "/tomee/ejb");
            System.setProperty(RemoteServer.SERVER_SHUTDOWN_PORT, parser.stop());

            final String blacklist = System.getProperty("tomee.serialization.class.blacklist");
            if (blacklist == null) {
                System.setProperty("tomee.serialization.class.blacklist", "-");
                EjbObjectInputStream.reloadResolverConfig();
            }
            try {
                instance = new RemoteTomEEEJBContainer();
                instance.container = new RemoteServer();
                instance.container.setDebug("true".equalsIgnoreCase(String.valueOf(properties.get("debug"))));
                instance.container.setPortStartup(Integer.parseInt(parser.http()));

                try {
                    instance.container.start(Arrays.asList(
                        "-Dtomee.serialization.class.blacklist=" + System.getProperty("tomee.serialization.class.blacklist"),
                        "-Dopenejb.system.apps=true", "-Dtomee.remote.support=true"),
                        "start", true);
                } catch (final Exception e) {
                    instance.container.destroy();
                    throw e;
                }

                instance.context = new InitialContext(new Properties() {{
                    setProperty(Context.INITIAL_CONTEXT_FACTORY, RemoteInitialContextFactory.class.getName());
                    setProperty(Context.PROVIDER_URL, String.valueOf(properties.containsKey(Context.PROVIDER_URL) ? properties.get(Context.PROVIDER_URL) : remoteEjb));
                }});

                Deployer deployer = null;
                for (int i = 0; i < (properties.containsKey("retries") ? Integer.parseInt(String.class.cast(properties.get("retries"))) : 4); i++) {
                    try {
                        deployer = Deployer.class.cast(instance.context.lookup("openejb/DeployerBusinessRemote"));
                        if (deployer != null) {
                            break;
                        }
                    } catch (final NamingException ne) {
                        try {
                            sleep(250);
                        } catch (final InterruptedException ie) {
                            Thread.interrupted();
                            break;
                        }
                    }
                }
                if (deployer == null) {
                    throw new TomEERemoteEJBContainerException("Can't lookup deployer, eother increse retries or setup it correctly", new IllegalStateException());
                }

                if (modules instanceof File) {
                    final File file = File.class.cast(modules);
                    deployFile(deployer, file);
                } else if (modules instanceof String) {
                    final String path = String.class.cast(modules);
                    final File file = new File(path);
                    deployFile(deployer, file);
                } else if (modules instanceof String[]) {
                    for (final String path : (String[]) modules) {
                        deployFile(deployer, new File(path));
                    }
                } else if (modules instanceof File[]) {
                    for (final File file : (File[]) modules) {
                        deployFile(deployer, file);
                    }
                } // else suppose already deployed

                return instance;
            } catch (final OpenEJBException | MalformedURLException e) {
                throw new EJBException(e);
            } catch (final ValidationException ve) {
                throw ve;
            } catch (final Exception e) {
                if (e instanceof EJBException) {
                    throw (EJBException) e;
                }
                throw new TomEERemoteEJBContainerException("initialization exception", e);
            } finally {
                if (blacklist == null) {
                    System.clearProperty("tomee.serialization.class.blacklist");
                    EjbObjectInputStream.reloadResolverConfig();
                }
            }
        }
    }

    private static void deployFile(final Deployer deployer, final File file) throws IOException, OpenEJBException {
        if ("true".equalsIgnoreCase(System.getProperty(DeployerEjb.OPENEJB_USE_BINARIES, "false"))) {
            final Properties props = new Properties();
            final byte[] slurpBinaries = IO.slurp(file).getBytes();
            props.put(DeployerEjb.OPENEJB_VALUE_BINARIES, slurpBinaries);
            props.put(DeployerEjb.OPENEJB_PATH_BINARIES, file.getName());
            deployer.deploy(file.getAbsolutePath(), props);
        } else {
            deployer.deploy(file.getAbsolutePath());
        }
    }

    protected static class TomEERemoteEJBContainerException extends RuntimeException {
        protected TomEERemoteEJBContainerException(final String s, final Exception e) {
            super(s, e);
        }
    }
}
