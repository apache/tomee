/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.cxf.jaxrs.lifecycle.PerRequestResourceProvider;
import org.apache.cxf.message.Message;
import org.apache.openejb.Injection;
import org.apache.openejb.InjectionProcessor;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.WebApplicationException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Romain Manni-Bucau
 */
public class OpenEJBPerRequestResourceProvider extends PerRequestResourceProvider {
    private Collection<Injection> injections;
    private Context context;

    public OpenEJBPerRequestResourceProvider(Class<?> clazz, Collection<Injection> injectionCollection, Context ctx) {
        super(clazz);
        injections = injectionCollection;
        context = ctx;
        if (ctx == null) {
            // TODO: context shouldn't be null here so it should be removed
            context = (Context) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{Context.class}, new InvocationHandler() {
                @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    Context ctx = new InitialContext();
                    if (method.getName().equals("lookup")) {
                        if (args[0].getClass().equals(String.class)) {
                            try {
                                return ctx.lookup("java:" + String.class.cast(args[0]));
                            } catch (NamingException ne) {
                                // let try it in the normal way (without java:)
                            }
                        }
                    }
                    return method.invoke(ctx, args);
                }
            });
        }
    }

    protected Object createInstance(Message m) {
        Object o = super.createInstance(m);
        try {
            InjectionProcessor<?> injector = new InjectionProcessor<Object>(o, new ArrayList<Injection>(injections), InjectionProcessor.unwrap(context));
            injector.createInstance();
            injector.postConstruct();
            return injector.getInstance();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
