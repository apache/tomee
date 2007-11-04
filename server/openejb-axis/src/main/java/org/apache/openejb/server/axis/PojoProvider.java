/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis;

import org.apache.axis.Handler;
import org.apache.axis.MessageContext;
import org.apache.axis.providers.java.RPCProvider;
import org.apache.openejb.server.httpd.HttpRequest;
import org.apache.openejb.server.webservices.WsConstants;

import javax.xml.rpc.holders.IntHolder;
import java.lang.reflect.Method;

public class PojoProvider extends RPCProvider {
    public Object getServiceObject(MessageContext msgContext, Handler service, String clsName, IntHolder scopeHolder) throws Exception {
        HttpRequest request = (HttpRequest) msgContext.getProperty(AxisWsContainer.REQUEST);
        return request.getAttribute(WsConstants.POJO_INSTANCE);
    }

    protected Object invokeMethod(MessageContext msgContext, Method interfaceMethod, Object pojo, Object[] arguments) throws Exception {
        Class pojoClass = pojo.getClass();

        Method pojoMethod = null;
        try {
            pojoMethod = pojoClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw (NoSuchMethodException) new NoSuchMethodException("The pojo class '" + pojoClass.getName() + "' does not have a method matching signature: " + interfaceMethod).initCause(e);
        }

        return pojoMethod.invoke(pojo, arguments);
    }
}
