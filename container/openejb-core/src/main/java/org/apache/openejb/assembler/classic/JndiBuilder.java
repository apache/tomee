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

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference;
import org.apache.openejb.core.ivm.naming.BusinessLocalReference;
import org.apache.openejb.core.ivm.naming.BusinessRemoteReference;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.core.ivm.naming.ObjectReference;
import org.apache.openejb.loader.Options;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.StringTemplate;
import org.apache.openejb.util.Strings;

import jakarta.ejb.embeddable.EJBContainer;
import jakarta.jms.MessageListener;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import java.lang.reflect.Constructor;
import java.rmi.Remote;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static org.apache.openejb.util.Classes.packageName;


/**
 * @version $Rev$ $Date$
 */
public class JndiBuilder {

    public static final String DEFAULT_NAME_KEY = "default";

    final boolean embeddedEjbContainerApi;

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, JndiBuilder.class.getPackage().getName());
    private static final boolean USE_OLD_JNDI_NAMES = SystemInstance.get().getOptions().get("openejb.use-old-jndi-names", false);

    private final Context openejbContext;
    private static final String JNDINAME_STRATEGY_CLASS = "openejb.jndiname.strategy.class";
    private static final String JNDINAME_FAILONCOLLISION = "openejb.jndiname.failoncollision";
    private final boolean failOnCollision;

    public JndiBuilder(final Context openejbContext) {
        this.openejbContext = openejbContext;

        final Options options = SystemInstance.get().getOptions();

        failOnCollision = options.get(JNDINAME_FAILONCOLLISION, true);
        embeddedEjbContainerApi = options.get(EJBContainer.class.getName(), false);
    }

    public void build(final EjbJarInfo ejbJar, final HashMap<String, BeanContext> deployments) {

        final JndiNameStrategy strategy = createStrategy(ejbJar, deployments);

        for (final EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
            final BeanContext beanContext = deployments.get(beanInfo.ejbDeploymentId);
            strategy.begin(beanContext);
            try {
                bind(ejbJar, beanContext, beanInfo, strategy);
            } finally {
                strategy.end();
            }
        }
    }

    public static JndiNameStrategy createStrategy(final EjbJarInfo ejbJar, final Map<String, BeanContext> deployments) {
        final Options options = new Options(ejbJar.properties, SystemInstance.get().getOptions());

        final Class strategyClass = options.get(JNDINAME_STRATEGY_CLASS, TemplatedStrategy.class);

        final String strategyClassName = strategyClass.getName();

        try {
            try {
                final Constructor constructor = strategyClass.getConstructor(EjbJarInfo.class, Map.class);
                return (JndiNameStrategy) constructor.newInstance(ejbJar, deployments);
            } catch (final NoSuchMethodException e) {
                // no-op
            }

            final Constructor constructor = strategyClass.getConstructor();
            return (JndiNameStrategy) constructor.newInstance();
        } catch (final InstantiationException e) {
            throw new IllegalStateException("Could not instantiate JndiNameStrategy: " + strategyClassName, e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Could not access JndiNameStrategy: " + strategyClassName, e);
        } catch (final Throwable t) {
            throw new IllegalStateException("Could not create JndiNameStrategy: " + strategyClassName, t);
        }
    }

    public interface JndiNameStrategy {

        enum Interface {

            REMOTE_HOME(InterfaceType.EJB_HOME, "RemoteHome", "home", ""),
            LOCAL_HOME(InterfaceType.EJB_LOCAL_HOME, "LocalHome", "local-home", "Local"),
            BUSINESS_LOCAL(InterfaceType.BUSINESS_LOCAL, "Local", "business-local", "BusinessLocal"),
            LOCALBEAN(InterfaceType.LOCALBEAN, "LocalBean", "localbean", "LocalBean"),
            BUSINESS_REMOTE(InterfaceType.BUSINESS_REMOTE, "Remote", "business-remote", "BusinessRemote"),
            SERVICE_ENDPOINT(InterfaceType.SERVICE_ENDPOINT, "Endpoint", "service-endpoint", "ServiceEndpoint");

            private final InterfaceType type;
            private final String annotatedName;
            private final String xmlName;
            private final String xmlNameCc;
            private final String openejbLegacy;

            Interface(final InterfaceType type, final String annotatedName, final String xmlName, final String openejbLegacy) {
                this.type = type;
                this.annotatedName = annotatedName;
                this.xmlName = xmlName;
                this.xmlNameCc = Strings.camelCase(xmlName);
                this.openejbLegacy = openejbLegacy;
            }


            public InterfaceType getType() {
                return type;
            }

            public String getAnnotationName() {
                return annotatedName;
            }

            public String getXmlName() {
                return xmlName;
            }

            public String getXmlNameCc() {
                return xmlNameCc;
            }

            public String getOpenejbLegacy() {
                return openejbLegacy;
            }

        }

        void begin(BeanContext beanContext);

        String getName(Class interfce, String key, Interface type);

        Map<String, String> getNames(Class interfce, Interface type);

        void end();
    }

    // TODO: put these into the classpath and get them with xbean-finder

    public static class TemplatedStrategy implements JndiNameStrategy {
        private static final String JNDINAME_FORMAT = "openejb.jndiname.format";
        private static final String KEYS = "default,local,global,app";
        private final StringTemplate template;
        private final HashMap<String, EnterpriseBeanInfo> beanInfos;

        // Set in begin()
        private BeanContext bean;

        // Set in begin()
        private HashMap<String, Map<String, StringTemplate>> templates;

        private String format;
        private Map<String, String> appContext;
        private HashMap<String, String> beanContext;

        public TemplatedStrategy(final EjbJarInfo ejbJarInfo, final Map<String, BeanContext> deployments) {
            final Options options = new Options(ejbJarInfo.properties, SystemInstance.get().getOptions());

            format = options.get(JNDINAME_FORMAT, "{deploymentId}{interfaceType.annotationName}");

            { // illegal format check
                final int index = format.indexOf(':');
                if (index > -1) {
                    logger.error("Illegal " + JNDINAME_FORMAT + " contains a colon ':'.  Everything before the colon will be removed, '" + format + "' ");
                    format = format.substring(index + 1);
                }
            }

            this.template = new StringTemplate(format);

            beanInfos = new HashMap<>();
            for (final EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
                beanInfos.put(beanInfo.ejbDeploymentId, beanInfo);
            }

            final Iterator<BeanContext> it = deployments.values().iterator();
            if (!it.hasNext()) {
                return;
            }

            // TODO we should just pass in the ModuleContext
            final ModuleContext moduleContext = it.next().getModuleContext();

            appContext = new HashMap<>();
            putAll(appContext, SystemInstance.get().getProperties());
            putAll(appContext, moduleContext.getAppContext().getProperties());
            putAll(appContext, moduleContext.getProperties());

            appContext.put("appName", moduleContext.getAppContext().getId());
            appContext.put("appId", moduleContext.getAppContext().getId());

            appContext.put("moduleName", moduleContext.getId());
            appContext.put("moduleId", moduleContext.getId());
        }

        private void putAll(final Map<String, String> map, final Properties properties) {
            for (final Map.Entry<Object, Object> e : properties.entrySet()) {
                if (!(e.getValue() instanceof String)) {
                    continue;
                }
                if (!(e.getKey() instanceof String)) {
                    continue;
                }

                map.put((String) e.getKey(), (String) e.getValue());
            }
        }

        private Map<String, StringTemplate> addTemplate(final Map<String, StringTemplate> map, final String key, final StringTemplate template) {
            Map<String, StringTemplate> m = map;
            if (m == null) {
                m = new TreeMap<>();
            }
            m.put(key, template);
            return m;
        }

        public void begin(final BeanContext bean) {
            this.bean = bean;

            final EnterpriseBeanInfo beanInfo = beanInfos.get(bean.getDeploymentID());

            templates = new HashMap<>();
            templates.put("", addTemplate(null, DEFAULT_NAME_KEY, template));

            for (final JndiNameInfo nameInfo : beanInfo.jndiNamess) {
                String intrface = nameInfo.intrface;
                if (intrface == null) {
                    intrface = "";
                }
                templates.put(intrface, addTemplate(templates.get(intrface), getType(nameInfo.name), new StringTemplate(nameInfo.name)));
            }
            beanInfo.jndiNames.clear();
            beanInfo.jndiNamess.clear();

            this.beanContext = new HashMap<>(appContext);
            putAll(this.beanContext, bean.getProperties());
            this.beanContext.put("ejbType", bean.getComponentType().name());
            this.beanContext.put("ejbClass", bean.getBeanClass().getName());
            this.beanContext.put("ejbClass.simpleName", bean.getBeanClass().getSimpleName());
            this.beanContext.put("ejbClass.packageName", packageName(bean.getBeanClass()));
            this.beanContext.put("ejbName", bean.getEjbName());
            this.beanContext.put("deploymentId", bean.getDeploymentID().toString());
        }

        private static String getType(final String name) {
            int start = 0;
            if (name.charAt(0) == '/') {
                start = 1;
            }
            final int end = name.substring(start).indexOf('/');
            if (end < 0) {
                return DEFAULT_NAME_KEY;
            }
            return name.substring(start, end);
        }

        public void end() {
        }

        public String getName(final Class interfce, final String key, final Interface type) {
            Map<String, StringTemplate> template = templates.get(interfce.getName());
            if (template == null) {
                template = templates.get(type.getAnnotationName());
            }
            if (template == null) {
                template = templates.get("");
            }

            final Map<String, String> contextData = new HashMap<>(beanContext);
            contextData.put("interfaceType", type.getAnnotationName());
            contextData.put("interfaceType.annotationName", type.getAnnotationName());
            contextData.put("interfaceType.annotationNameLC", type.getAnnotationName().toLowerCase());
            contextData.put("interfaceType.xmlName", type.getXmlName());
            contextData.put("interfaceType.xmlNameCc", type.getXmlNameCc());
            contextData.put("interfaceType.openejbLegacyName", type.getOpenejbLegacy());
            contextData.put("interfaceClass", interfce.getName());
            contextData.put("interfaceClass.simpleName", interfce.getSimpleName());
            contextData.put("interfaceClass.packageName", packageName(interfce));

            StringTemplate stringTemplate = null;

            if (template.containsKey(key)) {
                stringTemplate = template.get(key);
            } else {
                stringTemplate = template.get(DEFAULT_NAME_KEY);
            }

            if (stringTemplate == null) {
                stringTemplate = template.values().iterator().next();
            }

            return stringTemplate.apply(contextData);

        }

        @Override
        public Map<String, String> getNames(final Class interfce, final Interface type) {
            final Map<String, String> names = new HashMap<>();
            for (final String key : KEYS.split(",")) {
                names.put(key, getName(interfce, key, type));
            }
            return names;
        }
    }

    public static class LegacyAddedSuffixStrategy implements JndiNameStrategy {
        private BeanContext beanContext;

        public void begin(final BeanContext beanContext) {
            this.beanContext = beanContext;
        }

        public void end() {
        }

        public String getName(final Class interfce, final String key, final Interface type) {
            String id = String.valueOf(beanContext.getDeploymentID());
            if (id.charAt(0) == '/') {
                id = id.substring(1);
            }

            switch (type) {
                case REMOTE_HOME:
                    return id;
                case LOCAL_HOME:
                    return id + "Local";
                case BUSINESS_LOCAL:
                    return id + "BusinessLocal";
                case BUSINESS_REMOTE:
                    return id + "BusinessRemote";
            }
            return id;
        }

        @Override
        public Map<String, String> getNames(final Class interfce, final Interface type) {
            final Map<String, String> names = new HashMap<>();
            names.put("", getName(interfce, DEFAULT_NAME_KEY, type));
            return names;
        }
    }

    public void bind(final EjbJarInfo ejbJarInfo, final BeanContext bean, final EnterpriseBeanInfo beanInfo, final JndiNameStrategy strategy) {

        // in an ear ejbmodule, webmodule etc can get the same name so avoid Comp binding issue
        // and we shouldn't need it
        if (BeanContext.Comp.class.equals(bean.getBeanClass())) {
            return;
        }

        final Bindings bindings = new Bindings();
        bean.set(Bindings.class, bindings);

        Reference simpleNameRef = null;

        final Object id = bean.getDeploymentID();

        // Our openejb.jndiname.format concept works such that there doesn't need to be one explicit jndi name
        // for each view that the bean may offer.  If the user configured a name that results in few possible
        // jndi names than views, this is ok.  The 'optionalBind' method will do its best and log the results.
        // This openejb.jndiname.format affects only the OpenEJB-specific global jndi tree.
        //
        // Should there be a so described "deficit" of names, we give precedence to the most universal and local first
        // Essentially this:
        //     1. Local Bean view as it implements all business interfaces of the bean, local or remote
        //     2. The business local view -- "the" is applicable as create proxies with all possible local interfaces
        //     3. The business remote view -- same note on "the" as above
        //     4. The EJBLocalHome
        //     5. The EJBHome
        //
        // This ordering also has an affect on which view wins the "java:global/{app}/{module}/{ejbName}" jndi name.
        // In the case that the bean has just one view, the name refers to that view.  Otherwise, the name is unspecified

        try {
            if (bean.isLocalbean()) {
                final Class beanClass = bean.getBeanClass();

                final BeanContext.BusinessLocalBeanHome home = bean.getBusinessLocalBeanHome();
                final BusinessLocalBeanReference ref = new BusinessLocalBeanReference(home);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(id, beanClass.getName(), InterfaceType.LOCALBEAN));

                // if the user inject the EJB using a parent class
                if (!bean.getBeanClass().isInterface()) {
                    for (Class<?> clazz = bean.getBeanClass().getSuperclass(); !clazz.equals(Object.class); clazz = clazz.getSuperclass()) {
                        optionalBind(bindings, ref, "openejb/Deployment/" + format(id, clazz.getName(), InterfaceType.LOCALBEAN));
                    }
                }

                final String internalName = "openejb/Deployment/" + format(id, beanClass.getName(), InterfaceType.BUSINESS_LOCALBEAN_HOME);
                bind(internalName, ref, bindings, beanInfo, beanClass);

                final String name = strategy.getName(beanClass, DEFAULT_NAME_KEY, JndiNameStrategy.Interface.LOCALBEAN);
                bind("openejb/local/" + name, ref, bindings, beanInfo, beanClass);
                bindJava(bean, beanClass, ref, bindings, beanInfo);
                if (USE_OLD_JNDI_NAMES) {
                    bean.getModuleContext().getAppContext().getBindings().put(name, ref);
                }

                simpleNameRef = ref;
            }
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException("Unable to bind business remote deployment in jndi.", e);
        }

        try {

            for (final Class interfce : bean.getBusinessLocalInterfaces()) {

                final BeanContext.BusinessLocalHome home = bean.getBusinessLocalHome(interfce);
                final BusinessLocalReference ref = new BusinessLocalReference(home);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(id, interfce.getName()));

                final String internalName = "openejb/Deployment/" + format(id, interfce.getName(), InterfaceType.BUSINESS_LOCAL);
                bind(internalName, ref, bindings, beanInfo, interfce);

                final String name = strategy.getName(interfce, DEFAULT_NAME_KEY, JndiNameStrategy.Interface.BUSINESS_LOCAL);
                final String externalName = "openejb/local/" + name;
                bind(externalName, ref, bindings, beanInfo, interfce);
                bindJava(bean, interfce, ref, bindings, beanInfo);
                if (USE_OLD_JNDI_NAMES) {
                    bean.getModuleContext().getAppContext().getBindings().put(name, ref);
                }

                if (simpleNameRef == null) {
                    simpleNameRef = ref;
                }
            }
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException("Unable to bind business local interface for deployment " + id, e);
        }

        try {

            for (final Class interfce : bean.getBusinessRemoteInterfaces()) {

                final BeanContext.BusinessRemoteHome home = bean.getBusinessRemoteHome(interfce);
                final BusinessRemoteReference ref = new BusinessRemoteReference(home);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(id, interfce.getName(), null));

                final String internalName = "openejb/Deployment/" + format(id, interfce.getName(), InterfaceType.BUSINESS_REMOTE);
                bind(internalName, ref, bindings, beanInfo, interfce);

                final String name = strategy.getName(interfce, DEFAULT_NAME_KEY, JndiNameStrategy.Interface.BUSINESS_REMOTE);
                bind("openejb/local/" + name, ref, bindings, beanInfo, interfce);
                bind("openejb/remote/" + name, ref, bindings, beanInfo, interfce);
                bind("openejb/remote/" + computeGlobalName(bean, interfce), ref, bindings, beanInfo, interfce);
                bindJava(bean, interfce, ref, bindings, beanInfo);
                if (USE_OLD_JNDI_NAMES) {
                    bean.getModuleContext().getAppContext().getBindings().put(name, ref);
                }

                if (simpleNameRef == null) {
                    simpleNameRef = ref;
                }
            }
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException("Unable to bind business remote deployment in jndi.", e);
        }

        try {
            final Class localHomeInterface = bean.getLocalHomeInterface();
            if (localHomeInterface != null) {

                final ObjectReference ref = new ObjectReference(bean.getEJBLocalHome());

                String name = strategy.getName(bean.getLocalHomeInterface(), DEFAULT_NAME_KEY, JndiNameStrategy.Interface.LOCAL_HOME);
                bind("openejb/local/" + name, ref, bindings, beanInfo, localHomeInterface);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(id, localHomeInterface.getName(), InterfaceType.EJB_LOCAL_HOME));

                name = "openejb/Deployment/" + format(id, bean.getLocalInterface().getName());
                bind(name, ref, bindings, beanInfo, localHomeInterface);

                name = "openejb/Deployment/" + format(id, bean.getLocalInterface().getName(), InterfaceType.EJB_LOCAL);
                bind(name, ref, bindings, beanInfo, localHomeInterface);
                bindJava(bean, localHomeInterface, ref, bindings, beanInfo);

                if (simpleNameRef == null) {
                    simpleNameRef = ref;
                }
            }
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException("Unable to bind local home interface for deployment " + id, e);
        }

        try {
            final Class homeInterface = bean.getHomeInterface();
            if (homeInterface != null) {

                final ObjectReference ref = new ObjectReference(bean.getEJBHome());

                String name = strategy.getName(homeInterface, DEFAULT_NAME_KEY, JndiNameStrategy.Interface.REMOTE_HOME);
                bind("openejb/local/" + name, ref, bindings, beanInfo, homeInterface);
                bind("openejb/remote/" + name, ref, bindings, beanInfo, homeInterface);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(id, homeInterface.getName(), InterfaceType.EJB_HOME));

                name = "openejb/Deployment/" + format(id, bean.getRemoteInterface().getName());
                bind(name, ref, bindings, beanInfo, homeInterface);

                name = "openejb/Deployment/" + format(id, bean.getRemoteInterface().getName(), InterfaceType.EJB_OBJECT);
                bind(name, ref, bindings, beanInfo, homeInterface);
                bindJava(bean, homeInterface, ref, bindings, beanInfo);

                if (simpleNameRef == null) {
                    simpleNameRef = ref;
                }
            }
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException("Unable to bind remote home interface for deployment " + id, e);
        }

        try {
            if (simpleNameRef != null) {
                bindJava(bean, null, simpleNameRef, bindings, beanInfo);
            }
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException("Unable to bind simple java:global name in jndi", e);
        }

        try {
            if (MessageListener.class.equals(bean.getMdbInterface())) {

                final String destinationId = bean.getDestinationId();
                final String jndiName = "openejb/Resource/" + destinationId;
                final Reference reference = new IntraVmJndiReference(jndiName);

                final String deploymentId = id.toString();
                bind("openejb/local/" + deploymentId, reference, bindings, beanInfo, MessageListener.class);
                bind("openejb/remote/" + deploymentId, reference, bindings, beanInfo, MessageListener.class);
            }
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException("Unable to bind mdb destination in jndi.", e);
        } catch (final NoClassDefFoundError ncdfe) {
            // no-op: no jms API
        }
    }

    private void optionalBind(final Bindings bindings, final Reference ref, final String name) throws NamingException {
        try {
            openejbContext.bind(name, ref);
            logger.debug("bound ejb at name: " + name + ", ref: " + ref);
            bindings.add(name);
        } catch (final NamingException okIfBindFails) {
            logger.debug("failed to bind ejb at name: " + name + ", ref: " + ref);
        }
    }

    public static String format(final Object deploymentId, final String interfaceClassName) {
        return format((String) deploymentId, interfaceClassName, null);
    }

    public static String format(final Object deploymentId, final String interfaceClassName, final InterfaceType interfaceType) {
        return format((String) deploymentId, interfaceClassName, interfaceType);
    }

    public static String format(final String deploymentId, final String interfaceClassName, final InterfaceType interfaceType) {
        return deploymentId + "/" + interfaceClassName + (interfaceType == null ? "" : "!" + interfaceType.getSpecName());
    }

    private void bind(final String name, final Reference ref, final Bindings bindings, final EnterpriseBeanInfo beanInfo, final Class intrface) throws NamingException {

        if (name.startsWith("openejb/local/") || name.startsWith("openejb/remote/") || name.startsWith("openejb/localbean/") || name.startsWith("openejb/global/")) {

            final String externalName = name.replaceFirst("openejb/[^/]+/", "");

            if (bindings.contains(name)) {
                // We bind under two sections of jndi, only warn once.. the user doesn't need to be bothered with that detail
                if (name.startsWith("openejb/local/")) {
                    logger.debug("Duplicate: Jndi(name=" + externalName + ")");
                }
                return;
            }

            try {
                openejbContext.bind(name, ref);
                bindings.add(name);

                if (!beanInfo.jndiNames.contains(externalName)) {
                    beanInfo.jndiNames.add(externalName);

                    final JndiNameInfo nameInfo = new JndiNameInfo();
                    nameInfo.intrface = intrface == null ? null : intrface.getName();
                    nameInfo.name = externalName;
                    beanInfo.jndiNamess.add(nameInfo);

                    if (!embeddedEjbContainerApi
                        // filtering internal bean
                        && !(beanInfo instanceof ManagedBeanInfo && ((ManagedBeanInfo) beanInfo).hidden)) {
                        logger.info("Jndi(name=" + externalName + ") --> Ejb(deployment-id=" + beanInfo.ejbDeploymentId + ")");
                    }
                }
            } catch (final NameAlreadyBoundException e) {
                final BeanContext deployment = findNameOwner(name);
                if (deployment != null) {
                    logger.error("Jndi(name=" + externalName + ") cannot be bound to Ejb(deployment-id=" + beanInfo.ejbDeploymentId + ").  Name already taken by Ejb(deployment-id=" + deployment.getDeploymentID() + ")");
                } else {
                    logger.error("Jndi(name=" + externalName + ") cannot be bound to Ejb(deployment-id=" + beanInfo.ejbDeploymentId + ").  Name already taken by another object in the system.");
                }
                // Construct a new exception as the IvmContext doesn't include
                // the name in the exception that it throws
                if (failOnCollision) {
                    throw new NameAlreadyBoundException(externalName);
                }
            }
        } else {
            try {
                openejbContext.bind(name, ref);
                logger.debug("bound ejb at name: " + name + ", ref: " + ref);
                bindings.add(name);
            } catch (final NameAlreadyBoundException e) {
                logger.error("Jndi name could not be bound; it may be taken by another ejb.  Jndi(name=" + name + ")");
                // Construct a new exception as the IvmContext doesn't include
                // the name in the exception that it throws
                throw new NameAlreadyBoundException(name);
            }
        }

    }

    //ee6 specified ejb bindings in module, app, and global contexts

    private String computeGlobalName(final BeanContext cdi, final Class<?> intrface) {
        final ModuleContext module = cdi.getModuleContext();
        final AppContext application = module.getAppContext();

        final String appName = application.isStandaloneModule() ? "" : application.getId() + "/";
        final String moduleName = moduleName(cdi);
        String beanName = cdi.getEjbName();
        if (intrface != null) {
            beanName = beanName + "!" + intrface.getName();
        }

        return "global/" + appName + moduleName + beanName;
    }

    private String moduleName(BeanContext cdi) {
        String moduleName = cdi.getModuleName() + "/";
        if (moduleName.startsWith("ear-scoped-cdi-beans_")) {
            moduleName = moduleName.substring("ear-scoped-cdi-beans_".length());
        }
        return moduleName;
    }

    private void bindJava(final BeanContext cdi, final Class intrface, final Reference ref, final Bindings bindings, final EnterpriseBeanInfo beanInfo) throws NamingException {
        final ModuleContext module = cdi.getModuleContext();
        final AppContext application = module.getAppContext();

        final Context moduleContext = module.getModuleJndiContext();
        final Context appContext = application.getAppJndiContext();
        final Context globalContext = application.getGlobalJndiContext();

        final String appName = application.isStandaloneModule() ? "" : application.getId() + "/";
        String moduleName = moduleName(cdi);
        if (moduleName.startsWith("/")) {
            moduleName = moduleName.substring(1);
        }
        String beanName = cdi.getEjbName();
        if (intrface != null) {
            beanName = beanName + "!" + intrface.getName();
        }
        final String globalName = "global/" + appName + moduleName + beanName;
        try {

            if (embeddedEjbContainerApi
                && !(beanInfo instanceof ManagedBeanInfo && ((ManagedBeanInfo) beanInfo).hidden)) {
                logger.info(String.format("Jndi(name=\"java:%s\")", globalName));
            }
            globalContext.bind(globalName, ref);
            application.getBindings().put(globalName, ref);

            bind("openejb/global/" + globalName, ref, bindings, beanInfo, intrface);
        } catch (final NameAlreadyBoundException e) {
            //one interface in more than one role (e.g. both Local and Remote
            return;
        }

        appContext.bind("app/" + moduleName + beanName, ref);
        application.getBindings().put("app/" + moduleName + beanName, ref);

        final String moduleJndi = "module/" + beanName;
        moduleContext.bind(moduleJndi, ref);

        // contextual if the same ejb (api) is deployed in 2 wars of an ear
        ContextualEjbLookup contextual = ContextualEjbLookup.class.cast(application.getBindings().get(moduleJndi));
        if (contextual == null) {
            final Map<BeanContext, Object> potentials = new HashMap<>();
            contextual = new ContextualEjbLookup(potentials, ref);
            application.getBindings().put(moduleJndi, contextual); // TODO: we shouldn't do it but use web bindings
        }
        contextual.potentials.put(cdi, ref);
    }


    /**
     * This may not be that performant, but it's certain to be faster than the
     * user having to track down which deployment is using a particular jndi name
     *
     * @param name
     * @return .
     */
    private BeanContext findNameOwner(final String name) {
        final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        for (final BeanContext beanContext : containerSystem.deployments()) {
            final Bindings bindings = beanContext.get(Bindings.class);
            if (bindings != null && bindings.getBindings().contains(name)) {
                return beanContext;
            }
        }
        return null;
    }

    protected static final class Bindings {
        private final List<String> bindings = new ArrayList<>();

        public List<String> getBindings() {
            return bindings;
        }

        public boolean add(final String o) {
            return bindings.add(o);
        }

        public boolean contains(final String o) {
            return bindings.contains(o);
        }
    }

    public static class RemoteInterfaceComparator implements Comparator<Class> {

        public int compare(final Class a, final Class b) {
            final boolean aIsRmote = Remote.class.isAssignableFrom(a);
            final boolean bIsRmote = Remote.class.isAssignableFrom(b);

            if (aIsRmote == bIsRmote) {
                return 0;
            }
            return aIsRmote ? 1 : -1;
        }
    }

    public static class ContextualEjbLookup extends org.apache.openejb.core.ivm.naming.Reference {
        private final Map<BeanContext, Object> potentials;
        private final Object defaultValue;

        public ContextualEjbLookup(final Map<BeanContext, Object> potentials, final Object defaultValue) {
            this.potentials = potentials;
            this.defaultValue = defaultValue;
        }

        @Override
        public Object getObject() throws NamingException {
            if (potentials.size() == 1) {
                return unwrap(defaultValue);
            }
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader != null) {
                for (final Map.Entry<BeanContext, Object> o : potentials.entrySet()) {
                    if (loader.equals(o.getKey().getClassLoader())) {
                        return unwrap(o.getValue());
                    }
                }
            }
            return unwrap(defaultValue);
        }

        private Object unwrap(final Object value) throws NamingException {
            if (org.apache.openejb.core.ivm.naming.Reference.class.isInstance(value)) { // pretty sure
                return org.apache.openejb.core.ivm.naming.Reference.class.cast(value).getObject();
            }
            return value;
        }
    }
}
