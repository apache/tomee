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

import static org.apache.openejb.util.Classes.packageName;

import java.util.TreeMap;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.NameAlreadyBoundException;
import javax.naming.Context;
import javax.jms.MessageListener;

import org.apache.openejb.BeanContext;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.core.ivm.naming.BusinessLocalBeanReference;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Strings;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.Options;
import org.apache.openejb.core.ivm.naming.BusinessLocalReference;
import org.apache.openejb.core.ivm.naming.BusinessRemoteReference;
import org.apache.openejb.core.ivm.naming.ObjectReference;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.util.StringTemplate;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Properties;
import java.lang.reflect.Constructor;


/**
 * @version $Rev$ $Date$
 */
public class JndiBuilder {

    public static final String DEFAULT_NAME_KEY = "default";

    final boolean embeddedEjbContainerApi;

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, JndiBuilder.class.getPackage().getName());

    private final Context openejbContext;
    private static final String JNDINAME_STRATEGY_CLASS = "openejb.jndiname.strategy.class";
    private static final String JNDINAME_FAILONCOLLISION = "openejb.jndiname.failoncollision";
    private final boolean failOnCollision;

    public JndiBuilder(Context openejbContext) {
        this.openejbContext = openejbContext;

        final Options options = SystemInstance.get().getOptions();

        failOnCollision = options.get(JNDINAME_FAILONCOLLISION, true);
        embeddedEjbContainerApi = options.get(EJBContainer.class.getName(), false);
    }

    public void build(EjbJarInfo ejbJar, HashMap<String, BeanContext> deployments) {

        JndiNameStrategy strategy = createStrategy(ejbJar, deployments);

        for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
            BeanContext beanContext = deployments.get(beanInfo.ejbDeploymentId);
            strategy.begin(beanContext);
            try {
                bind(ejbJar, beanContext, beanInfo, strategy);
            } finally {
                strategy.end();
            }
        }
    }

    public static JndiNameStrategy createStrategy(EjbJarInfo ejbJar, Map<String, BeanContext> deployments) {
        Options options = new Options(ejbJar.properties, SystemInstance.get().getOptions());

        Class strategyClass = options.get(JNDINAME_STRATEGY_CLASS, TemplatedStrategy.class);

        String strategyClassName = strategyClass.getName();

        try {
            try {
                Constructor constructor = strategyClass.getConstructor(EjbJarInfo.class, Map.class);
                return (JndiNameStrategy) constructor.newInstance(ejbJar, deployments);
            } catch (NoSuchMethodException e) {
            }

            Constructor constructor = strategyClass.getConstructor();
            return (JndiNameStrategy) constructor.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not instantiate JndiNameStrategy: " + strategyClassName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access JndiNameStrategy: " + strategyClassName, e);
        } catch (Throwable t) {
            throw new IllegalStateException("Could not create JndiNameStrategy: " + strategyClassName, t);
        }
    }

    public static interface JndiNameStrategy {

        public static enum Interface {

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

            Interface(InterfaceType type, String annotatedName, String xmlName, String openejbLegacy) {
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

        public void begin(BeanContext beanContext);

        public String getName(Class interfce, String key, Interface type);
        public Map<String, String> getNames(Class interfce, Interface type);

        public void end();
    }

    // TODO: put these into the classpath and get them with xbean-finder

    public static class TemplatedStrategy implements JndiNameStrategy {
        private static final String JNDINAME_FORMAT = "openejb.jndiname.format";
        private static final String KEYS = "default,local,global,app";
        private org.apache.openejb.util.StringTemplate template;
        private HashMap<String, EnterpriseBeanInfo> beanInfos;

        // Set in begin()
        private BeanContext bean;
        
        // Set in begin()
        private HashMap<String, Map<String, StringTemplate>> templates;

        private String format;
        private Map<String, String> appContext;
        private HashMap<String, String> beanContext;

        public TemplatedStrategy(EjbJarInfo ejbJarInfo, Map<String, BeanContext> deployments) {
            Options options = new Options(ejbJarInfo.properties, SystemInstance.get().getOptions());

            format = options.get(JNDINAME_FORMAT, "{deploymentId}{interfaceType.annotationName}");

            { // illegal format check
                int index = format.indexOf(":");
                if (index > -1) {
                    logger.error("Illegal " + JNDINAME_FORMAT + " contains a colon ':'.  Everything before the colon will be removed, '" + format + "' ");
                    format = format.substring(index + 1);
                }
            }

            this.template = new StringTemplate(format);

            beanInfos = new HashMap<String, EnterpriseBeanInfo>();
            for (EnterpriseBeanInfo beanInfo : ejbJarInfo.enterpriseBeans) {
                beanInfos.put(beanInfo.ejbDeploymentId, beanInfo);
            }

            final Iterator<BeanContext> it = deployments.values().iterator();
            if (!it.hasNext()) return;

            // TODO we should just pass in the ModuleContext
            final ModuleContext moduleContext = it.next().getModuleContext();

            appContext = new HashMap<String, String>();
            putAll(appContext, SystemInstance.get().getProperties());
            putAll(appContext, moduleContext.getAppContext().getProperties());
            putAll(appContext, moduleContext.getProperties());

            appContext.put("appName", moduleContext.getAppContext().getId());
            appContext.put("appId", moduleContext.getAppContext().getId());

            appContext.put("moduleName", moduleContext.getId());
            appContext.put("moduleId", moduleContext.getId());
        }

        private void putAll(Map<String, String> map, Properties properties) {
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                if (!(e.getValue() instanceof String)) continue;
                if (!(e.getKey() instanceof String)) continue;

                map.put((String) e.getKey(), (String) e.getValue());
            }
        }

        private Map<String, StringTemplate> addTemplate(Map<String, StringTemplate> map, String key, StringTemplate template) {
            Map<String, StringTemplate> m = map;
            if (m == null) {
                m = new TreeMap<String, StringTemplate> ();
            }
            m.put(key, template);
            return m;
        }

        public void begin(BeanContext bean) {
            this.bean = bean;
            
            EnterpriseBeanInfo beanInfo = beanInfos.get(bean.getDeploymentID());

            templates = new HashMap<String, Map<String, StringTemplate>>();
            templates.put("", addTemplate(null, DEFAULT_NAME_KEY, template));

            for (JndiNameInfo nameInfo : beanInfo.jndiNamess) {
                String intrface = nameInfo.intrface;
                if (intrface == null) intrface = "";
                templates.put(intrface, addTemplate(templates.get(intrface), getType(nameInfo.name), new StringTemplate(nameInfo.name)));
            }
            beanInfo.jndiNames.clear();
            beanInfo.jndiNamess.clear();

            this.beanContext = new HashMap<String, String>(appContext);
            putAll(this.beanContext, bean.getProperties());
            this.beanContext.put("ejbType", bean.getComponentType().name());
            this.beanContext.put("ejbClass", bean.getBeanClass().getName());
            this.beanContext.put("ejbClass.simpleName", bean.getBeanClass().getSimpleName());
            this.beanContext.put("ejbClass.packageName", packageName(bean.getBeanClass()));
            this.beanContext.put("ejbName", bean.getEjbName());
            this.beanContext.put("deploymentId", bean.getDeploymentID().toString());
        }

        private static String getType(String name) {
            int start = 0;
            if (name.charAt(0) == '/') {
                start = 1;
            }
            int end = name.substring(start).indexOf('/');
            if (end < 0) {
                return DEFAULT_NAME_KEY;
            }
            return name.substring(start, end);
        }

        public void end() {
        }

        public String getName(Class interfce, String key, Interface type) {
            Map<String, StringTemplate> template = templates.get(interfce.getName());
            if (template == null) template = templates.get(type.getAnnotationName());
            if (template == null) template = templates.get("");

            Map<String, String> contextData = new HashMap<String, String>(beanContext);
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
            
            if (stringTemplate == null){
                stringTemplate = template.values().iterator().next();
            } 
            
            return  stringTemplate.apply(contextData);
          
        }

        @Override public Map<String, String> getNames(Class interfce, Interface type) {
            Map<String, String> names = new HashMap<String, String>();
            for (String key : KEYS.split(",")) {
                names.put(key, getName(interfce, key, type));
            }
            return names;
        }
    }

    public static class LegacyAddedSuffixStrategy implements JndiNameStrategy {
        private BeanContext beanContext;

        public void begin(BeanContext beanContext) {
            this.beanContext = beanContext;
        }

        public void end() {
        }

        public String getName(Class interfce, String key, Interface type) {
            String id = beanContext.getDeploymentID() + "";
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

        @Override public Map<String, String> getNames(Class interfce, Interface type) {
            Map<String, String> names = new HashMap<String, String>();
            names.put("", getName(interfce, DEFAULT_NAME_KEY, type));
            return names;
        }
    }

    public void bind(EjbJarInfo ejbJarInfo, BeanContext bean, EnterpriseBeanInfo beanInfo, JndiNameStrategy strategy) {

        Bindings bindings = new Bindings();
        bean.set(Bindings.class, bindings);

        Reference simpleNameRef = null;

        Object id = bean.getDeploymentID();

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
                Class beanClass = bean.getBeanClass();

                BeanContext.BusinessLocalBeanHome home = bean.getBusinessLocalBeanHome();
                BusinessLocalBeanReference ref = new BusinessLocalBeanReference(home);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(bean.getDeploymentID(), beanClass.getName(), InterfaceType.LOCALBEAN));

                String internalName = "openejb/Deployment/" + format(bean.getDeploymentID(), beanClass.getName(), InterfaceType.BUSINESS_LOCALBEAN_HOME);
                bind(internalName, ref, bindings, beanInfo, beanClass);

                String name = strategy.getName(beanClass, DEFAULT_NAME_KEY, JndiNameStrategy.Interface.LOCALBEAN);
                bind("openejb/local/" + name, ref, bindings, beanInfo, beanClass);
                bindJava(bean, beanClass, ref, bindings, beanInfo);

                simpleNameRef = ref;
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business remote deployment in jndi.", e);
        }

        try {
            List<Class> localInterfaces = bean.getBusinessLocalInterfaces();
            Class beanClass = bean.getBeanClass();

            for (Class interfce : bean.getBusinessLocalInterfaces()) {

                List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanClass, interfce, localInterfaces);
                BeanContext.BusinessLocalHome home = bean.getBusinessLocalHome(interfaces, interfce);
                BusinessLocalReference ref = new BusinessLocalReference(home);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(bean.getDeploymentID(), interfce.getName()));

                String internalName = "openejb/Deployment/" + format(bean.getDeploymentID(), interfce.getName(), InterfaceType.BUSINESS_LOCAL);
                bind(internalName, ref, bindings, beanInfo, interfce);

                String externalName = "openejb/local/" + strategy.getName(interfce, DEFAULT_NAME_KEY, JndiNameStrategy.Interface.BUSINESS_LOCAL);
                bind(externalName, ref, bindings, beanInfo, interfce);
                bindJava(bean, interfce, ref, bindings, beanInfo);
                
                if (simpleNameRef == null) simpleNameRef = ref;
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business local interface for deployment " + id, e);
        }

        try {

            List<Class> remoteInterfaces = bean.getBusinessRemoteInterfaces();
            Class beanClass = bean.getBeanClass();

            for (Class interfce : bean.getBusinessRemoteInterfaces()) {

                List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanClass, interfce, remoteInterfaces);
                BeanContext.BusinessRemoteHome home = bean.getBusinessRemoteHome(interfaces, interfce);
                BusinessRemoteReference ref = new BusinessRemoteReference(home);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(bean.getDeploymentID(), interfce.getName(), null));

                String internalName = "openejb/Deployment/" + format(bean.getDeploymentID(), interfce.getName(), InterfaceType.BUSINESS_REMOTE);
                bind(internalName, ref, bindings, beanInfo, interfce);

                String name = strategy.getName(interfce, DEFAULT_NAME_KEY, JndiNameStrategy.Interface.BUSINESS_REMOTE);
                bind("openejb/local/" + name, ref, bindings, beanInfo, interfce);
                bind("openejb/remote/" + name, ref, bindings, beanInfo, interfce);
                bindJava(bean, interfce, ref, bindings, beanInfo);
                
                if (simpleNameRef == null) simpleNameRef = ref;
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business remote deployment in jndi.", e);
        }

        try {
            Class localHomeInterface = bean.getLocalHomeInterface();
            if (localHomeInterface != null) {

                ObjectReference ref = new ObjectReference(bean.getEJBLocalHome());

                String name = strategy.getName(bean.getLocalHomeInterface(), DEFAULT_NAME_KEY, JndiNameStrategy.Interface.LOCAL_HOME);
                bind("openejb/local/" + name, ref, bindings, beanInfo, localHomeInterface);
                
                optionalBind(bindings, ref, "openejb/Deployment/" + format(bean.getDeploymentID(), localHomeInterface.getName(), InterfaceType.EJB_LOCAL_HOME));

                name = "openejb/Deployment/" + format(bean.getDeploymentID(), bean.getLocalInterface().getName());
                bind(name, ref, bindings, beanInfo, localHomeInterface);

                name = "openejb/Deployment/" + format(bean.getDeploymentID(), bean.getLocalInterface().getName(), InterfaceType.EJB_LOCAL);
                bind(name, ref, bindings, beanInfo, localHomeInterface);
                bindJava(bean, localHomeInterface, ref, bindings, beanInfo);

                if (simpleNameRef == null) simpleNameRef = ref;
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind local home interface for deployment " + id, e);
        }

        try {
            Class homeInterface = bean.getHomeInterface();
            if (homeInterface != null) {

                ObjectReference ref = new ObjectReference(bean.getEJBHome());

                String name = strategy.getName(homeInterface, DEFAULT_NAME_KEY, JndiNameStrategy.Interface.REMOTE_HOME);
                bind("openejb/local/" + name, ref, bindings, beanInfo, homeInterface);
                bind("openejb/remote/" + name, ref, bindings, beanInfo, homeInterface);
                
                optionalBind(bindings, ref, "openejb/Deployment/" + format(bean.getDeploymentID(), homeInterface.getName(), InterfaceType.EJB_HOME));
                
                name = "openejb/Deployment/" + format(bean.getDeploymentID(), bean.getRemoteInterface().getName());
                bind(name, ref, bindings, beanInfo, homeInterface);

                name = "openejb/Deployment/" + format(bean.getDeploymentID(), bean.getRemoteInterface().getName(), InterfaceType.EJB_OBJECT);
                bind(name, ref, bindings, beanInfo, homeInterface);
                bindJava(bean, homeInterface, ref, bindings, beanInfo);

                if (simpleNameRef == null) simpleNameRef = ref;
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind remote home interface for deployment " + id, e);
        }

        try {
            if (simpleNameRef != null) {
                bindJava(bean, null, simpleNameRef, bindings, beanInfo);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind simple java:global name in jndi", e);
        }

        try {
            if (MessageListener.class.equals(bean.getMdbInterface())) {

                String destinationId = bean.getDestinationId();
                String jndiName = "openejb/Resource/" + destinationId;
                Reference reference = new IntraVmJndiReference(jndiName);

                String deploymentId = bean.getDeploymentID().toString();
                bind("openejb/local/" + deploymentId, reference, bindings, beanInfo, MessageListener.class);
                bind("openejb/remote/" + deploymentId, reference, bindings, beanInfo, MessageListener.class);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind mdb destination in jndi.", e);
        }
    }

    private void optionalBind(Bindings bindings, Reference ref, String name) throws NamingException {
        try {
            openejbContext.bind(name, ref);
            logger.debug("bound ejb at name: " + name + ", ref: " + ref);
            bindings.add(name);
        } catch (NamingException okIfBindFails) {
            logger.debug("failed to bind ejb at name: " + name + ", ref: " + ref);
        }
    }

    public static String format(Object deploymentId, String interfaceClassName) {
        return format((String) deploymentId, interfaceClassName, null);
    }

    public static String format(Object deploymentId, String interfaceClassName, InterfaceType interfaceType) {
        return format((String) deploymentId, interfaceClassName, interfaceType);
    }

    public static String format(String deploymentId, String interfaceClassName, InterfaceType interfaceType) {
        return deploymentId + "/" + interfaceClassName + (interfaceType == null ? "" : "!" + interfaceType.getSpecName());
    }

    private void bind(String name, Reference ref, Bindings bindings, EnterpriseBeanInfo beanInfo, Class intrface) throws NamingException {

        if (name.startsWith("openejb/local/") || name.startsWith("openejb/remote/") || name.startsWith("openejb/localbean/") || name.startsWith("openejb/global/")) {

            String externalName = name.replaceFirst("openejb/[^/]+/", "");

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

                    JndiNameInfo nameInfo = new JndiNameInfo();
                    nameInfo.intrface = intrface == null ? null : intrface.getName();
                    nameInfo.name = externalName;
                    beanInfo.jndiNamess.add(nameInfo);

                    if (!embeddedEjbContainerApi) {
                        logger.info("Jndi(name=" + externalName + ") --> Ejb(deployment-id=" + beanInfo.ejbDeploymentId + ")");
                    }
                }
            } catch (NameAlreadyBoundException e) {
                BeanContext deployment = findNameOwner(name);
                if (deployment != null) {
                    logger.error("Jndi(name=" + externalName + ") cannot be bound to Ejb(deployment-id=" + beanInfo.ejbDeploymentId + ").  Name already taken by Ejb(deployment-id=" + deployment.getDeploymentID() + ")");
                } else {
                    logger.error("Jndi(name=" + externalName + ") cannot be bound to Ejb(deployment-id=" + beanInfo.ejbDeploymentId + ").  Name already taken by another object in the system.");
                }
                // Construct a new exception as the IvmContext doesn't include
                // the name in the exception that it throws
                if (failOnCollision) throw new NameAlreadyBoundException(externalName);
            }
        } else {
            try {
                openejbContext.bind(name, ref);
                logger.debug("bound ejb at name: " + name + ", ref: " + ref);
                bindings.add(name);
            } catch (NameAlreadyBoundException e) {
                logger.error("Jndi name could not be bound; it may be taken by another ejb.  Jndi(name=" + name + ")");
                // Construct a new exception as the IvmContext doesn't include
                // the name in the exception that it throws
                throw new NameAlreadyBoundException(name);
            }
        }

    }

    //ee6 specified ejb bindings in module, app, and global contexts

    private void bindJava(BeanContext cdi, Class intrface, Reference ref, Bindings bindings, EnterpriseBeanInfo beanInfo) throws NamingException {
        Context moduleContext = cdi.getModuleContext().getModuleJndiContext();
        Context appContext = cdi.getModuleContext().getAppContext().getAppJndiContext();
        Context globalContext = cdi.getModuleContext().getAppContext().getGlobalJndiContext();

        String appName = cdi.getModuleContext().getAppContext().isStandaloneModule() ? "" : cdi.getModuleContext().getAppContext().getId() + "/";
        String moduleName = cdi.getModuleName() + "/";
        String beanName = cdi.getEjbName();
        if (intrface != null) {
            beanName = beanName + "!" + intrface.getName();
        }
        try {
            String globalName = "global/" + appName + moduleName + beanName;

            if (embeddedEjbContainerApi) {
                logger.info(String.format("Jndi(name=\"java:%s\")", globalName));
            }
            globalContext.bind(globalName, ref);
            bind("openejb/global/" + globalName, ref, bindings, beanInfo, intrface);
        } catch (NameAlreadyBoundException e) {
            //one interface in more than one role (e.g. both Local and Remote
            return;
        }
        
        appContext.bind("app/" + moduleName + beanName, ref);
        
        moduleContext.bind("module/" + beanName, ref);
    }
    
    

    /**
     * This may not be that performant, but it's certain to be faster than the
     * user having to track down which deployment is using a particular jndi name
     *
     * @param name
     * @return .
     */
    private BeanContext findNameOwner(String name) {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        for (BeanContext beanContext : containerSystem.deployments()) {
            Bindings bindings = beanContext.get(Bindings.class);
            if (bindings != null && bindings.getBindings().contains(name)) return beanContext;
        }
        return null;
    }

    protected static final class Bindings {
        private final List<String> bindings = new ArrayList<String>();

        public List<String> getBindings() {
            return bindings;
        }

        public boolean add(String o) {
            return bindings.add(o);
        }

        public boolean contains(String o) {
            return bindings.contains(o);
        }
    }

    public static class RemoteInterfaceComparator implements Comparator<Class> {

        public int compare(java.lang.Class a, java.lang.Class b) {
            boolean aIsRmote = java.rmi.Remote.class.isAssignableFrom(a);
            boolean bIsRmote = java.rmi.Remote.class.isAssignableFrom(b);

            if (aIsRmote == bIsRmote) return 0;
            return (aIsRmote) ? 1 : -1;
        }
    }
}
