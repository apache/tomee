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
package org.apache.webbeans.web.tests.failover;

import java.io.Serializable;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

@ConversationScoped
public class ConversationScopedBean implements Serializable
{
    private static final long serialVersionUID = -1494676081508377549L;

    @Inject private BeanManager beanManager;
    @Inject private Conversation conversation;

    private String text;

    public void beginConversation()
    {
        if (conversation.isTransient())
        {
            conversation.begin();
        }
    }

    public void endConversation()
    {
        if (!conversation.isTransient())
        {
            conversation.end();
        }
    }

    public boolean isBeanManagerNotNull()
    {
        return beanManager != null;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
