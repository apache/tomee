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
import javax.ws.rs.WebApplicationException;
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
    }

    protected Object createInstance(Message m) {
        Object o = super.createInstance(m);
        try {
            InjectionProcessor<?> injector = new InjectionProcessor<Object>(o, new ArrayList<Injection>(injections), context);
            injector.createInstance();
            injector.postConstruct();
            return injector.getInstance();
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }
    }
}
