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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.server.ejbd;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.jms.ConnectionFactory;
import javax.xml.namespace.QName;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.Injection;
import org.apache.openejb.util.proxy.ProxyManager;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.core.ivm.BaseEjbProxyHandler;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.webservices.ServiceRefData;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerData;
import org.apache.openejb.core.webservices.PortRefData;
import org.apache.openejb.core.webservices.PortAddressRegistry;
import org.apache.openejb.core.webservices.PortAddress;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.JNDIRequest;
import org.apache.openejb.client.JNDIResponse;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.client.DataSourceMetaData;
import org.apache.openejb.client.InjectionMetaData;
import org.apache.openejb.client.WsMetaData;
import org.apache.openejb.client.HandlerChainMetaData;
import org.apache.openejb.client.HandlerMetaData;
import org.apache.openejb.client.CallbackMetaData;
import org.apache.openejb.client.PortRefMetaData;
import org.apache.openejb.client.ThrowableArtifact;
import org.apache.openejb.client.RequestMethodConstants;
import org.apache.openejb.client.NameClassPairEnumeration;
import org.apache.commons.dbcp.BasicDataSource;
import org.omg.CORBA.ORB;

class JndiRequestHandler {
    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE.createChild("jndi"), "org.apache.openejb.server.util.resources");

    private Context ejbJndiTree;
    private Context clientJndiTree;
    private Context deploymentsJndiTree;

    JndiRequestHandler(EjbDaemon daemon) throws Exception {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        ejbJndiTree = (Context) containerSystem.getJNDIContext().lookup("openejb/ejb");
        deploymentsJndiTree = (Context) containerSystem.getJNDIContext().lookup("openejb/Deployment");
        try {
            clientJndiTree = (Context) containerSystem.getJNDIContext().lookup("openejb/client");
        } catch (NamingException e) {
        }
    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
        JNDIResponse res = new JNDIResponse();
        JNDIRequest req = null;
        try {
            req = new JNDIRequest();
            req.readExternal(in);
        } catch (Throwable e) {
            res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
            NamingException namingException = new NamingException("Could not read jndi request");
            namingException.setRootCause(e);
            res.setResult(new ThrowableArtifact(namingException));

            if (logger.isDebugEnabled()){
                try {
                    logger.debug("JNDI REQUEST: "+req+" -- RESPONSE: " + res);
                } catch (Exception justInCase) {}
            }

            try {
                res.writeExternal(out);
            } catch (java.io.IOException ie) {
                logger.fatal("Couldn't write JndiResponse to output stream", ie);
            }
        }

        try {
            if (req.getRequestString().startsWith("/")) {
                req.setRequestString(req.getRequestString().substring(1));
            }
            Context context = getContext(req);

            switch(req.getRequestMethod()){
                case RequestMethodConstants.JNDI_LOOKUP: doLookup(req, res, context); break;
                case RequestMethodConstants.JNDI_LIST: doList(req, res, context); break;
            }

        } catch (Throwable e) {
            res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
            NamingException namingException = new NamingException("Unknown error in container");
            namingException.setRootCause(e);
            res.setResult(new ThrowableArtifact(namingException));
        } finally {

            if (logger.isDebugEnabled()){
                try {
                    logger.debug("JNDI REQUEST: "+req+" -- RESPONSE: " + res);
                } catch (Exception justInCase) {}
            }

            try {
                res.writeExternal(out);
            } catch (Throwable e) {
                logger.fatal("Couldn't write JndiResponse to output stream", e);
            }
        }
    }

    private Context getContext(JNDIRequest req) throws NamingException {
        Context context;
        if (req.getModuleId() != null && req.getModuleId().equals("openejb/Deployment")){
            context = deploymentsJndiTree;
        } else if (req.getModuleId() != null && clientJndiTree != null) {
            context = (Context) clientJndiTree.lookup(req.getModuleId());
        } else {
            context = ejbJndiTree;
        }
        return context;
    }

    private void doLookup(JNDIRequest req, JNDIResponse res, Context context) {
        Object object;
        String name = req.getRequestString();

        try {

            if (name.equals("comp/injections")) {

                //noinspection unchecked
                List<Injection> injections = (List<Injection>) context.lookup(name);
                InjectionMetaData metaData = new InjectionMetaData();
                for (Injection injection : injections) {
                    metaData.addInjection(injection.getTarget().getName(), injection.getName(), injection.getJndiName());
                }
                res.setResponseCode(ResponseCodes.JNDI_INJECTIONS);
                res.setResult(metaData);
                return;
            } else {
                object = context.lookup(name);
            }

            if (object instanceof Context) {
                res.setResponseCode(ResponseCodes.JNDI_CONTEXT);
                return;
            } else if (object == null) {
                throw new NullPointerException("lookup of '"+name+"' returned null");
            } else if (object instanceof BasicDataSource){
                BasicDataSource cf = (BasicDataSource) object;
                DataSourceMetaData dataSourceMetaData = new DataSourceMetaData(cf.getDriverClassName(), cf.getUrl(), cf.getUsername(), cf.getPassword());
                res.setResponseCode(ResponseCodes.JNDI_DATA_SOURCE);
                res.setResult(dataSourceMetaData);
                return;
            } else if (object instanceof ConnectionFactory){
                res.setResponseCode(ResponseCodes.JNDI_RESOURCE);
                res.setResult(ConnectionFactory.class.getName());
                return;
            } else if (object instanceof ORB){
                res.setResponseCode(ResponseCodes.JNDI_RESOURCE);
                res.setResult(ORB.class.getName());
                return;
            }

            ServiceRefData serviceRef;
            if (object instanceof ServiceRefData) {
                serviceRef = (ServiceRefData) object;
            } else {
                serviceRef = ServiceRefData.getServiceRefData(object);
            }

            if (serviceRef != null) {
                WsMetaData serviceMetaData = new WsMetaData();

                // service class
                String serviceClassName = null;
                if (serviceRef.getServiceClass() != null) {
                    serviceClassName = serviceRef.getServiceClass().getName();
                }
                serviceMetaData.setServiceClassName(serviceClassName);

                // reference class
                String referenceClassName = null;
                if (serviceRef.getReferenceClass() != null) {
                    referenceClassName = serviceRef.getReferenceClass().getName();
                }
                serviceMetaData.setReferenceClassName(referenceClassName);

                // set service qname
                if (serviceRef.getServiceQName() != null) {
                    serviceMetaData.setServiceQName(serviceRef.getServiceQName().toString());
                }

                // get the port addresses for this service
                PortAddressRegistry portAddressRegistry = SystemInstance.get().getComponent(PortAddressRegistry.class);
                Set<PortAddress> portAddresses = null;
                if (portAddressRegistry != null) {
                    portAddresses = portAddressRegistry.getPorts(serviceRef.getId(), serviceRef.getServiceQName(), referenceClassName);
                }

                // resolve the wsdl url
                if (serviceRef.getWsdlURL() != null) {
                    serviceMetaData.setWsdlUrl(serviceRef.getWsdlURL().toExternalForm());
                }
                if (portAddresses.size() == 1) {
                    PortAddress portAddress = portAddresses.iterator().next();
                    serviceMetaData.setWsdlUrl(portAddress.getAddress() + "?wsdl");
                }

                // add handler chains
                for (HandlerChainData handlerChain : serviceRef.getHandlerChains()) {
                    HandlerChainMetaData handlerChainMetaData = new HandlerChainMetaData();
                    handlerChainMetaData.setServiceNamePattern(handlerChain.getServiceNamePattern());
                    handlerChainMetaData.setPortNamePattern(handlerChain.getPortNamePattern());
                    handlerChainMetaData.getProtocolBindings().addAll(handlerChain.getProtocolBindings());
                    for (HandlerData handler : handlerChain.getHandlers()) {
                        HandlerMetaData handlerMetaData = new HandlerMetaData();
                        handlerMetaData.setHandlerClass(handler.getHandlerClass().getName());
                        for (Method method : handler.getPostConstruct()) {
                            CallbackMetaData callbackMetaData = new CallbackMetaData();
                            callbackMetaData.setClassName(method.getDeclaringClass().getName());
                            callbackMetaData.setMethod(method.getName());
                            handlerMetaData.getPostConstruct().add(callbackMetaData);
                        }
                        for (Method method : handler.getPreDestroy()) {
                            CallbackMetaData callbackMetaData = new CallbackMetaData();
                            callbackMetaData.setClassName(method.getDeclaringClass().getName());
                            callbackMetaData.setMethod(method.getName());
                            handlerMetaData.getPreDestroy().add(callbackMetaData);
                        }
                        handlerChainMetaData.getHandlers().add(handlerMetaData);
                    }
                    serviceMetaData.getHandlerChains().add(handlerChainMetaData);
                }

                // add port refs
                Map<QName,PortRefMetaData> portsByQName = new HashMap<QName,PortRefMetaData>();
                for (PortRefData portRef : serviceRef.getPortRefs()) {
                    PortRefMetaData portRefMetaData = new PortRefMetaData();
                    portRefMetaData.setQName(portRef.getQName());
                    portRefMetaData.setServiceEndpointInterface(portRef.getServiceEndpointInterface());
                    portRefMetaData.setEnableMtom(portRef.isEnableMtom());
                    portRefMetaData.getProperties().putAll(portRef.getProperties());
                    portRefMetaData.getAddresses().addAll(portRef.getAddresses());
                    if (portRef.getQName() != null) {
                        portsByQName.put(portRef.getQName(), portRefMetaData);
                    }
                    serviceMetaData.getPortRefs().add(portRefMetaData);
                }

                // add PortRefMetaData for any portAddress not added above
                for (PortAddress portAddress : portAddresses) {
                    PortRefMetaData portRefMetaData = portsByQName.get(portAddress.getPortQName());
                    if (portRefMetaData == null) {
                        portRefMetaData = new PortRefMetaData();
                        portRefMetaData.setQName(portAddress.getPortQName());
                        portRefMetaData.setServiceEndpointInterface(portAddress.getServiceEndpointInterface());
                        portRefMetaData.getAddresses().add(portAddress.getAddress());
                        serviceMetaData.getPortRefs().add(portRefMetaData);
                    } else {
                        portRefMetaData.getAddresses().add(portAddress.getAddress());
                        if (portRefMetaData.getServiceEndpointInterface() == null) {
                            portRefMetaData.setServiceEndpointInterface(portAddress.getServiceEndpointInterface());
                        }
                    }
                }

                res.setResponseCode(ResponseCodes.JNDI_WEBSERVICE);
                res.setResult(serviceMetaData);
                return;
            }
        } catch (NameNotFoundException e) {
            res.setResponseCode(ResponseCodes.JNDI_NOT_FOUND);
            return;
        } catch (NamingException e) {
            res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
            res.setResult(new ThrowableArtifact(e));
            return;
        }


        BaseEjbProxyHandler handler;
        try {
            handler = (BaseEjbProxyHandler) ProxyManager.getInvocationHandler(object);
        } catch (Exception e) {
            // Not a proxy.  See if it's serializable and send it
            if (object instanceof java.io.Serializable){
                res.setResponseCode(ResponseCodes.JNDI_OK);
                res.setResult(object);
                return;
            } else {
                res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                NamingException namingException = new NamingException("Expected an ejb proxy, found unknown object: type=" + object.getClass().getName() + ", toString=" + object);
                res.setResult(new ThrowableArtifact(namingException));
                return;
            }
        }

        ProxyInfo proxyInfo = handler.getProxyInfo();
        DeploymentInfo deployment = proxyInfo.getDeploymentInfo();
        String deploymentID = deployment.getDeploymentID().toString();

        switch(proxyInfo.getInterfaceType()){
            case EJB_HOME: {
                res.setResponseCode(ResponseCodes.JNDI_EJBHOME);
                EJBMetaDataImpl metaData = new EJBMetaDataImpl(deployment.getHomeInterface(),
                        deployment.getRemoteInterface(),
                        deployment.getPrimaryKeyClass(),
                        deployment.getComponentType().toString(),
                        deploymentID,
                        -1, null);
                res.setResult(metaData);
                break;
            }
            case EJB_LOCAL_HOME: {
                res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                NamingException namingException = new NamingException("Not remotable: '" + name + "'. EJBLocalHome interfaces are not remotable as per the EJB specification.");
                res.setResult(new ThrowableArtifact(namingException));
                break;
            }
            case BUSINESS_REMOTE: {
                res.setResponseCode(ResponseCodes.JNDI_BUSINESS_OBJECT);
                EJBMetaDataImpl metaData = new EJBMetaDataImpl(null,
                        null,
                        deployment.getPrimaryKeyClass(),
                        deployment.getComponentType().toString(),
                        deploymentID,
                        -1, proxyInfo.getInterfaces());
                metaData.setPrimaryKey(proxyInfo.getPrimaryKey());
                res.setResult(metaData);
                break;
            }
            case BUSINESS_LOCAL: {
                String property = SystemInstance.get().getProperty("openejb.remotable.businessLocals", "false");
                if (property.equalsIgnoreCase("true")) {
                    res.setResponseCode(ResponseCodes.JNDI_BUSINESS_OBJECT);
                    EJBMetaDataImpl metaData = new EJBMetaDataImpl(null,
                            null,
                            deployment.getPrimaryKeyClass(),
                            deployment.getComponentType().toString(),
                            deploymentID,
                            -1, proxyInfo.getInterfaces());
                    metaData.setPrimaryKey(proxyInfo.getPrimaryKey());
                    res.setResult(metaData);
                } else {
                    res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                    NamingException namingException = new NamingException("Not remotable: '" + name + "'. Business Local interfaces are not remotable as per the EJB specification.  To disable this restriction, set the system property 'openejb.remotable.businessLocals=true' in the server.");
                    res.setResult(new ThrowableArtifact(namingException));
                }
                break;
            }
            default: {
                res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                NamingException namingException = new NamingException("Not remotable: '" + name + "'.");
                res.setResult(new ThrowableArtifact(namingException));
            }
        }

    }

    private void doList(JNDIRequest req, JNDIResponse res, Context context) {
        String name = req.getRequestString();
        try {
            NamingEnumeration<NameClassPair> namingEnumeration = context.list(name);
            if (namingEnumeration == null){
                res.setResponseCode(ResponseCodes.JNDI_OK);
                res.setResult(null);
            } else {
                res.setResponseCode(ResponseCodes.JNDI_ENUMERATION);
                ArrayList<NameClassPair> list = Collections.list(namingEnumeration);
                for (NameClassPair pair : list) {
                    if (pair.getClassName().equals(IvmContext.class.getName())){
                        pair.setClassName(javax.naming.Context.class.getName());
                    }
                }
                res.setResult(new NameClassPairEnumeration(list));
            }
        } catch (NameNotFoundException e) {
            res.setResponseCode(ResponseCodes.JNDI_NOT_FOUND);
            return;
        } catch (NamingException e) {
            res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
            res.setResult(new ThrowableArtifact(e));
            return;
        }
    }

}
