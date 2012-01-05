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
import org.apache.openejb.util.Exceptions;
import org.apache.openejb.util.NetworkUtil;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.ejb.spi.EJBContainerProvider;
import javax.naming.Context;
import javax.validation.ValidationException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EmbeddedTomEEContainer extends EJBContainer {
    public static final String TOMEE_EJBCONTAINER_HTTP_PORT = "tomee.ejbcontainer.http.port";
    private static EmbeddedTomEEContainer tomEEContainer;
    private static final List<String> CONTAINER_NAMES = Arrays.asList(EmbeddedTomEEContainer.class.getName(), "tomee-embedded", "embedded-tomee");

    private Container container = new Container();
    private String appId;

    private EmbeddedTomEEContainer(String id) {
        appId = id;
    }

    @Override public void close() {
        try {
            if (tomEEContainer.container.getAppContexts(appId) != null) {
                tomEEContainer.container.undeploy(appId);
            }
            tomEEContainer.container.stop();
        } catch (Exception e) {
            throw Exceptions.newEJBException(e);
        }
        tomEEContainer = null;
    }

    @Override public Context getContext() {
        return tomEEContainer.container.getJndiContext();
    }

    public static class EmbeddedTomEEContainerProvider implements EJBContainerProvider {
        @Override public EJBContainer createEJBContainer(Map<?, ?> properties) {
            Object provider = properties.get(EJBContainer.PROVIDER);
            int ejbContainerProviders = 1;
            try {
                ejbContainerProviders = ProviderLocator.getServices(EJBContainerProvider.class.getName(), EJBContainer.class, Thread.currentThread().getContextClassLoader()).size();
            } catch (Exception e) {
                 // no-op
            }

            if ((provider == null && ejbContainerProviders > 1)
                    || (!provider.equals(EmbeddedTomEEContainer.class)
                            && !CONTAINER_NAMES.contains(provider))) {
                return null;
            }

            if (tomEEContainer != null) {
                return tomEEContainer;
            }

            final String appId = (String) properties.get(EJBContainer.APP_NAME);
            final Object modules = properties.get(EJBContainer.MODULES);

            tomEEContainer = new EmbeddedTomEEContainer(appId);
            Configuration configuration = new Configuration();
            if (properties.containsKey(TOMEE_EJBCONTAINER_HTTP_PORT)) {
                int port;
                Object portValue = properties.get(TOMEE_EJBCONTAINER_HTTP_PORT);
                if (portValue instanceof Integer) {
                    port = (Integer) portValue;
                } else if (portValue instanceof String) {
                    port = Integer.parseInt((String) portValue);
                } else {
                    throw new RuntimeException("port value should be an integer or a string");
                }
                if (port <= 0) {
                    port = NetworkUtil.getNextAvailablePort();
                    System.setProperty(TOMEE_EJBCONTAINER_HTTP_PORT, Integer.toString(port));
                }
                configuration.setHttpPort(port);
            }
            tomEEContainer.container.setup(configuration);
            try {
                tomEEContainer.container.start();

                if (modules instanceof File) {
                    tomEEContainer.container.deploy(appId, ((File) modules), true);
                } else if (modules instanceof String) {
                    tomEEContainer.container.deploy(appId, new File((String) modules), true);
                } else if (modules instanceof String[]) {
                    for (String path : (String[]) modules) {
                        tomEEContainer.container.deploy(appId, new File(path), true);
                    }
                } else if (modules instanceof File[]) {
                    for (File file : (File[]) modules) {
                        tomEEContainer.container.deploy(appId, file, true);
                    }
                } else {
                    try {
                        tomEEContainer.close();
                    } catch (Exception e) {
                        // no-op
                    }
                    tomEEContainer = null;
                    throw Exceptions.newNoModulesFoundException();
                }

                return tomEEContainer;
            } catch (OpenEJBException e) {
                throw new EJBException(e);
            } catch (MalformedURLException e) {
                throw new EJBException(e);
            } catch (ValidationException ve) {
                throw ve;
            } catch (Exception e) {
                if (e instanceof EJBException) {
                    throw (EJBException) e;
                }
                throw new RuntimeException("initialization exception", e);
            } finally {
                if (tomEEContainer == null) {
                    try {
                        tomEEContainer.close();
                    } catch (Exception e) {
                        // no-op
                    }
                }
            }
        }
    }
}
