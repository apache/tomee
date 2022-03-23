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
package org.superbiz.deltaspike.test;

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.deltaspike.WebappMessageBundle;
import org.superbiz.deltaspike.domain.User;
import org.superbiz.deltaspike.repository.UserRepository;
import org.superbiz.deltaspike.view.RegistrationPage;
import org.superbiz.deltaspike.view.config.Pages;

import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;

@RunWith(CdiTestRunner.class)
public class PageBeanTest {
    @Inject
    private RegistrationPage registrationPage;

    @Inject
    private WindowContext windowContext;

    @Inject
    private WebappMessageBundle webappMessageBundle;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ContextControl contextControl;

    @Test(expected = PersistenceException.class)
    public void duplicatedUser() {
        final String userName = "tomee";
        final String firstName = "Apache";
        final String lastName = "TomEE";

        this.userRepository.saveAndFlush(new User(userName, firstName, lastName));
        this.userRepository.saveAndFlush(new User(userName, firstName + "2", lastName + "2"));
    }

    @Test
    public void saveUser() {
        final String userName = "GP";
        final String firstName = "Gerhard";
        final String lastName = "Petracek";
        this.windowContext.activateWindow("testWindow");

        this.registrationPage.getUser().setUserName(userName);
        this.registrationPage.getUser().setFirstName(firstName);
        this.registrationPage.getUser().setLastName(lastName);
        this.registrationPage.getUser().setPassword("123");

        Class<? extends Pages> targetPage = this.registrationPage.register();

        Assert.assertEquals(Pages.Login.class, targetPage);
        Assert.assertFalse(FacesContext.getCurrentInstance().getMessageList().isEmpty());
        Assert.assertEquals(webappMessageBundle.msgUserRegistered(userName), FacesContext.getCurrentInstance().getMessageList().iterator().next().getSummary());

        User user = this.userRepository.findByUserName(userName);
        Assert.assertNotNull(user);
        Assert.assertEquals(firstName, user.getFirstName());
        Assert.assertEquals(lastName, user.getLastName());
    }

    @Test
    public void saveUserAndLogin() {
        final String userName = "tt";
        final String firstName = "Tom";
        final String lastName = "Tester";
        this.windowContext.activateWindow("testWindow");

        Assert.assertTrue(FacesContext.getCurrentInstance().getMessageList().isEmpty());

        this.registrationPage.getUser().setUserName(userName);
        this.registrationPage.getUser().setFirstName(firstName);
        this.registrationPage.getUser().setLastName(lastName);
        this.registrationPage.getUser().setPassword("123");

        Class<? extends Pages> targetPage = this.registrationPage.register();

        Assert.assertEquals(Pages.Login.class, targetPage);
        Assert.assertFalse(FacesContext.getCurrentInstance().getMessageList().isEmpty());
        Assert.assertEquals(webappMessageBundle.msgUserRegistered(userName), FacesContext.getCurrentInstance().getMessageList().iterator().next().getSummary());

        User user = this.userRepository.findByUserName(userName);
        Assert.assertNotNull(user);
        Assert.assertEquals(firstName, user.getFirstName());
        Assert.assertEquals(lastName, user.getLastName());

        this.contextControl.stopContexts();
        this.contextControl.startContexts();
        this.windowContext.activateWindow("testWindow");

        Assert.assertTrue(FacesContext.getCurrentInstance().getMessageList().isEmpty());

        this.registrationPage.getUser().setUserName(userName);
        this.registrationPage.getUser().setFirstName(firstName);
        this.registrationPage.getUser().setLastName(lastName);
        this.registrationPage.getUser().setPassword("123");

        targetPage = this.registrationPage.login();
        Assert.assertEquals(Pages.About.class, targetPage);
    }
}
