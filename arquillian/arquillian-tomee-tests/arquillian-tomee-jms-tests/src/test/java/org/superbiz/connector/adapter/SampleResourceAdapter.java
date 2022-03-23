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
package org.superbiz.connector.adapter;

import org.superbiz.connector.api.InboundListener;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.IllegalStateException;

@Connector(description = "Sample Resource Adapter", displayName = "Sample Resource Adapter", eisType = "Sample Resource Adapter", version = "1.0")
public class SampleResourceAdapter implements ResourceAdapter {

    final Map<SampleActivationSpec, EndpointTarget> targets = new ConcurrentHashMap<SampleActivationSpec, EndpointTarget>();

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
    }

    public void stop() {
    }

    public void endpointActivation(final MessageEndpointFactory messageEndpointFactory, final ActivationSpec activationSpec)
            throws ResourceException
    {
        final SampleActivationSpec sampleActivationSpec = (SampleActivationSpec) activationSpec;

        try {
            final MessageEndpoint messageEndpoint = messageEndpointFactory.createEndpoint(null);
            final EndpointTarget target = new EndpointTarget(messageEndpoint);
            targets.put(sampleActivationSpec, target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        final SampleActivationSpec sampleActivationSpec = (SampleActivationSpec) activationSpec;

        final EndpointTarget endpointTarget = targets.get(sampleActivationSpec);
        if (endpointTarget == null) {
            throw new IllegalStateException("No EndpointTarget to undeploy for ActivationSpec " + activationSpec);
        }

        endpointTarget.messageEndpoint.release();
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }

    public void sendMessage(final String message) {
        final Collection<EndpointTarget> endpoints = this.targets.values();
        for (final EndpointTarget endpoint : endpoints) {
            endpoint.invoke(message);
        }
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
}
