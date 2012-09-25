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
package org.apache.webbeans.el;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.el.ValueExpression;
import javax.el.ValueReference;

public class WrappedValueExpression extends ValueExpression
{
    private static final long serialVersionUID = 1L;

    private ValueExpression valueExpression;
    
    public WrappedValueExpression(ValueExpression valueExpression)
    {
        this.valueExpression = valueExpression;
    }
    
    

    /* (non-Javadoc)
     * @see javax.el.ValueExpression#getValueReference(javax.el.ELContext)
     */
    @Override
    public ValueReference getValueReference(ELContext context)
    {
        return valueExpression.getValueReference(context);
    }

    @Override
    public Class<?> getExpectedType()
    {
        return valueExpression.getExpectedType();
    }

    @Override
    public Class<?> getType(ELContext arg0) throws NullPointerException, PropertyNotFoundException, ELException
    {        
        return valueExpression.getType(arg0);
    }

    @Override
    public Object getValue(ELContext context) throws NullPointerException, PropertyNotFoundException, ELException
    {
        Object value = null;
        try
        {
           value = valueExpression.getValue(context);
            
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
        return valueExpression.isReadOnly(arg0);
    }

    @Override
    public void setValue(ELContext arg0, Object arg1) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException
    {
        valueExpression.setValue(arg0, arg1);
    }

    @Override
    public boolean equals(Object arg0)
    {        
        return valueExpression.equals(arg0);
    }

    @Override
    public String getExpressionString()
    {       
        return valueExpression.getExpressionString();
    }

    @Override
    public int hashCode()
    {        
        return valueExpression.hashCode();
    }

    @Override
    public boolean isLiteralText()
    {        
        return valueExpression.isLiteralText();
    }

}
