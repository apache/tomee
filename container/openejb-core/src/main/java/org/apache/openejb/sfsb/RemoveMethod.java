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
package org.apache.openejb.sfsb;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EJBOperation;
import org.apache.openejb.dispatch.MethodSignature;
import org.apache.openejb.dispatch.AbstractMethodOperation;

/**
 * Virtual operation handling removal of a stateful session bean instance instance.
 *
 * @version $Revision$ $Date$
 */
public class RemoveMethod extends AbstractMethodOperation {
    public RemoveMethod(Class beanClass, MethodSignature signature) {
        super(beanClass, signature);
    }

    public InvocationResult execute(EjbInvocation invocation) throws Throwable {
        InvocationResult result = invoke(invocation, EJBOperation.EJBREMOVE);
        if (result.isNormal()) {
            // flag the context as dead so it does not get put back in the cache
            invocation.getEJBInstanceContext().die();
        }
        return result;
    }
}
