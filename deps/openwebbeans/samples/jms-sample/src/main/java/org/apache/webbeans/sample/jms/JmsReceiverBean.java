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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.TextMessage;

import org.apache.webbeans.sample.bindings.JmsBinding;

@SessionScoped
@Named("receiver")
public class JmsReceiverBean implements MessageListener,Serializable
{
    private static final long serialVersionUID = 5704416477431590842L;

    private @Inject @JmsBinding QueueReceiver queueReceiver;
    
    private @Inject @JmsBinding QueueConnection queueConnection;
    
    private String message;
    
    private boolean receive = false;
    
    private List<String> messages = new ArrayList<String>();

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void startReceive()
    {
        try
        {
            if(!receive)
            {
                this.receive = true;
                this.queueConnection.start();
            }
            
            queueReceiver.setMessageListener(this);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onMessage(Message message)
    {
        TextMessage me = (TextMessage) message;
        try
        {
            this.message = me.getText();
            this.messages.add(this.message);
        }
        catch (JMSException e)
        {
            e.printStackTrace();
        }
    }

    public String refresh()
    {
        return null;
    }
    
    public boolean isReceive()
    {
        return receive;
    }

    public void setReceive(boolean receive)
    {
        this.receive = receive;
    }

    public List<String> getMessages()
    {
        return messages;
    }

    public void setMessages(List<String> messages)
    {
        this.messages = messages;
    }
    
    

}
