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
package org.apache.openejb.assembler.spring;

import org.apache.openejb.SystemException;
import org.apache.openejb.BeanType;
import org.apache.openejb.core.CoreDeploymentInfo;

/**
 * @org.apache.xbean.XBean element="statelessDeployment"
 */
public class StatelessDeploymentFactory extends AbstractDeploymentFactory {
    private boolean beanManagedTransaction;

    public boolean isBeanManagedTransaction() {
        return beanManagedTransaction;
    }

    public void setBeanManagedTransaction(boolean beanManagedTransaction) {
        this.beanManagedTransaction = beanManagedTransaction;
    }

    protected BeanType getComponentType() {
        return BeanType.STATELESS;
    }

    protected String getPkClass() {
        return null;
    }

    protected CoreDeploymentInfo createDeploymentInfo() throws SystemException {
        CoreDeploymentInfo deploymentInfo = super.createDeploymentInfo();
        deploymentInfo.setBeanManagedTransaction(beanManagedTransaction);
        return deploymentInfo;
    }
}
