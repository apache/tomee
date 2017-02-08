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

package org.apache.openejb.core.security;

import org.apache.openejb.core.security.jaas.UsernamePasswordCallbackHandler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.ConfUtils;
import org.apache.openejb.util.JavaSecurityManagers;
import org.apache.xbean.finder.archive.FileArchive;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Rev$ $Date$
 */
public class SecurityServiceImpl extends AbstractSecurityService {

    private static final Map<Object, LoginContext> contexts = new ConcurrentHashMap<Object, LoginContext>();

    public SecurityServiceImpl() {
        this(autoJaccProvider());
    }

    public SecurityServiceImpl(final String jaccProviderClass) {
        super(jaccProviderClass);
        installJaas();

        try {
            // Perform a login attempt (which should fail)
            // simply to excercise the initialize code of any
            // LoginModules that are configured.
            // They should have a chance to perform any special
            // boot-time code that they may need.
            login("", "");
        } catch (final Throwable e) {
            //Ignore
        }
    }

    @SuppressWarnings("deprecation")
    protected static void installJaas() {
        final String path = SystemInstance.get().getOptions().get("java.security.auth.login.config", (String) null);

        if (path != null) {
            return;
        }

        final URL loginConfig = ConfUtils.getConfResource("login.config");
        JavaSecurityManagers.setSystemProperty("java.security.auth.login.config", FileArchive.decode(loginConfig.toExternalForm()));
    }

    @Override
    public UUID login(String realmName, final String username, final String password) throws LoginException {
        if (realmName == null) {
            realmName = getRealmName();
        }
        final LoginContext context = new LoginContext(realmName, new UsernamePasswordCallbackHandler(username, password));
        context.login();

        final Subject subject = context.getSubject();

        final UUID token = registerSubject(subject);
        contexts.put(token, context);

        return token;
    }

    /* (non-Javadoc)
     * @see org.apache.openejb.core.security.AbstractSecurityService#logout(java.util.UUID)
     */
    @Override
    public void logout(final UUID securityIdentity) throws LoginException {
        final LoginContext context = contexts.remove(securityIdentity);
        if (null == context) {
            throw new IllegalStateException("Unable to logout. Can not recover LoginContext.");
        }
        context.logout();
        super.logout(securityIdentity);
    }

}
