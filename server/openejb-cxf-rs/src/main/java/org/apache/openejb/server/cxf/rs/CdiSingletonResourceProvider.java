/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
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

import org.apache.cxf.message.Message;
import org.apache.openejb.Injection;
import org.apache.webbeans.config.WebBeansContext;

import javax.naming.Context;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public class CdiSingletonResourceProvider extends CdiResourceProvider {
    private final BeanCreator creator;

    public CdiSingletonResourceProvider(final ClassLoader loader, final Class<?> clazz, final Object instance,
                                        final Collection<Injection> injectionCollection, final Context initialContext,
                                        final WebBeansContext owbCtx) {
        super(loader, clazz, injectionCollection, initialContext, owbCtx);
        if (normalScopeCreator != null) { // if the singleton is a normal scoped bean then use cdi instance instead of provided one
            creator = normalScopeCreator;
        } else { // do injections only
            creator = new SingletonBeanCreator(instance);
        }
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Object getInstance(final Message m) {
        return creator.create();
    }

    public void releaseInstance(final Message m, final Object o) {
        // no-op
    }

    public void release() {
        creator.release();
    }

    private class SingletonBeanCreator extends DefaultBeanCreator {
        private final Object instance;

        public SingletonBeanCreator(final Object instance) {
            super(null, null);
            this.instance = instance;
            super.create();
        }

        @Override
        protected Object newInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException {
            return instance;
        }

        @Override
        public Object create() {
            return instance;
        }
    }
}
