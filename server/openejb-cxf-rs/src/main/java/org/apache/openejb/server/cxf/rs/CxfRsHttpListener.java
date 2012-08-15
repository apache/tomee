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

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.ResourceProvider;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.apache.cxf.jaxrs.provider.JAXBElementProvider;
import org.apache.cxf.jaxrs.provider.json.JSONProvider;
import org.apache.cxf.service.invoker.Invoker;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.openejb.BeanContext;
import org.apache.openejb.Injection;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.httpd.HttpRequestImpl;
import org.apache.openejb.server.httpd.HttpResponse;
import org.apache.openejb.server.rest.RsHttpListener;
import org.apache.openejb.util.ListConfigurator;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.ObjectRecipeHelper;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.xbean.recipe.ObjectRecipe;

import javax.naming.Context;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.core.Application;
import javax.xml.bind.Marshaller;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * System property:
 * <ul>
 *     <li>-Dopenejb.cxf.jax-rs.providers=&lt;qualified name&gt;:default</li>
 * </ul>
 * Note: default means jaxb and json
 *
 * Providers are created from system properties so config is done:
 * [<service class name>.providers.</service>|openejb.cxf.jax-rs.providers].<provider class>.<property name>=<value>
 *
 */
public class CxfRsHttpListener implements RsHttpListener {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.CXF, CxfRsHttpListener.class);

    public static final String OPENEJB_CXF_JAXRS_PROVIDERS_KEY = "openejb.cxf.jax-rs.providers";
    public static final String OPENEJB_CXF_JAXRS_PROVIDERS_SUFFIX = ".providers";
    public static final String DEFAULT_CXF_JAXRS_PROVIDERS_KEY = "default";
    public static final String OPENEJB_CXF_PROPERTIES = "openejb.cxf.rs.jaxb.properties";

    public static final String OPENEJB_JAXRS_READ_PROPERTIES = "openejb.jaxrs.read-properties";
    public static final String OPENEJB_JAXRS_CXF_FEATURES = "openejb.jaxrs.cxf.features";

    private static final List<Object> PROVIDERS = createConfiguredProviderList("", CxfRsHttpListener.class.getClassLoader());
    private static final Map<String, Object> cxfProperties = toMap(SystemInstance.get().getProperty(OPENEJB_CXF_PROPERTIES));

    private HTTPTransportFactory transportFactory;
    private AbstractHTTPDestination destination;
    private Server server;

    private static List<AbstractFeature> GLOBAL_FEATURES = new ArrayList<AbstractFeature>();
    static {
        final List<AbstractFeature> features = ListConfigurator.getList(
                SystemInstance.get().getProperties(), OPENEJB_JAXRS_CXF_FEATURES,
                CxfRsHttpListener.class.getClassLoader(), AbstractFeature.class);
        if (features != null) {
            GLOBAL_FEATURES.addAll(features);
        }
    }

    public CxfRsHttpListener(HTTPTransportFactory httpTransportFactory) {
        transportFactory = httpTransportFactory;
    }

    @Override
    public void onMessage(final HttpRequest httpRequest, final HttpResponse httpResponse) throws Exception {
        destination.invoke(null, httpRequest.getServletContext(), new HttpServletRequestWrapper(httpRequest) {
            // see org.apache.cxf.jaxrs.utils.HttpUtils.getPathToMatch()
            // cxf uses implicitly getRawPath() from the endpoint but not for the request URI
            // so without stripping the address until the context the behavior is weird
            // this is just a workaround waiting for something better
            @Override
            public String getRequestURI() {
                if (httpRequest instanceof HttpRequestImpl) {
                    return ((HttpRequestImpl) httpRequest).requestRawPath();
                }
                return super.getRequestURI();
            }
        }, httpResponse);
    }

    @Override
    public void deploySingleton(String fullContext, Object o, Application appInstance, Collection<Class<?>> additionalProviders) {
        deploy(o.getClass(), fullContext, new SingletonResourceProvider(o), o, appInstance, null, additionalProviders);
    }

    @Override
    public void deployPojo(String fullContext, Class<?> loadedClazz, Application app, Collection<Injection> injections, Context context, WebBeansContext owbCtx, Collection<Class<?>> additionalProviders) {
        deploy(loadedClazz, fullContext, new OpenEJBPerRequestPojoResourceProvider(loadedClazz, injections, context, owbCtx), null, app, null, additionalProviders);
    }

    @Override
    public void deployEJB(String fullContext, BeanContext beanContext, Collection<Class<?>> additionalProviders) {
        deploy(beanContext.getBeanClass(), fullContext, null, null, null, new OpenEJBEJBInvoker(beanContext), additionalProviders);
    }

    private void deploy(Class<?> clazz, String address, ResourceProvider rp, Object serviceBean, Application app, Invoker invoker, Collection<Class<?>> additionalProviders) {
        final List<Object> providers = createConfiguredProviderList(nameForProviders(clazz), clazz.getClassLoader());
        if (additionalProviders != null && !additionalProviders.isEmpty()) {
            providers.addAll(instantiate(additionalProviders));
        }

        final String impl;
        if (serviceBean != null) {
            impl = serviceBean.getClass().getName();
        } else {
            impl = clazz.getName();
        }

        final Map<String, Object> specificProperties = toMap(SystemInstance.get().getProperty(OPENEJB_CXF_PROPERTIES + "." + impl + "."));

        final JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setResourceClasses(clazz);
        factory.setDestinationFactory(transportFactory);
        factory.setBus(transportFactory.getBus());
        factory.setAddress(address);
        factory.setProviders(providers);
        if (factory.getProperties() == null) {
            factory.setProperties(new HashMap<String, Object>());
        }

        if (specificProperties != null) {
            factory.getProperties().putAll(specificProperties);
        } else if (cxfProperties != null) {
            factory.getProperties().putAll(cxfProperties);
        }

        if (SystemInstance.get().getOptions().get(OPENEJB_JAXRS_READ_PROPERTIES, false)) {
            List<AbstractFeature> features = ListConfigurator.getList(
                    SystemInstance.get().getProperties(), clazz.getName() + "." + OPENEJB_JAXRS_CXF_FEATURES,
                    clazz.getClassLoader(), AbstractFeature.class);
            if (features == null) {
                features = new ArrayList<AbstractFeature>();
            }
            features.addAll(GLOBAL_FEATURES);
            if (!features.isEmpty()) {
                factory.setFeatures(features);
            }
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
    }

    private Collection<Object> instantiate(final Collection<Class<?>> classes) {
        final List<Object> instances = new ArrayList<Object>();
        for (Class<?> clazz : classes) {
            try {
                instances.add(clazz.newInstance());
            } catch (InstantiationException e) {
                LOGGER.warning("can't instantiate '" + clazz.getName() + "'", e);
            } catch (IllegalAccessException e) {
                LOGGER.warning("can't access '" + clazz.getName() + "'", e);
            }
        }
        return instances;
    }

    private String nameForProviders(Class<?> clazz) {
        if (clazz == null) {
            return "default"; // whatever it is it will be overriden by default providers
        }
        return clazz.getName();
    }

    public void undeploy() {
        server.stop();
    }

    private static List<Object> createConfiguredProviderList(final String prefix, final ClassLoader loader) {
        final String key;
        final String systPropPrefix;
        if (prefix == null || prefix.trim().isEmpty()) {
            key = OPENEJB_CXF_JAXRS_PROVIDERS_KEY;
            systPropPrefix = key + ".";
        } else {
            key = prefix + OPENEJB_CXF_JAXRS_PROVIDERS_SUFFIX;
            systPropPrefix = prefix + ".";
        }

        String providersProperty = SystemInstance.get().getProperty(key);
        // if no overriding
        if (PROVIDERS != null && (providersProperty == null || providersProperty.trim().isEmpty())) {
            return PROVIDERS;
        }

        final JAXBElementProvider jaxb = new JAXBElementProvider();
        final Map<String, Object> jaxbProperties = new HashMap<String, Object> ();
        jaxbProperties.put(Marshaller.JAXB_FRAGMENT, true);
        jaxb.setMarshallerProperties(jaxbProperties);

        final JSONProvider json = buildProvider(JSONProvider.class, loader,
                OPENEJB_CXF_JAXRS_PROVIDERS_KEY + "." + JSONProvider.class.getName() + ".",
                systPropPrefix + JSONProvider.class.getName() + ".");

        List<Object> providerList = new ArrayList<Object>();
        if (providersProperty != null && !providersProperty.trim().isEmpty()) {
            String[] providers = providersProperty.split(",|;| ");
            for (String provider : providers) {
                if (DEFAULT_CXF_JAXRS_PROVIDERS_KEY.equals(provider)) {
                    providerList.add(json);
                    providerList.add(jaxb);
                } else {
                    try {
                        providerList.add(ObjectRecipeHelper.createMeFromSystemProps(systPropPrefix + provider + ".", null, loader.loadClass(provider)));
                    } catch (Exception e) {
                        LOGGER.error("can't add jax-rs provider " + provider + " in the current webapp"); // don't print this exception
                    }
                }
            }
            return providerList;
        }

        providerList.add(jaxb);
        providerList.add(json);
        return providerList;
    }

    // generally faster than looping all system properties
    // Note: should respect general format of custom provider config
    private static <T> T buildProvider(final Class<T> clazz, final ClassLoader classloader, final String globalPrefix, final String prefix) {
        // quick look in clazz (and not parent) if we can look for some property
        final Map<String, Object> properties = new HashMap<String, Object>();
        final Options options = SystemInstance.get().getOptions();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (prefix != null) {
                final String key = prefix + field.getName();
                if (options.has(key)) {
                    properties.put(field.getName(), options.getProperties().get(key));
                } else if (options.has(globalPrefix + field.getName())) {
                    properties.put(field.getName(), options.getProperties().get(globalPrefix + field.getName()));
                }
            }
        }
        return clazz.cast(new ObjectRecipe(clazz, properties).create(classloader));
    }

    private static Map<String, Object> toMap(String property) {
        if (property == null || property.isEmpty()) {
            return null;
        }

        final Map<String, Object> properties = new HashMap<String, Object>();
        for (String str : property.split(",| ")) {
            final String[] kv = str.split("=");
            if (kv.length == 2) {
                properties.put(kv[0], kv[1]);
            } else {
                properties.put(str, "");
            }
        }
        return properties;
    }
}
