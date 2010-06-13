/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.core.ivm.naming;

import java.util.Map;


import javax.naming.*;

import org.apache.openejb.core.JndiFactory;
import org.apache.openejb.SystemException;

/**
 * @version $Rev$ $Date$
 */
public class IvmJndiFactory implements JndiFactory {
    private Context jndiRootContext;

    public IvmJndiFactory() {
        jndiRootContext = IvmContext.createRootContext();
        try {
            jndiRootContext.bind("openejb/local/.", "");
            jndiRootContext.bind("openejb/remote/.", "");
            jndiRootContext.bind("openejb/client/.", "");
            jndiRootContext.bind("openejb/Deployment/.", "");
        } catch (javax.naming.NamingException e) {
            throw new RuntimeException("this should not happen", e);
        }
    }

    public Context createComponentContext(Map<String, Object> bindings) throws SystemException {
        IvmContext context = new IvmContext();
        try {
            context.bind("java:comp/env/dummy", "dummy");
        } catch (javax.naming.NamingException e) {
            throw new org.apache.openejb.SystemException("Unable to create subcontext 'java:comp/env'.  Exception:"+e.getMessage(),e);
        }
        for (Map.Entry<String, Object> entry:  bindings.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            if (value == null) continue;
            try {
                context.bind(name, value);
            } catch (javax.naming.NamingException e) {
                throw new org.apache.openejb.SystemException("Unable to bind '" + name + "' into bean's enc.", e);
            }
        }

        return context;
    }

    public Context createRootContext() {
        return jndiRootContext;
    }

}
