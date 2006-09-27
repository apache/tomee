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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
public class PrefetchGroup implements Serializable {
    private static final long serialVersionUID = 9016261782922148263L;
    private final String groupName;
    private final SortedSet cmpFields = new TreeSet();
    private final SortedMap cmrFields = new TreeMap();

    public PrefetchGroup(String ejbName) {
        this.groupName = ejbName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void addCmpField(String cmpFieldName) {
        cmpFields.add(cmpFieldName);
    }

    public Set getCmpFields() {
        return Collections.unmodifiableSortedSet(cmpFields);
    }

    public void setCmpFields(Set cmpFields) {
        this.cmpFields.clear();
        for (Iterator iterator = cmpFields.iterator(); iterator.hasNext();) {
            String cmpFieldName = (String) iterator.next();
            this.cmpFields.add(cmpFieldName);
        }
    }

    public void addCmrField(String cmrFieldName) {
        this.addCmrField(cmrFieldName, groupName);
    }

    public void addCmrField(String cmrFieldName, String cmrGroupName) {
        cmrFields.put(cmrFieldName, cmrGroupName);
    }

    public Map getCmrFields() {
        return Collections.unmodifiableMap(cmrFields);
    }

    public void setCmrFields(Map cmrFields) {
        this.cmrFields.clear();
        for (Iterator iterator = cmrFields.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String cmrFieldName = (String) entry.getKey();
            String cmrGroupName = (String) entry.getValue();
            this.cmrFields.put(cmrFieldName, cmrGroupName);
        }
    }

    public int hashCode() {
        return groupName.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof PrefetchGroup) {
            PrefetchGroup entitySchema = (PrefetchGroup) obj;
            return groupName.equals(entitySchema.groupName);
        }
        return false;
    }

    public String toString() {
        return groupName;
    }
}