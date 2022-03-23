/**
 *
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
package org.apache.openejb.server.cxf.client;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.core.ivm.naming.JaxWsServiceReference;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.xml.namespace.QName;
import jakarta.xml.ws.WebServiceFeature;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.apache.openejb.server.cxf.transport.util.CxfUtil.configureInterceptors;

/**
 * Used to configure a @WebServiceRef.
 * Config uses application.properties.
 * Format is:
 *
 * # global for all clients
 * cxf.jaxws.client.out-interceptors = com.foo.MyInInterceptor
 * # specific
 * cxf.jaxws.client.{ns}MyPort.out-interceptors = com.foo.MyInInterceptor
 *
 * Services can be defines in tomee.xml or application.properties.
 *
 * Note: resources.xml are ignored for now (to be enhanced)
 */
public class WebServiceInjectionConfigurator implements JaxWsServiceReference.WebServiceClientCustomizer {
    private static final String CXF_JAXWS_CLIENT_PREFIX = "cxf.jaxws.client.";

    @Override
    public WebServiceFeature[] features(final QName qname, final Properties properties) {
        Collection<WebServiceFeature> list = null;
        for (final String suffix : asList("", (qname == null ? "_" : qname.toString()) + ".")) {
            final String wsFeatures = properties.getProperty(CXF_JAXWS_CLIENT_PREFIX + suffix + "wsFeatures");
            if (wsFeatures != null) {
                final Collection<Object> instances = ServiceInfos.resolve(createServiceInfos(properties), wsFeatures.split(" *, *"));
                if (instances != null && !instances.isEmpty()) {
                    for (final Object i : instances) {
                        if (!WebServiceFeature.class.isInstance(i)) {
                            throw new IllegalArgumentException("Not a WebServiceFeature: " + i);
                        }
                        if (list == null) { // lazy to avoid useless allocation in most of cases
                            list = new LinkedList<>();
                        }
                        list.add(WebServiceFeature.class.cast(i));
                    }
                }
            }
        }
        return list != null ? list.toArray(new WebServiceFeature[list.size()]) : null;
    }

    @Override
    public void customize(final Object o, final Properties properties) {
        final Client client;
        try {
            client = ClientProxy.getClient(o);
        } catch (final Exception e) {
            return;
        }

        configure(client, properties);
    }

    private void configure(final Client client, final Properties properties) {
        if (properties == null) {
            return;
        }

        for (final String suffix : asList("", client.getEndpoint().getEndpointInfo().getName().toString() + ".")) {
            // here (ie at runtime) we have no idea which services were linked to the app
            // so using tomee.xml ones for now (not that shocking since we externalize the config with this class)
            configureInterceptors(client, CXF_JAXWS_CLIENT_PREFIX + suffix, lazyServiceInfoList(properties), properties);
        }
    }

    private List<ServiceInfo> lazyServiceInfoList(final Properties properties) { // don't create service info if not needed, ie no conf
        return List.class.cast(
                Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class<?>[]{List.class},
                        new InvocationHandler() {
                            private List<ServiceInfo> list = null;

                            @Override
                            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                                if (list == null) {
                                    list = createServiceInfos(properties);
                                }
                                try {
                                    return method.invoke(list, args);
                                } catch (final InvocationTargetException ite) {
                                    throw ite.getCause();
                                }
                            }
                        }
                )
        );
    }

    private List<ServiceInfo> createServiceInfos(final Properties properties) {
        final OpenEjbConfiguration config = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        final List<ServiceInfo> services = new ArrayList<>(config.facilities != null && config.facilities.services != null ? config.facilities.services : Collections.<ServiceInfo>emptyList());
        services.addAll(getServices(properties));
        return services;
    }

    private Collection<ServiceInfo> getServices(final Properties properties) {
        final ConfigurationFactory cf = SystemInstance.get().getComponent(ConfigurationFactory.class);
        if (cf == null || !ConfigurationFactory.class.isInstance(cf)) {
            return Collections.emptyList();
        }

        final Openejb openejb = new Openejb();
        ConfigurationFactory.fillOpenEjb(openejb, properties);

        final List<Service> services = openejb.getServices();
        if (services.isEmpty()) {
            return Collections.emptyList();
        }

        final Collection<ServiceInfo> info = new ArrayList<>(services.size());
        for (final Service s : services) {
            final String prefix = s.getId() + ".";
            for (final String key : properties.stringPropertyNames()) {
                if (key.startsWith(prefix)) {
                    s.getProperties().put(key.substring(prefix.length()), properties.getProperty(key));
                }
            }

            try {
                info.add(cf.configureService(s, ServiceInfo.class));
            } catch (final OpenEJBException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return info;
    }
}
