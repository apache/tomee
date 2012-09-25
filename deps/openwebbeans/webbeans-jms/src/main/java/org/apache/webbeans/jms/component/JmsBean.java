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
package org.apache.webbeans.jms.component;

import java.lang.reflect.Method;
import java.util.logging.Level;

import javax.enterprise.context.spi.CreationalContext;
import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.JmsBeanMarker;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.jms.JMSModel;
import org.apache.webbeans.jms.util.Closable;
import org.apache.webbeans.logger.WebBeansLoggerFacade;

public class JmsBean<T> extends AbstractOwbBean<T> implements JmsBeanMarker
{
    private JMSModel jmsModel = null;
    
    JmsBean(JMSModel jmsModel)
    {
        super(WebBeansType.JMS, WebBeansContext.getInstance());
        this.jmsModel = jmsModel;
    }

    @Override
    protected T createInstance(CreationalContext<T> creationalContext)
    {
        return null;
    }

    @Override
    protected void destroyInstance(T instance,CreationalContext<T> creationalContext)
    {        
        try
        {
            if(instance != null)
            {
                Method method = Closable.class.getMethod("closeJMSObject", new Class[]{});
                
                method.invoke(instance, new Object[]{});                    
            }
        }
        
        catch (Exception e)
        {
            logger.log(Level.SEVERE, WebBeansLoggerFacade.constructMessage(OWBLogConst.ERROR_0010, e));
        }
}
        
    public JMSModel getJmsModel()
    {
        return this.jmsModel;
    }
}
