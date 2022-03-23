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

import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.client.CallbackMetaData;
import org.apache.openejb.client.DataSourceMetaData;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.HandlerChainMetaData;
import org.apache.openejb.client.HandlerMetaData;
import org.apache.openejb.client.InjectionMetaData;
import org.apache.openejb.client.JNDIRequest;
import org.apache.openejb.client.JNDIResponse;
import org.apache.openejb.client.NameClassPairEnumeration;
import org.apache.openejb.client.PortRefMetaData;
import org.apache.openejb.client.ProtocolMetaData;
import org.apache.openejb.client.Response;
import org.apache.openejb.client.ResponseCodes;
import org.apache.openejb.client.ThrowableArtifact;
import org.apache.openejb.client.WsMetaData;
import org.apache.openejb.core.ivm.BaseEjbProxyHandler;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.HandlerData;
import org.apache.openejb.core.webservices.PortAddress;
import org.apache.openejb.core.webservices.PortAddressRegistry;
import org.apache.openejb.core.webservices.PortRefData;
import org.apache.openejb.core.webservices.ServiceRefData;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.jdbc.DataSourceFactory;
import org.apache.openejb.server.context.RequestInfos;
import org.apache.openejb.server.stream.CountingInputStream;
import org.apache.openejb.server.stream.CountingOutputStream;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.ProxyManager;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.Topic;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import jakarta.resource.Referenceable;
import javax.sql.DataSource;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static org.apache.openejb.server.ejbd.ClientObjectFactory.convert;

class JndiRequestHandler extends RequestHandler {
    private static final Class<?> ORB_CLASS;

    static {
        Class<?> orb;
        try {
            orb = JndiRequestHandler.class.getClassLoader().loadClass("org.omg.CORBA.ORB");
        } catch (final ClassNotFoundException e) {
            orb = null;
        }
        ORB_CLASS = orb;
    }

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER_REMOTE.createChild("jndi"), "org.apache.openejb.server.util.resources");

    private Context clientJndiTree;

    private final ClusterableRequestHandler clusterableRequestHandler;
    private Context rootContext;

    JndiRequestHandler(final EjbDaemon daemon) throws Exception {
        super(daemon);
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        containerSystem.getJNDIContext().lookup("openejb/remote");
        containerSystem.getJNDIContext().lookup("openejb/Deployment");
        containerSystem.getJNDIContext().lookup("openejb/global");

        rootContext = containerSystem.getJNDIContext();
        try {
            clientJndiTree = (Context) containerSystem.getJNDIContext().lookup("openejb/client");
        } catch (NamingException ignore) {
        }
        clusterableRequestHandler = newClusterableRequestHandler();
    }

    public boolean isDebug() {
        return LOGGER.isDebugEnabled();
    }

    protected BasicClusterableRequestHandler newClusterableRequestHandler() {
        return new BasicClusterableRequestHandler();
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public String getName() {
        return "JNDI";
    }

    @Override
    public Response processRequest(final ObjectInputStream in, final ProtocolMetaData metaData) {

        final JNDIRequest req = new JNDIRequest();
        final JNDIResponse res = new JNDIResponse();
        res.setRequest(req);

        try {
            req.setMetaData(metaData);
            req.readExternal(in);
        } catch (Throwable e) {
            res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
            final NamingException namingException = new NamingException("Could not read jndi request");
            namingException.setRootCause(e);
            res.setResult(new ThrowableArtifact(namingException));

            if (LOGGER.isDebugEnabled()) {
                try {
                    logRequestResponse(req, res);
                } catch (Exception ignore) {
                    // no-op
                }
            }
        }
        return res;
    }

    @Override
    public void processResponse(final Response response, final ObjectOutputStream out, final ProtocolMetaData metaData) throws Exception {

        if (JNDIResponse.class.isInstance(response)) {

            final JNDIResponse res = (JNDIResponse) response;
            final JNDIRequest req = res.getRequest();

            try {

                //Only process if 'processRequest' was ok...
                final Object result = res.getResult();
                if (null == result || !ThrowableArtifact.class.isInstance(result)) {

                    if (req.getRequestString().startsWith("/")) {
                        req.setRequestString(req.getRequestString().substring(1));
                    }

                    final String prefix = getPrefix(req);

                    switch (req.getRequestMethod()) {
                        case JNDI_LOOKUP:
                            doLookup(req, res, prefix);
                            break;
                        case JNDI_LIST:
                            doList(req, res, prefix);
                            break;
                    }
                }

            } catch (Throwable e) {
                res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                final NamingException namingException = new NamingException("Unknown error in container");
                namingException.setRootCause(e);
                res.setResult(new ThrowableArtifact(namingException));
            } finally {

                try {
                    res.setMetaData(metaData);
                    res.writeExternal(out);
                } catch (Throwable e) {
                    LOGGER.fatal("Could not write JndiResponse to output stream", e);
                }

                if (LOGGER.isDebugEnabled()) {
                    try {
                        out.flush(); // force it to as correct as possible response size
                        logRequestResponse(req, res);
                    } catch (Exception ignore) {
                        // no-op
                    }
                }
            }

        } else {
            LOGGER.error("JndiRequestHandler cannot process an instance of: " + response.getClass().getName());
        }
    }

    private void logRequestResponse(final JNDIRequest req, final JNDIResponse res) {
        final RequestInfos.RequestInfo info = RequestInfos.info();
        final InputStream cis = info.getInputStream();
        final OutputStream cos = info.getOutputStream();

        LOGGER.debug("JNDI REQUEST: " + req + " (size = " + (null != cis ? CountingInputStream.class.cast(cis).getCount() : 0)
            + "b, remote-ip =" + info.ip
            + ") -- RESPONSE: " + res + " (size = " + (null != cos ? CountingOutputStream.class.cast(cos).getCount() : 0) + "b)");
    }

    private String getPrefix(final JNDIRequest req) throws NamingException {
        final String prefix;
        final String name = req.getRequestString();

        if (name.startsWith("openejb/Deployment/")) {
            prefix = "";
        } else if (req.getModuleId() != null && req.getModuleId().equals("openejb/Deployment")) {
            prefix = "openejb/Deployment/";
        } else if (req.getModuleId() != null && req.getModuleId().equals("openejb/global")) {
            prefix = "openejb/global/";
        } else if (req.getModuleId() != null && clientJndiTree != null) {
            prefix = "openejb/client/" + req.getModuleId() + "/";// + (Context) clientJndiTree.lookup(req.getModuleId());
        } else {
            prefix = "openejb/remote/";
        }
        return prefix;
    }

    private void doLookup(final JNDIRequest req, final JNDIResponse res, final String prefix) {
        Object object;
        final String name = req.getRequestString();

        try {

            if (name.equals("info/injections")) {

                //noinspection unchecked
                final List<Injection> injections = (List<Injection>) rootContext.lookup(prefix + name);
                final InjectionMetaData metaData = new InjectionMetaData();
                for (final Injection injection : injections) {
                    if (injection.getTarget() == null) {
                        continue;
                    }
                    metaData.addInjection(injection.getTarget().getName(), injection.getName(), injection.getJndiName());
                }
                res.setResponseCode(ResponseCodes.JNDI_INJECTIONS);
                res.setResult(metaData);
                return;
            } else {
                try {
                    object = rootContext.lookup(prefix + name);
                } catch (NameNotFoundException nnfe) { // fallback to resources
                    object = rootContext.lookup("openejb/Resource/" + name);
                }
            }

            if (object instanceof Context) {
                res.setResponseCode(ResponseCodes.JNDI_CONTEXT);
                return;
            } else if (object == null) {
                throw new NullPointerException("lookup of '" + name + "' returned null");
            } else if (object instanceof DataSource) {
                if (DataSourceFactory.knows(object)) {
                    try {
                        final DbcpDataSource cf = new DbcpDataSource(object);
                        final DataSourceMetaData dataSourceMetaData = new DataSourceMetaData(cf.getDriverClassName(), cf.getUrl(), cf.getUsername(), cf.getPassword());
                        res.setResponseCode(ResponseCodes.JNDI_DATA_SOURCE);
                        res.setResult(dataSourceMetaData);
                    } catch (Exception e) {
                        res.setResponseCode(ResponseCodes.JNDI_ERROR);
                        res.setResult(new ThrowableArtifact(e));
                    }
                    return;
                } else if (object instanceof Referenceable) {
                    res.setResponseCode(ResponseCodes.JNDI_REFERENCE);
                    res.setResult(((Referenceable) object).getReference());
                    return;
                }
            } else if (object instanceof ConnectionFactory) {
                res.setResponseCode(ResponseCodes.JNDI_RESOURCE);
                res.setResult(ConnectionFactory.class.getName());
                return;
            } else if (ORB_CLASS != null && ORB_CLASS.isInstance(object)) {
                res.setResponseCode(ResponseCodes.JNDI_RESOURCE);
                res.setResult(ORB_CLASS.getName());
                return;
            } else if (object instanceof ValidatorFactory) {
                res.setResponseCode(ResponseCodes.JNDI_RESOURCE);
                res.setResult(ValidatorFactory.class.getName());
                return;
            } else if (object instanceof Validator) {
                res.setResponseCode(ResponseCodes.JNDI_RESOURCE);
                res.setResult(Validator.class.getName());
                return;
            } else if (object instanceof Queue) {
                res.setResponseCode(ResponseCodes.JNDI_RESOURCE);
                res.setResult(Queue.class.getName());
                return;
            } else if (object instanceof Topic) {
                res.setResponseCode(ResponseCodes.JNDI_RESOURCE);
                res.setResult(Topic.class.getName());
                return;
            }

            final ServiceRefData serviceRef;
            if (object instanceof ServiceRefData) {
                serviceRef = (ServiceRefData) object;
            } else {
                serviceRef = ServiceRefData.getServiceRefData(object);
            }

            if (serviceRef != null) {
                final WsMetaData serviceMetaData = new WsMetaData();

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
                final PortAddressRegistry portAddressRegistry = SystemInstance.get().getComponent(PortAddressRegistry.class);
                Set<PortAddress> portAddresses = null;
                if (portAddressRegistry != null) {
                    portAddresses = portAddressRegistry.getPorts(serviceRef.getId(), serviceRef.getServiceQName(), referenceClassName);
                }

                // resolve the wsdl url
                if (serviceRef.getWsdlURL() != null) {
                    serviceMetaData.setWsdlUrl(serviceRef.getWsdlURL().toExternalForm());
                }
                if (portAddresses.size() == 1) {
                    final PortAddress portAddress = portAddresses.iterator().next();
                    serviceMetaData.setWsdlUrl(portAddress.getAddress() + "?wsdl");
                }

                // add handler chains
                for (final HandlerChainData handlerChain : serviceRef.getHandlerChains()) {
                    final HandlerChainMetaData handlerChainMetaData = new HandlerChainMetaData();
                    handlerChainMetaData.setServiceNamePattern(handlerChain.getServiceNamePattern());
                    handlerChainMetaData.setPortNamePattern(handlerChain.getPortNamePattern());
                    handlerChainMetaData.getProtocolBindings().addAll(handlerChain.getProtocolBindings());
                    for (final HandlerData handler : handlerChain.getHandlers()) {
                        final HandlerMetaData handlerMetaData = new HandlerMetaData();
                        handlerMetaData.setHandlerClass(handler.getHandlerClass().getName());
                        for (final Method method : handler.getPostConstruct()) {
                            final CallbackMetaData callbackMetaData = new CallbackMetaData();
                            callbackMetaData.setClassName(method.getDeclaringClass().getName());
                            callbackMetaData.setMethod(method.getName());
                            handlerMetaData.getPostConstruct().add(callbackMetaData);
                        }
                        for (final Method method : handler.getPreDestroy()) {
                            final CallbackMetaData callbackMetaData = new CallbackMetaData();
                            callbackMetaData.setClassName(method.getDeclaringClass().getName());
                            callbackMetaData.setMethod(method.getName());
                            handlerMetaData.getPreDestroy().add(callbackMetaData);
                        }
                        handlerChainMetaData.getHandlers().add(handlerMetaData);
                    }
                    serviceMetaData.getHandlerChains().add(handlerChainMetaData);
                }

                // add port refs
                final Map<QName, PortRefMetaData> portsByQName = new HashMap<>();
                for (final PortRefData portRef : serviceRef.getPortRefs()) {
                    final PortRefMetaData portRefMetaData = new PortRefMetaData();
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
                for (final PortAddress portAddress : portAddresses) {
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
            try {
                final Field field = object.getClass().getDeclaredField("invocationHandler");
                field.setAccessible(true);
                handler = (BaseEjbProxyHandler) field.get(object);
            } catch (Exception e1) {
                // Not a proxy.  See if it's serializable and send it
                if (object instanceof java.io.Serializable) {
                    res.setResponseCode(ResponseCodes.JNDI_OK);
                    res.setResult(object);
                    return;
                } else {
                    res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                    final NamingException namingException = new NamingException("Expected an ejb proxy, found unknown object: type=" +
                        object.getClass().getName() +
                        ", toString=" +
                        object);
                    res.setResult(new ThrowableArtifact(namingException));
                    return;
                }
            }
        }

        final ProxyInfo proxyInfo = handler.getProxyInfo();
        final BeanContext beanContext = proxyInfo.getBeanContext();
        final String deploymentID = beanContext.getDeploymentID().toString();

        updateServer(req, res, proxyInfo);

        switch (proxyInfo.getInterfaceType()) {
            case EJB_HOME: {
                res.setResponseCode(ResponseCodes.JNDI_EJBHOME);
                final EJBMetaDataImpl metaData = new EJBMetaDataImpl(beanContext.getHomeInterface(),
                    beanContext.getRemoteInterface(),
                    beanContext.getPrimaryKeyClass(),
                    beanContext.getComponentType().toString(),
                    deploymentID,
                    -1,
                    convert(proxyInfo.getInterfaceType()),
                    null,
                    beanContext.getAsynchronousMethodSignatures());
                metaData.loadProperties(beanContext.getProperties());
                log(metaData);
                res.setResult(metaData);
                break;
            }
            case EJB_LOCAL_HOME: {
                res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                final NamingException namingException = new NamingException("Not remotable: '" +
                    name +
                    "'. EJBLocalHome interfaces are not remotable as per the EJB specification.");
                res.setResult(new ThrowableArtifact(namingException));
                break;
            }
            case BUSINESS_REMOTE: {
                res.setResponseCode(ResponseCodes.JNDI_BUSINESS_OBJECT);
                final EJBMetaDataImpl metaData = new EJBMetaDataImpl(null,
                    null,
                    beanContext.getPrimaryKeyClass(),
                    beanContext.getComponentType().toString(),
                    deploymentID,
                    -1,
                    convert(proxyInfo.getInterfaceType()),
                    proxyInfo.getInterfaces(),
                    beanContext.getAsynchronousMethodSignatures());
                metaData.setPrimaryKey(proxyInfo.getPrimaryKey());
                metaData.loadProperties(beanContext.getProperties());

                log(metaData);
                res.setResult(metaData);
                break;
            }
            case BUSINESS_LOCAL: {
                res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                final NamingException namingException = new NamingException("Not remotable: '" +
                    name +
                    "'. Business Local interfaces are not remotable as per the EJB specification.  To disable this restriction, set the system property 'openejb.remotable.businessLocals=true' in the server.");
                res.setResult(new ThrowableArtifact(namingException));
                break;
            }
            case LOCALBEAN: {
                res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                final NamingException namingException = new NamingException("Not remotable: '" + name + "'. LocalBean classes are not remotable as per the EJB specification.");
                res.setResult(new ThrowableArtifact(namingException));
                break;
            }
            default: {
                res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
                final NamingException namingException = new NamingException("Not remotable: '" + name + "'.");
                res.setResult(new ThrowableArtifact(namingException));
            }
        }

    }

    private void log(final EJBMetaDataImpl metaData) {
        if (LOGGER.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Sending Ejb(");

            sb.append("deployment-id").append("=");
            sb.append(metaData.getDeploymentID());
            sb.append(", properties=[");
            final String delimiter = "|";
            for (final Map.Entry<Object, Object> entry : metaData.getProperties().entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(delimiter);
            }
            if (metaData.getProperties().size() > 1) {
                sb.delete(sb.length() - delimiter.length(), sb.length());
            }
            sb.append("])");
            LOGGER.debug(sb.toString());
        }
    }

    protected void updateServer(final JNDIRequest req, final JNDIResponse res, final ProxyInfo proxyInfo) {
        clusterableRequestHandler.updateServer(proxyInfo.getBeanContext(), req, res);
    }

    private void doList(final JNDIRequest req, final JNDIResponse res, final String prefix) {
        final String name = req.getRequestString();
        try {
            final NamingEnumeration<NameClassPair> namingEnumeration = rootContext.list(prefix + name);
            if (namingEnumeration == null) {
                res.setResponseCode(ResponseCodes.JNDI_OK);
                res.setResult(null);
            } else {
                res.setResponseCode(ResponseCodes.JNDI_ENUMERATION);
                final ArrayList<NameClassPair> list = Collections.list(namingEnumeration);
                for (final NameClassPair pair : list) {
                    if (pair.getClassName().equals(IvmContext.class.getName())) {
                        pair.setClassName(javax.naming.Context.class.getName());
                    }
                }
                res.setResult(new NameClassPairEnumeration(list));
            }
        } catch (NameNotFoundException e) {
            res.setResponseCode(ResponseCodes.JNDI_NOT_FOUND);
        } catch (NamingException e) {
            res.setResponseCode(ResponseCodes.JNDI_NAMING_EXCEPTION);
            res.setResult(new ThrowableArtifact(e));
        }
    }

    @SuppressWarnings("unchecked")
    public static class DbcpDataSource {

        private final Object object;
        private final Class clazz;

        public DbcpDataSource(final Object object) {
            clazz = object.getClass();
            this.object = object;
        }

        public java.lang.String getDriverClassName() throws Exception {
            try {
                return (String) clazz.getMethod("getDriverClassName").invoke(object);
            } catch (NoSuchMethodException nsme) {
                return (String) clazz.getMethod("getDriverClass").invoke(object);
            }
        }

        public java.lang.String getPassword() throws Exception {
            return (String) clazz.getMethod("getPassword").invoke(object);
        }

        public java.lang.String getUrl() throws Exception {
            try {
                return (String) clazz.getMethod("getUrl").invoke(object);
            } catch (NoSuchMethodException nsme) {
                return (String) clazz.getMethod("getJdbcUrl").invoke(object);
            }
        }

        public java.lang.String getUsername() throws Exception {
            return (String) clazz.getMethod("getUsername").invoke(object);
        }
    }
}

