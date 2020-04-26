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

import org.apache.deltaspike.core.api.config.view.controller.PreRenderView;
import org.apache.deltaspike.core.api.scope.GroupedConversation;
import org.apache.deltaspike.core.api.scope.GroupedConversationScoped;
import org.superbiz.deltaspike.domain.Feedback;
import org.superbiz.deltaspike.repository.FeedbackRepository;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@GroupedConversationScoped
public class FeedbackPage implements Serializable {
    private static final long serialVersionUID = 744025508253889974L;

    private List<Feedback> feedbackList;

    @Inject
    private GroupedConversation conversation;

    @Inject
    private FeedbackRepository feedbackRepository;

    private Feedback feedback;

    @PostConstruct
    protected void init() {
        this.feedback = new Feedback();
    }

    @PreRenderView
    public void reloadFeedbackList() {
        this.feedbackList = this.feedbackRepository.findAll();
    }

    public void save() {
        this.feedbackRepository.save(this.feedback);
        this.conversation.close();
    }

    /*
     * generated
     */

    public List<Feedback> getFeedbackList() {
        return feedbackList;
    }

    public Feedback getFeedback() {
        return feedback;
    }
}
