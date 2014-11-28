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
package org.apache.openejb.server.cxf.ejb;

import org.apache.cxf.Bus;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.openejb.BeanContext;
import org.apache.openejb.api.internal.Internal;
import org.apache.openejb.api.jmx.Description;
import org.apache.openejb.api.jmx.MBean;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.api.jmx.ManagedOperation;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerData;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.loader.IO;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.server.cxf.CxfWsContainer;

import javax.management.ObjectName;
import javax.management.openmbean.TabularData;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EjbWsContainer extends CxfWsContainer {
    private final BeanContext beanContext;
    private WsServiceMBean mbean;

    public EjbWsContainer(final Bus bus, final DestinationFactory transportFactory, final PortData port, final BeanContext beanContext, final ServiceConfiguration config) {
        super(bus, transportFactory, port, config);
        if (beanContext == null) throw new NullPointerException("deploymentInfo is null");
        this.beanContext = beanContext;
    }

    protected EjbEndpoint createEndpoint() {
        return new EjbEndpoint(bus, port, beanContext, transportFactory, serviceConfiguration);
    }

    @Override
    protected String getFakeUrl() {
        return beanContext.getEjbName().replace('/', '-') + "_ejb" + beanContext.hashCode();
    }

    protected ObjectName registerMBean() {
        final ObjectName name = new ObjectNameBuilder("openejb.management")
            .set("j2eeType", "JAX-WS")
            .set("J2EEServer", "openejb")
            .set("J2EEApplication", null)
            .set("EndpointType", "EJB")
            .set("name", beanContext.getEjbName())
            .build();

        mbean = new WsServiceMBean(beanContext, port);
        LocalMBeanServer.registerDynamicWrapperSilently(mbean, name);
        return name;
    }

    @Override
    protected void setWsldUrl(String wsdl) {
        mbean.wsdl(wsdl);
    }

    @MBean
    @Description("JAX-WS Service information")
    @Internal
    public class WsServiceMBean {

        private final BeanContext beanContext;
        private final PortData port;
        private String wsdl;

        public WsServiceMBean(final BeanContext beanContext, final PortData port) {
            this.beanContext = beanContext;
            this.port = port;
        }

        @ManagedAttribute
        @Description("The service endpoint interface")
        public String getServiceEndpointInterface() {
            return beanContext.getServiceEndpointInterface().getName();
        }

        @ManagedAttribute
        @Description("The EJB endpoint type")
        public String getComponentType() {
            return beanContext.getComponentType().name();
        }

        @ManagedOperation
        @Description("Slurp the WSDL")
        public String getWsdl() {
            try {
                return IO.slurp(new URL(wsdl));
            } catch (final IOException e) {
                return e.getMessage();
            }
        }

        @ManagedAttribute
        @Description("The WSDL url")
        public String getWsdlUrl() {
            return wsdl;
        }

        @ManagedAttribute
        @Description("The service port QName")
        public String getPort() {
            return port.getPortName().toString();
        }

        @ManagedAttribute
        @Description("The service QName")
        public String getService() {
            return port.getServiceName().toString();
        }

        @ManagedAttribute
        @Description("The handler list")
        public TabularData getHandlers() {
            final List<String> names = new ArrayList<String>();
            final List<String> values = new ArrayList<String>();

            for (final HandlerChainData handlerChainData : port.getHandlerChains()) {
                for (final HandlerData handlerData : handlerChainData.getHandlers()) {
                    names.add(handlerChainData.getServiceNamePattern().toString());
                    values.add(handlerData.getHandlerClass().getName());
                }
            }

            return LocalMBeanServer.tabularData(
                "handlers", "The list of handlers",
                names.toArray(new String[names.size()]), values.toArray(new String[values.size()]));
        }

        @ManagedAttribute
        @Description("Is the service secured?")
        public boolean getSecured() {
            return port.isSecure();
        }

        @ManagedAttribute
        @Description("Is MTOM enabled?")
        public boolean getMtomEnabled() {
            return port.isMtomEnabled();
        }

        @ManagedAttribute
        @Description("Service configuration properties")
        public TabularData getProperties() {
            return LocalMBeanServer.tabularData(
                "properties",
                "Service configuration properties",
                "Service configuration properties",
                port.getProperties()
            );
        }

        public void wsdl(final String wsdl) {
            if (!wsdl.endsWith("?wsdl")) {
                this.wsdl = wsdl + "?wsdl";
            } else {
                this.wsdl = wsdl;
            }
        }
    }
}
