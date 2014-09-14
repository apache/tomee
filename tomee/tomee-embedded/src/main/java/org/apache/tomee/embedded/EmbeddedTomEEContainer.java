/**
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

package org.apache.tomee.embedded;

import org.apache.geronimo.osgi.locator.ProviderLocator;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Exceptions;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.NetworkUtil;
import org.apache.tomee.catalina.TomEERuntimeException;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.validation.ValidationException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class EmbeddedTomEEContainer extends EJBContainer {
    public static final String TOMEE_EJBCONTAINER_HTTP_PORT = "tomee.ejbcontainer.http.port";
    private static EmbeddedTomEEContainer tomEEContainer;
    private static final List<String> CONTAINER_NAMES = Arrays.asList(EmbeddedTomEEContainer.class.getName(), "tomee-embedded", "embedded-tomee");

    private Container container = new Container();
    private Collection<String> deployedIds = new ArrayList<String>();

    private EmbeddedTomEEContainer() {
        // no-op
    }

    public Container getDelegate() {
        return container;
    }

    @Override
    public void close() {
        final Collection<Exception> errors = new ArrayList<Exception>();
        for (final String id : deployedIds) {
            if (tomEEContainer.container.getAppContexts(id) != null) {
                try {
                    tomEEContainer.container.undeploy(id);
                } catch (final Exception ex) {
                    Logger.getInstance(LogCategory.OPENEJB, EmbeddedTomEEContainer.class).error(ex.getMessage(), ex);
                    errors.add(ex);
                }
            }
        }
        deployedIds.clear();

        try {
            tomEEContainer.container.close();
        } catch (final Exception ex) {
            errors.add(ex);
            Logger.getInstance(LogCategory.OPENEJB, EmbeddedTomEEContainer.class).error(ex.getMessage(), ex);
        }
        tomEEContainer = null;

        if (!errors.isEmpty()) {
            throw Exceptions.newEJBException(new TomEERuntimeException(errors.toString()));
        }
    }

    @Override
    public Context getContext() {
        return tomEEContainer.container.getJndiContext();
    }

    public static class EmbeddedTomEEContainerProvider implements EJBContainerProvider {
        @Override
        public EJBContainer createEJBContainer(final Map<?, ?> properties) {
            final Object provider = properties.get(EJBContainer.PROVIDER);
            int ejbContainerProviders = 1;
            try {
                ejbContainerProviders = ProviderLocator.getServices(EJBContainerProvider.class.getName(), EJBContainer.class, Thread.currentThread().getContextClassLoader()).size();
            } catch (final Exception e) {
                // no-op
            }

            if ((provider == null && ejbContainerProviders > 1)
                    || (!EmbeddedTomEEContainer.class.equals(provider)
                        && !CONTAINER_NAMES.contains(String.valueOf(provider)))) {
                return null;
            }

            if (tomEEContainer != null) {
                return tomEEContainer;
            }

            final String appId = (String) properties.get(EJBContainer.APP_NAME);
            final Object modules = properties.get(EJBContainer.MODULES);

            tomEEContainer = new EmbeddedTomEEContainer();
            final Configuration configuration = new Configuration();
            if (properties.containsKey(TOMEE_EJBCONTAINER_HTTP_PORT)) {
                int port;
                final Object portValue = properties.get(TOMEE_EJBCONTAINER_HTTP_PORT);
                if (portValue instanceof Integer) {
                    port = (Integer) portValue;
                } else if (portValue instanceof String) {
                    port = Integer.parseInt((String) portValue);
                } else {
                    throw new TomEERuntimeException("port value should be an integer or a string");
                }
                if (port <= 0) {
                    port = NetworkUtil.getNextAvailablePort();
                }
                configuration.setHttpPort(port);
            }
            System.setProperty(TOMEE_EJBCONTAINER_HTTP_PORT, Integer.toString(configuration.getHttpPort()));
            tomEEContainer.container.setup(configuration);
            try {
                tomEEContainer.container.start();

                if (modules instanceof File) {
                    tomEEContainer.deployedIds.add(tomEEContainer.container.deploy(appId, ((File) modules), appId != null).getId());
                } else if (modules instanceof String) {
                    tomEEContainer.deployedIds.add(tomEEContainer.container.deploy(appId, new File((String) modules), appId != null).getId());
                } else if (modules instanceof String[]) {
                    for (final String path : (String[]) modules) {
                        tomEEContainer.deployedIds.add(tomEEContainer.container.deploy(appId, new File(path), appId != null).getId());
                    }
                } else if (modules instanceof File[]) {
                    for (final File file : (File[]) modules) {
                        tomEEContainer.deployedIds.add(tomEEContainer.container.deploy(appId, file, appId != null).getId());
                    }
                } else {
                    SystemInstance.get().getProperties().putAll(properties);
                    final Collection<File> files = tomEEContainer.container.getConfigurationFactory().getModulesFromClassPath(null, Thread.currentThread().getContextClassLoader());
                    if (files.size() == 0) {
                        try {
                            tomEEContainer.close();
                        } catch (final Exception e) {
                            // no-op
                        }
                        tomEEContainer = null;
                        throw Exceptions.newNoModulesFoundException();
                    }
                    for (final File file : files) {
                        tomEEContainer.deployedIds.add(tomEEContainer.container.deploy(appId, file, appId != null).getId());
                    }
                }

                return tomEEContainer;
            } catch (final OpenEJBException | MalformedURLException e) {
                tomEEContainer.close();
                throw new EJBException(e);
            } catch (final ValidationException ve) {
                if (tomEEContainer != null) {
                    tomEEContainer.close();
                }
                throw ve;
            } catch (final Exception e) {
                if (tomEEContainer != null) {
                    tomEEContainer.close();
                }
                if (e instanceof EJBException) {
                    throw (EJBException) e;
                }
                throw new TomEERuntimeException("initialization exception", e);
            }
        }
    }
}
