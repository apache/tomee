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

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.CertStoreCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.callback.PrivateKeyCallback;
import javax.security.auth.message.callback.SecretKeyCallback;
import javax.security.auth.message.callback.TrustStoreCallback;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

/**
 * Spec 16.4.1: must support CallerPrincipalCallback, GroupPrincipalCallback, PasswordValidationCallback. Recommended to support CertStoreCallback, PrivateKeyCallback, SecretKeyCallback, and TrustStoreCallback.
 * 
 * @version $Rev: 925911 $ $Date: 2010-03-21 22:03:35 +0000 (Sun, 21 Mar 2010) $
 */
public class ConnectorCallbackHandler implements CallbackHandler {

	private Principal callerPrincipal;
	private String[] groupsArray;
	private final String securityRealmName;

	public ConnectorCallbackHandler(String securityRealmName) {
		this.securityRealmName = securityRealmName;
	}

	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		for (Callback callback : callbacks) {
			// jaspi to server communication
			if (callback instanceof CallerPrincipalCallback) {
				callerPrincipal = ((CallerPrincipalCallback) callback).getPrincipal();
			} else if (callback instanceof GroupPrincipalCallback) {
				groupsArray = ((GroupPrincipalCallback) callback).getGroups();
			} else if (callback instanceof PasswordValidationCallback) {
				PasswordValidationCallback passwordValidationCallback = (PasswordValidationCallback) callback;
				Subject subject = passwordValidationCallback.getSubject();
				final String userName = passwordValidationCallback.getUsername();
				final char[] password = passwordValidationCallback.getPassword();

				SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
				try {
					Object loginObj = securityService.login(securityRealmName, userName, password == null ? "" : new String(password));
					securityService.associate(loginObj);
					callerPrincipal = securityService.getCallerPrincipal();
					passwordValidationCallback.setResult(true);
				} catch (LoginException e) {
					passwordValidationCallback.setResult(false);
				}
			}
			// server to jaspi communication
			// TODO implement these
			else if (callback instanceof CertStoreCallback) {
			
			} else if (callback instanceof PrivateKeyCallback) {
			
			} else if (callback instanceof SecretKeyCallback) {
			
			} else if (callback instanceof TrustStoreCallback) {
			
			} else {
				throw new UnsupportedCallbackException(callback);
			}
		}
	}

	public Principal getCallerPrincipal() {
		return callerPrincipal;
	}

	public List<String> getGroups() {
		return groupsArray == null ? null : Arrays.asList(groupsArray);
	}
}