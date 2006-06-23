/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.common;

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public class SecurityRoleRef {
    private String id;
    private List<String> description;
    private String roleName;
    private String roleLink;

    public SecurityRoleRef() {
    }

    public SecurityRoleRef(String roleName) {
        this.roleName = roleName;
    }

    public SecurityRoleRef(String roleName, String roleLink) {
        this.roleName = roleName;
        this.roleLink = roleLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleLink() {
        return roleLink;
    }

    public void setRoleLink(String roleLink) {
        this.roleLink = roleLink;
    }
}
