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

import org.apache.cxf.BusException;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.endpoint.ManagedEndpoint;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.endpoint.ServerImpl;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.ext.ResourceComparator;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.MethodDispatcher;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.jaxrs.model.ProviderInfo;
import org.apache.cxf.jaxrs.provider.ProviderFactory;
import org.apache.cxf.jaxrs.provider.ServerProviderFactory;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationInInterceptor;
import org.apache.cxf.jaxrs.validation.JAXRSBeanValidationOutInterceptor;
import org.apache.cxf.jaxrs.validation.ValidationExceptionMapper;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.servlet.BaseUrlHelper;
import org.apache.cxf.validation.BeanValidationFeature;
import org.apache.cxf.validation.BeanValidationInInterceptor;
import org.apache.cxf.validation.BeanValidationOutInterceptor;
import org.apache.cxf.validation.BeanValidationProvider;
import org.apache.cxf.validation.ResponseConstraintViolationException;
import org.apache.johnzon.jaxrs.WadlDocumentMessageBodyWriter;
import org.apache.openejb.AppContext;
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
import org.apache.openejb.core.WebContext;
import org.apache.openejb.dyni.DynamicSubclass;
import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.monitoring.ObjectNameBuilder;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.openejb.server.cxf.rs.event.ExtensionProviderRegistration;
import org.apache.openejb.server.cxf.rs.event.ServerCreated;
import org.apache.openejb.server.cxf.rs.event.ServerDestroyed;
import org.apache.openejb.server.cxf.transport.HttpDestination;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpRequestImpl;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.httpd.ServletRequestAdapter;
import org.apache.openejb.server.rest.EJBRestServiceInfo;
import org.apache.openejb.server.rest.InternalApplication;
import org.apache.openejb.server.rest.RsHttpListener;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.proxy.ProxyEJB;
import org.apache.openejb.util.reflection.Reflections;
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
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.apache.openejb.loader.JarLocation.jarLocation;

public class CxfRsHttpListener implements RsHttpListener {

    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RS, CxfRsHttpListener.class);

    private static final java.util.logging.Logger SERVER_IMPL_LOGGER = LogUtils.getL7dLogger(ServerImpl.class);

    public static final String CXF_JAXRS_PREFIX = "cxf.jaxrs.";
    public static final String PROVIDERS_KEY = CXF_JAXRS_PREFIX + "providers";
    public static final String STATIC_RESOURCE_KEY = CXF_JAXRS_PREFIX + "static-resources-list";
    public static final String STATIC_SUB_RESOURCE_RESOLUTION_KEY = "staticSubresourceResolution";
    public static final String RESOURCE_COMPARATOR_KEY = CXF_JAXRS_PREFIX + "resourceComparator";

    private static final String GLOBAL_PROVIDERS = SystemInstance.get().getProperty(PROVIDERS_KEY);
    public static final boolean TRY_STATIC_RESOURCES = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.jaxrs.static-first", "true"));
    private static final boolean FAIL_ON_CONSTRAINED_TO = "true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.jaxrs.fail-on-constrainedto", "true"));

    private static final Map<String, String> STATIC_CONTENT_TYPES;
    private static final String[] DEFAULT_WELCOME_FILES = new String[]{"/index.html", "/index.htm"};

    // we have proxies etc so we can't really give it to cxf properly,
    // bval impl supports it (message just uses Object instead of the real instance)
    private static final Object FAKE_SERVICE_OBJECT = new Object();

    private final DestinationFactory transportFactory;
    private final String wildcard;
    private final CxfRSService service;
    private HttpDestination destination;
    private Server server;
    private String context = "";
    private String servlet = "";
    private final Collection<Pattern> staticResourcesList = new CopyOnWriteArrayList<>();
    private final List<ObjectName> jmxNames = new ArrayList<>();
    private final Collection<CreationalContext<?>> toRelease = new LinkedHashSet<>();
    private final Collection<CdiSingletonResourceProvider> singletons = new LinkedHashSet<>();

    private static final char[] URL_SEP = new char[]{'?', '#', ';'};

    static {
        STATIC_CONTENT_TYPES = new HashMap<>();
        STATIC_CONTENT_TYPES.put("html", "text/html");
        STATIC_CONTENT_TYPES.put("htm", "text/html");
        STATIC_CONTENT_TYPES.put("xhtml", "text/html");
        STATIC_CONTENT_TYPES.put("txt", "text/plain");
        STATIC_CONTENT_TYPES.put("css", "text/css");
        STATIC_CONTENT_TYPES.put("jpg", "image/jpg");
        STATIC_CONTENT_TYPES.put("png", "image/png");
        STATIC_CONTENT_TYPES.put("ico", "image/ico");
        STATIC_CONTENT_TYPES.put("pdf", "application/pdf");
        STATIC_CONTENT_TYPES.put("xsd", "application/xml");
    }

    private String pattern;

    public CxfRsHttpListener(final DestinationFactory destinationFactory, final String star, final CxfRSService cxfRSService) {
        transportFactory = destinationFactory;
        wildcard = star;
        service = cxfRSService;
    }

    public void setUrlPattern(final String pattern) {
        this.pattern = pattern;
    }

    @Override
    public void onMessage(final HttpRequest httpRequest, final HttpResponse httpResponse) throws Exception {
        // fix the address (to manage multiple connectors)
        {
            ServletRequest unwrapped = httpRequest;
            while (ServletRequestAdapter.class.isInstance(unwrapped)) {
                unwrapped = ServletRequestAdapter.class.cast(unwrapped).getRequest();
            }
            while (HttpServletRequestWrapper.class.isInstance(unwrapped)) {
                unwrapped = HttpServletRequestWrapper.class.cast(unwrapped).getRequest();
            }
            if (HttpRequestImpl.class.isInstance(unwrapped)) {
                final HttpRequestImpl requestImpl = HttpRequestImpl.class.cast(unwrapped);
                requestImpl.initPathFromContext((!context.startsWith("/") ? "/" : "") + context);
                requestImpl.initServletPath(servlet);
            }
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

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.isEmpty()) {
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
        if ("/".equals(pathInfo) || pathInfo.isEmpty()) { // root is redirected to welcomefiles
            for (final String n : welcomeFiles) {
                final InputStream is = request.getServletContext().getResourceAsStream(n);
                if (is != null) {
                    return is;
                }
            }
            return null; // "/" resolves to an empty string otherwise, we need to avoid it
        }
        return request.getServletContext().getResourceAsStream(pathInfo);
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
        } finally {
            try {
                is.close();
            } catch (final IOException e) {
                // no-op
            }
        }
        return true;
    }

    @Override
    @Deprecated // we could drop it now I think
    public void deploySingleton(final String contextRoot, final String fullContext, final Object o, final Application appInstance,
                                final Collection<Object> additionalProviders, final ServiceConfiguration configuration) {
        deploy(contextRoot, o.getClass(), fullContext, new SingletonResourceProvider(o), o, appInstance, null, additionalProviders, configuration, null);
    }

    @Override
    @Deprecated // we could drop it now I think
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
                null, app, null, additionalProviders, configuration, owbCtx);
    }

    @Override
    @Deprecated // we could drop it now I think
    public void deployEJB(final String contextRoot,
                          final String fullContext,
                          final BeanContext beanContext,
                          final Collection<Object> additionalProviders,
                          final ServiceConfiguration configuration) {
        final Object proxy = ProxyEJB.subclassProxy(beanContext);

        deploy(contextRoot, beanContext.getBeanClass(), fullContext, new NoopResourceProvider(beanContext.getBeanClass(), proxy),
                proxy, null, new OpenEJBEJBInvoker(Collections.singleton(beanContext)), additionalProviders, configuration,
                beanContext.getWebBeansContext());
    }

    private void deploy(final String contextRoot, final Class<?> clazz, final String address, final ResourceProvider rp, final Object serviceBean,
                        final Application app, final Invoker invoker, final Collection<Object> additionalProviders, final ServiceConfiguration configuration,
                        final WebBeansContext webBeansContext) {
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.initBusLoader());
        try {
            final JAXRSServerFactoryBean factory = newFactory(address, createServiceJmxName(clazz.getClassLoader()), createEndpointName(app));
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
            destination = (HttpDestination) server.getDestination();

            fireServerCreated(oldLoader);
        } finally {
            if (oldLoader != null) {
                CxfUtil.clearBusLoader(oldLoader);
            }
        }
    }

    private void fireServerCreated(final ClassLoader oldLoader) {
        final Object ctx = AppFinder.findAppContextOrWeb(oldLoader, new AppFinder.Transformer<Object>() {
            @Override
            public Object from(final AppContext appCtx) {
                return appCtx;
            }

            @Override
            public Object from(final WebContext webCtx) {
                return webCtx;
            }
        });
        final AppContext appCtx = AppContext.class.isInstance(ctx) ? AppContext.class.cast(ctx) : WebContext.class.cast(ctx).getAppContext();
        WebContext webContext = appCtx == ctx ? null : WebContext.class.cast(ctx);
        if (webContext == null && appCtx.getWebContexts().size() == 1 && appCtx.getWebContexts().get(0).getClassLoader() == oldLoader) {
            webContext = appCtx.getWebContexts().get(0);
        }
        SystemInstance.get().fireEvent(new ServerCreated(server, appCtx, webContext, this.context));
    }

    private List<Object> providers(final Collection<ServiceInfo> services, final Collection<Object> additionalProviders, final WebBeansContext ctx) {
        final List<Object> instances = new ArrayList<>();
        final BeanManagerImpl bm = ctx == null ? null : ctx.getBeanManagerImpl();
        for (final Object o : additionalProviders) {
            if (o instanceof Class<?>) {
                final Class<?> clazz = (Class<?>) o;
                if (isNotServerProvider(clazz)) {
                    continue;
                }

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

                final Collection<Object> instance = ServiceInfos.resolve(services, new String[]{name}, OpenEJBProviderFactory.INSTANCE);
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
                final Class<?> clazz = o.getClass();
                if (isNotServerProvider(clazz)) {
                    continue;
                }
                final String name = clazz.getName();
                if (shouldSkipProvider(name)) {
                    continue;
                }
                instances.add(o);
            }
        }

        return instances;
    }

    private boolean isNotServerProvider(Class<?> clazz) {
        final ConstrainedTo ct = clazz.getAnnotation(ConstrainedTo.class);
        if (ct != null && ct.value() != RuntimeType.SERVER) {
            if (!FAIL_ON_CONSTRAINED_TO) {
                LOGGER.warning(clazz + " is not a SERVER provider, ignoring");
                return true;
            }
            throw new IllegalArgumentException(clazz + " is not a SERVER provider");
        }
        return false;
    }

    private boolean shouldSkipProvider(final String name) {
        return !service.isActive(name) || name.startsWith("org.apache.wink.common.internal.");
    }

    private void addMandatoryProviders(final Collection<Object> instances, final ServiceConfiguration serviceConfiguration) {
        if (SystemInstance.get().getProperty("openejb.jaxrs.jsonProviders") == null) {
            if (!shouldSkipProvider(WadlDocumentMessageBodyWriter.class.getName())) {
                instances.add(new WadlDocumentMessageBodyWriter());
            }
        }
        if (!shouldSkipProvider(EJBExceptionMapper.class.getName())) {
            instances.add(new EJBExceptionMapper());
        }
        if (!shouldSkipProvider(ValidationExceptionMapper.class.getName())) {
            instances.add(new ValidationExceptionMapper());
            final String level = SystemInstance.get()
                    .getProperty(
                        "openejb.cxf.rs.bval.log.level",
                        serviceConfiguration.getProperties().getProperty(CXF_JAXRS_PREFIX + "bval.log.level"));
            if (level != null) {
                try {
                    LogUtils.getL7dLogger(ValidationExceptionMapper.class).setLevel(Level.parse(level));
                } catch (final UnsupportedOperationException uoe) {
                    LOGGER.warning("Can't set level " + level + " on " +
                            "org.apache.cxf.jaxrs.validation.ValidationExceptionMapper logger, " +
                            "please configure it in your logging framework.");
                }
            }
        }
    }

    private Object newProvider(final Class<?> clazz) throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }

    @Override
    public void undeploy() {
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
        for (final CdiSingletonResourceProvider provider : singletons) {
            try {
                provider.release();
            } catch (final Exception e) {
                LOGGER.warning(e.getMessage(), e);
            }
        }

        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(CxfUtil.initBusLoader());
        try {
            server.destroy();
            SystemInstance.get().fireEvent(new ServerDestroyed(server));
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
            final JAXRSServerFactoryBean factory = newFactory(prefix, createServiceJmxName(classLoader), createEndpointName(application));
            configureFactory(additionalProviders, serviceConfiguration, factory, owbCtx);
            factory.setApplication(application);

            final List<Class<?>> classes = new ArrayList<>();

            for (final Class<?> clazz : application.getClasses()) {
                if (!additionalProviders.contains(clazz) && !clazz.isInterface()) {
                    classes.add(clazz);

                    final EJBRestServiceInfo restServiceInfo = getEjbRestServiceInfo(restEjbs, clazz);

                    if (restServiceInfo != null) {
                        final Object proxy = ProxyEJB.subclassProxy(restServiceInfo.context);
                        factory.setResourceProvider(clazz, new NoopResourceProvider(restServiceInfo.context.getBeanClass(), proxy));
                    } else {
                        factory.setResourceProvider(clazz, new OpenEJBPerRequestPojoResourceProvider(
                                classLoader, clazz, injections, context, owbCtx));
                    }
                }
            }

            for (final Object o : application.getSingletons()) {
                if (!additionalProviders.contains(o)) {
                    final Class<?> clazz = realClass(o.getClass());
                    classes.add(clazz);

                    final EJBRestServiceInfo restServiceInfo = getEjbRestServiceInfo(restEjbs, clazz);

                    if (restServiceInfo != null) {
                        final Object proxy = ProxyEJB.subclassProxy(restServiceInfo.context);
                        factory.setResourceProvider(clazz, new NoopResourceProvider(restServiceInfo.context.getBeanClass(), proxy));
                    } else {
                        if (owbCtx != null && owbCtx.getBeanManagerImpl().isInUse()) {
                            final CdiSingletonResourceProvider provider = new CdiSingletonResourceProvider(classLoader, clazz, o, injections, context, owbCtx);
                            singletons.add(provider);
                            factory.setResourceProvider(clazz, provider);
                        } else {
                            factory.setResourceProvider(clazz, new SingletonResourceProvider(o));
                        }
                    }
                }
            }

            factory.setResourceClasses(classes);
            factory.setInvoker(new AutoJAXRSInvoker(restEjbs));

            this.context = webContext;
            if (!webContext.startsWith("/")) {
                this.context = "/" + webContext;
            } // /webcontext/servlet for event firing

            final Level level = SERVER_IMPL_LOGGER.getLevel();
            try {
                SERVER_IMPL_LOGGER.setLevel(Level.OFF);
            } catch (final UnsupportedOperationException e) {
                //ignore
            }

            try {
                server = factory.create();
                fixProviderIfKnown();
                fireServerCreated(oldLoader);

                final ServerProviderFactory spf = ServerProviderFactory.class.cast(server.getEndpoint().get(ServerProviderFactory.class.getName()));
                LOGGER.info("Using readers:");
                for (final Object provider : List.class.cast(Reflections.get(spf, "messageReaders"))) {
                    LOGGER.info("     " + ProviderInfo.class.cast(provider).getProvider());
                }
                LOGGER.info("Using writers:");
                for (final Object provider : List.class.cast(Reflections.get(spf, "messageWriters"))) {
                    LOGGER.info("     " + ProviderInfo.class.cast(provider).getProvider());
                }
                LOGGER.info("Using exception mappers:");
                for (final Object provider : List.class.cast(Reflections.get(spf, "exceptionMappers"))) {
                    LOGGER.info("     " + ProviderInfo.class.cast(provider).getProvider());
                }
            } finally {
                try {
                    SERVER_IMPL_LOGGER.setLevel(level);
                } catch (final UnsupportedOperationException e) {
                    //ignore
                }
            }

            final int servletIdx = 1 + this.context.substring(1).indexOf('/');
            if (servletIdx > 0) {
                this.servlet = this.context.substring(servletIdx);
                this.context = this.context.substring(0, servletIdx);
            }
            destination = (HttpDestination) server.getDestination();

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

    private void fixProviderIfKnown() {
        final ServerProviderFactory spf = ServerProviderFactory.class.cast(server.getEndpoint().get(ServerProviderFactory.class.getName()));
        for (final String field : asList("messageWriters", "messageReaders")) {
            final List<ProviderInfo<?>> values = List.class.cast(Reflections.get(spf, field));

            boolean customJsonProvider = false;
            for (final ProviderInfo<?> o : values) { // using getName to not suppose any classloader setup
                final String name = o.getResourceClass().getName();
                if ("org.apache.johnzon.jaxrs.ConfigurableJohnzonProvider".equals(name)
                        || "org.apache.johnzon.jaxrs.jsonb.jaxrs.JsonbJaxrsProvider".equals(name)
                        // contains in case of proxying
                        || name.contains("com.fasterxml.jackson.jaxrs.json")) {
                    customJsonProvider = true;
                    break; //  cause we only handle json for now
                }
            }

            if (customJsonProvider) { // remove JohnzonProvider default versions
                final Iterator<ProviderInfo<?>> it = values.iterator();
                while (it.hasNext()) {
                    final String name = it.next().getResourceClass().getName();
                    if ("org.apache.johnzon.jaxrs.JohnzonProvider".equals(name) ||
                            "org.apache.openejb.server.cxf.rs.CxfRSService$TomEEJohnzonProvider".equals(name)) {
                        it.remove();
                        break;
                    }
                }
            }
        }

    }

    private static Class<?> realClass(final Class<?> aClass) {
        Class<?> result = aClass;
        while (result.getName().contains("$$")) {
            result = result.getSuperclass();
            if (result == null) {
                return aClass;
            }
        }
        return result;
    }

    private EJBRestServiceInfo getEjbRestServiceInfo(Map<String, EJBRestServiceInfo> restEjbs, Class<?> clazz) {
        String name = clazz.getName();
        EJBRestServiceInfo restServiceInfo = restEjbs.get(name);

        if (name.endsWith(DynamicSubclass.IMPL_SUFFIX)) {
            name = name.substring(0, name.length() - DynamicSubclass.IMPL_SUFFIX.length());
            restServiceInfo = restEjbs.get(name);
            if (restServiceInfo != null) { // AutoJAXRSInvoker relies on it
                restEjbs.put(clazz.getName(), restServiceInfo);
            }
        }
        return restServiceInfo;
    }

    private static String createEndpointName(final Application application) {
        if (application == null) {
            return "jaxrs-application";
        }
        if (InternalApplication.class.isInstance(application)) {
            final Application original = InternalApplication.class.cast(application).getOriginal();
            if (original != null) {
                return original.getClass().getSimpleName();
            }
            return "jaxrs-application";
        }
        return application.getClass().getSimpleName();
    }

    private static String createServiceJmxName(final ClassLoader classLoader) {
        final AppContext app = AppFinder.findAppContextOrWeb(classLoader, AppFinder.AppContextTransformer.INSTANCE);
        return app == null ? "application" : app.getId();
    }

    private void logEndpoints(final Application application, final String prefix,
                              final Map<String, EJBRestServiceInfo> restEjbs,
                              final JAXRSServerFactoryBean factory, final String base) {
        final List<Logs.LogResourceEndpointInfo> resourcesToLog = new ArrayList<>();
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

            final List<Logs.LogOperationEndpointInfo> toLog = new ArrayList<>();

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

        LOGGER.info("REST Application: " + Logs.forceLength(prefix, addressSize, true) + " -> " +
                (InternalApplication.class.isInstance(application) && InternalApplication.class.cast(application).getOriginal() != null ?
                        InternalApplication.class.cast(application).getOriginal() : application));

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

    private JAXRSServerFactoryBean newFactory(final String prefix, final String service, final String endpoint) {
        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean() {
            @Override
            protected Endpoint createEndpoint() throws BusException, EndpointException {
                final Endpoint created = super.createEndpoint();
                created.put(ManagedEndpoint.SERVICE_NAME, service);
                created.put(ManagedEndpoint.ENDPOINT_NAME, endpoint);
                return created;
            }
        };
        factory.setDestinationFactory(transportFactory);
        factory.setBus(CxfUtil.getBus());
        factory.setAddress(prefix);
        return factory;
    }

    private void configureFactory(final Collection<Object> givenAdditionalProviders,
                                  final ServiceConfiguration serviceConfiguration,
                                  final JAXRSServerFactoryBean factory,
                                  final WebBeansContext ctx) {
        if (!"true".equalsIgnoreCase(SystemInstance.get().getProperty("openejb.cxf.rs.skip-provider-sorting", "false"))) {
            final Comparator<?> providerComparator = findProviderComparator(serviceConfiguration, ctx);
            if (providerComparator != null) {
                factory.setProviderComparator(providerComparator);
            }
        }
        CxfUtil.configureEndpoint(factory, serviceConfiguration, CXF_JAXRS_PREFIX);

        boolean enforceCxfBvalMapper = false;
        if (ctx == null || !ctx.getBeanManagerImpl().isInUse()) { // activate bval
            boolean bvalActive = Boolean.parseBoolean(
                    SystemInstance.get().getProperty("openejb.cxf.rs.bval.active",
                            serviceConfiguration.getProperties().getProperty(CXF_JAXRS_PREFIX + "bval.active", "true")));
            if (factory.getFeatures() == null && bvalActive) {
                factory.setFeatures(new ArrayList<Feature>());
            } else if (bvalActive) { // check we should activate it and user didn't configure it
                for (final Feature f : factory.getFeatures()) {
                    if (BeanValidationFeature.class.isInstance(f)) {
                        bvalActive = false;
                        break;
                    }
                }
                for (final Interceptor<?> i : factory.getInInterceptors()) {
                    if (BeanValidationInInterceptor.class.isInstance(i)) {
                        bvalActive = false;
                        break;
                    }
                }
                for (final Interceptor<?> i : factory.getOutInterceptors()) {
                    if (BeanValidationOutInterceptor.class.isInstance(i)) {
                        bvalActive = false;
                        break;
                    }
                }
            }
            if (bvalActive) { // bval doesn't need the actual instance so faking it to avoid to lookup the bean
                final BeanValidationProvider provider = new BeanValidationProvider();

                final BeanValidationInInterceptor in = new JAXRSBeanValidationInInterceptor() {
                    @Override
                    protected Object getServiceObject(final Message message) {
                        return CxfRsHttpListener.this.getServiceObject(message);
                    }
                };
                in.setProvider(provider);
                in.setServiceObject(FAKE_SERVICE_OBJECT);
                factory.getInInterceptors().add(in);

                final BeanValidationOutInterceptor out = new JAXRSBeanValidationOutInterceptor() {
                    @Override
                    protected Object getServiceObject(final Message message) {
                        return CxfRsHttpListener.this.getServiceObject(message);
                    }
                };
                out.setProvider(provider);
                out.setServiceObject(FAKE_SERVICE_OBJECT);
                factory.getOutInterceptors().add(out);

                // and add a mapper to get back a 400 like for bval
                enforceCxfBvalMapper = true;
            }
        }

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
                providersConfig = new HashSet<>();
                for (final String p : Arrays.asList(provider.split(","))) {
                    providersConfig.add(p.trim());
                }
            }

            {
                if (GLOBAL_PROVIDERS != null) {
                    if (providersConfig == null) {
                        providersConfig = new HashSet<>();
                    }
                    providersConfig.addAll(Arrays.asList(GLOBAL_PROVIDERS.split(",")));
                }
            }
        }

        // another property to configure the scanning of providers but this one is consistent with current cxf config
        // the other one is more generic but need another file
        final String key = CXF_JAXRS_PREFIX + "skip-provider-scanning";
        final boolean ignoreAutoProviders = "true".equalsIgnoreCase(SystemInstance.get().getProperty(key, serviceConfiguration.getProperties().getProperty(key, "false")));
        final Collection<Object> additionalProviders = ignoreAutoProviders ? Collections.emptyList() : givenAdditionalProviders;
        List<Object> providers = null;
        if (providersConfig != null) {
            providers = ServiceInfos.resolve(services, providersConfig.toArray(new String[providersConfig.size()]), OpenEJBProviderFactory.INSTANCE);
            if (providers != null && additionalProviders != null && !additionalProviders.isEmpty()) {
                providers.addAll(providers(serviceConfiguration.getAvailableServices(), additionalProviders, ctx));
            }
        }
        if (providers == null) {
            providers = new ArrayList<>(4);
            if (additionalProviders != null && !additionalProviders.isEmpty()) {
                providers.addAll(providers(serviceConfiguration.getAvailableServices(), additionalProviders, ctx));
            }
        }

        if (!ignoreAutoProviders) {
            addMandatoryProviders(providers, serviceConfiguration);
            if (enforceCxfBvalMapper) {
                if (!shouldSkipProvider(CxfResponseValidationExceptionMapper.class.getName())) {
                    providers.add(new CxfResponseValidationExceptionMapper());
                }
            }
        }

        SystemInstance.get().fireEvent(new ExtensionProviderRegistration(
                AppFinder.findAppContextOrWeb(Thread.currentThread().getContextClassLoader(), AppFinder.AppContextTransformer.INSTANCE), providers));

        if (!providers.isEmpty()) {
            factory.setProviders(providers);
        }
    }

    private Object getServiceObject(final Message message) {
        final OperationResourceInfo ori = message.getExchange().get(OperationResourceInfo.class);
        if (ori == null) {
            return null;
        }
        if (!ori.getClassResourceInfo().isRoot()) {
            return message.getExchange().get("org.apache.cxf.service.object.last");
        }
        final ResourceProvider resourceProvider = ori.getClassResourceInfo().getResourceProvider();
        if (resourceProvider.isSingleton()) {
            return resourceProvider.getInstance(message);
        }
        final Object o = message.getExchange().get(CdiResourceProvider.INSTANCE_KEY);
        return o != null || !OpenEJBPerRequestPojoResourceProvider.class.isInstance(resourceProvider) ? o : resourceProvider.getInstance(message);
    }

    private Comparator<?> findProviderComparator(final ServiceConfiguration serviceConfiguration, final WebBeansContext ctx) {
        final String comparatorKey = CXF_JAXRS_PREFIX + "provider-comparator";
        final String comparatorClass = serviceConfiguration.getProperties()
                .getProperty(comparatorKey, SystemInstance.get().getProperty(comparatorKey));

        Comparator<Object> comparator = null;
        if (comparatorClass == null) {
            return null; // try to rely on CXF behavior otherwise just reactivate DefaultProviderComparator.INSTANCE if it is an issue
        } else {
            final BeanManagerImpl bm = ctx == null ? null : ctx.getBeanManagerImpl();
            if (bm != null && bm.isInUse()) {
                try {
                    final Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(comparatorClass);
                    final Set<Bean<?>> beans = bm.getBeans(clazz);
                    if (beans != null && !beans.isEmpty()) {
                        final Bean<?> bean = bm.resolve(beans);
                        final CreationalContextImpl<?> creationalContext = bm.createCreationalContext(bean);
                        comparator = Comparator.class.cast(bm.getReference(bean, clazz, creationalContext));
                        toRelease.add(creationalContext);
                    }
                } catch (final Throwable th) {
                    LOGGER.debug("Can't use CDI to load comparator " + comparatorClass);
                }
            }

            if (comparator == null) {
                comparator = Comparator.class.cast(ServiceInfos.resolve(serviceConfiguration.getAvailableServices(), comparatorClass));
            }
            if (comparator == null) {
                try {
                    comparator = Comparator.class.cast(Thread.currentThread().getContextClassLoader().loadClass(comparatorClass).newInstance());
                } catch (final Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }

            for (final Type itf : comparator.getClass().getGenericInterfaces()) {
                if (!ParameterizedType.class.isInstance(itf)) {
                    continue;
                }

                final ParameterizedType pt = ParameterizedType.class.cast(itf);
                if (Comparator.class == pt.getRawType() && pt.getActualTypeArguments().length > 0) {
                    final Type t = pt.getActualTypeArguments()[0];
                    if (Class.class.isInstance(t) && ProviderInfo.class == t) {
                        return comparator;
                    }
                    if (ParameterizedType.class.isInstance(t) && ProviderInfo.class == ParameterizedType.class.cast(t).getRawType()) {
                        return comparator;
                    }
                }
            }

            return new ProviderComparatorWrapper(comparator);
        }
    }

    // public to ensure it can be configured since not setup by default anymore
    public static final class DefaultProviderComparator extends ProviderFactory implements Comparator<ProviderInfo<?>> {
        private static final ClassLoader SYSTEM_LOADER = ClassLoader.getSystemClassLoader();

        public DefaultProviderComparator() {
            super(null);
        }

        @Override
        public int compare(final ProviderInfo<?> o1, final ProviderInfo<?> o2) {
            if (o1 == o2 || (o1 != null && o1.equals(o2))) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }

            final Class<?> c1 = o1.getProvider().getClass();
            final Class<?> c2 = o2.getProvider().getClass();
            if (c1.getName().startsWith("org.apache.cxf.")) {
                if (!c2.getName().startsWith("org.apache.cxf.")) {
                    return 1;
                }
                return -1;
            }
            if (c2.getName().startsWith("org.apache.cxf.")) {
                return -1;
            }

            final ClassLoader classLoader1 = c1.getClassLoader();
            final ClassLoader classLoader2 = c2.getClassLoader();

            final boolean loadersNotNull = classLoader1 != null && classLoader2 != null;

            if (classLoader1 != classLoader2
                    && loadersNotNull
                    && !classLoader1.equals(classLoader2) && !classLoader2.equals(classLoader1)) {
                if (isParent(classLoader1, classLoader2)) {
                    return 1;
                }
                if (isParent(classLoader2, classLoader1)) {
                    return -1;
                }
            } else {
                int result = compareClasses(o1.getProvider(), o2.getProvider());
                if (result != 0) {
                    return result;
                }

                if (MessageBodyWriter.class.isInstance(o1.getProvider())) {
                    final List<MediaType> types1 =
                            JAXRSUtils.sortMediaTypes(JAXRSUtils.getProviderProduceTypes(MessageBodyWriter.class.cast(o1.getProvider())), JAXRSUtils.MEDIA_TYPE_QS_PARAM);
                    final List<MediaType> types2 =
                            JAXRSUtils.sortMediaTypes(JAXRSUtils.getProviderProduceTypes(MessageBodyWriter.class.cast(o2.getProvider())), JAXRSUtils.MEDIA_TYPE_QS_PARAM);

                    if (types1.contains(MediaType.WILDCARD_TYPE) && !types2.contains(MediaType.WILDCARD_TYPE)) {
                        return 1;
                    }
                    if (types2.contains(MediaType.WILDCARD_TYPE) && !types1.contains(MediaType.WILDCARD_TYPE)) {
                        return -1;
                    }

                    result = JAXRSUtils.compareSortedMediaTypes(types1, types2, JAXRSUtils.MEDIA_TYPE_QS_PARAM);
                    if (result != 0) {
                        return result;
                    }
                } else if (MessageBodyReader.class.isInstance(o1.getProvider())) { // else is not super good but using both is not sa well so let it be for now
                    final List<MediaType> types1 =
                            JAXRSUtils.sortMediaTypes(JAXRSUtils.getProviderConsumeTypes(MessageBodyReader.class.cast(o1.getProvider())), null);
                    final List<MediaType> types2 =
                            JAXRSUtils.sortMediaTypes(JAXRSUtils.getProviderConsumeTypes(MessageBodyReader.class.cast(o2.getProvider())), null);

                    if (types1.contains(MediaType.WILDCARD_TYPE) && !types2.contains(MediaType.WILDCARD_TYPE)) {
                        return 1;
                    }
                    if (types2.contains(MediaType.WILDCARD_TYPE) && !types1.contains(MediaType.WILDCARD_TYPE)) {
                        return -1;
                    }

                    result = JAXRSUtils.compareSortedMediaTypes(types1, types2, JAXRSUtils.MEDIA_TYPE_QS_PARAM);
                    if (result != 0) {
                        return result;
                    }
                }

                final Boolean custom1 = o1.isCustom();
                final Boolean custom2 = o2.isCustom();
                final int customComp = custom1.compareTo(custom2) * -1;
                if (customComp != 0) {
                    return customComp;
                }

                try { // WEB-INF/classes will be before WEB-INF/lib
                    final File file1 = jarLocation(c1);
                    final File file2 = jarLocation(c2);
                    if ("classes".equals(file1.getName())) {
                        if ("classes".equals(file2.getName())) {
                            return c1.getName().compareTo(c2.getName());
                        }
                        return -1;
                    }
                    if ("classes".equals(file2.getName())) {
                        return 1;
                    }
                } catch (final Exception e) {
                    // no-op: sort by class name
                }
            }
            return c1.getName().compareTo(c2.getName());
        }

        private static boolean isParent(final ClassLoader l1, ClassLoader l2) {
            ClassLoader current = l2;
            while (current != null && current != SYSTEM_LOADER) {
                if (current.equals(l1) || l1.equals(current)) {
                    return true;
                }
                current = current.getParent();
            }
            return false;
        }

        @Override
        public Configuration getConfiguration(final Message message) {
            throw new UnsupportedOperationException("not a real inheritance");
        }

        @Override
        protected void setProviders(final boolean b, final boolean b1, final Object... objects) {
            throw new UnsupportedOperationException("not a real inheritance");
        }
    }

    // we use Object cause an app with a custom comparator can desire to compare instances
    private static final class ProviderComparatorWrapper implements Comparator<ProviderInfo<?>> {
        private final Comparator<Object> delegate;

        private ProviderComparatorWrapper(final Comparator<Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(final ProviderInfo<?> o1, final ProviderInfo<?> o2) {
            return delegate.compare(o1.getProvider(), o2.getProvider());
        }
    }

    private static class OpenEJBProviderFactory implements ServiceInfos.Factory {
        private static final ServiceInfos.Factory INSTANCE = new OpenEJBProviderFactory();

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

    @Provider
    public static class CxfResponseValidationExceptionMapper implements ExceptionMapper<ResponseConstraintViolationException> {
        @Override
        public Response toResponse(final ResponseConstraintViolationException exception) {
            return JAXRSUtils.toResponse(Response.Status.BAD_REQUEST);
        }
    }
}
