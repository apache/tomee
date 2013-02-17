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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
import java.util.Properties;

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
            } catch (Throwable t) {
                // no-op
            }

            context.set((Context) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{ Context.class }, new MultipleContextHandler(new InitialContext(), existing, new InitialContext(props))));
        } catch (ClassNotFoundException e) {
            // no-op
        } catch (NamingException e) {
            // no-op
        }
    }

    private static class MultipleContextHandler implements InvocationHandler {
        private final Context[] contexts;

        public MultipleContextHandler(final Context... initialContexts) {
            contexts = initialContexts;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Exception err = null;
            for (Context ctx : contexts) {
                if (ctx == null) {
                    continue;
                }

                try {
                    return method.invoke(ctx, args);
                } catch (Exception e) {
                    err = e;
                }
            }
            if (err != null) {
                if (err instanceof InvocationTargetException) {
                    throw err.getCause();
                }
                throw err;
            }
            return null;
        }
    }
}
