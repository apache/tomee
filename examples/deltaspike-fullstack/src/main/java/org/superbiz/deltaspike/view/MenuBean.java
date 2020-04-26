/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.deltaspike.view;

import org.apache.deltaspike.core.spi.scope.conversation.GroupedConversationManager;
import org.superbiz.deltaspike.view.config.Pages;

import jakarta.enterprise.inject.Model;
import jakarta.inject.Inject;

@Model
public class MenuBean {
    @Inject
    private GroupedConversationManager groupedConversationManager;

    public Class<? extends Pages> home() {
        //close all conversations of the current window
        this.groupedConversationManager.closeConversations();
        return Pages.Index.class;
    }

    public Class<? extends Pages.Secure> feedback() {
        //close all conversations of the current window
        this.groupedConversationManager.closeConversations();
        return Pages.Secure.FeedbackList.class;
    }

    public Class<? extends Pages> about() {
        //close all conversations of the current window
        this.groupedConversationManager.closeConversations();
        return Pages.About.class;
    }

    public Class<? extends Pages> login() {
        //close all conversations of the current window
        this.groupedConversationManager.closeConversations();
        return Pages.Login.class;
    }

    public Class<? extends Pages> register() {
        //close all conversations of the current window
        this.groupedConversationManager.closeConversations();
        return Pages.Registration.class;
    }
}
