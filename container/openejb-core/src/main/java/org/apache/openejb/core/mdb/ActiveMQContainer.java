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
package org.apache.openejb.core.mdb;

import org.apache.activemq.ra.ActiveMQActivationSpec;
import org.apache.activemq.ra.ActiveMQResourceAdapter;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.spi.SecurityService;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.connector.GeronimoBootstrapContext;

import javax.resource.spi.work.WorkManager;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapterInternalException;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.concurrent.Executor;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;

public class ActiveMQContainer extends MdbContainer {
    public ActiveMQContainer(Object containerID,
                             GeronimoTransactionManager transactionManager,
                             SecurityService securityService,
                             Map<String, DeploymentInfo> deploymentRegistry,
                             String serverUrl,
                             int threadPoolSize,
                             int instanceLimit) throws OpenEJBException {

        super(containerID,
                transactionManager,
                securityService,
                createActiveMQResourceAdapter(transactionManager, serverUrl, threadPoolSize),
                ActiveMQActivationSpec.class,
                instanceLimit);

        // deploy the beans
        try {
            for (Map.Entry<String, DeploymentInfo> entry : deploymentRegistry.entrySet()) {
                deploy(entry.getKey(), entry.getValue());
            }
        } catch (OpenEJBException e) {
            // there was a failure deploying the beans... undeploy them
            for (DeploymentInfo deploymentInfo : deployments()) {
                try {
                    undeploy(deploymentInfo.getDeploymentID());
                } finally {
                    logger.error("Error undeploying " + deploymentInfo.getDeploymentID(), e);
                }
            }
        }
    }

    public static ActiveMQResourceAdapter createActiveMQResourceAdapter(GeronimoTransactionManager transactionManager, String serverUrl, int threadPoolSize) throws OpenEJBException {
        // create the ActiveMQ resource adapter instance
        ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();

        // initialize properties
        ra.setServerUrl(serverUrl);

        // create a thead pool for ActiveMQ
        if (threadPoolSize <= 0) throw new IllegalArgumentException("threadPoolSizes <= 0: " + threadPoolSize);
        Executor threadPool = Executors.newFixedThreadPool(threadPoolSize);

        // create a work manager which ActiveMQ uses to dispatch message delivery jobs
        WorkManager workManager = new GeronimoWorkManager(threadPool, threadPool, threadPool, transactionManager);

        // wrap the work mananger and transaction manager in a bootstrap context (connector spec thing)
        BootstrapContext bootstrapContext = new GeronimoBootstrapContext(workManager, transactionManager);

        // start the resource adapter
        try {
            ra.start(bootstrapContext);
        } catch (ResourceAdapterInternalException e) {
            throw new OpenEJBException(e);
        }
        return ra;
    }
}
