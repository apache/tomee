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
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.config.sys.Service;
import org.apache.openejb.core.ivm.naming.JaxWsServiceReference;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    @Override
    public void customize(final Object o, final Properties properties) {
        try {
            if (!javax.xml.ws.Service.class.isInstance(o)) {
                configure(ClientProxy.getClient(o), properties);
            }
        } catch (final Exception e) {
            Logger.getInstance(LogCategory.CXF, WebServiceInjectionConfigurator.class.getName())
                    .error(e.getMessage(), e);
        }
    }

    private void configure(final Client client, final Properties properties) {
        if (properties == null) {
            return;
        }

        for (final String suffix : asList("", client.getEndpoint().getEndpointInfo().getName().toString() + ".")) {
            // here (ie at runtime) we have no idea which services were linked to the app
            // so using tomee.xml ones for now (not that shocking since we externalize the config with this class)
            final OpenEjbConfiguration config = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
            final List<ServiceInfo> services = new ArrayList<ServiceInfo>(config.facilities != null && config.facilities.services != null ? config.facilities.services : Collections.<ServiceInfo>emptyList());
            services.addAll(getServices(properties));
            configureInterceptors(client, "cxf.jaxws.client." + suffix, services, properties);
        }
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

        final Collection<ServiceInfo> info = new ArrayList<ServiceInfo>(services.size());
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
