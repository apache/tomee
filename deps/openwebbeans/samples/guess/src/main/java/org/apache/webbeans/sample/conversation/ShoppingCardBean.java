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
package org.apache.webbeans.sample.conversation;

import java.io.Serializable;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;
import javax.faces.application.FacesMessage;

import org.apache.webbeans.sample.model.conversation.ConversationModel;
import org.apache.webbeans.sample.util.FacesMessageUtil;

@RequestScoped
@Named
public class ShoppingCardBean implements Serializable
{
    private static final long serialVersionUID = 7914095399647910625L;

    private @Inject @Default Conversation conversation;
    
    private @Inject @Default ConversationModel model;
    
    private @Inject @Default FacesMessageUtil messageUtil;
    
    private String book;
    
    public String startConversation()
    {
        conversation.begin();
        
        messageUtil.addMessage(FacesMessage.SEVERITY_INFO, "Conversation with id : " + conversation.getId() + " is started", null);
        
        return null;
    }
    
    public String addNewBook()
    {
        model.getList().add(this.book);
        
        StringBuffer buffer = new StringBuffer("Your shopping card contents : [");
        
        for(String b : model.getList())
        {
            buffer.append("," + b);
        }
        
        messageUtil.addMessage(FacesMessage.SEVERITY_INFO, buffer.toString() + "]", null);
        
        
        return null;
    }
    
    public String getBooks()
    {
        StringBuffer buffer = new StringBuffer("Your shopping card contents : [");
        
        for(String b : model.getList())
        {
            buffer.append("," + b);
        }
        
        return buffer.toString();
        
    }
    
    public String endConversation()
    {
        messageUtil.addMessage(FacesMessage.SEVERITY_INFO, "Conversation with id : " + conversation.getId() + " is ended", null);
        
        conversation.end();        
        
        return null;
    }

    /**
     * @return the book
     */
    public String getBook()
    {
        return book;
    }

    /**
     * @param book the book to set
     */
    public void setBook(String book)
    {
        this.book = book;
    }

    
}
