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
package org.superbiz.myfaces.view;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.Conversation;
import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.apache.myfaces.extensions.cdi.jsf.api.Jsf;
import org.apache.myfaces.extensions.cdi.message.api.MessageContext;
import org.apache.myfaces.extensions.validator.beanval.annotation.BeanValidation;
import org.apache.myfaces.extensions.validator.crossval.annotation.Equals;
import org.superbiz.myfaces.domain.User;
import org.superbiz.myfaces.domain.validation.Full;
import org.superbiz.myfaces.repository.UserRepository;
import org.superbiz.myfaces.view.config.Pages;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;

import static org.apache.myfaces.extensions.cdi.message.api.payload.MessageSeverity.ERROR;

@Named
@ViewAccessScoped
public class RegistrationPage implements Serializable {

    private static final long serialVersionUID = 3844502441069448490L;

    @Inject
    private UserRepository userRepository;

    @Inject
    private Conversation conversation;

    @Inject
    private
    @Jsf
    MessageContext messageContext;

    private User user = new User();

    @Inject
    private UserHolder userHolder;

    @Equals("user.password")
    private String repeatedPassword;

    @BeanValidation(useGroups = Full.class) //triggers UniqueUserNameValidator
    public Class<? extends Pages> register() {
        this.userRepository.save(this.user);
        this.messageContext.message()
                .text("{msgUserRegistered}")
                .namedArgument("userName", this.user.getUserName())
                .add();

        //in order to re-use the page-bean for the login-page
        this.conversation.close();

        return Pages.Login.class;
    }

    public Class<? extends Pages> login() {
        User user = this.userRepository.loadUser(this.user.getUserName());
        if (user != null && user.getPassword().equals(this.user.getPassword())) {
            this.messageContext.message().text("{msgLoginSuccessful}").add();
            this.userHolder.setCurrentUser(user);
            return Pages.About.class;
        }

        this.messageContext.message().text("{msgLoginFailed}").payload(ERROR).add();

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