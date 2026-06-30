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

package org.apache.tomee.arquillian.remote;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder;
import org.jboss.arquillian.core.spi.context.Context;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Reproducer for TOMEE-4631.
 *
 * <p>{@link RemoteTomEEExtension} guards container registration behind a JVM-static
 * {@code AtomicBoolean} latch. {@link LoadableExtension#register(ExtensionBuilder)} is invoked
 * once per Arquillian {@code Manager} bootstrap. When a second {@code Manager} is bootstrapped in
 * the same JVM fork (the classic trigger being maven-failsafe-plugin's
 * {@code rerunFailingTestsCount}, which spins up a fresh {@code Manager} to rerun a failed test),
 * the static flag is already {@code true}, so the second bootstrap skips
 * {@code builder.service(DeployableContainer.class, ...)} and that {@code Manager} ends up with an
 * empty {@code ContainerRegistry}. Deployments then fail with:</p>
 *
 * <pre>
 * org.jboss.arquillian.container.test.impl.client.deployment.ValidationException:
 *   DeploymentScenario contains a target (_DEFAULT_) not matching any defined Container in the
 *   registry. Please include at least 1 Deployable Container on your Classpath.
 * </pre>
 *
 * <p>The sibling {@code EmbeddedTomEEExtension} has no such latch and registers unconditionally,
 * which is why every other Arquillian adapter survives a rerun and only arquillian-tomee-remote
 * does not.</p>
 *
 * <p>This test drives {@code register(...)} the same way Arquillian's {@code LoadableExtensionLoader}
 * does (once per simulated {@code Manager} bootstrap, each with its own {@link ExtensionBuilder})
 * and asserts the {@link DeployableContainer} is registered for every bootstrap, not just the
 * first. It failed on the former static-latch implementation and guards the unconditional
 * registration that replaced it.</p>
 */
public class RemoteTomEEExtensionReRegistrationTest {

    @Test
    public void containerIsReRegisteredOnEveryManagerBootstrap() {
        // 1st Manager bootstrap (e.g. the initial failsafe run)
        final RecordingExtensionBuilder first = new RecordingExtensionBuilder();
        new RemoteTomEEExtension().register(first);
        assertTrue("Sanity: the first Manager bootstrap must register the DeployableContainer",
                first.registeredDeployableContainer());

        // 2nd Manager bootstrap in the same JVM (e.g. a failsafe rerunFailingTestsCount rerun)
        final RecordingExtensionBuilder second = new RecordingExtensionBuilder();
        new RemoteTomEEExtension().register(second);

        // TOMEE-4631: the static latch makes the second bootstrap skip the DeployableContainer
        // service, leaving the second Manager's ContainerRegistry empty -> _DEFAULT_ matches nothing.
        assertTrue("TOMEE-4631: a second Arquillian Manager in the same JVM did NOT get a "
                        + "DeployableContainer registered; reruns (e.g. failsafe rerunFailingTestsCount) "
                        + "will fail with 'DeploymentScenario contains a target (_DEFAULT_) not matching "
                        + "any defined Container in the registry'.",
                second.registeredDeployableContainer());
    }

    /**
     * Minimal {@link ExtensionBuilder} that records which service contracts were registered,
     * standing in for the real builder backed by Arquillian's {@code ServiceRegistry}.
     */
    private static final class RecordingExtensionBuilder implements ExtensionBuilder {
        private final List<Class<?>> services = new ArrayList<>();

        @Override
        public <T> ExtensionBuilder service(final Class<T> service, final Class<? extends T> impl) {
            services.add(service);
            return this;
        }

        @Override
        public <T> ExtensionBuilder override(final Class<T> service, final Class<? extends T> oldImpl,
                                             final Class<? extends T> newImpl) {
            return this;
        }

        @Override
        public ExtensionBuilder observer(final Class<?> observer) {
            return this;
        }

        @Override
        public ExtensionBuilder context(final Class<? extends Context> context) {
            return this;
        }

        boolean registeredDeployableContainer() {
            return services.contains(DeployableContainer.class);
        }
    }
}
