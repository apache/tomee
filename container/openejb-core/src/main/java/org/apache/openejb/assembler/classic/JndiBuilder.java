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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.assembler.classic;

import static org.apache.openejb.util.Classes.packageName;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.NameAlreadyBoundException;
import javax.naming.Context;
import javax.jms.MessageListener;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Strings;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.loader.Options;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.JndiFactory;
import org.apache.openejb.core.ivm.naming.BusinessLocalReference;
import org.apache.openejb.core.ivm.naming.BusinessRemoteReference;
import org.apache.openejb.core.ivm.naming.ObjectReference;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.apache.openejb.util.StringTemplate;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;


/**
 * @version $Rev$ $Date$
 */
public class JndiBuilder {

    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, JndiBuilder.class.getPackage().getName());

    private final Context context;
    private static final String JNDINAME_STRATEGY_CLASS = "openejb.jndiname.strategy.class";
    private static final String JNDINAME_FAILONCOLLISION = "openejb.jndiname.failoncollision";
    private final boolean failOnCollision;

    public JndiBuilder(Context context) {
        this.context = context;
        failOnCollision = SystemInstance.get().getOptions().get(JNDINAME_FAILONCOLLISION, true);
    }

    public void build(EjbJarInfo ejbJar, HashMap<String, DeploymentInfo> deployments) {

        JndiNameStrategy strategy = createStrategy(ejbJar, deployments);

        for (EnterpriseBeanInfo beanInfo : ejbJar.enterpriseBeans) {
            DeploymentInfo deploymentInfo = deployments.get(beanInfo.ejbDeploymentId);
            strategy.begin(deploymentInfo);
            try {
                bind(ejbJar, deploymentInfo, beanInfo, strategy);
            } finally {
                strategy.end();
            }
        }
    }

    public static JndiNameStrategy createStrategy(EjbJarInfo ejbJar, Map<String, DeploymentInfo> deployments) {
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
            throw new IllegalStateException("Could not instantiate JndiNameStrategy: "+strategyClassName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access JndiNameStrategy: "+strategyClassName, e);
        } catch (Throwable t){
            throw new IllegalStateException("Could not create JndiNameStrategy: "+strategyClassName, t);
        }
    }

    public static interface JndiNameStrategy {

        public static enum Interface {

            REMOTE_HOME(InterfaceType.EJB_HOME, "RemoteHome", "home", ""),
            LOCAL_HOME(InterfaceType.EJB_LOCAL_HOME, "LocalHome", "local-home", "Local"),
            BUSINESS_LOCAL(InterfaceType.BUSINESS_LOCAL, "Local", "business-local", "BusinessLocal"),
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

        public void begin(DeploymentInfo deploymentInfo);
        public String getName(Class interfce, Interface type);
        public void end();
    }

    // TODO: put these into the classpath and get them with xbean-finder
    public static class TemplatedStrategy implements JndiNameStrategy {
        private static final String JNDINAME_FORMAT = "openejb.jndiname.format";
        private org.apache.openejb.util.StringTemplate template;
        private HashMap<String, EnterpriseBeanInfo> beanInfos;

        // Set in begin()
        private DeploymentInfo deploymentInfo;
        // Set in begin()
        private Map<String, StringTemplate> templates;
        
        private String format;

        public TemplatedStrategy(EjbJarInfo ejbJarInfo, Map<String, DeploymentInfo> deployments) {
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
        }

        public void begin(DeploymentInfo deploymentInfo) {
            this.deploymentInfo = deploymentInfo;
            EnterpriseBeanInfo beanInfo = beanInfos.get(deploymentInfo.getDeploymentID());

            templates = new HashMap<String, StringTemplate>();
            templates.put("", template);

            for (JndiNameInfo nameInfo : beanInfo.jndiNamess) {
                String intrface = nameInfo.intrface;
                if (intrface == null) intrface = "";
                templates.put(intrface, new StringTemplate(nameInfo.name));
            }
            beanInfo.jndiNames.clear();
            beanInfo.jndiNamess.clear();
        }

        public void end() {
        }

        public String getName(Class interfce, Interface type) {
            StringTemplate template = templates.get(interfce.getName());
            if (template == null) template = templates.get(type.getAnnotationName());
            if (template == null) template = templates.get("");

            Map<String,String> contextData = new HashMap<String,String>();
            contextData.put("moduleId", deploymentInfo.getModuleID());
            contextData.put("ejbType", deploymentInfo.getComponentType().name());
            contextData.put("ejbClass", deploymentInfo.getBeanClass().getName());
            contextData.put("ejbClass.simpleName", deploymentInfo.getBeanClass().getSimpleName());
            contextData.put("ejbClass.packageName", packageName(deploymentInfo.getBeanClass()));
            contextData.put("ejbName", deploymentInfo.getEjbName());
            contextData.put("deploymentId", deploymentInfo.getDeploymentID().toString());
            contextData.put("interfaceType", type.getAnnotationName());
            contextData.put("interfaceType.annotationName", type.getAnnotationName());
            contextData.put("interfaceType.annotationNameLC",type.getAnnotationName().toLowerCase());
            contextData.put("interfaceType.xmlName", type.getXmlName());
            contextData.put("interfaceType.xmlNameCc", type.getXmlNameCc());
            contextData.put("interfaceType.openejbLegacyName", type.getOpenejbLegacy());
            contextData.put("interfaceClass", interfce.getName());
            contextData.put("interfaceClass.simpleName", interfce.getSimpleName());
            contextData.put("interfaceClass.packageName", packageName(interfce));

            return template.apply(contextData);
        }
    }

    public static class LegacyAddedSuffixStrategy implements JndiNameStrategy {
        private DeploymentInfo deploymentInfo;

        public void begin(DeploymentInfo deploymentInfo) {
            this.deploymentInfo = deploymentInfo;
        }

        public void end() {
        }

        public String getName(Class interfce, Interface type) {
            String id = deploymentInfo.getDeploymentID() + "";
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
    }

    public void bind(EjbJarInfo ejbJarInfo, DeploymentInfo deploymentInfo, EnterpriseBeanInfo beanInfo, JndiNameStrategy strategy) {
        CoreDeploymentInfo deployment = (CoreDeploymentInfo) deploymentInfo;

        Bindings bindings = new Bindings();
        deployment.set(Bindings.class, bindings);


        Object id = deployment.getDeploymentID();
        try {
            Class homeInterface = deployment.getHomeInterface();
            if (homeInterface != null) {

                ObjectReference ref = new ObjectReference(deployment.getEJBHome());

                String name = strategy.getName(homeInterface, JndiNameStrategy.Interface.REMOTE_HOME);
                bind("openejb/local/" + name, ref, bindings, beanInfo, homeInterface);
                bind("openejb/remote/" + name, ref, bindings, beanInfo, homeInterface);

                name = "openejb/Deployment/" + format(deployment.getDeploymentID(), deployment.getRemoteInterface().getName());
                bind(name, ref, bindings, beanInfo, homeInterface);

                name = "openejb/Deployment/" + format(deployment.getDeploymentID(), deployment.getRemoteInterface().getName(), InterfaceType.EJB_OBJECT);
                bind(name, ref, bindings, beanInfo, homeInterface);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind remote home interface for deployment " + id, e);
        }

        try {
            Class localHomeInterface = deployment.getLocalHomeInterface();
            if (localHomeInterface != null) {

                ObjectReference ref = new ObjectReference(deployment.getEJBLocalHome());

                String name = strategy.getName(deploymentInfo.getLocalHomeInterface(), JndiNameStrategy.Interface.LOCAL_HOME);
                bind("openejb/local/" + name, ref, bindings, beanInfo, localHomeInterface);

                name = "openejb/Deployment/" + format(deployment.getDeploymentID(), deployment.getLocalInterface().getName());
                bind(name, ref, bindings, beanInfo, localHomeInterface);

                name = "openejb/Deployment/" + format(deployment.getDeploymentID(), deployment.getLocalInterface().getName(), InterfaceType.EJB_LOCAL);
                bind(name, ref, bindings, beanInfo, localHomeInterface);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind local home interface for deployment " + id, e);
        }

        try {
            List<Class> localInterfaces = deployment.getBusinessLocalInterfaces();
            Class beanClass = deployment.getBeanClass();

            for (Class interfce : deployment.getBusinessLocalInterfaces()) {

                List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanClass, interfce, localInterfaces);
                DeploymentInfo.BusinessLocalHome home = deployment.getBusinessLocalHome(interfaces);
                BusinessLocalReference ref = new BusinessLocalReference(home);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(deployment.getDeploymentID(), interfce.getName()));

                String internalName = "openejb/Deployment/" + format(deployment.getDeploymentID(), interfce.getName(), InterfaceType.BUSINESS_LOCAL);
                bind(internalName, ref, bindings, beanInfo, interfce);

                String externalName = "openejb/local/" + strategy.getName(interfce, JndiNameStrategy.Interface.BUSINESS_LOCAL);
                bind(externalName, ref, bindings, beanInfo, interfce);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business local interface for deployment " + id, e);
        }

        try {

            List<Class> remoteInterfaces = deployment.getBusinessRemoteInterfaces();
            Class beanClass = deployment.getBeanClass();

            for (Class interfce : deployment.getBusinessRemoteInterfaces()) {

                List<Class> interfaces = ProxyInterfaceResolver.getInterfaces(beanClass, interfce, remoteInterfaces);
                DeploymentInfo.BusinessRemoteHome home = deployment.getBusinessRemoteHome(interfaces);
                BusinessRemoteReference ref = new BusinessRemoteReference(home);

                optionalBind(bindings, ref, "openejb/Deployment/" + format(deployment.getDeploymentID(), interfce.getName(), null));

                String internalName = "openejb/Deployment/" + format(deployment.getDeploymentID(), interfce.getName(), InterfaceType.BUSINESS_REMOTE);
                bind(internalName, ref, bindings, beanInfo, interfce);

                String name = strategy.getName(interfce, JndiNameStrategy.Interface.BUSINESS_REMOTE);
                bind("openejb/local/" + name, ref, bindings, beanInfo, interfce);
                bind("openejb/remote/" + name, ref, bindings, beanInfo, interfce);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business remote deployment in jndi.", e);
        }

        try {
            if (MessageListener.class.equals(deployment.getMdbInterface())) {

                String destinationId = deployment.getDestinationId();
                String jndiName = "openejb/Resource/" + destinationId;
                Reference reference = new IntraVmJndiReference(jndiName);

                String deploymentId = deployment.getDeploymentID().toString();
                bind("openejb/local/" + deploymentId, reference, bindings, beanInfo, MessageListener.class);
                bind("openejb/remote/" + deploymentId, reference, bindings, beanInfo, MessageListener.class);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind mdb destination in jndi.", e);
        }
    }

    private void optionalBind(Bindings bindings, Reference ref, String name) throws NamingException {
        try {
            context.bind(name, ref);
            bindings.add(name);
        } catch (NamingException okIfBindFails) {
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

        if (name.startsWith("openejb/local/") || name.startsWith("openejb/remote/")) {

            String externalName = name.replaceFirst("openejb/[^/]+/", "");

            if (bindings.contains(name)){
                // We bind under two sections of jndi, only warn once.. the user doesn't need to be bothered with that detail
                if (name.startsWith("openejb/local/")) {
                    logger.debug("Duplicate: Jndi(name=" + externalName +")");
                }
                return;
            }

            try {
                context.bind(name, ref);
                bindings.add(name);

                if (!beanInfo.jndiNames.contains(externalName)) {
                    beanInfo.jndiNames.add(externalName);

                    JndiNameInfo nameInfo = new JndiNameInfo();
                    nameInfo.intrface = intrface.getName();
                    nameInfo.name = externalName;
                    beanInfo.jndiNamess.add(nameInfo);

                    logger.info("Jndi(name=" + externalName +") --> Ejb(deployment-id="+beanInfo.ejbDeploymentId+")");
                }
            } catch (NameAlreadyBoundException e) {
                DeploymentInfo deployment = findNameOwner(name);
                if (deployment != null){
                    logger.error("Jndi(name=" + externalName +") cannot be bound to Ejb(deployment-id="+beanInfo.ejbDeploymentId+").  Name already taken by Ejb(deployment-id="+deployment.getDeploymentID()+")");
                } else {
                    logger.error("Jndi(name=" + externalName +") cannot be bound to Ejb(deployment-id="+beanInfo.ejbDeploymentId+").  Name already taken by another object in the system.");
                }
                // Construct a new exception as the IvmContext doesn't include
                // the name in the exception that it throws
                if (failOnCollision) throw new NameAlreadyBoundException(externalName);
            }
        } else {
            try {
                context.bind(name, ref);
                bindings.add(name);
            } catch (NameAlreadyBoundException e) {
                logger.error("Jndi name could not be bound; it may be taken by another ejb.  Jndi(name=" + name +")");
                // Construct a new exception as the IvmContext doesn't include
                // the name in the exception that it throws
                throw new NameAlreadyBoundException(name);
            }
        }

    }

    /**
     * This may not be that performant, but it's certain to be faster than the
     * user having to track down which deployment is using a particular jndi name
     * @param name
     * @return .
     */
    private DeploymentInfo findNameOwner(String name) {
        ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
        for (DeploymentInfo deploymentInfo : containerSystem.deployments()) {
            Bindings bindings = deploymentInfo.get(Bindings.class);
            if (bindings != null && bindings.getBindings().contains(name)) return deploymentInfo;
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
            return (aIsRmote)? 1: -1;
        }
    }
}
