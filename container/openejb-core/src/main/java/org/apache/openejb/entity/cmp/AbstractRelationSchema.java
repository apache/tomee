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
