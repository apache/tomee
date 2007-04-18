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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.interceptor;

import org.apache.openejb.core.Operation;

import javax.xml.rpc.handler.MessageContext;
import java.util.List;
import java.lang.reflect.Method;

/**
 * We could really get by with usinga plain ReflectionInvocationContext
 * but just in case we need to or want to add something special, we can
 * use this subclass.  At the very least it matches JaxWsInvocationContext
 * 
 * @version $Rev$ $Date$
 */
public class JaxRpcInvocationContext extends ReflectionInvocationContext {

    public JaxRpcInvocationContext(Operation operation, List<Interceptor> interceptors, Object target, Method method, MessageContext messageContext, Object... parameters) {
        super(operation, interceptors, target, method, parameters);
        getContextData().put(MessageContext.class.getName(), messageContext);
    }
}
