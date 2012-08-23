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
package org.apache.openejb.server.cxf;

import org.apache.cxf.Bus;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.cxf.client.SaajInterceptor;
import org.apache.openejb.server.cxf.ejb.EjbWsContainer;
import org.apache.openejb.server.cxf.pojo.PojoWsContainer;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.webservices.WsService;

import javax.naming.Context;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class CxfService extends WsService {
    public static final String OPENEJB_JAXWS_CXF_FEATURES = "features";

    private final Map<String, CxfWsContainer> wsContainers = new TreeMap<String, CxfWsContainer>();

    public CxfService() {
        SaajInterceptor.registerInterceptors();
    }

    public String getName() {
        return "cxf";
    }

    public void init(final Properties props) throws java.lang.Exception {
        super.init(props);
        CxfUtil.configureBus();
    }

    protected HttpListener createEjbWsContainer(URL moduleBaseUrl, PortData port, BeanContext beanContext, ServiceConfiguration config) {
        Bus bus = CxfUtil.getBus();

        CxfCatalogUtils.loadOASISCatalog(bus, moduleBaseUrl, "META-INF/jax-ws-catalog.xml");

        EjbWsContainer container = new EjbWsContainer(bus, port, beanContext, config);
        container.start();
        wsContainers.put(beanContext.getDeploymentID().toString(), container);
        return container;
    }

    protected void destroyEjbWsContainer(String deploymentId) {
        CxfWsContainer container = wsContainers.remove(deploymentId);
        if (container != null) {
            container.destroy();
        }
    }

    protected HttpListener createPojoWsContainer(URL moduleBaseUrl, PortData port, String serviceId, Class target, Context context, String contextRoot, Map<String, Object> bdgs, ServiceConfiguration services) {
        Bus bus = CxfUtil.getBus();

        CxfCatalogUtils.loadOASISCatalog(bus, moduleBaseUrl, "META-INF/jax-ws-catalog.xml");

        PojoWsContainer container = new PojoWsContainer(bus, port, context, target, bdgs, services);
        container.start();
        wsContainers.put(serviceId, container);
        return container;
    }

    protected void destroyPojoWsContainer(String serviceId) {
        CxfWsContainer container = wsContainers.remove(serviceId);
        if (container != null) {
            container.destroy();
        }
    }
}
