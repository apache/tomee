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

package org.apache.openejb.ri.sp;

import org.apache.openejb.InterfaceType;
import org.apache.openejb.spi.SecurityService;

import javax.security.auth.login.LoginException;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * @org.apache.xbean.XBean element="pseudoSecurityService"
 */
public class PseudoSecurityService implements SecurityService {
    public PseudoSecurityService() {
        PseudoPolicyConfigurationFactory.install();
    }

    public void init(final Properties props) {
    }

    public Object login(final String user, final String pass) throws LoginException {
        return null;
    }

    public Object login(final String securityRealm, final String user, final String pass) throws LoginException {
        return null;
    }

    public Set<String> getLogicalRoles(final Principal[] principals, final Set<String> logicalRoles) {
        return Collections.emptySet();
    }

    public void associate(final Object securityIdentity) throws LoginException {
    }

    public Object disassociate() {
        return null;
    }

    public void logout(final Object securityIdentity) throws LoginException {
    }

    public boolean isCallerInRole(final String role) {
        return false;
    }

    public Principal getCallerPrincipal() {
        return null;
    }

    public boolean isCallerAuthorized(final Method method, final InterfaceType type) {
        return true;
    }

    public void setState(final Object o) {
        // no-op
    }

    public Object currentState() {
        return null;
    }

    @Override
    public void onLogout(final HttpServletRequest request) {
        // no-op
    }

    @Override
    public Set getPrincipalsByType(final Class pType) {
        return Collections.emptySet();
    }

    @Override
    public ProtectionDomain getProtectionDomain() {
        return null;
    }
}