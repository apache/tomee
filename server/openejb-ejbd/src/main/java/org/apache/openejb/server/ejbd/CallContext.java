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

import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.client.EJBRequest;

import java.util.HashMap;

public class CallContext {

    private static final ThreadLocal threads = new ThreadLocal();
    private final HashMap<Class, Object> data = new HashMap();

    public CallContext() {
    }

    public void reset() {
        data.clear();
    }

    public <T> T get(Class<T> type) {
        return (T)data.get(type);
    }

    public <T> T set(Class<T> type, T value) {
        return (T) data.put(type, value);
    }

    public DeploymentInfo getDeploymentInfo() {
        return get(DeploymentInfo.class);
    }

    public void setDeploymentInfo(DeploymentInfo info) {
        set(DeploymentInfo.class, info);
    }

    public EJBRequest getEJBRequest() {
        return get(EJBRequest.class);
    }

    public void setEJBRequest(EJBRequest request) {
        set(EJBRequest.class, request);
    }

    public static void setCallContext(CallContext ctx) {
        if (ctx == null) {
            ctx = (CallContext) threads.get();
            if (ctx != null) ctx.reset();
        } else {
            threads.set(ctx);
        }
    }

    public static CallContext getCallContext() {
        CallContext ctx = (CallContext) threads.get();
        if (ctx == null) {
            ctx = new CallContext();
            threads.set(ctx);
        }

        return ctx;
    }

}

