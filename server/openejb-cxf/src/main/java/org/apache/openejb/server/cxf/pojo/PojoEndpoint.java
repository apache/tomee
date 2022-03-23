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
import org.apache.cxf.common.injection.ResourceInjector;
import org.apache.cxf.jaxws.JAXWSMethodInvoker;
import org.apache.cxf.jaxws.context.WebServiceContextResourceResolver;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;
import org.apache.cxf.resource.DefaultResourceManager;
import org.apache.cxf.resource.ResourceManager;
import org.apache.cxf.resource.ResourceResolver;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.cdi.CdiEjbBean;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.cxf.CxfEndpoint;
import org.apache.openejb.server.cxf.CxfServiceConfiguration;
import org.apache.openejb.server.cxf.JaxWsImplementorInfoImpl;
import org.apache.openejb.util.AppFinder;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.context.creational.CreationalContextImpl;
import org.apache.webbeans.inject.OWBInjector;
import org.apache.webbeans.util.WebBeansUtil;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.Producer;
import javax.naming.Context;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.openejb.InjectionProcessor.unwrap;

public class PojoEndpoint extends CxfEndpoint {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.CXF, PojoEndpoint.class);
    private static final WebServiceContextResourceResolver WEB_SERVICE_CONTEXT_RESOURCE_RESOLVER = new WebServiceContextResourceResolver();
    private static final boolean JAXWS_AS_CDI_BEANS = SystemInstance.get().getOptions().get("openejb.cxf.cdi", true);

    private final ResourceInjector injector;
    private CreationalContextImpl toClean = null;

    public PojoEndpoint(ClassLoader loader, Bus bus, PortData port, Context context, Class<?> instance,
                        DestinationFactory httpTransportFactory,
                        Map<String, Object> bindings, ServiceConfiguration config) {
        super(bus, port, context, instance, httpTransportFactory, config);

        String bindingURI = null;
        if (port.getBindingID() != null) {
            bindingURI = JaxWsUtils.getBindingURI(port.getBindingID());
        }
        implInfo = new JaxWsImplementorInfoImpl(instance, bindingURI);

        serviceFactory = configureService(new JaxWsServiceFactoryBean(implInfo), config, CXF_JAXWS_PREFIX);
        serviceFactory.setBus(bus);
        serviceFactory.setServiceClass(instance);

        // install as first to overwrite annotations (wsdl-file, wsdl-port, wsdl-service)
        CxfServiceConfiguration configuration = new CxfServiceConfiguration(port);
        serviceFactory.getConfigurations().add(0, configuration);

        service = doServiceCreate();

        { // cleanup jax-ws injections
            final Iterator<Injection> injections = port.getInjections().iterator();
            while (injections.hasNext()) {
                final Injection next = injections.next();
                if (WebServiceContext.class.equals(type(loader, next))) {
                    injections.remove();
                }
            }
        }

        ResourceInjector injector = null;

        // instantiate and inject resources into service using the app classloader to be sure to get the right InitialContext
        implementor = null;

        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            final WebBeansContext webBeansContext = AppFinder.findAppContextOrWeb(
                    Thread.currentThread().getContextClassLoader(), AppFinder.WebBeansContextTransformer.INSTANCE);
            final BeanManagerImpl bm = webBeansContext == null ? null : webBeansContext.getBeanManagerImpl();
            if (bm != null && bm.isInUse()) { // try cdi bean
                if (JAXWS_AS_CDI_BEANS) {
                    try {
                        final Set<Bean<?>> beans = bm.getBeans(instance);
                        final Bean<?> bean = bm.resolve(beans);
                        CreationalContextImpl creationalContext = bm.createCreationalContext(bean);
                        if (bean != null) {
                            Bean<?> oldBean = creationalContext.putBean(bean);
                            try {
                                if (AbstractOwbBean.class.isInstance(bean)) {
                                    final AbstractOwbBean<?> aob = AbstractOwbBean.class.cast(bean);

                                    final Producer producer = aob.getProducer();
                                    implementor = producer.produce(creationalContext);
                                    if (producer instanceof InjectionTarget) {
                                        final InjectionTarget injectionTarget = (InjectionTarget) producer;
                                        injectionTarget.inject(implementor, creationalContext);
                                        injector = injectCxfResources(implementor); // we need it before postconstruct
                                        injectionTarget.postConstruct(implementor);
                                    }
                                    if (aob.getScope().equals(Dependent.class)) {
                                        creationalContext.addDependent(aob, instance);
                                    }
                                }
                            } finally {
                                creationalContext.putBean(oldBean);
                            }

                            // not a plain Pojo or CDI bean so it will fail on invocation because we use a JAXWSMethodInvoker
                            // instead of an EjbMethodInvoker - let's fallback to old style pojo bellow
                            // this should not happen anymore because the createPojoWsContainer in the CxfService will
                            // retrieve a previously deployed EJB and reuse it.
                            if (bean instanceof CdiEjbBean) {
                                implementor = null;
                            }

                            if (implementor != null && WebBeansUtil.isDependent(bean)) { // should be isPseudoScope but should be ok for jaxws
                                toClean = creationalContext;
                            }



                        /* This will never work because if bean == null, OWB will throw an exception on getReference
                        } else {
                            implementor = bm.getReference(bean, instance, creationalContext);
                            injector = injectCxfResources(implementor);
                         */
                        }


                    } catch (final Exception ie) {
                        LOGGER.info("Can't use cdi to create " + instance + " webservice: " + ie.getMessage());
                    }
                }
            }
            if (implementor == null) { // old pojo style
                final InjectionProcessor<Object> injectionProcessor = new InjectionProcessor<Object>(instance, port.getInjections(), null, null, unwrap(context), bindings);
                injectionProcessor.createInstance();
                implementor = injectionProcessor.getInstance();
                injector = injectCxfResources(implementor);
                if (!JAXWS_AS_CDI_BEANS && bm != null && bm.isInUse()) {
                    final CreationalContextImpl creationalContext = bm.createCreationalContext(null);
                    OWBInjector.inject(bm, implementor, null);
                    toClean = creationalContext;
                }
                injector.invokePostConstruct();
            }
        } catch (final Exception e) {
            throw new WebServiceException("Service resource injection failed", e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        this.injector = injector;
        service.setInvoker(new JAXWSMethodInvoker(implementor));
    }

    private Type type(final ClassLoader loader, final Injection next) {
        try {
            return loader.loadClass(next.getClassname()).getDeclaredField(next.getName()).getGenericType();
        } catch (final Throwable th) {
            return null; // ignore
        }
    }

    private ResourceInjector injectCxfResources(final Object implementor) {
        ResourceManager resourceManager = bus.getExtension(ResourceManager.class);

        final List<ResourceResolver> resolvers = resourceManager.getResourceResolvers();
        resourceManager = new DefaultResourceManager(resolvers);
        if (!resourceManager.getResourceResolvers().contains(WEB_SERVICE_CONTEXT_RESOURCE_RESOLVER)) {
            resourceManager.addResourceResolver(WEB_SERVICE_CONTEXT_RESOURCE_RESOLVER);
        }

        final ResourceInjector injector = new ResourceInjector(resourceManager);
        injector.inject(implementor);
        return injector;
    }

    protected void init() {
        // configure and inject handlers
        try {
            initHandlers();
        } catch (Exception e) {
            throw new WebServiceException("Error configuring handlers", e);
        }
    }

    public void stop() {
        // call handler preDestroy
        destroyHandlers();

        if (injector != null) {
            injector.invokePreDestroy();
        }

        if (toClean != null) {
            toClean.release();
        }

        // shutdown server
        super.stop();
    }
}
