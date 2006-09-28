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

import java.util.Set;
import javax.ejb.Timer;
import javax.naming.Context;
import javax.security.auth.Subject;

import org.apache.openejb.dispatch.VirtualOperation;
import org.apache.openejb.security.PermissionManager;
import org.apache.openejb.timer.BasicTimerServiceImpl;
import org.apache.openejb.transaction.TransactionPolicyManager;

/**
 * @version $Revision$ $Date$
 */
public interface ExtendedEjbDeployment extends EjbDeployment{
    Subject getRunAsSubject();

    Context getComponentContext();

    void logSystemException(Throwable t);

    VirtualOperation getVirtualOperation(int methodIndex);

    boolean isSecurityEnabled();

    String getPolicyContextId();

    PermissionManager getPermissionManager();

    TransactionPolicyManager getTransactionPolicyManager();

    Class getBeanClass();

    Timer getTimerById(Long id);

    BasicTimerServiceImpl getTimerService();

    Set getUnshareableResources();

    Set getApplicationManagedSecurityResources();
}
