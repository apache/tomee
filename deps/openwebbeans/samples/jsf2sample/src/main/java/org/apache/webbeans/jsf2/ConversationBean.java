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
package org.apache.webbeans.jsf2;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class ConversationBean
{
    private @Inject Conversation conversation;
    
    private @Inject @Named("mynumber") int current;
    
    private String message;
    
    public ConversationBean()
    {
        
    }
    
    public String startConversation()
    {
        conversation.begin();
        
        message = "Conversation is started with id : " + this.conversation.getId();
        
        return null;
    }
    
    public String next()
    {
        return "next";
    }
    
    public String stopConversation()
    {
        conversation.end();
        
        current = 10;
        
       message = "Conversation is ended";

       return null;
    }

    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }

    /**
     * @return the conversation
     */
    public Conversation getConversation()
    {
        return conversation;
    }

    /**
     * @param conversation the conversation to set
     */
    public void setConversation(Conversation conversation)
    {
        this.conversation = conversation;
    }

    /**
     * @return the current
     */
    public int getCurrent()
    {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(int current)
    {
        this.current = current;
    }
    
    public String conversationLive()
    {
        return null;
    }
    

}
