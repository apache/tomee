/*
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
package org.apache.openejb.core.security.jaas;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

public class ServiceProviderLoginModule implements LoginModule {
    private static Logger log = Logger.getInstance(LogCategory.OPENEJB_SECURITY, "org.apache.openejb.util.resources");

    private Subject subject;
    private CallbackHandler callbackHandler;
    private ServiceLoader<LoginProvider> loader;

    public Set<Principal> principals = new LinkedHashSet<Principal>();

    private UserData userData;

    private class UserData {
        public final String user;
        public final String pass;
        public final Set<String> groups = new HashSet<String>();

        private UserData(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.loader = ServiceLoader.load(LoginProvider.class);
    }

    private UserData getUserData() throws LoginException {
        final Callback[] callbacks = new Callback[2];

        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);
        try {
            this.callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException(uce.getMessage() + " not available to obtain information from user");
        }

        final String user = ((NameCallback) callbacks[0]).getName();

        char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();
        if (tmpPassword == null) {
            tmpPassword = new char[0];
        }

        final String password = new String(tmpPassword);

        return new UserData(user, password);
    }

    @Override
    public boolean login() throws LoginException {
        final Iterator<LoginProvider> loginProviders = loader.iterator();
        if (!loginProviders.hasNext()) {
            throw new FailedLoginException("No LoginProvider defined.");
        }

        this.userData = getUserData();
        while (loginProviders.hasNext()) {
            final LoginProvider loginProvider = loginProviders.next();

            final List<String> myGroups = loginProvider.authenticate(this.userData.user, this.userData.pass);
            if (myGroups != null) {
                this.userData.groups.addAll(myGroups);
            }
        }
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        this.principals.add(new UserPrincipal(this.userData.user));

        for (String myGroup : this.userData.groups) {
            principals.add(new GroupPrincipal(myGroup));
        }

        this.subject.getPrincipals().addAll(this.principals);

        clear();

        log.debug("commit");
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        clear();
        log.debug("abort");
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        this.subject.getPrincipals().removeAll(this.principals);
        this.principals.clear();

        log.debug("logout");
        return true;
    }

    private void clear() {
        this.userData = null;
    }
}
