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

import org.apache.geronimo.interceptor.Interceptor;
import org.apache.geronimo.interceptor.Invocation;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.InstanceContext;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class ConnectionTrackingInterceptor implements Interceptor {

    private final Interceptor next;
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    public ConnectionTrackingInterceptor(Interceptor next, TrackedConnectionAssociator trackedConnectionAssociator) {
        this.next = next;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public InvocationResult invoke(Invocation invocation) throws Throwable {
        EjbInvocation ejbInvocation = (EjbInvocation) invocation;
        EJBInstanceContext ejbInstanceContext = ejbInvocation.getEJBInstanceContext();

        InstanceContext connectorCtx = (InstanceContext) ejbInstanceContext.getConnectorInstanceData();
        if (connectorCtx == null) {
            ExtendedEjbDeployment ejbDeployment = ejbInvocation.getEjbDeployment();
            connectorCtx = new DefaultInstanceContext(ejbDeployment.getUnshareableResources(),
                    ejbDeployment.getApplicationManagedSecurityResources());
            ejbInstanceContext.setConnectorInstanceData(connectorCtx);
        }

        InstanceContext leavingInstanceContext = trackedConnectionAssociator.enter(connectorCtx);
        try {
            return next.invoke(invocation);
        } finally {
            trackedConnectionAssociator.exit(leavingInstanceContext);
        }
    }
}
