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
package org.apache.openejb.server.cxf.pojo;

import org.apache.cxf.Bus;
import org.apache.cxf.transport.DestinationFactory;
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
import javax.naming.Context;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PojoWsContainer extends CxfWsContainer {
    private final Context context;
    private final Class target;
    private final Map<String, Object> bindings;
    private final ClassLoader loader;
    private WsServiceMBean mbean;

    public PojoWsContainer(final ClassLoader loader, final DestinationFactory transportFactory,
                           final Bus bus, final PortData port, final Context context,
                           final Class target,
                           final Map<String, Object> bdgs, final ServiceConfiguration configuration) {
        super(bus, transportFactory, port, configuration);
        if (target == null) throw new NullPointerException("target is null");
        this.context = context;
        this.target = target;
        this.bindings = bdgs;
        this.loader = loader;
    }

    @Override
    protected String getFakeUrl() {
        return (Class.class.isInstance(target)? Class.class.cast(target).getName() : target.getClass().getName()) + "_" + hashCode(); // pojo are not like ejbName: unique
    }

    protected PojoEndpoint createEndpoint() {
        return new PojoEndpoint(loader, bus, port, context, target, transportFactory, bindings, serviceConfiguration);
    }

    @Override
    protected ObjectName registerMBean() {
        final ObjectName name = new ObjectNameBuilder("openejb.management")
            .set("j2eeType", "JAX-WS")
            .set("J2EEServer", "openejb")
            .set("J2EEApplication", null)
            .set("EndpointType", "POJO")
            .set("name", target.getSimpleName())
            .build();

        mbean = new WsServiceMBean(context, target, port);
        LocalMBeanServer.registerDynamicWrapperSilently(mbean, name);
        return name;
    }

    @Override
    protected void setWsldUrl(final String wsdl) {
        mbean.wsdl(wsdl);
    }

    @MBean
    @Description("JAX-WS Service information")
    @Internal
    public class WsServiceMBean {

        private final Context context;
        private final Class target;
        private final PortData port;
        private String wsdl;

        public WsServiceMBean(final Context context, final Class target, final PortData port) {
            this.context = context;
            this.target = target;
            this.port = port;
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
