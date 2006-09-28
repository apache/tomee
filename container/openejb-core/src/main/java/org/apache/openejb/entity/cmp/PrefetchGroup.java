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