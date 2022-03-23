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
package org.apache.openejb.client;

import org.w3c.dom.Element;

import jakarta.jws.WebService;
import jakarta.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.WebServiceFeature;
import jakarta.xml.ws.handler.HandlerResolver;
import jakarta.xml.ws.soap.SOAPBinding;
import jakarta.xml.ws.spi.Provider;
import jakarta.xml.ws.spi.ServiceDelegate;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JaxWsProviderWrapper extends Provider {

    private static final Logger logger = Logger.getLogger("OpenEJB.client");

    //
    // Magic to get our proider wrapper installed with the PortRefData
    //

    private static final ThreadLocal<ProviderWrapperData> threadPortRefs = new ThreadLocal<ProviderWrapperData>();
    private static final String JAXWSPROVIDER_PROPERTY = Provider.class.getName();

    public static void beforeCreate(final List<PortRefMetaData> portRefMetaDatas) {
        // Axis JAXWS api is non compliant and checks system property before classloader
        // so we replace system property so this wrapper is selected.  The original value
        // is saved into an openejb property so we can load the class in the find method
        final String oldProperty = System.getProperty(JAXWSPROVIDER_PROPERTY);
        if (oldProperty != null && !oldProperty.equals(JaxWsProviderWrapper.class.getName())) {
            System.setProperty("openejb." + JAXWSPROVIDER_PROPERTY, oldProperty);
        }

        if (oldProperty == null || !oldProperty.equals(JaxWsProviderWrapper.class.getName())) {
            System.setProperty(JAXWSPROVIDER_PROPERTY, JaxWsProviderWrapper.class.getName());
        }

        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        if (oldClassLoader != null) {
            Thread.currentThread().setContextClassLoader(new ProviderClassLoader(oldClassLoader));
        } else {
            Thread.currentThread().setContextClassLoader(new ProviderClassLoader());
        }
        threadPortRefs.set(new ProviderWrapperData(portRefMetaDatas, oldClassLoader));
    }

    public static void afterCreate() {
        Thread.currentThread().setContextClassLoader(threadPortRefs.get().callerClassLoader);
        threadPortRefs.set(null);
    }

    private static class ProviderWrapperData {

        private final List<PortRefMetaData> portRefMetaData;
        private final ClassLoader callerClassLoader;

        public ProviderWrapperData(final List<PortRefMetaData> portRefMetaDatas, final ClassLoader callerClassLoader) {
            this.portRefMetaData = portRefMetaDatas;
            this.callerClassLoader = callerClassLoader;
        }
    }

    //
    // Provider wappre implementation
    //

    private final Provider delegate;
    private final List<PortRefMetaData> portRefs;

    public JaxWsProviderWrapper() {
        delegate = findProvider();
        portRefs = threadPortRefs.get().portRefMetaData;
    }

    public Provider getDelegate() {
        return delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServiceDelegate createServiceDelegate(final URL wsdlDocumentLocation, final QName serviceName, final Class serviceClass) {
        ServiceDelegate serviceDelegate = delegate.createServiceDelegate(wsdlDocumentLocation, serviceName, serviceClass);
        serviceDelegate = new ServiceDelegateWrapper(serviceDelegate);
        return serviceDelegate;
    }

    @Override
    public Endpoint createEndpoint(final String bindingId, final Object implementor) {
        return delegate.createEndpoint(bindingId, implementor);
    }

    @Override
    public Endpoint createAndPublishEndpoint(final String address, final Object implementor) {
        return delegate.createAndPublishEndpoint(address, implementor);
    }

    @Override
    public W3CEndpointReference createW3CEndpointReference(final String address,
                                                           final QName serviceName,
                                                           final QName portName,
                                                           final List<Element> metadata,
                                                           final String wsdlDocumentLocation,
                                                           final List<Element> referenceParameters) {

        return (W3CEndpointReference) invoke21Delegate(delegate, createW3CEndpointReference,
            address,
            serviceName,
            portName,
            metadata,
            wsdlDocumentLocation,
            referenceParameters);
    }

    @Override
    public EndpointReference readEndpointReference(final Source source) {
        return (EndpointReference) invoke21Delegate(delegate, readEndpointReference, source);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T getPort(final EndpointReference endpointReference, final Class<T> serviceEndpointInterface, final WebServiceFeature... features) {
        return (T) invoke21Delegate(delegate, providerGetPort, endpointReference, serviceEndpointInterface, features);
    }

    private class ServiceDelegateWrapper extends ServiceDelegate {

        private final ServiceDelegate serviceDelegate;

        public ServiceDelegateWrapper(final ServiceDelegate serviceDelegate) {
            this.serviceDelegate = serviceDelegate;
        }

        @Override
        public <T> T getPort(final QName portName, final Class<T> serviceEndpointInterface) {
            final T t = serviceDelegate.getPort(portName, serviceEndpointInterface);
            setProperties((BindingProvider) t, portName);
            return t;
        }

        @Override
        public <T> T getPort(final Class<T> serviceEndpointInterface) {
            final T t = serviceDelegate.getPort(serviceEndpointInterface);

            QName qname = null;
            if (serviceEndpointInterface.isAnnotationPresent(WebService.class)) {
                final WebService webService = serviceEndpointInterface.getAnnotation(WebService.class);
                final String targetNamespace = webService.targetNamespace();
                final String name = webService.name();
                if (targetNamespace != null && targetNamespace.length() > 0 && name != null && name.length() > 0) {
                    qname = new QName(targetNamespace, name);
                }
            }

            setProperties((BindingProvider) t, qname);
            return t;
        }

        @Override
        public void addPort(final QName portName, final String bindingId, final String endpointAddress) {
            serviceDelegate.addPort(portName, bindingId, endpointAddress);
        }

        @Override
        public <T> Dispatch<T> createDispatch(final QName portName, final Class<T> type, final Service.Mode mode) {
            final Dispatch<T> dispatch = serviceDelegate.createDispatch(portName, type, mode);
            setProperties(dispatch, portName);
            return dispatch;
        }

        @Override
        public Dispatch<Object> createDispatch(final QName portName, final JAXBContext context, final Service.Mode mode) {
            final Dispatch<Object> dispatch = serviceDelegate.createDispatch(portName, context, mode);
            setProperties(dispatch, portName);
            return dispatch;
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public <T> Dispatch<T> createDispatch(final QName portName, final Class<T> type, final Service.Mode mode, final WebServiceFeature... features) {
            return (Dispatch<T>) invoke21Delegate(serviceDelegate, createDispatchInterface,
                portName,
                type,
                mode,
                features);
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public Dispatch<java.lang.Object> createDispatch(final QName portName, final JAXBContext context, final Service.Mode mode, final WebServiceFeature... features) {
            return (Dispatch<Object>) invoke21Delegate(serviceDelegate, createDispatchJaxBContext,
                portName,
                context,
                mode,
                features);
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public Dispatch<Object> createDispatch(
            final EndpointReference endpointReference,
            final JAXBContext context,
            final Service.Mode mode,
            final WebServiceFeature... features) {
            return (Dispatch<Object>) invoke21Delegate(serviceDelegate, createDispatchReferenceJaxB,
                endpointReference,
                context,
                mode,
                features);
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public <T> Dispatch<T> createDispatch(final EndpointReference endpointReference,
                                              final java.lang.Class<T> type,
                                              final Service.Mode mode,
                                              final WebServiceFeature... features) {
            return (Dispatch<T>) invoke21Delegate(serviceDelegate, createDispatchReferenceClass,
                endpointReference,
                type,
                mode,
                features);

        }

        @Override
        @SuppressWarnings({"unchecked"})
        public <T> T getPort(final QName portName, final Class<T> serviceEndpointInterface, final WebServiceFeature... features) {
            return (T) invoke21Delegate(serviceDelegate, serviceGetPortByQName,
                portName,
                serviceEndpointInterface,
                features);
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public <T> T getPort(final EndpointReference endpointReference, final Class<T> serviceEndpointInterface, final WebServiceFeature... features) {
            return (T) invoke21Delegate(serviceDelegate, serviceGetPortByEndpointReference,
                endpointReference,
                serviceEndpointInterface,
                features);
        }

        @Override
        @SuppressWarnings({"unchecked"})
        public <T> T getPort(final Class<T> serviceEndpointInterface, final WebServiceFeature... features) {
            return (T) invoke21Delegate(serviceDelegate, serviceGetPortByInterface,
                serviceEndpointInterface,
                features);
        }

        @Override
        public QName getServiceName() {
            return serviceDelegate.getServiceName();
        }

        @Override
        public Iterator<QName> getPorts() {
            return serviceDelegate.getPorts();
        }

        @Override
        public URL getWSDLDocumentLocation() {
            return serviceDelegate.getWSDLDocumentLocation();
        }

        @Override
        public HandlerResolver getHandlerResolver() {
            return serviceDelegate.getHandlerResolver();
        }

        @Override
        public void setHandlerResolver(final HandlerResolver handlerResolver) {
            serviceDelegate.setHandlerResolver(handlerResolver);
        }

        @Override
        public Executor getExecutor() {
            return serviceDelegate.getExecutor();
        }

        @Override
        public void setExecutor(final Executor executor) {
            serviceDelegate.setExecutor(executor);
        }

        private void setProperties(final BindingProvider proxy, final QName qname) {
            for (final PortRefMetaData portRef : portRefs) {
                Class<?> intf = null;
                if (portRef.getServiceEndpointInterface() != null) {
                    try {
                        intf = proxy.getClass().getClassLoader().loadClass(portRef.getServiceEndpointInterface());
                    } catch (ClassNotFoundException e) {
                        logger.log(Level.INFO, "Not loading: " + portRef.getServiceEndpointInterface());
                    }
                }
                if ((qname != null && qname.equals(portRef.getQName())) || (intf != null && intf.isInstance(proxy))) {
                    // set address
                    if (!portRef.getAddresses().isEmpty()) {
                        proxy.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, portRef.getAddresses().get(0));
                    }

                    // set mtom
                    final boolean enableMTOM = portRef.isEnableMtom();
                    if (enableMTOM && proxy.getBinding() instanceof SOAPBinding) {
                        ((SOAPBinding) proxy.getBinding()).setMTOMEnabled(enableMTOM);
                    }

                    // set properties
                    for (final Map.Entry<Object, Object> entry : portRef.getProperties().entrySet()) {
                        final String name = (String) entry.getKey();
                        final String value = (String) entry.getValue();
                        proxy.getRequestContext().put(name, value);
                    }

                    return;
                }
            }
        }
    }

    private static Provider findProvider() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassLoader.getSystemClassLoader();
        }

        // 0. System.getProperty("openejb.jakarta.xml.ws.spi.Provider")
        // This is so those using old axis rules still work as expected
        String providerClass = System.getProperty("openejb." + JAXWSPROVIDER_PROPERTY);
        Provider provider = createProviderInstance(providerClass, classLoader);
        if (provider != null) {
            return provider;
        }

        // 1. META-INF/services/jakarta.xml.ws.spi.Provider
        try {
            for (final URL url : Collections.list(classLoader.getResources("META-INF/services/" + JAXWSPROVIDER_PROPERTY))) {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(url.openStream()));

                    providerClass = in.readLine();
                    provider = createProviderInstance(providerClass, classLoader);
                    if (provider != null) {
                        return provider;
                    }
                } catch (Exception ignored) {
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable e) {
                            //ignore
                        }
                    }
                }
            }
        } catch (Exception ingored) {
            logger.log(Level.INFO, "No META-INF/services/jakarta.xml.ws.spi.Provider found");
        }

        // 2. $java.home/lib/jaxws.properties
        final String javaHome = System.getProperty("java.home");
        final File jaxrpcPropertiesFile = new File(new File(javaHome, "lib"), "jaxrpc.properties");
        if (jaxrpcPropertiesFile.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(jaxrpcPropertiesFile);
                final Properties properties = new Properties();
                properties.load(in);

                providerClass = properties.getProperty(JAXWSPROVIDER_PROPERTY);
                provider = createProviderInstance(providerClass, classLoader);
                if (provider != null) {
                    return provider;
                }
            } catch (Exception ignored) {
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Throwable e) {
                        //Ignore
                    }
                }
            }
        }

        // 3. System.getProperty("jakarta.xml.ws.spi.Provider")
        providerClass = System.getProperty(JAXWSPROVIDER_PROPERTY);
        provider = createProviderInstance(providerClass, classLoader);
        if (provider != null) {
            return provider;
        }

        // 4. Use jakarta.xml.ws.spi.Provider default
        try {
            // disable the OpenEJB JaxWS provider
            if (classLoader instanceof ProviderClassLoader) {
                ((ProviderClassLoader) classLoader).enabled = false;
            }
            System.getProperties().remove(JAXWSPROVIDER_PROPERTY);

            provider = Provider.provider();
            if (provider != null && !provider.getClass().getName().equals(JaxWsProviderWrapper.class.getName())) {
                return provider;
            }
        } finally {
            // reenable the OpenEJB JaxWS provider
            System.setProperty(JAXWSPROVIDER_PROPERTY, providerClass);
            if (classLoader instanceof ProviderClassLoader) {
                ((ProviderClassLoader) classLoader).enabled = true;
            }
        }

        throw new WebServiceException("No " + JAXWSPROVIDER_PROPERTY + " implementation found");
    }

    private static Provider createProviderInstance(final String providerClass, final ClassLoader classLoader) {
        if (providerClass != null && providerClass.length() > 0 && !providerClass.equals(JaxWsProviderWrapper.class.getName())) {
            try {
                final Class<? extends Provider> clazz = classLoader.loadClass(providerClass).asSubclass(Provider.class);
                return clazz.newInstance();
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Unable to construct provider implementation " + providerClass, e);
            }
        }
        return null;
    }

    private static class ProviderClassLoader extends ClassLoader {

        private static final String PROVIDER_RESOURCE = "META-INF/services/" + JAXWSPROVIDER_PROPERTY;
        private static final URL PROVIDER_URL;

        static {

            File tempFile = null;

            try {

                try {
                    tempFile = File.createTempFile("openejb-jaxws-provider", "tmp");
                } catch (Throwable e) {
                    final File dir = new File("tmp");
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new Exception("Failed to create: " + dir.getAbsolutePath());
                    }
                    tempFile = File.createTempFile("openejb-jaxws-provider", "tmp", dir);
                }

                tempFile.deleteOnExit();

                OutputStream out = null;
                try {
                    out = new FileOutputStream(tempFile);
                    out.write(JaxWsProviderWrapper.class.getName().getBytes());
                } finally {
                    if (null != out) {
                        try {
                            out.close();
                        } catch (Throwable e) {
                            //Ignore
                        }
                    }
                }

                out.close();
                PROVIDER_URL = tempFile.toURI().toURL();

            } catch (Throwable e) {
                throw new ClientRuntimeException("Failed to create openejb-jaxws-provider file: " + tempFile, e);
            }
        }

        public boolean enabled = true;

        public ProviderClassLoader() {
        }

        public ProviderClassLoader(final ClassLoader parent) {
            super(parent);
        }

        @Override
        public Enumeration<URL> getResources(final String name) throws IOException {
            Enumeration<URL> resources = super.getResources(name);
            if (enabled && PROVIDER_RESOURCE.equals(name)) {
                final ArrayList<URL> list = new ArrayList<URL>();
                list.add(PROVIDER_URL);
                list.addAll(Collections.list(resources));
                resources = Collections.enumeration(list);
            }
            return resources;
        }

        @Override
        public URL getResource(final String name) {
            if (enabled && PROVIDER_RESOURCE.equals(name)) {
                return PROVIDER_URL;
            }
            return super.getResource(name);
        }
    }

    //
    // Delegate methods for JaxWS 2.1
    //

    private static Object invoke21Delegate(final Object delegate, final Method method, final Object... args) {
        if (method == null) {
            throw new UnsupportedOperationException("JaxWS 2.1 APIs are not supported");
        }
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException e) {
            throw new WebServiceException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw new WebServiceException(e.getCause());
            }
            throw new WebServiceException(e);
        }
    }

    // Provider methods
    private static final Method createW3CEndpointReference;
    private static final Method providerGetPort;
    private static final Method readEndpointReference;

    // ServiceDelegate methods
    private static final Method createDispatchReferenceJaxB;
    private static final Method createDispatchReferenceClass;
    private static final Method createDispatchInterface;
    private static final Method createDispatchJaxBContext;
    private static final Method serviceGetPortByEndpointReference;
    private static final Method serviceGetPortByQName;
    private static final Method serviceGetPortByInterface;

    static {
        Method method = null;
        try {
            method = Provider.class.getMethod("createW3CEndpointReference",
                String.class,
                QName.class,
                QName.class,
                List.class,
                String.class,
                List.class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        createW3CEndpointReference = method;

        method = null;
        try {
            method = Provider.class.getMethod("getPort",
                EndpointReference.class,
                Class.class,
                WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        providerGetPort = method;

        method = null;
        try {
            method = Provider.class.getMethod("readEndpointReference", Source.class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        readEndpointReference = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("createDispatch",
                EndpointReference.class,
                JAXBContext.class,
                Service.Mode.class,
                WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        createDispatchReferenceJaxB = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("createDispatch",
                EndpointReference.class,
                Class.class,
                Service.Mode.class,
                WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        createDispatchReferenceClass = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("createDispatch",
                QName.class,
                JAXBContext.class,
                Service.Mode.class,
                WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        createDispatchJaxBContext = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("createDispatch",
                QName.class,
                Class.class,
                Service.Mode.class,
                WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        createDispatchInterface = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("getPort",
                EndpointReference.class,
                Class.class,
                WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        serviceGetPortByEndpointReference = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("getPort",
                QName.class,
                Class.class,
                WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        serviceGetPortByQName = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("getPort",
                Class.class,
                WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
            //Ignore
        }
        serviceGetPortByInterface = method;
    }
}
