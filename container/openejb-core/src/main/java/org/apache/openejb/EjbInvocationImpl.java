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

import java.util.Map;
import java.util.HashMap;

import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.SimpleInvocationResult;
import org.apache.geronimo.interceptor.InvocationKey;

import org.apache.openejb.transaction.EjbTransactionContext;

/**
 *
 *
 * @version $Revision$ $Date$
 */
public class EjbInvocationImpl implements EjbInvocation {

    private final Map data = new HashMap();
    private final EJBInterfaceType type;
    private final int index;
    private final Object[] arguments;
    private final Object id;

    // The deployment that we are invoking, this is set in the deployment before sending the invocation to the interceptor stack
    private ExtendedEjbDeployment ejbDeployment;

    // Valid in server-side interceptor stack once an instance has been identified
    private EJBInstanceContext instanceContext;

    // Valid in server-side interceptor stack once a TransactionContext has been created
    private EjbTransactionContext ejbTransactionContext;

    public EjbInvocationImpl(EJBInterfaceType type, int index, Object[] arguments) {
        assert type != null : "Interface type may not be null";
        assert index >= 0 : "Invalid method index: "+index;
        this.type = type;
        this.index = index;
        this.arguments = arguments;
        id = null;
    }

    public EjbInvocationImpl(EJBInterfaceType type, Object id, int index, Object[] arguments) {
        assert type != null : "Interface type may not be null";
        assert index >= 0 : "Invalid method index: "+index;
        this.type = type;
        this.index = index;
        this.arguments = arguments;
        this.id = id;
    }

    public EjbInvocationImpl(int index, Object[] arguments, EJBInstanceContext instanceContext) {
        assert index >= 0 : "Invalid method index: "+index;
        assert instanceContext != null;
        this.type = EJBInterfaceType.LIFECYCLE;
        this.index = index;
        this.arguments = arguments;
        this.id = null;
        this.instanceContext = instanceContext;
    }

    public Object get(InvocationKey key) {
        if(data==null) {
            return null;
        }
        return data.get(key);
    }

    public void put(InvocationKey key, Object value) {
        data.put(key, value);
    }

    public int getMethodIndex() {
        return index;
    }

    public EJBInterfaceType getType() {
        return type;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public Object getId() {
        return id;
    }

    public ExtendedEjbDeployment getEjbDeployment() {
        return ejbDeployment;
    }

    public void setEjbDeployment(ExtendedEjbDeployment ejbDeployment) {
        this.ejbDeployment = ejbDeployment;
    }

    public EJBInstanceContext getEJBInstanceContext() {
        return instanceContext;
    }

    public void setEJBInstanceContext(EJBInstanceContext instanceContext) {
        this.instanceContext = instanceContext;
    }

    public EjbTransactionContext getEjbTransactionData() {
        return ejbTransactionContext;
    }

    public void setEjbTransactionData(EjbTransactionContext ejbTransactionContext) {
        this.ejbTransactionContext = ejbTransactionContext;
    }

    public InvocationResult createResult(Object object) {
        return new SimpleInvocationResult(true, object);
    }

    public InvocationResult createExceptionResult(Exception exception) {
        return new SimpleInvocationResult(false, exception);
    }
}
