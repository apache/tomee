/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.junit.security;

import org.apache.openejb.core.security.jaas.GroupPrincipal;
import org.apache.openejb.core.security.jaas.UserPrincipal;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is used to "emulate" a login for the "securityRole" option in the JUnit runner.
 * The TestContext should set the {@link Context#SECURITY_PRINCIPAL } {@link InitialContext} property
 * to the rolename it wants to "RunAs", and this login module will do the result.
 *
 * @author quintin
 */
public class TestLoginModule implements LoginModule {
    private Subject subject;

    private CallbackHandler callbackHandler;

    private String user;

    private Set principals = new HashSet();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username:");
        callbacks[1] = new PasswordCallback("Password:", false);
        try {
            callbackHandler.handle(callbacks);
        }
        catch (Exception e) {
            throw new LoginException("Failed to perform emulated login: " + e.getMessage());
        }

        user = ((NameCallback) callbacks[0]).getName();

        return true;
    }

    public boolean commit() throws LoginException {
        principals.add(new UserPrincipal(user));
        principals.add(new GroupPrincipal(user));

        subject.getPrincipals().addAll(principals);

        user = null;
        return true;
    }

    public boolean abort() throws LoginException {
        user = null;
        return true;
    }

    public boolean logout() throws LoginException {
        subject.getPrincipals().removeAll(principals);
        principals.clear();
        return true;
    }
}
