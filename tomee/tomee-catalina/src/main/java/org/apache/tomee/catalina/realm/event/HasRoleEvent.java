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
package org.apache.tomee.catalina.realm.event;

import org.apache.catalina.Wrapper;

import java.security.Principal;

public class HasRoleEvent {
    private final Wrapper wrapper;
    private final Principal principal;
    private final String role;

    private boolean hasRole;

    public HasRoleEvent(final Wrapper wrapper, final Principal principal, final String role) {
        this.wrapper = wrapper;
        this.principal = principal;
        this.role = role;
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public String getRole() {
        return role;
    }

    public boolean isHasRole() {
        return hasRole;
    }

    public void setHasRole(boolean hasRole) {
        this.hasRole = hasRole;
    }
}
