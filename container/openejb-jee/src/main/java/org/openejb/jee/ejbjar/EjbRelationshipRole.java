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
package org.openejb.jee.ejbjar;

/**
 * @version $Revision$ $Date$
 */
public class EjbRelationshipRole {
    private String id;
    private String ejbRelationshipRoleName;
    private Multiplicity multiplicity;
    private boolean cascadeDelete;

//        private String ejbName;
    private RelationshipRoleSource relationshipRoleSource;
    private CmrField cmrField;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEjbRelationshipRoleName() {
        return ejbRelationshipRoleName;
    }

    public void setEjbRelationshipRoleName(String ejbRelationshipRoleName) {
        this.ejbRelationshipRoleName = ejbRelationshipRoleName;
    }

    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Multiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }

    public boolean isCascadeDelete() {
        return cascadeDelete;
    }

    public void setCascadeDelete(boolean cascadeDelete) {
        this.cascadeDelete = cascadeDelete;
    }

    public RelationshipRoleSource getRelationshipRoleSource() {
        return relationshipRoleSource;
    }

    public void setRelationshipRoleSource(RelationshipRoleSource relationshipRoleSource) {
        this.relationshipRoleSource = relationshipRoleSource;
    }

    public CmrField getCmrField() {
        return cmrField;
    }

    public void setCmrField(CmrField cmrField) {
        this.cmrField = cmrField;
    }
}
