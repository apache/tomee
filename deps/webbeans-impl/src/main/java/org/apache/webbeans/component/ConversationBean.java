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
package org.apache.webbeans.component;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.spi.CreationalContext;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.conversation.ConversationImpl;
import org.apache.webbeans.spi.ConversationService;

/**
 * Conversation bean implementation.
 * @version $Rev$ $Date$
 *
 */
public class ConversationBean extends AbstractInjectionTargetBean<Conversation>
{
    /**
     * Default constructor.
     * @param webBeansContext
     */
    public ConversationBean(WebBeansContext webBeansContext)
    {
        super(WebBeansType.CONVERSATION, Conversation.class, webBeansContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Conversation createInstance(CreationalContext<Conversation> creationalContext)
    {
        Conversation conversation = null;
        //Gets conversation service
        ConversationService conversationService = getWebBeansContext().getService(ConversationService.class);
        //Gets conversation id
        String conversationId = conversationService.getConversationId();       
        //Gets session id that conversation is created
        String sessionId = conversationService.getConversationSessionId();

        //If conversation id is not null, this means that
        //conversation is propogated
        if (conversationId != null)
        {
            //Gets propogated conversation
            conversation = getWebBeansContext().getConversationManager().getPropogatedConversation(conversationId,sessionId);
        }
        
        if (conversation == null)
        {
            if(sessionId != null)
            {
                conversation = new ConversationImpl(conversationService.getConversationSessionId(),
                                                    getWebBeansContext());
            }
            else
            {
                //Used in Tests
                conversation = new ConversationImpl(getWebBeansContext());
            }
            
        }

        return conversation;
    }

    @Override
    protected void destroyInstance(Conversation instance, CreationalContext<Conversation> creationalContext)
    {
    }
    
}
