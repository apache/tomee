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

import java.util.Map;
import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
public class RoleSchema implements Serializable {
    private static final long serialVersionUID = -6734703386924221598L;
    private final String relationName;
    private final String relationshipRoleName;
    private final String ejbName;
    private final String cmrFieldName;
    private boolean one;
    private boolean cascadeDelete;
    private Map pkMapping;

    public RoleSchema(String relationName, String roleName, String ejbName, String cmrFieldName) {
        this.relationName = relationName;
        this.relationshipRoleName = roleName;
        this.ejbName = ejbName;
        this.cmrFieldName = cmrFieldName;
    }

    public String getRelationName() {
        return relationName;
    }

    public String getRelationshipRoleName() {
        return relationshipRoleName;
    }

    public String getEjbName() {
        return ejbName;
    }

    public String getCmrFieldName() {
        return cmrFieldName;
    }

    public boolean isOne() {
        return one;
    }

    public void setOne(boolean one) {
        this.one = one;
    }

    public boolean isMany() {
        return !one;
    }

    public boolean isCascadeDelete() {
        return cascadeDelete;
    }

    public void setCascadeDelete(boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
    }

    public Map getPkMapping() {
        return pkMapping;
    }

    public void setPkMapping(Map pkMapping) {
        this.pkMapping = pkMapping;
    }

    public boolean implies(RoleSchema other) {
        if (!ejbName.equals(other.ejbName)) {
            return false;
        }
        if (relationName != null && relationName.equals(other.relationName)) {
            if (relationshipRoleName != null && other.relationshipRoleName != null) {
                if (!relationshipRoleName.equals(other.relationshipRoleName)) {
                    return false;
                }
                if (cmrFieldName != null && other.cmrFieldName != null && !cmrFieldName.equals(other.cmrFieldName)) {
                    throw new IllegalArgumentException("ejb-relation-name [" + relationName +
                            "]/ejb-relationship-role-name [" + relationshipRoleName + "] is invalid: cmr-field [" +
                            other.cmrFieldName + "] is expected for this role. Found [" + cmrFieldName + "].");
                }
                return true;
            }
        }
        return null != cmrFieldName && null != other.cmrFieldName && cmrFieldName.equals(other.cmrFieldName);
    }

    public int hashCode() {
        int hashCode = 17;
        hashCode = 37 * hashCode + ejbName.hashCode();
        hashCode = 37 * hashCode + (relationName == null ? 0 : relationName.hashCode());
        hashCode = 37 * hashCode + (relationshipRoleName == null ? 0 : relationshipRoleName.hashCode());
        hashCode = 37 * hashCode + (cmrFieldName == null ? 0 : cmrFieldName.hashCode());
        return hashCode;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof RoleSchema)) {
            return false;
        }
        RoleSchema other = (RoleSchema) obj;
        if (relationName != null && !relationName.equals(other.relationName)) {
            return false;
        }
        if (relationshipRoleName != null && !relationshipRoleName.equals(other.relationshipRoleName)) {
            return false;
        }
        if (cmrFieldName != null && !cmrFieldName.equals(other.cmrFieldName)) {
            return false;
        }
        return ejbName.equals(other.ejbName);
    }

    public String toString() {
        return "ejb-relation-name [" + relationName + "]; ejb-relationship-role-name [" + relationshipRoleName +
                "]; ejb-name [" + ejbName + "]; cmr-field [" + cmrFieldName + "]";
    }
}
