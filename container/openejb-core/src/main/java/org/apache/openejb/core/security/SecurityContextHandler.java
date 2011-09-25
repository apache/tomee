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

import javax.resource.spi.work.SecurityContext;
import javax.resource.spi.work.WorkCompletedException;
import javax.resource.spi.work.WorkContext;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;

import org.apache.geronimo.connector.work.WorkContextHandler;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

public class SecurityContextHandler implements WorkContextHandler<SecurityContext>{

    private ConnectorCallbackHandler callbackHandler;
	private final String securityRealmName;

	public SecurityContextHandler(String securityRealmName) {
		this.securityRealmName = securityRealmName;
	}

	public void before(SecurityContext securityContext) throws WorkCompletedException {
        if (securityContext != null) {
            callbackHandler = new ConnectorCallbackHandler(securityRealmName);
            
            Subject clientSubject = new Subject();
			securityContext.setupSecurityContext(callbackHandler, clientSubject, null);
        }
    }

    public void after(SecurityContext securityContext) throws WorkCompletedException {
    	SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
    	Object loginObj = securityService.disassociate();
    	if (loginObj != null) {
    		try {
				securityService.logout(loginObj);
			} catch (LoginException e) {
			}
    	}
    }

	public boolean supports(Class<? extends WorkContext> clazz) {
		return SecurityContext.class.isAssignableFrom(clazz);
	}

	public boolean required() {
		return false;
	}

}
