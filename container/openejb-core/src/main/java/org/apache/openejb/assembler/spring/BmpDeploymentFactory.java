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

import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.SystemException;

/**
 * @org.apache.xbean.XBean element="bmpDeployment"
 */
public class BmpDeploymentFactory extends AbstractDeploymentFactory {
    private boolean reentrant;
    protected String pkClass;

    public boolean isReentrant() {
        return reentrant;
    }

    public void setReentrant(boolean reentrant) {
        this.reentrant = reentrant;
    }

    protected boolean isBeanManagedTransaction() {
        return false;
    }

    protected byte getComponentType() {
        return CoreDeploymentInfo.BMP_ENTITY;
    }

    protected CoreDeploymentInfo createDeploymentInfo() throws SystemException {
        CoreDeploymentInfo deploymentInfo = super.createDeploymentInfo();
        deploymentInfo.setIsReentrant(reentrant);
        return deploymentInfo;
    }

    public String getPkClass() {
        return pkClass;
    }

    public void setPkClass(String pkClass) {
        this.pkClass = pkClass;
    }
}
