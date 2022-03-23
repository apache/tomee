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

package org.apache.openejb.cdi;

import org.apache.openejb.AppContext;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.el22.WebBeansELResolver;
import org.apache.webbeans.el22.WrappedExpressionFactory;
import org.apache.webbeans.spi.adaptor.ELAdaptor;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;

/**
 * @version $Rev$ $Date$
 */
public class CustomELAdapter implements ELAdaptor {

    private final AppContext appContext;

    public CustomELAdapter(final AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public ELResolver getOwbELResolver() {
        WebBeansContext old = null;
        boolean exit = false;
        try { // just some safety around this but should be very very rare
            WebBeansContext.currentInstance();
        } catch (final IllegalStateException ise) {
            old = ThreadSingletonServiceImpl.enter(appContext.getWebBeansContext());
            exit = true;
        }
        try {
            return new WebBeansELResolver();
        } finally {
            if (exit) {
                ThreadSingletonServiceImpl.exit(old);
            }
        }
    }

    @Override
    public ExpressionFactory getOwbWrappedExpressionFactory(final ExpressionFactory expressionFactory) {
        if (!appContext.isCdiEnabled()) {
            return expressionFactory;
        }
        return new WrappedExpressionFactory(expressionFactory);
    }
}
