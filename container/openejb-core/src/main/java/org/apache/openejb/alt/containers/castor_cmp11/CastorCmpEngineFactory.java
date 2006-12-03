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
package org.apache.openejb.alt.containers.castor_cmp11;

import org.apache.openejb.core.cmp.CmpEngine;
import org.apache.openejb.core.cmp.CmpCallback;
import org.apache.openejb.core.cmp.CmpEngineFactory;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.OpenEJBException;

import javax.transaction.TransactionManager;

public class CastorCmpEngineFactory implements CmpEngineFactory {
    private TransactionManager transactionManager;
    private DeploymentInfo[] deploys;
    private String engine;
    private String connectorName;
    private CmpCallback cmpCallback;

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public DeploymentInfo[] getDeploys() {
        return deploys;
    }

    public void setDeploymentInfos(DeploymentInfo[] deploys) {
        this.deploys = deploys;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getConnectorName() {
        return connectorName;
    }

    public void setConnectorName(String connectorName) {
        this.connectorName = connectorName;
    }

    public CmpCallback getCmpCallback() {
        return cmpCallback;
    }

    public void setCmpCallback(CmpCallback cmpCallback) {
        this.cmpCallback = cmpCallback;
    }

    public CmpEngine create() throws OpenEJBException {
        return new CastorCmpEngine(cmpCallback, transactionManager, deploys, engine, connectorName);
    }
}
