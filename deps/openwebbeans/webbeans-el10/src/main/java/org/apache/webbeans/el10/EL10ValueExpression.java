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
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;

import org.apache.webbeans.el.ELContextStore;

public class EL10ValueExpression extends ValueExpression
{
    private static final long serialVersionUID = 1L;

    private ValueExpression valueExpression;
    
    public EL10ValueExpression(ValueExpression valueExpression)
    {
        this.valueExpression = valueExpression;
    }
    

    @Override
    public Class<?> getExpectedType()
    {
        return this.valueExpression.getExpectedType();
    }

    @Override
    public Class<?> getType(ELContext arg0) throws NullPointerException, PropertyNotFoundException, ELException
    {
        return this.valueExpression.getType(arg0);
    }

    @Override
    public Object getValue(ELContext context) throws NullPointerException, PropertyNotFoundException, ELException
    {
        Object value = null;
        try
        {
           value = this.valueExpression.getValue(context);           
            
        }
        finally
        {
            //Destroy dependent store
            ELContextStore store = ELContextStore.getInstance(false);
            if(store != null)
            {
                store.destroyDependents();
            }
        }
        
        return value;
    }
    
    
    @Override
    public boolean isReadOnly(ELContext arg0) throws NullPointerException, PropertyNotFoundException, ELException
    {
        return this.valueExpression.isReadOnly(arg0);
    }

    @Override
    public void setValue(ELContext arg0, Object arg1) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
        this.valueExpression.setValue(arg0, arg1);
    }

    @Override
    public boolean equals(Object arg0)
    {
        return this.valueExpression.equals(arg0);
    }

    @Override
    public String getExpressionString()
    {
        return this.valueExpression.getExpressionString();
    }

    @Override
    public int hashCode()
    {
        return this.valueExpression.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {
        return this.valueExpression.isLiteralText();
    }

}
