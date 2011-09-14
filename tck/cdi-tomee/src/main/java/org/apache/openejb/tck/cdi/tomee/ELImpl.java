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
package org.apache.openejb.tck.cdi.tomee;

import org.apache.el.ExpressionFactoryImpl;
import org.apache.el.lang.FunctionMapperImpl;
import org.apache.el.lang.VariableMapperImpl;
import org.apache.webbeans.el.WebBeansELResolver;
import org.apache.webbeans.el.WrappedExpressionFactory;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.VariableMapper;

/**
 * @version $Rev$ $Date$
 */
public class ELImpl implements org.jboss.jsr299.tck.spi.EL {

    private static final ExpressionFactory EXPRESSION_FACTORY = new WrappedExpressionFactory(new ExpressionFactoryImpl());

    public ELImpl() {
    }

    public static ELResolver getELResolver() {
        CompositeELResolver composite = new CompositeELResolver();
        composite.add(new BeanELResolver());
        composite.add(new ArrayELResolver());
        composite.add(new MapELResolver());
        composite.add(new ListELResolver());
        composite.add(new ResourceBundleELResolver());
        composite.add(new WebBeansELResolver());

        return composite;
    }

    public static class ELContextImpl extends ELContext {
        @Override
        public ELResolver getELResolver() {
            return ELImpl.getELResolver();
        }

        @Override
        public FunctionMapper getFunctionMapper() {
            return new FunctionMapperImpl();
        }

        @Override
        public VariableMapper getVariableMapper() {
            return new VariableMapperImpl();
        }

    }

    @SuppressWarnings("unchecked")
    public <T> T evaluateMethodExpression(String expression, Class<T> expectedType, Class<?>[] expectedParamTypes, Object[] expectedParams) {
        ELContext context = createELContext();
        Object object = EXPRESSION_FACTORY.createMethodExpression(context, expression, expectedType, expectedParamTypes).invoke(context, expectedParams);

        return (T) object;
    }

    @SuppressWarnings("unchecked")
    public <T> T evaluateValueExpression(String expression, Class<T> expectedType) {
        ELContext context = createELContext();
        Object object = EXPRESSION_FACTORY.createValueExpression(context, expression, expectedType).getValue(context);

        return (T) object;
    }

    @Override
    public ELContext createELContext() {
        return new ELContextImpl();
    }
}
