/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.ext.ResourceComparator;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.MethodDispatcher;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.ProviderInfo;
import org.apache.cxf.jaxrs.model.wadl.WadlGenerator;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.transport.servlet.BaseUrlHelper;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.api.internal.Internal;
import org.apache.openejb.api.jmx.Description;
import org.apache.openejb.api.jmx.MBean;
import org.apache.openejb.api.jmx.ManagedAttribute;
import org.apache.openejb.api.jmx.ManagedOperation;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.assembler.classic.util.ServiceInfos;
import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpRequestImpl;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.httpd.ServletRequestAdapter;
import org.apache.openejb.server.rest.EJBRestServiceInfo;
import org.apache.openejb.server.rest.RsHttpListener;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.ProxyEJB;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.management.ObjectName;
import javax.management.openmbean.TabularData;
import javax.naming.Context;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class CxfRsHttpListener implements RsHttpListener {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RS, CxfRsHttpListener.class);

    private static final java.util.logging.Logger SERVER_IMPL_LOGGER = LogUtils.getL7dLogger(ServerImpl.class);

    public static final String CXF_JAXRS_PREFIX = "cxf.jaxrs.";
    public static final String PROVIDERS_KEY = CXF_JAXRS_PREFIX + "providers";
    public static final String STATIC_RESOURCE_KEY = CXF_JAXRS_PREFIX + "static-resources-list";
    public static final String STATIC_SUB_RESOURCE_RESOLUTION_KEY = "staticSubresourceResolution";
    public static final String RESOURCE_COMPARATOR_KEY = CXF_JAXRS_PREFIX + "resourceComparator";

    public static final boolean TRY_STATIC_RESOURCES = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.jaxrs.static-first", "true"));
    private static final String GLOBAL_PROVIDERS = SystemInstance.get().getProperty(PROVIDERS_KEY);

    private static final Map<String, String> STATIC_CONTENT_TYPES;
    private static final String[] DEFAULT_WELCOME_FILES = new String[]{ "/index.html", "/index.htm" };

    private final HTTPTransportFactory transportFactory;
    private final String wildcard;
    private AbstractHTTPDestination destination;
    private Server server;
    private String context = "";
    private String servlet = "";
    private final Collection<Pattern> staticResourcesList = new CopyOnWriteArrayList<Pattern>();
    private final List<ObjectName> jmxNames = new ArrayList<ObjectName>();
    private final Collection<CreationalContext<?>> toRelease = new LinkedHashSet<CreationalContext<?>>();

    private static final char[] URL_SEP = new char[] { '?', '#', ';' };

    static {
        STATIC_CONTENT_TYPES = new HashMap<String, String>();
        STATIC_CONTENT_TYPES.put("html", "text/html");
        STATIC_CONTENT_TYPES.put("xhtml", "text/html");
        STATIC_CONTENT_TYPES.put("txt", "text/plain");
        STATIC_CONTENT_TYPES.put("css", "text/css");
        STATIC_CONTENT_TYPES.put("jpg", "image/jpg");
        STATIC_CONTENT_TYPES.put("png", "image/png");
        STATIC_CONTENT_TYPES.put("ico", "image/ico");
        STATIC_CONTENT_TYPES.put("pdf", "application/pdf");
        STATIC_CONTENT_TYPES.put("xsd", "application/xml");

        for (final ProviderInfo<RequestHandler> rh : org.apache.cxf.jaxrs.provider.ProviderFactory.getSharedInstance().getRequestHandlers()) {
            final RequestHandler provider = rh.getProvider();
            if (WadlGenerator.class.isInstance(provider)) {
                final WadlGenerator wadlGenerator = WadlGenerator.class.cast(provider);
                final String ignoreRequests = SystemInstance.get().getProperty("openejb.cxf-rs.wadl-generator.ignoreRequests");
                final String ignoreMessageWriters = SystemInstance.get().getProperty("openejb.cxf-rs.wadl-generator.ignoreMessageWriters", "false");

                if (ignoreRequests != null) {
                    wadlGenerator.setIgnoreRequests(Boolean.parseBoolean(ignoreRequests));
                }
                // CXF-5319: bug in CXF? it prevents to get the wadl as json otherwise
                if (ignoreMessageWriters != null) {
                    wadlGenerator.setIgnoreMessageWriters(Boolean.parseBoolean(ignoreMessageWriters));
                }
            }
        }
    }

    private String pattern;

    public CxfRsHttpListener(final HTTPTransportFactory httpTransportFactory, final String star) {
        transportFactory = httpTransportFactory;
        wildcard = star;
    }

    public void setUrlPattern(final String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void onMessage(final HttpRequest httpRequest, final HttpResponse httpResponse) throws Exception {
        // fix the address (to manage multiple connectors)
        if (HttpRequestImpl.class.isInstance(httpRequest)) {
            final HttpRequestImpl requestImpl = HttpRequestImpl.class.cast(httpRequest);
            requestImpl.initPathFromContext(context);
            requestImpl.initServletPath(servlet);
        }

        boolean matchedStatic = false;
        if (TRY_STATIC_RESOURCES || (matchedStatic = matchPath(httpRequest))) {
            final String pathInfo = httpRequest.getPathInfo();
            if (serveStaticContent(httpRequest, httpResponse, pathInfo)) {
                if (matchedStatic) { // we should have gotten the resource
                    throw new ServletException("Static resource " + pathInfo + " is not available");
                }
                return; // ok that's a surely rest service
            }
        }

        doInvoke(httpRequest, httpResponse);
    }

    // normal endpoint without static resource handling
    public void doInvoke(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
        String baseURL = BaseUrlHelper.getBaseURL(pattern != null ? new ServletRequestAdapter(httpRequest) {
            @Override // we have a filter so we need the computed servlet path to not break CXF
            public String getServletPath() {
                return pattern;
            }
        } : httpRequest);
        if (!baseURL.endsWith("/")) {
            baseURL += "/";
        }
        httpRequest.setAttribute("org.apache.cxf.transport.endpoint.address", baseURL);

        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.initBusLoader());
        try {
            destination.invoke(null, httpRequest.getServletContext(), httpRequest, httpResponse);
        } finally {
            CxfUtil.clearBusLoader(oldLoader);
        }
    }

    public boolean matchPath(final HttpServletRequest request) {
        if (staticResourcesList.isEmpty()) {
            return false;
        }

        String path = request.getPathInfo();
        if (path == null) {
            path = "/";
        }
        for (final Pattern pattern : staticResourcesList) {
            if (pattern.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }

    public InputStream findStaticContent(final HttpServletRequest request, final String[] welcomeFiles) throws ServletException {
        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
        for (final char c : URL_SEP) {
            final int indexOf = pathInfo.indexOf(c);
            if (indexOf > 0) {
                pathInfo = pathInfo.substring(0, indexOf);
            }
        }
        InputStream is = request.getServletContext().getResourceAsStream(pathInfo);
        if (is == null && ("/".equals(pathInfo) || pathInfo.isEmpty())) {
            for (final String n : welcomeFiles) {
                is = request.getServletContext().getResourceAsStream(n);
                if (is != null) {
                    break;
                }
            }
        }
        return is;
    }

    public boolean serveStaticContent(final HttpServletRequest request,
                                      final HttpServletResponse response,
                                      final String pathInfo) throws ServletException {
        final InputStream is = findStaticContent(request, DEFAULT_WELCOME_FILES);
        if (is == null) {
            return false;
        }
        try {
            final int ind = pathInfo.lastIndexOf(".");
            if (ind != -1 && ind < pathInfo.length()) {
                final String type = STATIC_CONTENT_TYPES.get(pathInfo.substring(ind + 1));
                if (type != null) {
                    response.setContentType(type);
                }
            }

            final ServletOutputStream os = response.getOutputStream();
            IOUtils.copy(is, os);
            os.flush();
            response.setStatus(HttpURLConnection.HTTP_OK);
        } catch (final IOException ex) {
            throw new ServletException("Static resource " + pathInfo + " can not be written to the output stream");
        }
        return true;
    }

    @Deprecated
    @Override
    public void deploySingleton(final String contextRoot, final String fullContext, final Object o, final Application appInstance,
                                final Collection<Object> additionalProviders, final ServiceConfiguration configuration) {
        deploy(contextRoot, o.getClass(), fullContext, new SingletonResourceProvider(o),
                o, appInstance, null, additionalProviders, configuration, null);
    }

    @Deprecated
    @Override
    public void deployPojo(final ClassLoader loader,
                           final String contextRoot,
                           final String fullContext,
                           final Class<?> loadedClazz,
                           final Application app,
                           final Collection<Injection> injections,
                           final Context context,
                           final WebBeansContext owbCtx,
                           final Collection<Object> additionalProviders,
                           final ServiceConfiguration configuration) {
        deploy(contextRoot, loadedClazz, fullContext, new OpenEJBPerRequestPojoResourceProvider(loader, loadedClazz, injections, context, owbCtx),
            null, app, null, additionalProviders, configuration, null);
    }

    @Deprecated
    @Override
    public void deployEJB(final String contextRoot,
                          final String fullContext,
                          final BeanContext beanContext,
                          final Collection<Object> additionalProviders,
                          final ServiceConfiguration configuration) {
        final Object proxy = ProxyEJB.subclassProxy(beanContext);

        deploy(contextRoot, beanContext.getBeanClass(), fullContext, new NoopResourceProvider(beanContext.getBeanClass(), proxy),
                proxy, null, new OpenEJBEJBInvoker(Collections.singleton(beanContext)), additionalProviders, configuration, beanContext.getWebBeansContext());
    }

    private void deploy(final String contextRoot, final Class<?> clazz, final String address, final ResourceProvider rp, final Object serviceBean,
                        final Application app, final Invoker invoker, final Collection<Object> additionalProviders, final ServiceConfiguration configuration,
                        final WebBeansContext webBeansContext) {
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.initBusLoader());
        try {
            final JAXRSServerFactoryBean factory = newFactory(address);
            configureFactory(additionalProviders, configuration, factory, webBeansContext);
            factory.setResourceClasses(clazz);
            context = contextRoot;
            if (context == null) {
                context = "";
            }
            if (!context.startsWith("/")) {
                context = "/" + context;
            }

            if (rp != null) {
                factory.setResourceProvider(rp);
            }
            if (app != null) {
                factory.setApplication(app);
            }
            if (invoker != null) {
                factory.setInvoker(invoker);
            }
            if (serviceBean != null) {
                factory.setServiceBean(serviceBean);
            } else {
                factory.setServiceClass(clazz);
            }

            server = factory.create();
            destination = (AbstractHTTPDestination) server.getDestination();
        } finally {
            if (oldLoader != null) {
                CxfUtil.clearBusLoader(oldLoader);
            }
        }
    }

    private Collection<Object> providers(final Collection<ServiceInfo> services, final Collection<Object> additionalProviders, final WebBeansContext ctx) {
        final Collection<Object> instances = new ArrayList<Object>();
        final BeanManagerImpl bm = ctx == null ? null : ctx.getBeanManagerImpl();
        for (final Object o : additionalProviders) {
            if (o instanceof Class<?>) {
                final Class<?> clazz = (Class<?>) o;
                final String name = clazz.getName();
                if (shouldSkipProvider(name)) {
                    continue;
                }

                if (bm != null && bm.isInUse()) {
                    try {
                        final Set<Bean<?>> beans = bm.getBeans(clazz);
                        if (beans != null && !beans.isEmpty()) {
                            final Bean<?> bean = bm.resolve(beans);
                            final CreationalContextImpl<?> creationalContext = bm.createCreationalContext(bean);
                            instances.add(bm.getReference(bean, clazz, creationalContext));
                            toRelease.add(creationalContext);
                            continue;
                        }
                    } catch (final Throwable th) {
                        LOGGER.info("Can't use CDI to create provider " + name);
                    }
                }

                final Collection<Object> instance = ServiceInfos.resolve(services, new String[]{name}, ProviderFactory.INSTANCE);
                if (instance != null && !instance.isEmpty()) {
                    instances.add(instance.iterator().next());
                } else {
                    try {
                        instances.add(newProvider(clazz));
                    } catch (final Exception e) {
                        LOGGER.error("can't instantiate " + name, e);
                    }
                }
            } else {
                final String name = o.getClass().getName();
                if (shouldSkipProvider(name)) {
                    continue;
                }
                instances.add(o);
            }
        }

        return instances;
    }

    private static boolean shouldSkipProvider(final String name) {
        return "false".equalsIgnoreCase(SystemInstance.get().getProperty(name + ".activated", "true"))
                || name.startsWith("org.apache.wink.common.internal.");
    }

    private static void addMandatoryProviders(final Collection<Object> instances) {
        instances.add(EJBAccessExceptionMapper.INSTANCE);
    }

    private Object newProvider(final Class<?> clazz) throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }

    @Override
    public void undeploy() {
        // unregister all MBeans
        for (final ObjectName objectName : jmxNames) {
            LocalMBeanServer.unregisterSilently(objectName);
        }

        for (final CreationalContext<?> cc : toRelease) {
            try {
                cc.release();
            } catch (final Exception e) {
                LOGGER.warning(e.getMessage(), e);
            }
        }

        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.initBusLoader());
        try {
            server.destroy();
        } catch (final RuntimeException ise) {
            LOGGER.warning("Can't stop correctly the endpoint " + server);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(ise.getMessage(), ise);
            }
        } finally {
            if (oldLoader != null) {
                CxfUtil.clearBusLoader(oldLoader);
            }
        }
    }

    @Override
    public void deployApplication(final Application application, final String prefix, final String webContext,
                                  final Collection<Object> additionalProviders,
                                  final Map<String, EJBRestServiceInfo> restEjbs, final ClassLoader classLoader,
                                  final Collection<Injection> injections, final Context context, final WebBeansContext owbCtx,
                                  final ServiceConfiguration serviceConfiguration) {
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.initBusLoader());
        try {
            final JAXRSServerFactoryBean factory = newFactory(prefix);
            configureFactory(additionalProviders, serviceConfiguration, factory, owbCtx);
            factory.setApplication(application);

            final List<Class<?>> classes = new ArrayList<Class<?>>();

            for (final Class<?> clazz : application.getClasses()) {
                if (!additionalProviders.contains(clazz) && !clazz.isInterface()) {
                    classes.add(clazz);
                }
            }

            for (final Object o : application.getSingletons()) {
                if (!additionalProviders.contains(o)) {
                    final Class<?> clazz = o.getClass();
                    classes.add(clazz);
                }
            }

            for (final Class<?> clazz : classes) {
                String name = clazz.getName();
                EJBRestServiceInfo restServiceInfo = restEjbs.get(name);

                if (name.endsWith(DynamicSubclass.IMPL_SUFFIX)) {
                    name = name.substring(0, name.length() - DynamicSubclass.IMPL_SUFFIX.length());
                    restServiceInfo = restEjbs.get(name);
                    if (restServiceInfo != null) { // AutoJAXRSInvoker relies on it
                        restEjbs.put(clazz.getName(), restServiceInfo);
                    }
                }

                if (restServiceInfo != null) {
                    final Object proxy = ProxyEJB.subclassProxy(restServiceInfo.context);
                    factory.setResourceProvider(clazz, new NoopResourceProvider(restServiceInfo.context.getBeanClass(), proxy));
                } else {
                    factory.setResourceProvider(clazz, new OpenEJBPerRequestPojoResourceProvider(classLoader, clazz, injections, context, owbCtx));
                }
            }

            factory.setResourceClasses(classes);
            factory.setInvoker(new AutoJAXRSInvoker(restEjbs));

            final Level level = SERVER_IMPL_LOGGER.getLevel();
            try {
                SERVER_IMPL_LOGGER.setLevel(Level.OFF);
            } catch (final UnsupportedOperationException e) {
                //ignore
            }
            try {
                server = factory.create();
            } finally {
                try {
                    SERVER_IMPL_LOGGER.setLevel(level);
                } catch (final UnsupportedOperationException e) {
                    //ignore
                }
            }

            this.context = webContext;
            if (!webContext.startsWith("/")) {
                this.context = "/" + webContext;
            }
            final int servletIdx = 1 + this.context.substring(1).indexOf('/');
            if (servletIdx > 0) {
                this.servlet = this.context.substring(servletIdx);
                this.context = this.context.substring(0, servletIdx);
            }
            destination = (AbstractHTTPDestination) server.getDestination();

            final String base;
            if (prefix.endsWith("/")) {
                base = prefix.substring(0, prefix.length() - 1);
            } else if (prefix.endsWith(wildcard)) {
                base = prefix.substring(0, prefix.length() - wildcard.length());
            } else {
                base = prefix;
            }

            // stack info to log to get nice logs
            logEndpoints(application, prefix, restEjbs, factory, base);
        } finally {
            if (oldLoader != null) {
                CxfUtil.clearBusLoader(oldLoader);
            }
        }
    }

    private void logEndpoints(final Application application, final String prefix,
                              final Map<String, EJBRestServiceInfo> restEjbs,
                              final JAXRSServerFactoryBean factory, final String base) {
        final List<Logs.LogResourceEndpointInfo> resourcesToLog = new ArrayList<Logs.LogResourceEndpointInfo>();
        int classSize = 0;
        int addressSize = 0;

        final JAXRSServiceImpl service = (JAXRSServiceImpl) factory.getServiceFactory().getService();
        final List<ClassResourceInfo> resources = service.getClassResourceInfos();
        for (final ClassResourceInfo info : resources) {
            if (info.getResourceClass() == null) { // possible?
                continue;
            }

            final String address = Logs.singleSlash(base, info.getURITemplate().getValue());

            final String clazz = info.getResourceClass().getName();
            final String type;
            if (restEjbs.containsKey(clazz)) {
                type = "EJB";
            } else {
                type = "Pojo";
            }

            classSize = Math.max(classSize, clazz.length());
            addressSize = Math.max(addressSize, address.length());

            int methodSize = 7;
            int methodStrSize = 0;

            final List<Logs.LogOperationEndpointInfo> toLog = new ArrayList<Logs.LogOperationEndpointInfo>();

            final MethodDispatcher md = info.getMethodDispatcher();
            for (final OperationResourceInfo ori : md.getOperationResourceInfos()) {
                final String httpMethod = ori.getHttpMethod();
                final String currentAddress = Logs.singleSlash(address, ori.getURITemplate().getValue());
                final String methodToStr = Logs.toSimpleString(ori.getMethodToInvoke());
                toLog.add(new Logs.LogOperationEndpointInfo(httpMethod, currentAddress, methodToStr));

                if (httpMethod != null) {
                    methodSize = Math.max(methodSize, httpMethod.length());
                }
                addressSize = Math.max(addressSize, currentAddress.length());
                methodStrSize = Math.max(methodStrSize, methodToStr.length());
            }

            Collections.sort(toLog);

            resourcesToLog.add(new Logs.LogResourceEndpointInfo(type, address, clazz, toLog, methodSize, methodStrSize));
        }

        // effective logging

        LOGGER.info("REST Application: " + Logs.forceLength(prefix, addressSize, true) + " -> " + application.getClass().getName());

        Collections.sort(resourcesToLog);

        for (final Logs.LogResourceEndpointInfo resource : resourcesToLog) {

            // Init and register MBeans
            final ObjectNameBuilder jmxName = new ObjectNameBuilder("openejb.management")
                .set("j2eeType", "JAX-RS")
                .set("J2EEServer", "openejb")
                .set("J2EEApplication", base)
                .set("EndpointType", resource.type)
                .set("name", resource.classname);

            final ObjectName jmxObjectName = jmxName.build();
            LocalMBeanServer.registerDynamicWrapperSilently(
                new RestServiceMBean(resource),
                jmxObjectName);

            jmxNames.add(jmxObjectName);

            LOGGER.info("     Service URI: "
                + Logs.forceLength(resource.address, addressSize, true) + " -> "
                + Logs.forceLength(resource.type, 4, false) + " "
                + Logs.forceLength(resource.classname, classSize, true));

            for (final Logs.LogOperationEndpointInfo log : resource.operations) {
                LOGGER.info("          "
                    + Logs.forceLength(log.http, resource.methodSize, false) + " "
                    + Logs.forceLength(log.address, addressSize, true) + " ->      "
                    + Logs.forceLength(log.method, resource.methodStrSize, true));
            }

            resource.operations.clear();
        }
        resourcesToLog.clear();
    }

    private JAXRSServerFactoryBean newFactory(final String prefix) {
        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setDestinationFactory(transportFactory);
        factory.setBus(transportFactory.getBus());
        factory.setAddress(prefix);
        return factory;
    }

    private void configureFactory(final Collection<Object> givenAdditionalProviders,
                                  final ServiceConfiguration serviceConfiguration,
                                  final JAXRSServerFactoryBean factory,
                                  final WebBeansContext ctx) {
        CxfUtil.configureEndpoint(factory, serviceConfiguration, CXF_JAXRS_PREFIX);

        final Collection<ServiceInfo> services = serviceConfiguration.getAvailableServices();

        final String staticSubresourceResolution = serviceConfiguration.getProperties().getProperty(CXF_JAXRS_PREFIX + STATIC_SUB_RESOURCE_RESOLUTION_KEY);
        if (staticSubresourceResolution != null) {
            factory.setStaticSubresourceResolution("true".equalsIgnoreCase(staticSubresourceResolution));
        }

        // resource comparator
        final String resourceComparator = serviceConfiguration.getProperties().getProperty(RESOURCE_COMPARATOR_KEY);
        if (resourceComparator != null) {
            try {
                ResourceComparator instance = (ResourceComparator) ServiceInfos.resolve(services, resourceComparator);
                if (instance == null) {
                    instance = (ResourceComparator) Thread.currentThread().getContextClassLoader()
                        .loadClass(resourceComparator).newInstance();
                }
                factory.setResourceComparator(instance);
            } catch (final Exception e) {
                LOGGER.error("Can't create the resource comparator " + resourceComparator, e);
            }
        }

        // static resources
        final String staticResources = serviceConfiguration.getProperties().getProperty(STATIC_RESOURCE_KEY);
        if (staticResources != null) {
            final String[] resources = staticResources.split(",");
            for (final String r : resources) {
                final String trimmed = r.trim();
                if (!trimmed.isEmpty()) {
                    staticResourcesList.add(Pattern.compile(trimmed));
                }
            }
        }

        // providers
        Set<String> providersConfig = null;

        {
            final String provider = serviceConfiguration.getProperties().getProperty(PROVIDERS_KEY);
            if (provider != null) {
                providersConfig = new HashSet<String>();
                for (final String p : Arrays.asList(provider.split(","))) {
                    providersConfig.add(p.trim());
                }
            }

            {
                if (GLOBAL_PROVIDERS != null) {
                    if (providersConfig == null) {
                        providersConfig = new HashSet<String>();
                    }
                    providersConfig.addAll(Arrays.asList(GLOBAL_PROVIDERS.split(",")));
                }
            }
        }

        // another property to configure the scanning of providers but this one is consistent with current cxf config
        // the other one is more generic but need another file
        final boolean ignoreAutoProviders = "false".equalsIgnoreCase(serviceConfiguration.getProperties().getProperty(CXF_JAXRS_PREFIX + "skip-provider-scanning"));
        final Collection<Object> additionalProviders = ignoreAutoProviders ? Collections.emptyList() : givenAdditionalProviders;
        List<Object> providers = null;
        if (providersConfig != null) {
            providers = ServiceInfos.resolve(services, providersConfig.toArray(new String[providersConfig.size()]), ProviderFactory.INSTANCE);
            if (providers != null && additionalProviders != null && !additionalProviders.isEmpty()) {
                providers.addAll(providers(services, additionalProviders, ctx));
            }
        }
        if (providers == null) {
            providers = new ArrayList<Object>(4);
            if (additionalProviders != null && !additionalProviders.isEmpty()) {
                providers.addAll(providers(services, additionalProviders, ctx));
            } else {
                providers.addAll(defaultProviders());
            }
        }

        if (!ignoreAutoProviders) {
            addMandatoryProviders(providers);
        }

        LOGGER.info("Using providers:");
        for (final Object provider : providers) {
            LOGGER.info("     " + provider);
        }
        factory.setProviders(providers);
    }

    private static List<Object> defaultProviders() {
        final JAXBElementProvider jaxb = new JAXBElementProvider();
        final Map<String, Object> jaxbProperties = new HashMap<String, Object>();
        jaxbProperties.put(Marshaller.JAXB_FRAGMENT, true);
        jaxb.setMarshallerProperties(jaxbProperties);

        final List<Object> providers = new ArrayList<Object>(4);
        providers.add(jaxb);
        providers.add(new JSONProvider());
        return providers;
    }

    private static class ProviderFactory implements ServiceInfos.Factory {

        private static final ServiceInfos.Factory INSTANCE = new ProviderFactory();

        @Override
        public Object newInstance(final Class<?> clazz) throws Exception {
            boolean found = false;
            Object instance = null;
            for (final Constructor<?> c : clazz.getConstructors()) {
                int contextAnnotations = 0;
                for (final Annotation[] annotations : c.getParameterAnnotations()) {
                    for (final Annotation a : annotations) {
                        if (javax.ws.rs.core.Context.class.equals(a.annotationType())) {
                            contextAnnotations++;
                            break;
                        }
                    }
                }
                if (contextAnnotations == c.getParameterTypes().length) {
                    if (found) {
                        LOGGER.warning("Found multiple matching constructor for " + clazz.getName());
                        return instance;
                    }

                    final Object[] params = new Object[c.getParameterTypes().length];
                    for (int i = 0; i < params.length; i++) {
                        params[i] = ThreadLocalContextManager.findThreadLocal(c.getParameterTypes()[i]);
                        // params[i] can be null if not a known type
                    }
                    instance = c.newInstance(params);
                    found = true;
                }
            }
            if (instance != null) {
                return instance;
            }
            return clazz.newInstance();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @MBean
    @Internal
    @Description("JAX-RS service information")
    public class RestServiceMBean {

        private final String type;
        private final String address;
        private final String classname;
        private final TabularData operations;

        public RestServiceMBean(final Logs.LogResourceEndpointInfo jmxName) {
            this.type = jmxName.type;
            this.address = jmxName.address;
            this.classname = jmxName.classname;

            final String[] names = new String[jmxName.operations.size()];
            final String[] values = new String[jmxName.operations.size()];
            int idx = 0;
            for (final Logs.LogOperationEndpointInfo operation : jmxName.operations) {
                names[idx] = operation.http + " " + operation.address;
                values[idx] = operation.method;
                idx++;
            }
            operations = LocalMBeanServer.tabularData("Operations", "Operations for this endpoint", names, values);
        }

        @ManagedAttribute
        @Description("The type of the REST service")
        public String getWadlUrl() {
            if (address.endsWith("?_wadl")) {
                return address;
            }
            return address + "?_wadl";
        }

        @ManagedOperation
        @Description("The type of the REST service")
        public String getWadl(final String format) {
            if (format != null && format.toLowerCase().contains("json")) {
                InputStream inputStream = null;
                try {
                    final URL url = new URL(getWadlUrl() + "&_type=json");
                    final HttpURLConnection connection = HttpURLConnection.class.cast(url.openConnection());
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-type", "application/json");
                    inputStream = connection.getInputStream();
                    return IO.slurp(inputStream);
                } catch (final Exception e) {
                    return e.getMessage();
                } finally {
                    IO.close(inputStream);
                }
            } else { // xml
                try {
                    return IO.slurp(new URL(getWadlUrl()));
                } catch (final IOException e) {
                    return e.getMessage();
                }
            }
        }

        @ManagedAttribute
        @Description("The type of the REST service")
        public String getType() {
            return type;
        }

        @ManagedAttribute
        @Description("The REST service address")
        public String getAddress() {
            return address;
        }

        @ManagedAttribute
        @Description("The REST service class name")
        public String getClassname() {
            return classname;
        }

        @ManagedAttribute
        @Description("All available methods")
        public TabularData getOperations() {
            return operations;
        }
    }
}
