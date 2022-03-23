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

package org.apache.openejb.bval;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;

public final class ValidatorUtil {
    private ValidatorUtil() {
        // no-op
    }

    public static ValidatorFactory validatorFactory() {
        try {
            return lookupFactory();
        } catch (final NamingException e) {
            return tryJndiLaterFactory();
        }
    }

    public static ValidatorFactory lookupFactory() throws NamingException {
        return (ValidatorFactory) new InitialContext().lookup("java:comp/ValidatorFactory");
    }

    public static ValidatorFactory tryJndiLaterFactory() {
        return proxy(ValidatorFactory.class, "java:comp/ValidatorFactory");
    }

    public static Validator validator() {
        try {
            return (Validator) new InitialContext().lookup("java:comp/Validator");
        } catch (final NamingException e) {
            return proxy(Validator.class, "java:comp/Validator");
        }
    }

    // proxy because depending on when injection/threadcontext is set
    // it is better to do it lazily
    // this is mainly done for tests since the first lookup will work in TomEE
    private static <T> T proxy(final Class<T> t, final String jndi) {
        return t.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{t},
            new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    if (Object.class.equals(method.getDeclaringClass())) {
                        return method.invoke(this);
                    }

                    final ThreadContext ctx = ThreadContext.getThreadContext();
                    if (ctx != null) {
                        return method.invoke(ctx.getBeanContext().getJndiContext().lookup(jndi), args);
                    }

                    // try to find from current ClassLoader
                    // can lead to find the bad validator regarding module separation
                    // but since it shares the same classloader
                    // it will probably share the same config
                    // so the behavior will be the same
                    // + this code should rarely be used
                    final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                    if (tccl == null) {
                        return null;
                    }

                    final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);

                    Object value = null;
                    for (final AppContext appContext : containerSystem.getAppContexts()) {
                        final ClassLoader appContextClassLoader = appContext.getClassLoader();
                        if (tccl.equals(appContextClassLoader) || appContextClassLoader.equals(tccl)) {
                            final Collection<String> tested = new ArrayList<>();
                            for (final BeanContext bean : appContext.getBeanContexts()) {
                                if (BeanContext.Comp.class.equals(bean.getBeanClass())) {
                                    final String uniqueId = bean.getModuleContext().getUniqueId();
                                    if (tested.contains(uniqueId)) {
                                        continue;
                                    }

                                    tested.add(uniqueId);

                                    try {
                                        value = containerSystem.getJNDIContext().lookup(
                                            (jndi.endsWith("Factory") ?
                                                Assembler.VALIDATOR_FACTORY_NAMING_CONTEXT
                                                : Assembler.VALIDATOR_NAMING_CONTEXT)
                                                + uniqueId);
                                        break;
                                    } catch (final NameNotFoundException nnfe) {
                                        // no-op
                                    }
                                }
                            }
                            if (ClassLoader.getSystemClassLoader() != appContextClassLoader) {
                                break;
                            } // else we surely have a single AppContext so let's try WebContext
                        }
                        for (final WebContext web : appContext.getWebContexts()) {
                            final ClassLoader webClassLoader = web.getClassLoader();
                            if (webClassLoader.equals(tccl) || tccl.equals(webClassLoader)) {
                                value = web.getJndiEnc().lookup(jndi);
                                break;
                            }
                        }
                        if (value != null) {
                            break;
                        }
                    }

                    if (value != null) {
                        return method.invoke(value, args);
                    }

                    return null;
                }

                @Override
                public String toString() {
                    return "Proxy::" + t.getName();
                }
            }));
    }
}
