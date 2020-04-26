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

import org.apache.deltaspike.core.api.scope.GroupedConversation;
import org.apache.deltaspike.core.api.scope.GroupedConversationScoped;
import org.apache.deltaspike.jsf.api.message.JsfMessage;
import org.apache.myfaces.extensions.validator.beanval.annotation.BeanValidation;
import org.apache.myfaces.extensions.validator.crossval.annotation.Equals;
import org.superbiz.deltaspike.WebappMessageBundle;
import org.superbiz.deltaspike.domain.User;
import org.superbiz.deltaspike.domain.validation.Full;
import org.superbiz.deltaspike.repository.UserRepository;
import org.superbiz.deltaspike.view.config.Pages;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@GroupedConversationScoped
public class RegistrationPage implements Serializable {
    private static final long serialVersionUID = 3844502441069448490L;

    @Inject
    private UserRepository userService;

    @Inject
    private GroupedConversation conversation;

    @Inject
    private JsfMessage<WebappMessageBundle> webappMessages;

    private User user = new User();

    @Inject
    private UserHolder userHolder;

    @Equals("user.password")
    private String repeatedPassword;

    @BeanValidation(useGroups = Full.class) //triggers UniqueUserNameValidator
    public Class<? extends Pages> register() {
        this.userService.save(this.user);
        this.webappMessages.addInfo().msgUserRegistered(this.user.getUserName());

        //in order to re-use the page-bean for the login-page
        this.conversation.close();

        return Pages.Login.class;
    }

    public Class<? extends Pages> login() {
        User user = this.userService.findByUserName(this.user.getUserName());
        if (user != null && user.getPassword().equals(this.user.getPassword())) {
            this.webappMessages.addInfo().msgLoginSuccessful();
            this.userHolder.setCurrentUser(user);
            return Pages.About.class;
        }

        this.webappMessages.addError().msgLoginFailed();

        return null;
    }

    public User getUser() {
        return user;
    }

    public String getRepeatedPassword() {
        return repeatedPassword;
    }

    public void setRepeatedPassword(String repeatedPassword) {
        this.repeatedPassword = repeatedPassword;
    }
}