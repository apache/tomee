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
public abstract class AbstractRelationSchema implements RelationSchema {
    protected final String relationName;
    protected final RoleSchema leftRole;
    protected final RoleSchema rightRole;

    public AbstractRelationSchema(String relationName, RoleSchema leftRole, RoleSchema rightRole) {
        this.relationName = relationName;
        this.leftRole = leftRole;
        this.rightRole = rightRole;
    }

    public String getRelationName() {
        return relationName;
    }

    public RoleSchema getLeftRole() {
        return leftRole;
    }

    public RoleSchema getRightRole() {
        return rightRole;
    }

    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + (relationName == null ? 0 : relationName.hashCode());
        hashCode = 37 * hashCode + leftRole.hashCode();
        hashCode = 37 * hashCode + rightRole.hashCode();
        return hashCode;
    }

    public abstract boolean equals(Object obj);

    public String toString() {
        return "Relation: name=" + relationName + ", left=" + leftRole + ", right=" + rightRole;
    }
}
