/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.datasourcerealm;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class RoleId {
    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_role")
    private String userRole;

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(final String userRole) {
        this.userRole = userRole;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !RoleId.class.isInstance(o)) {
            return false;
        }
        final RoleId roleId = RoleId.class.cast(o);
        return userName.equals(roleId.userName) && userRole.equals(roleId.userRole);

    }

    @Override
    public int hashCode() {
        int result = userName.hashCode();
        result = 31 * result + userRole.hashCode();
        return result;
    }
}
