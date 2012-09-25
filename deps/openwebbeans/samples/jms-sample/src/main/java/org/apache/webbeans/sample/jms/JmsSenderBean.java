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
package org.apache.webbeans.sample.jms;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import org.apache.webbeans.sample.bindings.JmsBinding;

@RequestScoped
@Named("sender")
public class JmsSenderBean
{
    private @Inject @JmsBinding QueueSender queueSender;
    
    private @Inject @JmsBinding QueueSession queueSession;
    
    private String text;

    public JmsSenderBean()
    {
        
    }
    
    public String addMessage()
    {
        try
        {
            TextMessage message = queueSession.createTextMessage(this.text);
            queueSender.send(message);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
        
        return null;
    }

    /**
     * @return the text
     */
    public String getText()
    {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text)
    {
        this.text = text;
    }
    
    
}
