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
package org.apache.openejb.assembler.spring;

import java.util.Map;
import javax.naming.Context;
import javax.naming.NamingException;

import org.springframework.beans.factory.FactoryBean;

/**
 * @org.apache.xbean.XBean element="jndiBinding"
 * @version $Revision$ $Date$
 */
public class JndiBinding implements FactoryBean {
    private Context context;
    private Map<String, Object> bindings;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings;
    }

    /**
     * @org.apache.xbean.InitMethod
     */
    public void start() throws NamingException {
        if (context == null && bindings != null) {
            throw new NullPointerException("Naming context has not been set");
        }
        if (bindings == null) {
            return;
        }
        try {
            for (Map.Entry<String, Object> entry : bindings.entrySet()) {
                String name = entry.getKey();
                Object value = entry.getValue();
                context.bind(name, value);
            }
        } catch (NamingException e) {
            stop();
            throw e;
        }
    }

    /**
     * @org.apache.xbean.DestroyMethod
     */
    public void stop() {
        if (context == null) {
            return;
        }
        if (bindings == null) {
            return;
        }
        for (String name : bindings.keySet()) {
            try {
                context.unbind(name);
            } catch (NamingException ignored) {
            }
        }
    }

    public Object getObject() throws Exception {
        return context;
    }

    public Class getObjectType() {
        return Context.class;
    }

    public boolean isSingleton() {
        return true;
    }
}
