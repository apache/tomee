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
package org.apache.openejb.entity.cmp;

/**
 * @version $Revision$ $Date$
 */
public class OneToManyRelationSchema extends AbstractRelationSchema {
    public OneToManyRelationSchema(String relationName, RoleSchema pkRoleSchema, RoleSchema fkRoleSchema) {
        super(relationName, pkRoleSchema, fkRoleSchema);
    }

    public RoleSchema getPkRole() {
        return leftRole;
    }

    public RoleSchema getFkRole() {
        return rightRole;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof OneToManyRelationSchema)) {
            return false;
        }
        OneToManyRelationSchema other = (OneToManyRelationSchema) obj;
        if (relationName != null && !relationName.equals(other.relationName)) {
            return false;
        }
        if (!leftRole.equals(other.leftRole)) {
            return false;
        }
        if (!rightRole.equals(other.rightRole)) {
            return false;
        }
        return true;
    }

    public String toString() {
        return "OneToManyRelation: name=" + relationName + ", pk=" + leftRole + ", fk=" + rightRole;
    }
}