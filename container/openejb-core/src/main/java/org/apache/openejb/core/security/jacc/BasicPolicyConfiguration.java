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

package org.apache.openejb.core.security.jacc;

import org.apache.openejb.assembler.classic.DelegatePermissionCollection;
import org.apache.openejb.loader.SystemInstance;

import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyContextException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class BasicPolicyConfiguration implements PolicyConfiguration {
    static final int OPEN = 1;
    static final int IN_SERVICE = 2;
    static final int DELETED = 3;

    private final String contextID;
    private int state;
    protected final Map<String, PermissionCollection> rolePermissionsMap = new LinkedHashMap<>();
    protected PermissionCollection unchecked;
    protected PermissionCollection excluded;

    protected BasicPolicyConfiguration(final String contextID) {
        this.contextID = contextID;
        this.state = OPEN;
    }

    public String getContextID() throws PolicyContextException {
        return contextID;
    }

    public boolean implies(final ProtectionDomain domain, final Permission permission) {

        if (excluded != null && excluded.implies(permission)) {
            return false;
        }

        if (unchecked != null && unchecked.implies(permission)) {
            return true;
        }

        final Principal[] principals = domain.getPrincipals();
        if (principals.length == 0) {
            return false;
        }

        final RoleResolver roleResolver = SystemInstance.get().getComponent(RoleResolver.class);
        final Set<String> roles = roleResolver.getLogicalRoles(principals, rolePermissionsMap.keySet());

        for (final String role : roles) {
            final PermissionCollection permissions = rolePermissionsMap.get(role);

            if (permissions != null && permissions.implies(permission)) {
                return true;
            }
        }

        return false;
    }

    public void addToRole(final String roleName, final PermissionCollection permissions) throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        final Enumeration e = permissions.elements();
        while (e.hasMoreElements()) {
            addToRole(roleName, (Permission) e.nextElement());
        }
    }

    public void addToRole(final String roleName, final Permission permission) throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        PermissionCollection permissions = rolePermissionsMap.get(roleName);
        if (permissions == null) {
            permissions = new DelegatePermissionCollection();
            rolePermissionsMap.put(roleName, permissions);
        }
        permissions.add(permission);
    }

    public void addToUncheckedPolicy(final PermissionCollection permissions) throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        final Enumeration e = permissions.elements();
        while (e.hasMoreElements()) {
            addToUncheckedPolicy((Permission) e.nextElement());
        }
    }

    public void addToUncheckedPolicy(final Permission permission) throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        if (unchecked == null) {
            unchecked = new DelegatePermissionCollection();
        }

        unchecked.add(permission);
    }

    public void addToExcludedPolicy(final PermissionCollection permissions) throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        final Enumeration e = permissions.elements();
        while (e.hasMoreElements()) {
            addToExcludedPolicy((Permission) e.nextElement());
        }
    }

    public void addToExcludedPolicy(final Permission permission) throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        if (excluded == null) {
            excluded = new DelegatePermissionCollection();
        }

        excluded.add(permission);
    }

    public void removeRole(final String roleName) throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        rolePermissionsMap.remove(roleName);
    }

    public void removeUncheckedPolicy() throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        unchecked = null;
    }

    public void removeExcludedPolicy() throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }

        excluded = null;
    }

    public void linkConfiguration(final PolicyConfiguration link) throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }
    }

    public void delete() throws PolicyContextException {
        state = DELETED;
    }

    public void commit() throws PolicyContextException {
        if (state != OPEN) {
            throw new UnsupportedOperationException("Not in an open state");
        }
        state = IN_SERVICE;
    }

    public boolean inService() throws PolicyContextException {
        return state == IN_SERVICE;
    }

    //TODO I have no idea what side effects this might have, but it's needed in some form from PolicyConfigurationFactoryImpl.
    //see JACC spec 1.0 section 3.1.1.1 discussion of in service and deleted.
    //spec p. 31 3.1.7 on the effects of remove:
    //If the getPolicyConfiguration method  is used, the value true should be passed as the second
    //  argument to cause the  corresponding policy statements to be deleted from the context.
    public void open(final boolean remove) {
        if (remove) {
            rolePermissionsMap.clear();
            unchecked = null;
            excluded = null;
        }
        state = OPEN;
    }

    int getState() {
        return state;
    }

    public interface RoleResolver {
        Set<String> getLogicalRoles(Principal[] principals, Set<String> logicalRoles);
    }
}
