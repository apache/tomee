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
package org.apache.openejb.ri.sp;

import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.InterfaceType;

import javax.security.auth.login.LoginException;
import java.util.Collection;
import java.util.Set;
import java.util.Collections;
import java.security.Principal;
import java.lang.reflect.Method;

/**
 * @org.apache.xbean.XBean element="pseudoSecurityService"
 */
public class PseudoSecurityService implements SecurityService {
    public PseudoSecurityService() {
        PseudoPolicyConfigurationFactory.install();
    }

    public void init(java.util.Properties props) {
    }

    public Object login(String user, String pass) throws LoginException {
        return null;
    }

    public Object login(String securityRealm, String user, String pass) throws LoginException {
        return null;
    }

    public Set<String> getLogicalRoles(Principal[] principals, Set<String> logicalRoles) {
        return Collections.emptySet();
    }

    public void associate(Object securityIdentity) throws LoginException {
    }

    public Object disassociate() {
        return null;
    }

    public void logout(Object securityIdentity) throws LoginException {
    }

    public boolean isCallerInRole(String role) {
        return false;
    }

    public Principal getCallerPrincipal() {
        return null;
    }

    public boolean isCallerAuthorized(Method method, InterfaceType type) {
        return true;
    }
}