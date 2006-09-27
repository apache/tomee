/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
