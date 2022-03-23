/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.mdb.connector.impl;

import org.apache.openejb.core.mdb.connector.api.InboundListener;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.BootstrapContext;
import jakarta.resource.spi.Connector;
import jakarta.resource.spi.ResourceAdapter;
import jakarta.resource.spi.ResourceAdapterInternalException;
import jakarta.resource.spi.UnavailableException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Connector(description = "Sample Resource Adapter", displayName = "Sample Resource Adapter", eisType = "Sample Resource Adapter", version = "1.0")
public class SampleResourceAdapter implements ResourceAdapter {

    private final Map<SampleActivationSpec, MessageEndpointFactory> targets = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(50);

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
    }

    public void stop() {
    }

    public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec)
            throws ResourceException
    {
        final SampleActivationSpec sampleActivationSpec = (SampleActivationSpec) activationSpec;

        try {
            targets.put(sampleActivationSpec, messageEndpointFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        final SampleActivationSpec sampleActivationSpec = (SampleActivationSpec) activationSpec;
        targets.remove(sampleActivationSpec);
        threadPool.shutdownNow();
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }

    public void sendMessage(final String message) {
        threadPool.submit(new MessageSender(message));
    }

    public static class EndpointTarget {
        private final MessageEndpoint messageEndpoint;

        public EndpointTarget(final MessageEndpoint messageEndpoint) {
            this.messageEndpoint = messageEndpoint;
        }

        public void invoke(final String message) {
            ((InboundListener)this.messageEndpoint).receiveMessage(message);
        }
    }

    public class MessageSender implements Runnable {
        private final String message;

        public MessageSender(final String message) {
            this.message = message;
        }

        @Override
        public void run() {
            while (targets.isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            final Collection<MessageEndpointFactory> messageEndpointFactories = targets.values();
            for (final MessageEndpointFactory messageEndpointFactory : messageEndpointFactories) {
                try {
                    final MessageEndpoint endpoint = messageEndpointFactory.createEndpoint(null);
                    final EndpointTarget endpointTarget = new EndpointTarget(endpoint);
                    endpointTarget.invoke(message);
                    endpoint.release();
                } catch (UnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
