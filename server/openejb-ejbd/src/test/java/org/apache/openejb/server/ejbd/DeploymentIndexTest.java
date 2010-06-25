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
package org.apache.openejb.server.ejbd;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.SystemException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.client.EJBMetaDataImpl;
import org.apache.openejb.client.EJBRequest;
import org.apache.openejb.client.InterfaceType;
import org.apache.openejb.core.CoreDeploymentInfo;
import org.apache.openejb.core.AppContext;
import org.apache.openejb.core.ModuleContext;
import org.junit.Before;
import org.junit.Test;

public class DeploymentIndexTest {

    private Method method;
    private DeploymentInfo deploymentInfo;
    private DeploymentIndex deploymentIndex;

    @Before
    public void setUp() throws SystemException {
        method = Method.class.getMethods()[0];
        deploymentInfo = new CoreDeploymentInfo("aDeploymentId", null, new ModuleContext("", new AppContext("", SystemInstance.get(), null)), DeploymentIndexTest.class, null, null, null, null, null, null, null, null, null);
        deploymentIndex = new DeploymentIndex(new DeploymentInfo[] { deploymentInfo, deploymentInfo });
    }

    @Test
    public void testGetDeploymentEJBRequest() throws RemoteException {
        EJBMetaDataImpl ejbMetadataWithId = new EJBMetaDataImpl(null, null, null, null, null, 1, InterfaceType.BUSINESS_REMOTE, null);
        EJBRequest request = new EJBRequest(0, ejbMetadataWithId, method, null, null);
        DeploymentInfo info = deploymentIndex.getDeployment(request);
        assert deploymentInfo.equals(info);
        assert request.getDeploymentId().equals(info.getDeploymentID());
    }

    @Test(expected = RemoteException.class)
    public void testGetDeploymentEJBRequestRemoteException() throws RemoteException {
        // 0 causes DeploymentIndex to move further
        EJBMetaDataImpl ejbMetadata = new EJBMetaDataImpl(null, null, null, null, null, 0, InterfaceType.BUSINESS_REMOTE, null);
        EJBRequest request = new EJBRequest(0, ejbMetadata, method, null, null);
        deploymentIndex.getDeployment(request);
    }

}
