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
package org.apache.openejb.dispatch;

import javax.ejb.EnterpriseBean;
import javax.ejb.TimedObject;
import javax.ejb.Timer;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.dispatch.AbstractCallbackOperation;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class EJBTimeoutOperation extends AbstractCallbackOperation {

    public static final EJBTimeoutOperation INSTANCE = new EJBTimeoutOperation();

    private EJBTimeoutOperation() {}


    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        return invoke(invocation, EJBOperation.TIMEOUT);
    }

    protected Object doOperation(EnterpriseBean instance, Object[] arguments) throws Throwable {
        ((TimedObject)instance).ejbTimeout((Timer)arguments[0]);
        return null;
    }

}
