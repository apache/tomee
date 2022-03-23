/*
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

package org.apache.openejb.core.interceptor;

import org.apache.openejb.core.Operation;

import jakarta.xml.ws.handler.MessageContext;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class JaxWsInvocationContext extends ReflectionInvocationContext {
    private final MessageContext messageContext;

    public JaxWsInvocationContext(final Operation operation, final List<Interceptor> interceptors, final Object target, final Method method, final MessageContext messageContext, final Object... parameters) {
        super(operation, interceptors, target, method, parameters);
        this.messageContext = messageContext;
    }

    public Map<String, Object> getContextData() {
        return messageContext;
    }

}
