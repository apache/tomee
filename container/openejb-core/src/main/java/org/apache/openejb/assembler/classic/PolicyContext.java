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
package org.apache.openejb.assembler.classic;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import java.io.Serializable;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Permission;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class PolicyContext {

    private final PermissionCollection excludedPermissions = new Permissions();
    private final PermissionCollection uncheckedPermissions = new Permissions();
    private final Map rolePermissions = new HashMap();
    private final String contextId;

    public PolicyContext(String contextId) {
        this.contextId = contextId;
    }

    public PermissionCollection getExcludedPermissions() {
        return excludedPermissions;
    }

    public PermissionCollection getUncheckedPermissions() {
        return uncheckedPermissions;
    }

    public Map<String,PermissionCollection> getRolePermissions() {
        return rolePermissions;
    }

    public String getContextID() {
        return contextId;
    }

}
