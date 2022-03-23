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

import jakarta.ejb.EJBException;
import jakarta.ejb.embeddable.EJBContainer;
import jakarta.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import jakarta.validation.ValidationException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class EmbeddedTomEEContainer extends EJBContainer {
    public static final String TOMEE_EJBCONTAINER_HTTP_PORT = "tomee.ejbcontainer.http.port";
    private static final AtomicReference<EmbeddedTomEEContainer> tomEEContainer = new AtomicReference<EmbeddedTomEEContainer>();
    private static final List<String> CONTAINER_NAMES = Arrays.asList(EmbeddedTomEEContainer.class.getName(), "tomee-embedded", "embedded-tomee");

    private final Container container = new Container();
    private final Collection<String> deployedIds = new ArrayList<>();

    private EmbeddedTomEEContainer() {
        // no-op
    }

    public Container getDelegate() {
        return container;
    }

    @Override
    public void close() {
        final Collection<Exception> errors = new ArrayList<>();
        final EmbeddedTomEEContainer etc = tomEEContainer.get();
        if (null != etc) {
            for (final String id : deployedIds) {
                if (etc.container.getAppContexts(id) != null) {
                    try {
                        etc.container.undeploy(id);
                    } catch (final Exception ex) {
                        Logger.getInstance(LogCategory.OPENEJB, EmbeddedTomEEContainer.class).error(ex.getMessage(), ex);
                        errors.add(ex);
                    }
                }
            }


            try {
                etc.container.close();
            } catch (final Exception ex) {
                errors.add(ex);
                Logger.getInstance(LogCategory.OPENEJB, EmbeddedTomEEContainer.class).error(ex.getMessage(), ex);
            }
        }
        deployedIds.clear();
        tomEEContainer.set(null);

        if (!errors.isEmpty()) {
            throw Exceptions.newEJBException(new TomEERuntimeException(errors.toString()));
        }
    }

    @Override
    public Context getContext() {
        return tomEEContainer.get().container.getJndiContext();
    }

    public static class EmbeddedTomEEContainerProvider implements EJBContainerProvider {
        @Override
        public EJBContainer createEJBContainer(final Map<?, ?> props) {
            final Map<?, ?> properties = props == null ? new HashMap<>() : props;
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

            EmbeddedTomEEContainer etc = tomEEContainer.get();
            if (etc != null) {
                return etc;
            }

            final String appId = (String) properties.get(EJBContainer.APP_NAME);
            final Object modules = properties.get(EJBContainer.MODULES);
            etc = new EmbeddedTomEEContainer();
            tomEEContainer.set(etc);
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

            for (final Map.Entry<?, ?> entry : properties.entrySet()) {
                final Object key = entry.getKey();
                final Object value = entry.getValue();
                if (String.class.isInstance(key) && String.class.isInstance(value)) {
                    configuration.property(String.valueOf(key), String.valueOf(value));
                }
            }

            etc.container.setup(configuration);
            try {
                etc.container.start();

                // later to ensure random port are not overwritten
                System.setProperty(TOMEE_EJBCONTAINER_HTTP_PORT, Integer.toString(configuration.getHttpPort()));

                if (modules instanceof File) {
                    etc.deployedIds.add(etc.container.deploy(appId, ((File) modules), appId != null).getId());
                } else if (modules instanceof String) {
                    etc.deployedIds.add(etc.container.deploy(appId, new File((String) modules), appId != null).getId());
                } else if (modules instanceof String[]) {
                    for (final String path : (String[]) modules) {
                        etc.deployedIds.add(etc.container.deploy(appId, new File(path), appId != null).getId());
                    }
                } else if (modules instanceof File[]) {
                    for (final File file : (File[]) modules) {
                        etc.deployedIds.add(etc.container.deploy(appId, file, appId != null).getId());
                    }
                } else {
                    SystemInstance.get().getProperties().putAll(properties);
                    final Collection<File> files = etc.container.getConfigurationFactory().getModulesFromClassPath(null, Thread.currentThread().getContextClassLoader());
                    if (files.isEmpty()) {
                        try {
                            etc.close();
                        } catch (final Exception e) {
                            // no-op
                        }
                        tomEEContainer.set(null);
                        throw Exceptions.newNoModulesFoundException();
                    }
                    for (final File file : files) {
                        etc.deployedIds.add(etc.container.deploy(appId, file, appId != null).getId());
                    }
                }

                return etc;
            } catch (final OpenEJBException | MalformedURLException e) {
                try {
                    etc.close();
                } catch (final Exception e1) {
                    //Ignore
                }
                throw new EJBException(e);
            } catch (final ValidationException ve) {
                try {
                    etc.close();
                } catch (final Exception e1) {
                    //Ignore
                }
                throw ve;
            } catch (final Exception e) {
                try {
                    etc.close();
                } catch (final Exception e1) {
                    //Ignore
                }
                if (e instanceof EJBException) {
                    throw (EJBException) e;
                }
                throw new TomEERuntimeException("initialization exception", e);
            }
        }
    }
}
