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
package org.apache.openejb;

import org.apache.openejb.api.LocalClient;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.Context;
import javax.naming.NamingException;
import java.util.List;

public final class ClientInjections {
    private ClientInjections() {
        // no-op
    }

    public static InjectionProcessor<?> clientInjector(final Object object) throws OpenEJBException {
        if (object == null) {
            throw new NullPointerException("Object supplied to 'inject' operation is null");
        }

        Context clients;
        try {
            clients = (Context) SystemInstance.get().getComponent(ContainerSystem.class).getJNDIContext()
                    .lookup("openejb/client/");
        } catch (NamingException e) {
            throw new OpenEJBException(object.getClass().getName(), e);
        }

        Context ctx = null;
        List<Injection> injections = null;

        Class<?> current = object.getClass();
        while (current != null && !current.equals(Object.class)) {
            try {
                String moduleId = (String) clients.lookup(current.getName());
                ctx = (Context) clients.lookup(moduleId);
                injections = (List<Injection>) ctx.lookup("info/injections");
                break;
            } catch (NamingException e) {
                current = current.getSuperclass();
            }
        }

        if (injections == null) {
            throw new OpenEJBException("Unable to find injection meta-data for "
                    + object.getClass().getName()
                    + ".  Ensure that class was annotated with @"
                    + LocalClient.class.getName()+" and was successfully discovered and deployed. "
                    + " See http://openejb.apache.org/3.0/local-client-injection.html");
        }

        return new InjectionProcessor(object, injections, ctx);
    }
}
