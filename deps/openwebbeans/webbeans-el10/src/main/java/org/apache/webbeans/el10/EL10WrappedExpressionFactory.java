/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.el10;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;

public class EL10WrappedExpressionFactory extends ExpressionFactory
{

    private ExpressionFactory expressionFactory;

    public EL10WrappedExpressionFactory(ExpressionFactory expressionFactory)
    {
        this.expressionFactory = expressionFactory;
    }
    
    @Override
    public Object coerceToType(Object arg0, Class<?> arg1) throws ELException
    {
        return this.expressionFactory.coerceToType(arg0, arg1);
    }

    @Override
    public MethodExpression createMethodExpression(ELContext arg0, String arg1, Class<?> arg2, Class<?>[] arg3) throws ELException, NullPointerException
    {
        return this.expressionFactory.createMethodExpression(arg0, arg1, arg2, arg3);
    }

    @Override
    public ValueExpression createValueExpression(Object arg0, Class<?> arg1)
    {
        ValueExpression wrapped = this.expressionFactory.createValueExpression(arg0, arg1);
        
        return new EL10ValueExpression(wrapped);
    }

    @Override
    public ValueExpression createValueExpression(ELContext arg0, String arg1, Class<?> arg2) throws NullPointerException, ELException
    {   
        ValueExpression wrapped = this.expressionFactory.createValueExpression(arg0, arg1, arg2);
                
        return new EL10ValueExpression(wrapped);
    }

}
