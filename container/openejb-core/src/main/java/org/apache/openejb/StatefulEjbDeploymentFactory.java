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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb;

/**
 * @version $Revision$ $Date$
 */
public class StatefulEjbDeploymentFactory extends RpcEjbDeploymentFactory {
    protected boolean beanManagedTransactions;

    public boolean isBeanManagedTransactions() {
        return beanManagedTransactions;
    }

    public void setBeanManagedTransactions(boolean beanManagedTransactions) {
        this.beanManagedTransactions = beanManagedTransactions;
    }

    public EjbDeployment create() throws Exception {
        Class beanClass = loadClass(beanClassName, "bean class");
        Class homeInterface = loadClass(homeInterfaceName, "home interface");
        Class remoteInterface = loadClass(remoteInterfaceName, "remote interface");
        Class localHomeInterface = loadClass(localHomeInterfaceName, "local home interface");
        Class localInterface = loadClass(localInterfaceName, "local interface");

        return new StatefulEjbDeployment(containerId,
                ejbName,

                homeInterface,
                remoteInterface,
                localHomeInterface,
                localInterface,
                beanClass,

                classLoader,
                (StatefulEjbContainer) ejbContainer,
                jndiNames,
                localJndiNames,

                securityEnabled,
                policyContextId,
                null,
                runAs,
                
                beanManagedTransactions,
                transactionPolicies,
                componentContext,
                unshareableResources,
                applicationManagedSecurityResources);
    }
}
