/*
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

package org.apache.openejb.assembler.classic;

import org.apache.openejb.Injection;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.CoreUserTransaction;
import org.apache.openejb.core.JndiFactory;
import org.apache.openejb.core.ParentClassLoaderFinder;
import org.apache.openejb.core.TransactionSynchronizationRegistryWrapper;
import org.apache.openejb.core.ivm.naming.ClassReference;
import org.apache.openejb.core.ivm.naming.CrossClassLoaderJndiReference;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.core.ivm.naming.JaxWsServiceReference;
import org.apache.openejb.core.ivm.naming.JndiReference;
import org.apache.openejb.core.ivm.naming.JndiUrlReference;
import org.apache.openejb.core.ivm.naming.LazyObjectReference;
import org.apache.openejb.core.ivm.naming.MapObjectReference;
import org.apache.openejb.core.ivm.naming.ObjectReference;
import org.apache.openejb.core.ivm.naming.PersistenceContextReference;
import org.apache.openejb.core.ivm.naming.Reference;
import org.apache.openejb.core.ivm.naming.SystemComponentReference;
import org.apache.openejb.core.ivm.naming.URLReference;
import org.apache.openejb.core.webservices.HandlerChainData;
import org.apache.openejb.core.webservices.PortRefData;
import org.apache.openejb.core.webservices.ServiceRefData;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.persistence.JtaEntityManager;
import org.apache.openejb.persistence.JtaEntityManagerRegistry;
import org.apache.openejb.rest.ThreadLocalContextManager;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Classes;
import org.apache.openejb.util.IntrospectionSupport;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.InjectableBeanManager;

import jakarta.annotation.ManagedBean;
import jakarta.ejb.EJBContext;
import jakarta.ejb.TimerService;
import jakarta.ejb.spi.HandleDelegate;
import jakarta.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NamingException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Providers;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.WebServiceContext;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * TODO: This class is essentially an over glorified sym-linker.  The names we were linking to are no longer guaranteed to be what we assume them to be.  We need to come up with a
 * different internal naming structure for the global JNDI and finally create the default which will be the default symlinked version of all the components.
 */
public class JndiEncBuilder {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, JndiEncBuilder.class.getPackage().getName());

    public static enum JndiScope {
        comp,
        module,
        app,
        global,
    }

    private final boolean beanManagedTransactions;
    private final JndiEncInfo jndiEnc;
    private final URI moduleUri;
    private final String moduleId;
    private final String uniqueId;
    private final Collection<Injection> injections;
    private final ClassLoader classLoader;
    private final Properties properties;

    private boolean useCrossClassLoaderRef = true;
    private boolean client;

    public JndiEncBuilder(final JndiEncInfo jndiEnc, final Collection<Injection> injections, final String moduleId, final URI moduleUri,
                          final String uniqueId, final ClassLoader classLoader, final Properties properties) throws OpenEJBException {
        this(jndiEnc, injections, null, moduleId, moduleUri, uniqueId, classLoader, properties);
    }

    public JndiEncBuilder(final JndiEncInfo jndiEnc, final Collection<Injection> injections, final String transactionType,
                          final String moduleId, final URI moduleUri, final String uniqueId, final ClassLoader classLoader, final Properties properties) throws OpenEJBException {
        this.jndiEnc = jndiEnc;
        this.properties = properties;
        this.injections = injections;
        beanManagedTransactions = transactionType != null && transactionType.equalsIgnoreCase("Bean");

        this.moduleId = moduleId;
        this.moduleUri = moduleUri;

        this.uniqueId = uniqueId;
        this.classLoader = classLoader;
    }

    public boolean isUseCrossClassLoaderRef() {
        return useCrossClassLoaderRef;
    }

    public void setUseCrossClassLoaderRef(final boolean useCrossClassLoaderRef) {
        this.useCrossClassLoaderRef = useCrossClassLoaderRef;
    }

    public boolean isClient() {
        return client;
    }

    public void setClient(final boolean client) {
        this.client = client;
    }

    public Context build(final JndiScope type) throws OpenEJBException {
        final Map<String, Object> bindings = buildBindings(type);

        return build(bindings);
    }

    public Context build(final Map<String, Object> bindings) throws SystemException {
        final JndiFactory jndiFactory = SystemInstance.get().getComponent(JndiFactory.class);

        if (SystemInstance.get().hasProperty("openejb.geronimo")) {
            return jndiFactory.createComponentContext(new HashMap());
        }

        return jndiFactory.createComponentContext(bindings);
    }

    public Map<String, Object> buildBindings(final JndiScope type) throws OpenEJBException {
        final Map<String, Object> bindings = buildMap(type);
        switch (type) {
            case comp:
                addSpecialCompBindings(bindings);
                break;
            case module:
                addSpecialModuleBindings(bindings);
                break;
            case app:
                addSpecialAppBindings(bindings);
                break;
            case global:
                addSpecialGlobalBindings(bindings);
                break;
        }
        return bindings;
    }

    public Map<String, Object> buildMap(final JndiScope scope) throws OpenEJBException {
        final Map<String, Object> bindings = new TreeMap<>(); // let it be sorted for real binding

        // get JtaEntityManagerRegistry
        final JtaEntityManagerRegistry jtaEntityManagerRegistry = SystemInstance.get().getComponent(JtaEntityManagerRegistry.class);

        for (final EjbReferenceInfo referenceInfo : jndiEnc.ejbReferences) {

            final Reference reference;

            if (referenceInfo.location != null) {
                reference = buildReferenceLocation(referenceInfo.location);
            } else if (referenceInfo.ejbDeploymentId == null) {
                reference = new LazyEjbReference(new Ref(referenceInfo), moduleUri, useCrossClassLoaderRef);
            } else {
                final String jndiName = "openejb/Deployment/" + JndiBuilder.format(referenceInfo.ejbDeploymentId, referenceInfo.interfaceClassName, referenceInfo.localbean ? InterfaceType.LOCALBEAN : InterfaceType.BUSINESS_REMOTE);
                if (useCrossClassLoaderRef && referenceInfo.externalReference) {
                    reference = new CrossClassLoaderJndiReference(jndiName);
                } else {
                    reference = new IntraVmJndiReference(jndiName);
                }
            }
            bindings.put(normalize(referenceInfo.referenceName), reference);
        }

        for (final EjbReferenceInfo referenceInfo : jndiEnc.ejbLocalReferences) {
            final Reference reference;

            if (referenceInfo.location != null) {
                reference = buildReferenceLocation(referenceInfo.location);
            } else if (referenceInfo.ejbDeploymentId == null) {
                reference = new LazyEjbReference(new Ref(referenceInfo), moduleUri, false);
            } else {
                final String jndiName = "openejb/Deployment/" + JndiBuilder.format(referenceInfo.ejbDeploymentId, referenceInfo.interfaceClassName, referenceInfo.localbean ? InterfaceType.LOCALBEAN : InterfaceType.BUSINESS_LOCAL);
                reference = new IntraVmJndiReference(jndiName);
            }
            bindings.put(normalize(referenceInfo.referenceName), reference);
        }

        for (final EnvEntryInfo entry : jndiEnc.envEntries) {

            if (entry.location != null) {
                final Reference reference = buildReferenceLocation(entry.location);
                bindings.put(normalize(entry.referenceName), reference);
                continue;
            }

            //It is possible that the value and location are both null, as it is allowed to use @Resource(name="java:global/env/abc") with no value is specified in DD            
            if (entry.value == null) {
                continue;
            }

            try {
                final Class type = Classes.deprimitivize(getType(entry.type, entry));
                final Object obj;
                if (type == String.class) {
                    obj = new String(entry.value);
                } else if (type == Double.class) {
                    obj = new Double(entry.value);
                } else if (type == Integer.class) {
                    obj = new Integer(entry.value);
                } else if (type == Long.class) {
                    obj = new Long(entry.value);
                } else if (type == Float.class) {
                    obj = new Float(entry.value);
                } else if (type == Short.class) {
                    obj = new Short(entry.value);
                } else if (type == Boolean.class) {
                    obj = Boolean.valueOf(entry.value);
                } else if (type == Byte.class) {
                    obj = new Byte(entry.value);
                } else if (type == Character.class) {
                    final StringBuilder sb = new StringBuilder(entry.value + " ");
                    obj = sb.charAt(0);
                } else if (type == URL.class) {
                    obj = new URL(entry.value);
                } else if (type == Class.class) {
                    obj = new ClassReference(entry.value.trim());
                } else if (type.isEnum()) {
                    obj = Enum.valueOf(type, entry.value.trim());
                } else {
                    throw new IllegalArgumentException("Invalid env-entry-type " + type);
                }

                bindings.put(normalize(entry.referenceName), obj);
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("The env-entry-value for entry " + entry.referenceName + " was not recognizable as type " + entry.type + ". Received Message: " + e.getLocalizedMessage(), e);
            } catch (final MalformedURLException e) {
                throw new IllegalArgumentException("URL for reference " + entry.referenceName + " was not a valid URL: " + entry.value, e);
            }
        }

        for (final ResourceReferenceInfo referenceInfo : jndiEnc.resourceRefs) {
            if (!(referenceInfo instanceof ContextReferenceInfo)) {
                if (referenceInfo.location != null) {
                    final Reference reference = buildReferenceLocation(referenceInfo.location);
                    bindings.put(normalize(referenceInfo.referenceName), reference);
                    continue;
                }

                final Class<?> type = getType(referenceInfo.referenceType, referenceInfo);

                final Object reference;
                if (URL.class.equals(type)) {
                    reference = new URLReference(referenceInfo.resourceID);
                } else if (type.isAnnotationPresent(ManagedBean.class)) {
                    final ManagedBean managed = type.getAnnotation(ManagedBean.class);
                    final String name = managed.value().length() == 0 ? type.getSimpleName() : managed.value();
                    reference = new LinkRef("module/" + name);
                } else if (referenceInfo.resourceID != null) {
                    final String jndiName = "openejb/Resource/" + referenceInfo.resourceID;
                    reference = new IntraVmJndiReference(jndiName);
                } else {
                    final String jndiName = "openejb/Resource/" + referenceInfo.referenceName;
                    reference = new IntraVmJndiReference(jndiName);
                }

                bindings.put(normalize(referenceInfo.referenceName), reference);
            } else {
                final Class<?> type = getType(referenceInfo.referenceType, referenceInfo);
                final Object reference;

                if (Request.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.REQUEST);
                } else if (HttpServletRequest.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.HTTP_SERVLET_REQUEST);
                } else if (ServletRequest.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.SERVLET_REQUEST);
                } else if (UriInfo.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.URI_INFO);
                } else if (HttpHeaders.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.HTTP_HEADERS);
                } else if (SecurityContext.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.SECURITY_CONTEXT);
                } else if (ContextResolver.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.CONTEXT_RESOLVER);
                } else if (Providers.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.PROVIDERS);
                } else if (ServletConfig.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.SERVLET_CONFIG);
                } else if (ServletContext.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.SERVLET_CONTEXT);
                } else if (HttpServletResponse.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.HTTP_SERVLET_RESPONSE);
                } else if (jakarta.ws.rs.container.ResourceInfo.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.RESOURCE_INFO);
                } else if (ResourceContext.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.RESOURCE_CONTEXT);
                } else if (Configuration.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.CONFIGURATION);
                } else if (Application.class.equals(type)) {
                    reference = new ObjectReference(ThreadLocalContextManager.APPLICATION);
                } else {
                    reference = new MapObjectReference(ThreadLocalContextManager.OTHERS, referenceInfo.referenceType);
                }
                bindings.put(normalize(referenceInfo.referenceName), reference);
            }
        }

        for (final ResourceEnvReferenceInfo referenceInfo : jndiEnc.resourceEnvRefs) {

            if (referenceInfo.location != null) {
                final Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.referenceName), reference);
                continue;
            }

            final Class<?> type = getType(referenceInfo.resourceEnvRefType, referenceInfo);
            final Object reference;

            if (EJBContext.class.isAssignableFrom(type)) {
                final String jndiName = "comp/EJBContext";
                reference = new LinkRef(jndiName);

                // Let the container bind this into JNDI
                if (jndiName.equals(referenceInfo.referenceName)) {
                    continue;
                }
            } else if (Validator.class.equals(type)) {
                final String jndiName = "comp/Validator";
                reference = new LinkRef(jndiName);
            } else if (ValidatorFactory.class.equals(type)) {
                final String jndiName = "comp/ValidatorFactory";
                reference = new LinkRef(jndiName);
            } else if (WebServiceContext.class.equals(type)) {
                final String jndiName = "comp/WebServiceContext";
                reference = new LinkRef(jndiName);
            } else if (TimerService.class.equals(type)) {
                final String jndiName = "comp/TimerService";
                reference = new LinkRef(jndiName);

            } else if (BeanManager.class.equals(type)) {
                reference = new LazyObjectReference<>(new BeanManagerLazyReference());

            } else if (UserTransaction.class.equals(type)) {
                reference = new IntraVmJndiReference("comp/UserTransaction");
            } else if (referenceInfo.resourceID != null) {
                final String jndiName = "openejb/Resource/" + referenceInfo.resourceID;
                reference = new IntraVmJndiReference(jndiName);
            } else {
                final String jndiName = "openejb/Resource/" + referenceInfo.referenceName;
                reference = new IntraVmJndiReference(jndiName);
            }
            bindings.put(normalize(referenceInfo.referenceName), reference);
        }

        for (final PersistenceUnitReferenceInfo referenceInfo : jndiEnc.persistenceUnitRefs) {
            if (referenceInfo.location != null) {
                final Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.referenceName), reference);
                continue;
            }

            final String jndiName = PersistenceBuilder.getOpenEJBJndiName(referenceInfo.unitId);
            final Reference reference = new IntraVmJndiReference(jndiName);
            bindings.put(normalize(referenceInfo.referenceName), reference);
        }

        for (final PersistenceContextReferenceInfo contextInfo : jndiEnc.persistenceContextRefs) {
            if (contextInfo.location != null) {
                final Reference reference = buildReferenceLocation(contextInfo.location);
                bindings.put(normalize(contextInfo.referenceName), reference);
                continue;
            }

            final Context context = SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext();
            final EntityManagerFactory factory;
            try {
                final String jndiName = PersistenceBuilder.getOpenEJBJndiName(contextInfo.unitId);
                factory = (EntityManagerFactory) context.lookup(jndiName);
            } catch (final NamingException e) {
                throw new OpenEJBException("PersistenceUnit '" + contextInfo.unitId + "' not found for EXTENDED ref '" + contextInfo.referenceName + "'");
            }

            final JtaEntityManager jtaEntityManager = new JtaEntityManager(
                    contextInfo.persistenceUnitName, jtaEntityManagerRegistry, factory, contextInfo.properties, contextInfo.extended, contextInfo.synchronizationType);
            final Reference reference = new PersistenceContextReference(jtaEntityManager);
            bindings.put(normalize(contextInfo.referenceName), reference);
        }

        for (final ServiceReferenceInfo referenceInfo : jndiEnc.serviceRefs) {
            if (referenceInfo.location != null) {
                final Reference reference = buildReferenceLocation(referenceInfo.location);
                bindings.put(normalize(referenceInfo.referenceName), reference);
                continue;
            }

            // load service class which is used to construct the port
            Class<? extends Service> serviceClass = Service.class;
            if (referenceInfo.serviceType != null) {
                try {
                    serviceClass = classLoader.loadClass(referenceInfo.serviceType).asSubclass(Service.class);
                } catch (final Exception e) {
                    throw new OpenEJBException("Could not load service type class " + referenceInfo.serviceType, e);
                }
            }

            // load the reference class which is the ultimate type of the port
            Class<?> referenceClass = null;
            if (referenceInfo.referenceType != null) {
                try {
                    referenceClass = classLoader.loadClass(referenceInfo.referenceType);
                } catch (final Exception e) {
                    throw new OpenEJBException("Could not load reference type class " + referenceInfo.referenceType, e);
                }
            }

            // if ref class is a subclass of Service, use it for the service class
            if (referenceClass != null && Service.class.isAssignableFrom(referenceClass)) {
                serviceClass = referenceClass.asSubclass(Service.class);
            }

            // determine the location of the wsdl file
            URL wsdlUrl = null;
            if (referenceInfo.wsdlFile != null) {
                try {
                    wsdlUrl = new URL(referenceInfo.wsdlFile);
                } catch (final MalformedURLException e) {
                    wsdlUrl = classLoader.getResource(referenceInfo.wsdlFile);
                    if (wsdlUrl == null) {
                        logger.warning("Error obtaining WSDL: " + referenceInfo.wsdlFile, e);
                    }

                }
            }

            // port refs
            final List<PortRefData> portRefs = new ArrayList<>(referenceInfo.portRefs.size());
            for (final PortRefInfo portRefInfo : referenceInfo.portRefs) {
                final PortRefData portRef = new PortRefData();
                portRef.setQName(portRefInfo.qname);
                portRef.setServiceEndpointInterface(portRefInfo.serviceEndpointInterface);
                portRef.setEnableMtom(portRefInfo.enableMtom);
                portRef.getProperties().putAll(portRefInfo.properties);
                portRefs.add(portRef);
            }

            // create the handle chains
            List<HandlerChainData> handlerChains = null;
            if (!referenceInfo.handlerChains.isEmpty()) {
                handlerChains = WsBuilder.toHandlerChainData(referenceInfo.handlerChains, classLoader);
            }

            if (!client) {
                final Reference reference = new JaxWsServiceReference(referenceInfo.id,
                    referenceInfo.serviceQName,
                    serviceClass, referenceInfo.portQName,
                    referenceClass,
                    wsdlUrl,
                    portRefs,
                    handlerChains,
                    injections,
                    properties);
                bindings.put(normalize(referenceInfo.referenceName), reference);
            } else {
                final ServiceRefData serviceRefData = new ServiceRefData(referenceInfo.id,
                    referenceInfo.serviceQName,
                    serviceClass, referenceInfo.portQName,
                    referenceClass,
                    wsdlUrl,
                    handlerChains,
                    portRefs);
                bindings.put(normalize(referenceInfo.referenceName), serviceRefData);
            }
        }

        final OpenEjbConfiguration config = SystemInstance.get().getComponent(OpenEjbConfiguration.class);

        if (config != null) {

            for (final ResourceInfo resource : config.facilities.resources) {
                final String jndiName = resource.jndiName;
                if (jndiName != null && !jndiName.isEmpty() && isNotGobalOrIsHoldByThisApp(resource, scope)) {
                    final String refName = "openejb/Resource/" + resource.id;
                    final Object reference = new IntraVmJndiReference(refName);
                    final String boundName = normalize(jndiName);
                    bindings.put(boundName, reference);
                }
            }

        }
        return bindings;
    }

    // we don't want to bind globally a global resource multiple times in the Assembler
    // if the datasource if defined globally in the currently deployed app originAppname hould not be null
    private boolean isNotGobalOrIsHoldByThisApp(final ResourceInfo info, final JndiScope scope) {
        return !info.jndiName.startsWith("global/")
            || info.originAppName != null && info.originAppName.equals(moduleId) && JndiScope.global.equals(scope);
    }

    private void addSpecialCompBindings(final Map<String, Object> bindings) {
        // bind TransactionManager
        final TransactionManager transactionManager = SystemInstance.get().getComponent(TransactionManager.class);
        bindings.put("comp/TransactionManager", transactionManager);

        // bind TransactionSynchronizationRegistry
        bindings.put("comp/TransactionSynchronizationRegistry", new TransactionSynchronizationRegistryWrapper());

        try {
            bindings.put("comp/ORB", new SystemComponentReference(ParentClassLoaderFinder.Helper.get().loadClass("org.omg.CORBA.ORB")));
        } catch (final NoClassDefFoundError | ClassNotFoundException e) {
            // no corba, who does recall what it is today anyway :D
        }
        bindings.put("comp/HandleDelegate", new SystemComponentReference(HandleDelegate.class));

        // bind bean validation objects
        bindings.put("comp/ValidatorFactory", new IntraVmJndiReference(Assembler.VALIDATOR_FACTORY_NAMING_CONTEXT + uniqueId));
        bindings.put("comp/Validator", new IntraVmJndiReference(Assembler.VALIDATOR_NAMING_CONTEXT + uniqueId));

        // bind UserTransaction if bean managed transactions
        if (beanManagedTransactions) {
            final UserTransaction userTransaction = new CoreUserTransaction(transactionManager);
            bindings.put("comp/UserTransaction", userTransaction);
        }
    }

    private void addSpecialModuleBindings(final Map<String, Object> bindings) {
        if (moduleId != null) {
            bindings.put("module/ModuleName", moduleId);
        }
        // ensure the bindings will be non-empty
        if (bindings.isEmpty()) {
            bindings.put("module/dummy", "dummy");
        }
    }

    private void addSpecialAppBindings(final Map<String, Object> bindings) {
        if (moduleId != null) {
            bindings.put("app/AppName", moduleId);
        }
        // ensure the bindings will be non-empty
        if (bindings.isEmpty()) {
            bindings.put("app/dummy", "dummy");
        }
    }

    private void addSpecialGlobalBindings(final Map<String, Object> bindings) {
        // ensure the bindings will be non-empty
        if (bindings.isEmpty()) {
            bindings.put("global/dummy", "dummy");
        }
    }

    public static boolean bindingExists(final Context context, final Name contextName) {
        try {
            return context.lookup(contextName) != null;
        } catch (final NamingException e) {
            // no-op
        }
        return false;
    }

    private Reference buildReferenceLocation(final ReferenceLocationInfo location) {
        if (location.jndiProviderId != null) {
            final String subContextName = "openejb/remote_jndi_contexts/" + location.jndiProviderId;
            return new JndiReference(subContextName, location.jndiName);
        } else {
            return new JndiUrlReference(location.jndiName);
        }
    }

    private String normalize(final String name) {
        //currently all names seem to be normalized properly
        return name;
    }

    private Class getType(final String type, final InjectableInfo injectable) throws OpenEJBException {
        if (type != null) {
            try {
                return classLoader.loadClass(type.trim());
            } catch (final ClassNotFoundException e) {
                throw new OpenEJBException("Unable to load type '" + type + "' for " + injectable.referenceName);
            }
        }
        return inferType(injectable);
    }

    private Class inferType(final InjectableInfo injectable) throws OpenEJBException {
        for (final InjectionInfo injection : injectable.targets) {
            try {
                final Class<?> target = classLoader.loadClass(injection.className.trim());
                return IntrospectionSupport.getPropertyType(target, injection.propertyName.trim());
            } catch (final ClassNotFoundException | NoSuchFieldException e) {
                // ignore
            }
        }
        throw new OpenEJBException("Unable to infer type for " + injectable.referenceName);
    }

    private static class Ref implements EjbResolver.Reference, Serializable {
        private final EjbReferenceInfo info;

        public Ref(final EjbReferenceInfo info) {
            this.info = info;
        }

        public String getEjbLink() {
            return info.link;
        }

        public String getHome() {
            return info.homeClassName;
        }

        public String getInterface() {
            return info.interfaceClassName;
        }

        public String getMappedName() {
            return null;
        }

        public String getName() {
            return info.referenceName;
        }

        public EjbResolver.Type getRefType() {
            if (info instanceof EjbLocalReferenceInfo) {
                return EjbResolver.Type.LOCAL;
            } else if (info.homeClassName != null) {
                return EjbResolver.Type.REMOTE;
            } else {
                return EjbResolver.Type.UNKNOWN;
            }
        }
    }

    public static class BeanManagerLazyReference implements Callable<BeanManager> {
        @Override
        public BeanManager call() throws Exception {
            return new InjectableBeanManager(WebBeansContext.currentInstance().getBeanManagerImpl());
        }
    }
}
