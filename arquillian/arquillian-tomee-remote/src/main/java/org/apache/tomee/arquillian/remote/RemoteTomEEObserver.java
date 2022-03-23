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

import org.apache.openejb.util.AppFinder;
import org.apache.webbeans.config.WebBeansContext;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

import jakarta.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class RemoteTomEEObserver {
    @Inject
    @SuiteScoped
    private InstanceProducer<BeanManager> beanManager;

    @Inject
    @SuiteScoped
    private InstanceProducer<Context> context;

    public void beforeSuite(@Observes final BeforeSuite event) {
        final WebBeansContext webBeansContext = AppFinder.findAppContextOrWeb(
                Thread.currentThread().getContextClassLoader(), AppFinder.WebBeansContextTransformer.INSTANCE);
        if (webBeansContext != null) {
            beanManager.set(webBeansContext.getBeanManagerImpl());
        }
        try {
            context.set(new InitialContext());
        } catch (final NamingException e) {
            // no-op
        }
    }
}
