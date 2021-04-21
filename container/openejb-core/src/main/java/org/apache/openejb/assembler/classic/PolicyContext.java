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

package org.apache.openejb.assembler.classic;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.HashMap;
import java.util.Map;

public class PolicyContext {

    private final PermissionCollection excludedPermissions = new DelegatePermissionCollection();
    private final PermissionCollection uncheckedPermissions = new DelegatePermissionCollection();
    private final Map<String, PermissionCollection> rolePermissions = new HashMap<>();
    private final String contextId;

    public PolicyContext(final String contextId) {
        this.contextId = contextId;
    }

    public PermissionCollection getExcludedPermissions() {
        return excludedPermissions;
    }

    public PermissionCollection getUncheckedPermissions() {
        return uncheckedPermissions;
    }

    public Map<String, PermissionCollection> getRolePermissions() {
        return rolePermissions;
    }

    public void addRole(final String name, final Permission permission) {
        rolePermissions.computeIfAbsent(name, (k) -> new DelegatePermissionCollection());
        rolePermissions.get(name).add(permission);
    }

    public String getContextID() {
        return contextId;
    }
}
