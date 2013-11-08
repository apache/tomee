/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.openjpa.meta;

import java.util.Comparator;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.util.ImplHelper;

/**
 * Order by a field in the related type in memory.
 *
 * @author Abe White
 */
class InMemoryRelatedFieldOrder
    implements Order, Comparator {

    private final FieldMetaData _rel;
    private final boolean _asc;
    private final OpenJPAConfiguration _conf;

    public InMemoryRelatedFieldOrder(FieldMetaData rel, boolean asc,
        OpenJPAConfiguration conf) {
        _rel = rel;
        _asc = asc;
        _conf = conf;
    }

    public String getName() {
        return _rel.getName();
    }

    public boolean isAscending() {
        return _asc;
    }

    public Comparator getComparator() {
        return this;
    }

    public int compare(Object o1, Object o2) {
        if (o1 == o2)
            return 0;
        if (!(ImplHelper.isManageable(o1))
            || !(ImplHelper.isManageable(o2)))
            return 0;

        PersistenceCapable pc1 = ImplHelper.toPersistenceCapable(o1, _conf);
        PersistenceCapable pc2 = ImplHelper.toPersistenceCapable(o2, _conf);
        OpenJPAStateManager sm1 = (OpenJPAStateManager) pc1.pcGetStateManager();
        OpenJPAStateManager sm2 = (OpenJPAStateManager) pc2.pcGetStateManager();
        if (sm1 == null || sm2 == null)
            return 0;

        Object v1 = sm1.fetchField(_rel.getIndex(), false);
        Object v2 = sm2.fetchField(_rel.getIndex(), false);
        if (v1 == v2)
            return 0;
        if (v1 == null)
            return (_asc) ? -1 : 1;
        if (v2 == null)
            return (_asc) ? 1 : -1;
        int cmp = ((Comparable) v1).compareTo(v2);
        return (_asc) ? cmp : -cmp;
    }
}
