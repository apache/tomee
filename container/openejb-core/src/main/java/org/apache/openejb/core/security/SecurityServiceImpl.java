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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.core.security;

import org.apache.openejb.core.security.jaas.UsernamePasswordCallbackHandler;
import org.apache.openejb.util.ConfUtils;
import org.apache.openejb.util.URLs;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.UUID;

/**
 * @version $Rev$ $Date$
 */
public class SecurityServiceImpl extends AbstractSecurityService {
    public SecurityServiceImpl() {
        installJaas();

        try {
            // Perform a login attempt (which should fail)
            // simply to excercise the initialize code of any
            // LoginModules that are configured.
            // They should have a chance to perform any special
            // boot-time code that they may need.
            login("","");
        } catch (Throwable e) {
        }
    }

    protected static void installJaas() {
        String path = System.getProperty("java.security.auth.login.config");

        if (path != null) {
            return;
        }

        URL loginConfig = ConfUtils.getConfResource("login.config");

        System.setProperty("java.security.auth.login.config", URLDecoder.decode(loginConfig.toExternalForm()));
    }

    public UUID login(String realmName, String username, String password) throws LoginException {
        if (realmName == null){
            realmName = getRealmName();
        }
        LoginContext context = new LoginContext(realmName, new UsernamePasswordCallbackHandler(username, password));
        context.login();

        Subject subject = context.getSubject();

        return registerSubject(subject);
    }
}
