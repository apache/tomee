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
package org.apache.openejb.arquillian.common;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.enrichment.BeforeEnrichment;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Callable;

public class RemoteInitialContextObserver {
    private static final String REMOTE_INITIAL_CONTEXT_FACTORY = "org.apache.openejb.client.RemoteInitialContextFactory";

    @Inject
    @ApplicationScoped
    private InstanceProducer<Context> context;

    @Inject
    private Instance<Context> existingContext;

    @Inject
    private Instance<ProtocolMetaData> protocolMetadata;

    public void beforeSuite(@Observes final BeforeEnrichment event) {
        final ProtocolMetaData metaData = protocolMetadata.get();
        if(metaData == null || !metaData.hasContext(HTTPContext.class)) {
            return;
        }

        try {
            Thread.currentThread().getContextClassLoader().loadClass(REMOTE_INITIAL_CONTEXT_FACTORY);

            final HTTPContext httpContext = metaData.getContexts(HTTPContext.class).iterator().next();
            final Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, REMOTE_INITIAL_CONTEXT_FACTORY);
            props.setProperty(Context.PROVIDER_URL, "http://" + httpContext.getHost() + ":" + httpContext.getPort() + "/tomee/ejb");

            Context existing = null;
            try {
                existing = existingContext.get();
            } catch (final Throwable t) {
                // no-op
            }

            final Context proxyInstance = (Context) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{Context.class}, new MultipleContextHandler(props, existing));
            context.set(new InitialContextWrapper(proxyInstance)); // cause ContextProducer of arquillian supports InitialContext
        } catch (final ClassNotFoundException | NamingException e) {
            // no-op
        }
    }

    private static class MultipleContextHandler implements InvocationHandler {
        private final Context context;
        private final Properties properties;

        public MultipleContextHandler(final Properties props, final Context initialContexts) {
            properties = props;
            context = initialContexts;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            Exception err = null;
            for (final Callable<Context> callable : Arrays.asList( // order is important to avoid to start an embedded container for some cases
                    new Callable<Context>() { // then try to create a remote context
                        @Override
                        public Context call() throws Exception {
                            return new InitialContext(properties);
                        }
                    },
                    new Callable<Context>() { // then existing context
                        @Override
                        public Context call() throws Exception {
                            return context;
                        }
                    },
                    new Callable<Context>() { // then contextual context, this can start an embedded container in some cases
                        @Override
                        public Context call() throws Exception {
                            return new InitialContext();
                        }
                    }

            )) {

                try {
                    final Context ctx = callable.call();
                    if (ctx == null) {
                        continue;
                    }
                    return method.invoke(ctx, args);
                } catch (final Exception e) {
                    err = e;
                }
            }

            if (err != null) {
                if (InvocationTargetException.class.isInstance(err)) {
                    throw err.getCause();
                }
                throw err;
            }

            return null;
        }
    }

    private static class InitialContextWrapper extends InitialContext {
        private final Context delegate;

        public InitialContextWrapper(final Context proxyInstance) throws NamingException {
            super(true);
            this.delegate = proxyInstance;
        }

        @Override
        protected Context getURLOrDefaultInitCtx(final String name) {
            return delegate;
        }
    }
}
