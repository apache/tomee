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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.arquillian.common.ArquillianUtil;
import org.apache.openejb.arquillian.common.deployment.DeploymentExceptionObserver;
import org.apache.openejb.arquillian.common.deployment.DeploymentExceptionProvider;
import org.apache.openejb.arquillian.transaction.OpenEJBTransactionProvider;
import org.apache.openejb.util.JuliLogStreamFactory;
import org.apache.openejb.util.LogCategory;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.transaction.spi.provider.TransactionProvider;

import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class OpenEJBExtension implements LoadableExtension {
    private static final String OPENEJB_ADAPTER_NAME = "openejb";

    static { // logging conf
        if (ArquillianUtil.isCurrentAdapter(OPENEJB_ADAPTER_NAME)) {
            if (System.getProperty("java.util.logging.config.class") == null || System.getProperty("java.util.logging.config.file") == null) {
                final Enumeration<String> list = LogManager.getLogManager().getLoggerNames();
                while (list.hasMoreElements()) {
                    initLogger(list.nextElement());
                }
                initLogger(LogCategory.OPENEJB.getName());
            }
        }
    }

    private static void initLogger(final String name) {
        final Logger logger = Logger.getLogger(name);
        final Handler[] handlers = logger.getHandlers();
        if (handlers != null) {
            for (int i = 0; i < handlers.length; i++) {
                logger.removeHandler(handlers[i]);
            }
        }
        logger.setUseParentHandlers(false);
        logger.addHandler(new JuliLogStreamFactory.OpenEJBSimpleLayoutHandler());

    }

    @Override
    public void register(final ExtensionBuilder extensionBuilder) {
        if (ArquillianUtil.isCurrentAdapter(OPENEJB_ADAPTER_NAME)) {
            extensionBuilder.service(DeployableContainer.class, OpenEJBDeployableContainer.class)
                .service(TestEnricher.class, OpenEJBInjectionEnricher.class)
                .service(ResourceProvider.class, DeploymentExceptionProvider.class)
                .service(TransactionProvider.class, OpenEJBTransactionProvider.class)
                .observer(TestObserver.class)
                .observer(DeploymentExceptionObserver.class);
        }
    }
}
