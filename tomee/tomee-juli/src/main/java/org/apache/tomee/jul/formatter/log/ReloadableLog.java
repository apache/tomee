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
package org.apache.tomee.jul.formatter.log;

import org.apache.juli.logging.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

public final class ReloadableLog {

    public static final Class<?>[] INTERFACES = new Class<?>[]{Log.class};

    private ReloadableLog() {
        // no-op
    }

    public static Log newLog(final String name, final String factory) {
        return Log.class.cast(Proxy.newProxyInstance(
                ReloadableLog.class.getClassLoader(), INTERFACES, new ReloadableLogHandler(factory, name)));
    }

    private static final class ReloadableLogHandler implements InvocationHandler {
        private static final String LOG4J_IMPL = "org.apache.tomee.loader.log.Log4jLog";
        private static final String LOG4J2_IMPL = "org.apache.tomee.loader.log.Log4j2Log";
        private static final String SLF4J_IMPL = "org.apache.tomee.loader.log.Slf4jLog";
        private static final String MAVEN_IMPL = "org.apache.openejb.maven.util.TomEEMavenLog";

        private volatile String factory;
        private final String name;
        private final AtomicReference<Log> delegate = new AtomicReference<>();
        private volatile boolean done = false;

        public ReloadableLogHandler(final String factory, final String name) {
            this.factory = factory;
            this.name = name;
            initDelegate();
        }

        private Log initDelegate() {
            if (done) {
                return delegate.get();
            }

            try {
                if (factory == null) {
                    final String f = TomEELog.getLoggerClazz();
                    if (f != null) {
                        factory = f;
                    }

                    final Log log = delegate.get();
                    if (factory == null && log != null) {
                        return log;
                    }
                }
                switch (factory) {
                    case "org.apache.openejb.util.Log4jLogStreamFactory":
                        delegate.set(newInstance(LOG4J_IMPL));
                        break;
                    case "org.apache.openejb.util.Log4j2LogStreamFactory":
                        delegate.set(newInstance(LOG4J2_IMPL));
                        break;
                    case "org.apache.openejb.util.Slf4jLogStreamFactory":
                        delegate.set(newInstance(SLF4J_IMPL));
                        break;
                    case "org.apache.openejb.maven.util.MavenLogStreamFactory":
                        delegate.set(newInstance(MAVEN_IMPL));
                        break;
                    default:
                        delegate.set(new JULLogger(name));
                }
                done = true;
            } catch (final Throwable the) {
                if (delegate.get() == null) {
                    delegate.set(new JULLogger(name));
                }
            }
            return delegate.get();
        }

        private Log newInstance(final String impl) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
            return Log.class.cast(Thread.currentThread()
                    .getContextClassLoader()
                    .loadClass(impl)
                    .getConstructor(String.class)
                    .newInstance(name));
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            try {
                return method.invoke(initDelegate(), args);
            } catch (final InvocationTargetException ite) {
                throw ite.getCause();
            }
        }
    }
}
