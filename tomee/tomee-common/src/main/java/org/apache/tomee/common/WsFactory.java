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
package org.apache.tomee.common;

import org.apache.naming.ResourceRef;
import org.apache.openejb.Injection;
import org.apache.openejb.core.ivm.naming.JaxWsServiceReference;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.PortRefData;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.URL;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class WsFactory extends AbstractObjectFactory {
    
    @Override
    public Object getObjectInstance(final Object object, final Name name, final Context context, final Hashtable environment) throws Exception {
        // ignore non resource-refs
        if (!(object instanceof ResourceRef)) {
            return null;
        }

        final Reference ref = (Reference) object;

        final Object value;
        if (NamingUtil.getProperty(ref, NamingUtil.JNDI_NAME) != null) {
            // lookup the value in JNDI
            value = super.getObjectInstance(object, name, context, environment);
        } else {
            // load service class which is used to construct the port
            final String serviceClassName = NamingUtil.getProperty(ref, NamingUtil.WS_CLASS);
            Class<? extends Service> serviceClass = Service.class;
            if (serviceClassName != null) {
                serviceClass = NamingUtil.loadClass(serviceClassName).asSubclass(Service.class);
                if (serviceClass == null) {
                    throw new NamingException("Could not load service type class "+ serviceClassName);
                }
            }

            // load the reference class which is the ultimate type of the port
            final Class<?> referenceClass = NamingUtil.loadClass(ref.getClassName());

            // if ref class is a subclass of Service, use it for the service class
            if (referenceClass != null && Service.class.isAssignableFrom(referenceClass)) {
                serviceClass = referenceClass.asSubclass(Service.class);
            }

            // PORT ID
            final String serviceId = NamingUtil.getProperty(ref, NamingUtil.WS_ID);

            // Service QName
            QName serviceQName = null;
            if (NamingUtil.getProperty(ref, NamingUtil.WS_QNAME) != null) {
                serviceQName = QName.valueOf(NamingUtil.getProperty(ref, NamingUtil.WS_QNAME));
            }

            // WSDL URL
            URL wsdlUrl = null;
            if (NamingUtil.getProperty(ref, NamingUtil.WSDL_URL) != null) {
                wsdlUrl = new URL(NamingUtil.getProperty(ref, NamingUtil.WSDL_URL));
            }

            // Port QName
            QName portQName = null;
            if (NamingUtil.getProperty(ref, NamingUtil.WS_PORT_QNAME) != null) {
                portQName = QName.valueOf(NamingUtil.getProperty(ref, NamingUtil.WS_PORT_QNAME));
            }

            // port refs
            List<PortRefData> portRefs = NamingUtil.getStaticValue(ref, "port-refs");
            if (portRefs == null) {
                portRefs = Collections.emptyList();
            }

            // HandlerChain
            List<HandlerChainData> handlerChains = NamingUtil.getStaticValue(ref, "handler-chains");
            if (handlerChains == null) {
                handlerChains = Collections.emptyList();
            }
            Collection<Injection> injections = NamingUtil.getStaticValue(ref, "injections");
            if (injections == null) {
                injections = Collections.emptyList();
            }

            final Properties properties = new Properties();
            properties.putAll(environment);
            final JaxWsServiceReference serviceReference = new JaxWsServiceReference(serviceId,
                    serviceQName,
                    serviceClass, portQName,
                    referenceClass,
                    wsdlUrl,
                    portRefs,
                    handlerChains,
                    injections,
                    properties);
            value = serviceReference.getObject();
        }

        return value;
    }

    @Override
    protected String buildJndiName(final Reference reference) throws NamingException {
        throw new UnsupportedOperationException();
    }
}
