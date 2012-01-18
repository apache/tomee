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
package org.apache.openejb.jsf;

import org.apache.openejb.AppContext;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.adaptor.ELAdaptor;

import javax.el.ExpressionFactory;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.ApplicationWrapper;

/**
 * @version $Rev$ $Date$
 */
public class CustomApplicationFactory extends ApplicationFactory {

    private final ApplicationFactory factory;
    private final WebBeansContext webBeansContext;

    private volatile Application wrappedApplication;

    public CustomApplicationFactory(final ApplicationFactory applicationFactory) {
        this.factory = applicationFactory;
        webBeansContext = WebBeansContext.currentInstance();
    }

    @Override
    public Application getApplication() {

        if (!webBeansContext.getBeanManagerImpl().isInUse()) {
            return factory.getApplication();
        }

        if (wrappedApplication == null) {

            final AppContext appContext = webBeansContext.getService(AppContext.class);

            if (appContext != null && !appContext.isCdiEnabled()) {

                wrappedApplication = factory.getApplication();

            } else {

                final Application application = factory.getApplication();

                wrappedApplication = new ApplicationWrapper() {

                    private volatile ExpressionFactory expressionFactory;

                    @Override
                    public ExpressionFactory getExpressionFactory() {
                        if (expressionFactory == null) {
                            final ELAdaptor elAdaptor = webBeansContext.getService(ELAdaptor.class);
                            expressionFactory = elAdaptor.getOwbWrappedExpressionFactory(application.getExpressionFactory());
                        }
                        return expressionFactory;
                    }

                    @Override
                    public Application getWrapped() {
                        return application;
                    }
                };

            }
        }

        return wrappedApplication;
    }

    @Override
    public void setApplication(final Application application) {
        wrappedApplication = application;
        this.factory.setApplication(application);
    }

    /* (non-Javadoc)
    * @see javax.faces.application.ApplicationFactory#getWrapped()
    */
    @Override
    public ApplicationFactory getWrapped() {
        return factory;
    }

}
