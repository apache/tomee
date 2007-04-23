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

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.jms.MessageListener;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ivm.naming.BusinessLocalReference;
import org.apache.openejb.core.ivm.naming.BusinessRemoteReference;
import org.apache.openejb.core.ivm.naming.ObjectReference;
import org.apache.openejb.core.ivm.naming.IntraVmJndiReference;
import org.codehaus.swizzle.stream.StringTemplate;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


/**
 * @version $Rev$ $Date$
 */
public class JndiBuilder {

    private JndiNameStrategy strategy = new LegacyAddedSuffixStrategy();
    private final Context context;

    public JndiBuilder(Context context) {
        this.context = context;

        String strategyClass = SystemInstance.get().getProperty("openejb.jndiname.strategy.class", LegacyAddedSuffixStrategy.class.getName());
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            strategy = (JndiNameStrategy) classLoader.loadClass(strategyClass).newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not instantiate JndiNameStrategy: "+strategyClass, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access JndiNameStrategy: "+strategyClass, e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load JndiNameStrategy: "+strategyClass, e);
        } catch (Throwable t){
            throw new IllegalStateException("Could not create JndiNameStrategy: "+strategyClass, t);
        }
    }

    public static interface JndiNameStrategy {

        public static enum Interface {

            REMOTE_HOME, LOCAL_HOME, BUSINESS_LOCAL, BUSINESS_REMOTE, SERVICE_ENDPOINT
        }

        public String getName(DeploymentInfo deploymentInfo, Class interfce, Interface type);
    }

    // TODO: put these into the classpath and get them with xbean-finder
    public static class TemplatedStrategy implements JndiNameStrategy {
        private org.codehaus.swizzle.stream.StringTemplate template;

        public TemplatedStrategy() {
            String format = SystemInstance.get().getProperty("openejb.jndiname.format", "{deploymentId}/{interfaceClass.simpleName}");
            this.template = new StringTemplate(format);
        }


        public String getName(DeploymentInfo deploymentInfo, Class interfce, Interface type) {
            Map<String,String> contextData = new HashMap<String,String>();
            contextData.put("moduleId", deploymentInfo.getModuleID());
            contextData.put("ejbType", deploymentInfo.getComponentType().name());
            contextData.put("ejbClass", deploymentInfo.getBeanClass().getName());
            contextData.put("ejbClass.simpleName", deploymentInfo.getBeanClass().getSimpleName());
            contextData.put("ejbName", deploymentInfo.getEjbName());
            contextData.put("deploymentId", deploymentInfo.getDeploymentID().toString());
            contextData.put("interfaceType", deploymentInfo.getInterfaceType(interfce).name());
            contextData.put("interfaceClass", interfce.getName());
            contextData.put("interfaceClass.simpleName", interfce.getSimpleName());
            return template.apply(contextData);
        }
    }

    public static class LegacyAddedSuffixStrategy implements JndiNameStrategy {

        public String getName(DeploymentInfo deploymentInfo, Class interfce, Interface type) {
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

    public static class AddedSuffixStrategy implements JndiNameStrategy {

        public String getName(DeploymentInfo deploymentInfo, Class interfce, Interface type) {
            String id = deploymentInfo.getDeploymentID() + "";
            if (id.charAt(0) == '/') {
                id = id.substring(1);
            }

            switch (type) {
                case REMOTE_HOME:
                    return id + "Remote";
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


    public static class CommonPrefixStrategy implements JndiNameStrategy {

        public String getName(DeploymentInfo deploymentInfo, Class interfce, Interface type) {
            String id = deploymentInfo.getDeploymentID() + "";
            if (id.charAt(0) == '/') {
                id = id.substring(1);
            }

            switch (type) {
                case REMOTE_HOME:
                    return "component/remote/" + id;
                case LOCAL_HOME:
                    return "component/local/" + id;
                case BUSINESS_REMOTE:
                    return "business/remote/" + id;
                case BUSINESS_LOCAL:
                    return "business/local/" + id;
            }
            return id;
        }
    }

    public static class InterfaceSimpleNameStrategy implements JndiNameStrategy {

        public String getName(DeploymentInfo deploymentInfo, Class interfce, Interface type) {
            return interfce.getSimpleName();
        }
    }

    public JndiNameStrategy getStrategy() {
        return strategy;
    }

    public void bind(DeploymentInfo deploymentInfo) {
        JndiNameStrategy strategy = getStrategy();
        CoreDeploymentInfo deployment = (CoreDeploymentInfo) deploymentInfo;

        Bindings bindings = new Bindings();
        deployment.set(Bindings.class, bindings);

        Object id = deployment.getDeploymentID();
        try {
            Class homeInterface = deployment.getHomeInterface();
            if (homeInterface != null) {
                String name = strategy.getName(deployment, homeInterface, JndiNameStrategy.Interface.REMOTE_HOME);
                bindings.add(name);
                ObjectReference ref = new ObjectReference(deployment.getEJBHome());
                context.bind("openejb/ejb/" + name, ref);
                name = deployment.getDeploymentID() + "/" + deployment.getRemoteInterface().getName();
                context.bind("openejb/Deployment/" + name, ref);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind home interface for deployment " + id, e);
        }

        try {
            Class localHomeInterface = deployment.getLocalHomeInterface();
            if (localHomeInterface != null) {
                String name = strategy.getName(deployment, localHomeInterface, JndiNameStrategy.Interface.LOCAL_HOME);
                bindings.add(name);
                ObjectReference ref = new ObjectReference(deployment.getEJBLocalHome());
                context.bind("openejb/ejb/" + name, ref);
                context.bind("openejb/Deployment/" + deployment.getDeploymentID() + "/" + deployment.getLocalInterface().getName(), ref);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind local interface for deployment " + id, e);
        }

        try {
            Class businessLocalInterface = deployment.getBusinessLocalInterface();
            if (businessLocalInterface != null) {
                String name = strategy.getName(deployment, businessLocalInterface, JndiNameStrategy.Interface.BUSINESS_LOCAL);
                DeploymentInfo.BusinessLocalHome businessLocalHome = deployment.getBusinessLocalHome();
                bindings.add(name);
                context.bind("openejb/ejb/" + name, new BusinessLocalReference(businessLocalHome));

                for (Class interfce : deployment.getBusinessLocalInterfaces()) {
                    DeploymentInfo.BusinessLocalHome home = deployment.getBusinessLocalHome(asList(interfce));
                    BusinessLocalReference ref = new BusinessLocalReference(home);
                    context.bind("openejb/Deployment/" + deployment.getDeploymentID() + "/" + interfce.getName(), ref);
                    try {
                        name = strategy.getName(deployment, interfce, JndiNameStrategy.Interface.BUSINESS_LOCAL);
                        context.bind("openejb/ejb/" + name, ref);
                    } catch (NamingException dontCareJustYet) {
                    }
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business local interface for deployment " + id, e);
        }

        try {
            Class businessRemoteInterface = deployment.getBusinessRemoteInterface();
            if (businessRemoteInterface != null) {
                String name = strategy.getName(deployment, businessRemoteInterface, JndiNameStrategy.Interface.BUSINESS_REMOTE);
                DeploymentInfo.BusinessRemoteHome businessRemoteHome = deployment.getBusinessRemoteHome();
                bindings.add(name);
                BusinessRemoteReference ref = new BusinessRemoteReference(businessRemoteHome);
                context.bind("openejb/ejb/" + name, ref);

                for (Class interfce : deployment.getBusinessRemoteInterfaces()) {
                    DeploymentInfo.BusinessRemoteHome home = deployment.getBusinessRemoteHome(asList(interfce));
                    ref = new BusinessRemoteReference(home);
                    context.bind("openejb/Deployment/" + deployment.getDeploymentID() + "/" + interfce.getName(), ref);

                    try {
                        name = strategy.getName(deployment, interfce, JndiNameStrategy.Interface.BUSINESS_REMOTE);
                        context.bind("openejb/ejb/" + name, ref);
                    } catch (NamingException dontCareJustYet) {
                    }
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business remote deployment in jndi.", e);
        }

        try {
            if (MessageListener.class.equals(deployment.getMdbInterface())) {
                String name = deployment.getDeploymentID().toString();

                String destinationId = deployment.getDestinationId();
                String jndiName = "java:openejb/Resource/" + destinationId;
                Reference reference = new IntraVmJndiReference(jndiName);

                bindings.add(name);
                context.bind("openejb/ejb/" + name, reference);
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind mdb destination in jndi.", e);
        }
    }

    private static List<Class> asList(Class interfce) {
        List<Class> list = new ArrayList<Class>();
        list.add(interfce);
        return list;
    }

    protected static final class Bindings {
        private final List<String> bindings = new ArrayList<String>();

        public List<String> getBindings() {
            return bindings;
        }

        public boolean add(String o) {
            return bindings.add(o);
        }
    }
}
