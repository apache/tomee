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


package org.apache.openejb.core.ivm.naming;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.JndiFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class IvmJndiFactory implements JndiFactory {
    private final Context jndiRootContext;

    public IvmJndiFactory() {
        jndiRootContext = IvmContext.createRootContext();
        try {
            jndiRootContext.bind("openejb/local/.", "");
            jndiRootContext.bind("openejb/remote/.", "");
            jndiRootContext.bind("openejb/client/.", "");
            jndiRootContext.bind("openejb/Deployment/.", "");
            jndiRootContext.bind("openejb/global/.", "");
        } catch (final NamingException e) {
            throw new OpenEJBRuntimeException("this should not happen", e);
        }
    }

    public Context createComponentContext(final Map<String, Object> bindings) throws SystemException {
        final IvmContext context = new IvmContext();
        try {
            context.bind("java:comp/env/dummy", "dummy");
        } catch (final NamingException e) {
            throw new SystemException("Unable to create subcontext 'java:comp/env'.  Exception:" + e.getMessage(), e);
        }
        for (final Map.Entry<String, Object> entry : bindings.entrySet()) {
            final String name = entry.getKey();
            final Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            try {
                context.bind(name, value);
            } catch (final NamingException e) {
                throw new SystemException("Unable to bind '" + name + "' into bean's enc.", e);
            }
        }

        return context;
    }

    public Context createRootContext() {
        return jndiRootContext;
    }

}
