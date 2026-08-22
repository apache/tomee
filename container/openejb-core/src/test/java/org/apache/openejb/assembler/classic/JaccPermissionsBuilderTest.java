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
package org.apache.openejb.assembler.classic;

import jakarta.security.jacc.Policy;
import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyConfigurationFactory;
import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.PolicyFactory;
import org.apache.openejb.core.security.jacc.BasicPolicyConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.security.auth.Subject;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JaccPermissionsBuilderTest {

    private static final String CONTEXT_ID = "JaccPermissionsBuilderTest";

    private PolicyConfigurationFactory previousConfigurationFactory;
    private PolicyFactory previousPolicyFactory;

    @Before
    public void rememberFactories() throws Exception {
        try {
            previousConfigurationFactory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        } catch (final ClassNotFoundException | PolicyContextException e) {
            previousConfigurationFactory = null;
        }
        previousPolicyFactory = PolicyFactory.getPolicyFactory();
    }

    @After
    public void restoreFactories() {
        PolicyConfigurationFactory.setPolicyConfigurationFactory(previousConfigurationFactory);
        PolicyFactory.setPolicyFactory(previousPolicyFactory);
    }

    @Test
    public void refreshesContextPolicyAfterCommit() throws Exception {
        final List<String> events = new ArrayList<>();
        final RecordingConfiguration configuration = new RecordingConfiguration(events);
        final RecordingPolicy policy = new RecordingPolicy(events);
        final RecordingPolicyFactory policyFactory = new RecordingPolicyFactory(policy);

        PolicyConfigurationFactory.setPolicyConfigurationFactory(new RecordingConfigurationFactory(configuration));
        PolicyFactory.setPolicyFactory(policyFactory);

        new JaccPermissionsBuilder().install(new PolicyContext(CONTEXT_ID));

        assertEquals(Arrays.asList("commit", "refresh"), events);
        assertEquals(CONTEXT_ID, policyFactory.requestedContextId);
    }

    private static final class RecordingConfigurationFactory extends PolicyConfigurationFactory {
        private final PolicyConfiguration configuration;

        private RecordingConfigurationFactory(final PolicyConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public PolicyConfiguration getPolicyConfiguration(final String contextID, final boolean remove) {
            return configuration;
        }

        @Override
        public PolicyConfiguration getPolicyConfiguration(final String contextID) {
            return configuration;
        }

        @Override
        public PolicyConfiguration getPolicyConfiguration() {
            return configuration;
        }

        @Override
        public boolean inService(final String contextID) throws PolicyContextException {
            return configuration.inService();
        }
    }

    private static final class RecordingConfiguration extends BasicPolicyConfiguration {
        private final List<String> events;

        private RecordingConfiguration(final List<String> events) {
            super(CONTEXT_ID);
            this.events = events;
        }

        @Override
        public void commit() throws PolicyContextException {
            super.commit();
            events.add("commit");
        }
    }

    private static final class RecordingPolicyFactory extends PolicyFactory {
        private final Policy policy;
        private String requestedContextId;

        private RecordingPolicyFactory(final Policy policy) {
            this.policy = policy;
        }

        @Override
        public Policy getPolicy(final String contextId) {
            requestedContextId = contextId;
            return policy;
        }

        @Override
        public void setPolicy(final String contextId, final Policy policy) {
            // no-op
        }
    }

    private static final class RecordingPolicy implements Policy {
        private final List<String> events;

        private RecordingPolicy(final List<String> events) {
            this.events = events;
        }

        @Override
        public PermissionCollection getPermissionCollection(final Subject subject) {
            return new Permissions();
        }

        @Override
        public void refresh() {
            events.add("refresh");
        }
    }
}
