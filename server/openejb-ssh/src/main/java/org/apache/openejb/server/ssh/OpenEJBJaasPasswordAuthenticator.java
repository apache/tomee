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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.ssh;

import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import java.io.IOException;

public class OpenEJBJaasPasswordAuthenticator extends JaasPasswordAuthenticator {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_SERVER, OpenEJBJaasPasswordAuthenticator.class);

    public static final Session.AttributeKey<String> USERNAME_KEY = new Session.AttributeKey<String>();
    public static final Session.AttributeKey<LoginContext> LOGIN_CONTEXT_KEY = new Session.AttributeKey<LoginContext>();

    @Override
    public boolean authenticate(final String username, final String password, final ServerSession session) {
        try {
            final Subject subject = new Subject();
            final LoginContext loginContext = new LoginContext(getDomain(), subject, new CallbackHandler() {
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (final Callback callback : callbacks) {
                        if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(username);
                        } else if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword(password.toCharArray());
                        } else {
                            throw new UnsupportedCallbackException(callback);
                        }
                    }
                }
            });
            loginContext.login();

            session.setAttribute(USERNAME_KEY, username);
            session.setAttribute(LOGIN_CONTEXT_KEY, loginContext);
            return true;
        } catch (Exception e) {
            LOGGER.debug("can't log using username '" + username + "'", e);
            return false;
        }
    }
}
