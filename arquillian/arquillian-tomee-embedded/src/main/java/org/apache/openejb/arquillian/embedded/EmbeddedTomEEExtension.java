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

package org.apache.openejb.arquillian.embedded;

import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.arquillian.common.RemoteInitialContextObserver;
import org.apache.openejb.arquillian.common.TomEEInjectionEnricher;
import org.apache.openejb.arquillian.common.deployment.DeploymentExceptionObserver;
import org.apache.openejb.arquillian.common.deployment.DeploymentExceptionProvider;
import org.apache.openejb.arquillian.transaction.OpenEJBTransactionProvider;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;

public class EmbeddedTomEEExtension implements LoadableExtension {
    private static final String ADAPTER = "tomee-embedded";

    @Override
    public void register(final ExtensionBuilder builder) {
        if (ArquillianUtil.isCurrentAdapter(ADAPTER)) {
            builder.service(DeployableContainer.class, EmbeddedTomEEContainer.class)
                .observer(DeploymentExceptionObserver.class)
                .observer(RemoteInitialContextObserver.class)
                .service(TestEnricher.class, TomEEInjectionEnricher.class)
                .service(TransactionProvider.class, OpenEJBTransactionProvider.class)
                .service(ResourceProvider.class, DeploymentExceptionProvider.class);
        }
    }
}
