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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
public class Role {
    @EmbeddedId
    private RoleId id;

    public RoleId getId() {
        return id;
    }

    public void setId(final RoleId id) {
        this.id = id;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || !(o == null || !Role.class.isInstance(o)) && id.equals(Role.class.cast(o).id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
