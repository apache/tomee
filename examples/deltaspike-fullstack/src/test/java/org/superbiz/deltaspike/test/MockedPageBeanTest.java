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
import org.apache.deltaspike.testcontrol.api.mock.DynamicMockManager;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.superbiz.deltaspike.WebappMessageBundle;
import org.superbiz.deltaspike.domain.User;
import org.superbiz.deltaspike.repository.UserRepository;
import org.superbiz.deltaspike.view.RegistrationPage;
import org.superbiz.deltaspike.view.config.Pages;

import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

//@RunWith(CdiTestRunner.class)
public class MockedPageBeanTest {

    @Inject
    private RegistrationPage registrationPage; //will use a the mocked UserRepository

    @Inject
    private WindowContext windowContext;

    @Inject
    private WebappMessageBundle webappMessageBundle;

    @Inject
    private DynamicMockManager mockManager;

    @Inject
    private UserRepository userRepository; //will inject the mocked instance

    @Inject
    private ContextControl contextControl;

    @Ignore("Does no work because DS cannot mock dynamic repositories")
    @Test
    public void saveUserWithMockedBean() {
        final String userName = "gp";
        final String firstName = "Gerhard";
        final String lastName = "Petracek";

        // mockito doesn't support interfaces...seriously? but you can mock CDI impl
        // here we don't have one so implementing for the test the interface
        final UserRepository mockedUserRepository = (UserRepository) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{UserRepository.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                        return new User(userName, firstName, lastName.toUpperCase() /*just to illustrate that the mock-instance is used*/);
                    }
                });
        mockManager.addMock(mockedUserRepository);


        this.windowContext.activateWindow("testWindow");

        this.registrationPage.getUser().setUserName(userName);
        this.registrationPage.getUser().setFirstName(firstName);
        this.registrationPage.getUser().setLastName(lastName);
        this.registrationPage.getUser().setPassword("123");

        final Class<? extends Pages> targetPage = this.registrationPage.register();

        Assert.assertEquals(Pages.Login.class, targetPage);
        Assert.assertFalse(FacesContext.getCurrentInstance().getMessageList().isEmpty());
        Assert.assertEquals(webappMessageBundle.msgUserRegistered(userName), FacesContext.getCurrentInstance().getMessageList().iterator().next().getSummary());

        final User user = this.userRepository.findByUserName(userName);
        Assert.assertNotNull(user);
        Assert.assertEquals(firstName, user.getFirstName());
        Assert.assertEquals(lastName.toUpperCase(), user.getLastName());
    }
}
