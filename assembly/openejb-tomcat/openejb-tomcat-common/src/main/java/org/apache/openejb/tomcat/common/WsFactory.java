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
package org.apache.openejb.tomcat.common;

import org.apache.naming.ResourceRef;
import org.apache.openejb.Injection;
import org.apache.openejb.core.ivm.naming.JaxWsServiceReference;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.PortRefData;
import static org.apache.openejb.tomcat.common.NamingUtil.JNDI_NAME;
import static org.apache.openejb.tomcat.common.NamingUtil.WSDL_URL;
import static org.apache.openejb.tomcat.common.NamingUtil.WS_CLASS;
import static org.apache.openejb.tomcat.common.NamingUtil.WS_ID;
import static org.apache.openejb.tomcat.common.NamingUtil.WS_PORT_QNAME;
import static org.apache.openejb.tomcat.common.NamingUtil.WS_QNAME;
import static org.apache.openejb.tomcat.common.NamingUtil.getProperty;
import static org.apache.openejb.tomcat.common.NamingUtil.getStaticValue;
import static org.apache.openejb.tomcat.common.NamingUtil.loadClass;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

public class WsFactory extends AbstractObjectFactory {
    public Object getObjectInstance(Object object, Name name, Context context, Hashtable environment) throws Exception {
        // ignore non resource-refs
        if (!(object instanceof ResourceRef)) {
            return null;
        }

        Reference ref = (Reference) object;

        Object value;
        if (getProperty(ref, JNDI_NAME) != null) {
            // lookup the value in JNDI
            value = super.getObjectInstance(object, name, context, environment);
        } else {
            // load service class which is used to construct the port
            String serviceClassName = getProperty(ref, WS_CLASS);
            Class<? extends Service> serviceClass = Service.class;
            if (serviceClassName != null) {
                serviceClass = loadClass(serviceClassName).asSubclass(Service.class);
                if (serviceClass == null) {
                    throw new NamingException("Could not load service type class "+ serviceClassName);
                }
            }

            // load the reference class which is the ultimate type of the port
            Class<?> referenceClass = loadClass(ref.getClassName());

            // if ref class is a subclass of Service, use it for the service class
            if (referenceClass != null && Service.class.isAssignableFrom(referenceClass)) {
                serviceClass = referenceClass.asSubclass(Service.class);
            }

            // PORT ID
            String serviceId = getProperty(ref, WS_ID);

            // Service QName
            QName serviceQName = null;
            if (getProperty(ref, WS_QNAME) != null) {
                serviceQName = QName.valueOf(getProperty(ref, WS_QNAME));
            }

            // WSDL URL
            URL wsdlUrl = null;
            if (getProperty(ref, WSDL_URL) != null) {
                wsdlUrl = new URL(getProperty(ref, WSDL_URL));
            }

            // Port QName
            QName portQName = null;
            if (getProperty(ref, WS_PORT_QNAME) != null) {
                portQName = QName.valueOf(getProperty(ref, WS_PORT_QNAME));
            }

            // port refs
            List<PortRefData> portRefs = getStaticValue(ref, "port-refs");
            if (portRefs == null) portRefs = Collections.emptyList();

            // HandlerChain
            List<HandlerChainData> handlerChains = getStaticValue(ref, "handler-chains");
            if (handlerChains == null) handlerChains = Collections.emptyList();
            List<Injection> injections = getStaticValue(ref, "injections");
            if (injections == null) injections = Collections.emptyList();

            JaxWsServiceReference serviceReference = new JaxWsServiceReference(serviceId,
                    serviceQName,
                    serviceClass, portQName,
                    referenceClass,
                    wsdlUrl,
                    portRefs,
                    handlerChains,
                    injections);
            value = serviceReference.getObject();
        }

        return value;
    }

    protected String buildJndiName(Reference reference) throws NamingException {
        throw new UnsupportedOperationException();
    }
}
