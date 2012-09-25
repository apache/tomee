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

import java.lang.annotation.Annotation;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.apache.webbeans.annotation.DependentScopeLiteral;
import org.apache.webbeans.jms.JMSModel;
import org.apache.webbeans.jms.JMSModel.JMSType;
import org.apache.webbeans.util.Asserts;

public final class JmsComponentFactory
{
    private static JmsComponentFactory instance = new JmsComponentFactory();

    private JmsComponentFactory()
    {
        
    }
    
    public static JmsComponentFactory getJmsComponentFactory()
    {
        return instance;
    }
    
    public <T> JmsBean<T> getJmsComponent(JMSModel model)
    {
        Asserts.assertNotNull(model,"model parameter can not be null");
        
        JmsBean<T> component = new JmsBean<T>(model);
        
        if(model.getJmsType().equals(JMSType.QUEUE))
        {
            component.addApiType(Queue.class);
            component.addApiType(QueueConnection.class);
            component.addApiType(QueueSession.class);
            component.addApiType(QueueSender.class);
            component.addApiType(QueueReceiver.class);
        }
        else
        {
            component.addApiType(Topic.class);
            component.addApiType(TopicConnection.class);
            component.addApiType(TopicSession.class);
            component.addApiType(TopicPublisher.class);
            component.addApiType(TopicSubscriber.class);
        }
        
        component.setImplScopeType(new DependentScopeLiteral());
        
        Annotation[] anns = model.getBindings();
        
        for(Annotation a : anns)
        {
            component.addQualifier(a);
        }
        
        return component;
    }
}
