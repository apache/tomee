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
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.ivm.naming.BusinessLocalReference;
import org.apache.openejb.core.ivm.naming.BusinessRemoteReference;
import org.apache.openejb.core.ivm.naming.ObjectReference;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

import java.util.List;
import java.util.ArrayList;


/**
 * @version $Rev$ $Date$
 */
public class JndiBuilder {

    private JndiNameStrategy strategy = new LegacyAddedSuffixStrategy();
    private final Context context;

    public JndiBuilder(Context context) {
        this.context = context;
    }

    public static interface JndiNameStrategy {

        public static enum Interface {

            REMOTE_HOME, LOCAL_HOME, BUSINESS_LOCAL, BUSINESS_REMOTE, SERVICE_ENDPOINT
        }

        public String getName(DeploymentInfo deploymentInfo, Class interfce, Interface type);
    }

    // TODO: put these into the classpath and get them with xbean-finder
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

    public void bind(DeploymentInfo deploymentInfo) {
        CoreDeploymentInfo deployment = (CoreDeploymentInfo) deploymentInfo;

        Bindings bindings = new Bindings();
        deployment.set(Bindings.class, bindings);

        Object id = deployment.getDeploymentID();
        try {
            Class homeInterface = deployment.getHomeInterface();
            if (homeInterface != null) {
                String name = strategy.getName(deployment, homeInterface, JndiNameStrategy.Interface.REMOTE_HOME);
                bindings.add(name);
                context.bind("openejb/ejb/" + name, new ObjectReference(deployment.getEJBHome()));
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind home interface for deployment " + id, e);
        }

        try {
            Class localHomeInterface = deployment.getLocalHomeInterface();
            if (localHomeInterface != null) {
                String name = strategy.getName(deployment, localHomeInterface, JndiNameStrategy.Interface.LOCAL_HOME);
                bindings.add(name);
                context.bind("openejb/ejb/" + name, new ObjectReference(deployment.getEJBLocalHome()));
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
                context.bind("openejb/ejb/" + name, new BusinessRemoteReference(businessRemoteHome));
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind business remote deployment in jndi.", e);
        }

        try {
            if (MessageListener.class.equals(deployment.getMdbInterface())) {
                String name = deployment.getMessageDestination();
                String destination = deployment.getActivationProperties().get("destination");
                if (destination != null) {
                    String destinationType = deployment.getActivationProperties().get("destinationType");
                    if (Queue.class.getName().equals(destinationType)) {
                        Queue queue = new ActiveMQQueue(destination);
                        bindings.add(name);
                        context.bind("openejb/ejb/" + name, queue);
                    } else if (Topic.class.getName().equals(destinationType)) {
                        Topic topic = new ActiveMQTopic(destination);
                        bindings.add(name);
                        context.bind("openejb/ejb/" + name, topic);
                    }
                }
            }
        } catch (NamingException e) {
            throw new RuntimeException("Unable to bind mdb destination in jndi.", e);
        }
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
