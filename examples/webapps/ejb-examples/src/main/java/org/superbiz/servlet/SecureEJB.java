/**
 *
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
package org.superbiz.servlet;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.annotation.security.DenyAll;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import java.security.Principal;

@Stateless
@DeclareRoles({"user", "manager", "fake"})
public class SecureEJB implements SecureEJBLocal {
    @Resource
    private SessionContext context;

    public Principal getCallerPrincipal() {
        return context.getCallerPrincipal();
    }

    public boolean isCallerInRole(String role) {
        return context.isCallerInRole(role);
    }

    @RolesAllowed("user")
    public void allowUserMethod() {
    }

    @RolesAllowed("manager")
    public void allowManagerMethod() {
    }

    @RolesAllowed("fake")
    public void allowFakeMethod() {
    }

    @DenyAll
    public void denyAllMethod() {
    }

    public String toString() {
        return "SecureEJB[userName=" + getCallerPrincipal() + "]";
    }
}
