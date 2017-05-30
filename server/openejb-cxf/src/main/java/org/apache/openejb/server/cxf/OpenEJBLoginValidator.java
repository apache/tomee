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
package org.apache.openejb.server.cxf;

import org.apache.openejb.core.security.AbstractSecurityService;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.message.token.UsernameToken;
import org.apache.wss4j.dom.validate.UsernameTokenValidator;

import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;

public class OpenEJBLoginValidator extends UsernameTokenValidator {
    @Override
    protected void verifyDigestPassword(final UsernameToken usernameToken,
                                        final RequestData data) throws WSSecurityException {
        // check password
        super.verifyDigestPassword(usernameToken, data);

        // get the plain text password
        final WSPasswordCallback pwCb = new WSPasswordCallback(usernameToken.getName(), null, usernameToken.getPasswordType(), WSPasswordCallback.USERNAME_TOKEN);
        try {
            data.getCallbackHandler().handle(new Callback[]{pwCb});
        } catch (Exception e) {
            // no-op: the login will fail
        }

        // log the user
        final String user = usernameToken.getName();
        final String password = pwCb.getPassword();
        final SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        final Object token;
        try {
            securityService.disassociate();

            token = securityService.login(user, password);
            if (AbstractSecurityService.class.isInstance(securityService) && AbstractSecurityService.class.cast(securityService).currentState() == null) {
                securityService.associate(token);
            }
        } catch (final LoginException e) {
            throw new SecurityException("cannot log user " + user, e);
        }
    }
}
