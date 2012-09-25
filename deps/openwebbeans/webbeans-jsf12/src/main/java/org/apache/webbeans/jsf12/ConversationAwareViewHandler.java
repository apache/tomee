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
package org.apache.webbeans.jsf12;

import javax.enterprise.context.Conversation;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.conversation.ConversationManager;

public class ConversationAwareViewHandler extends ViewHandlerWrapper
{
    private final ViewHandler delegate;

    private Boolean owbApplication = null;

    public ConversationAwareViewHandler(ViewHandler delegate)
    {
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getActionURL(FacesContext context, String viewId)
    {
        if(isOwbApplication())
        {
            return delegate.getActionURL(context, viewId);
        }
        
        String url = delegate.getActionURL(context, viewId);

        ConversationManager conversationManager = WebBeansContext.getInstance().getConversationManager();
        Conversation conversation = conversationManager.getConversationBeanReference();
        if (conversation != null && !conversation.isTransient())
        {
            url = JSFUtil.getRedirectViewIdWithCid(url, conversation.getId());
        }

        return url;
    }

    private boolean isOwbApplication()
    {
        if (owbApplication == null)
        {
            owbApplication = Boolean.valueOf(WebBeansContext.getInstance().getBeanManagerImpl().isInUse());
        }

        return owbApplication.booleanValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewHandler getWrapped()
    {
        return delegate;
    }
}
