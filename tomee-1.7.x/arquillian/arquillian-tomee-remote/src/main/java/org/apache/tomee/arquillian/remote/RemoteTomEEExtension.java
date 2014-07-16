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

import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.arquillian.common.RemoteInitialContextObserver;
import org.apache.openejb.arquillian.common.deployment.DeploymentExceptionObserver;
import org.apache.openejb.arquillian.common.deployment.DeploymentExceptionProvider;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoteTomEEExtension implements LoadableExtension {

    private static final String ADAPTER = "tomee-remote";
    private static final AtomicBoolean registered = new AtomicBoolean(false);
    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    public void register(final ExtensionBuilder builder) {
        if (ArquillianUtil.isCurrentAdapter(ADAPTER)) {

            final ReentrantLock l = lock;
            l.lock();

            try {

                if (!registered.getAndSet(true)) {

                    try {
                        builder.observer(RemoteInitialContextObserver.class);
                        builder.observer(DeploymentExceptionObserver.class);
                        builder.service(DeployableContainer.class, RemoteTomEEContainer.class)
                                                    .service(AuxiliaryArchiveAppender.class, RemoteTomEEEJBEnricherArchiveAppender.class)
                                                    .service(ResourceProvider.class, DeploymentExceptionProvider.class);
                    } catch (final IllegalArgumentException e) {
                        Logger.getLogger(RemoteTomEEExtension.class.getName()).log(Level.WARNING, "RemoteTomEEExtension: " + e.getMessage());
                    }
                }
            } finally {
                l.unlock();
            }
        }
    }
}
