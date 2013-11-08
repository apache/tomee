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
import org.apache.openjpa.util.ApplicationIds;
import org.apache.openjpa.util.ImplHelper;
import org.apache.openjpa.conf.OpenJPAConfiguration;

/**
 * Order by the field value in memory. If the field contains
 * {@link PersistenceCapable} objects, order on their primary key values.
 * Relies on primary key values, including datastore oid objects, being
 * {@link Comparable}.
 *
 * @author Abe White
 */
class InMemoryValueOrder
    implements Order, Comparator {

    private final boolean _asc;
    private final OpenJPAConfiguration _conf;

    public InMemoryValueOrder(boolean asc, OpenJPAConfiguration conf) {
        _asc = asc;
        _conf = conf;
    }

    public String getName() {
        return Order.ELEMENT;
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
        if (o1 == null)
            return (_asc) ? -1 : 1;
        if (o2 == null)
            return (_asc) ? 1 : -1;

        // non-pc values must be comparable
        int cmp;
        if (!(ImplHelper.isManageable(o1))
            || !(ImplHelper.isManageable(o2))) {
            cmp = ((Comparable) o1).compareTo(o2);
            return (_asc) ? cmp : -cmp;
        }

        // order on primary key values
        PersistenceCapable pc1 = ImplHelper.toPersistenceCapable(o1, _conf);
        PersistenceCapable pc2 = ImplHelper.toPersistenceCapable(o2, _conf);
        OpenJPAStateManager sm1 = (OpenJPAStateManager) pc1.pcGetStateManager();
        OpenJPAStateManager sm2 = (OpenJPAStateManager) pc2.pcGetStateManager();
        if (sm1 == null || sm2 == null)
            return 0;

        Object[] pk1 = toPKValues(sm1);
        Object[] pk2 = toPKValues(sm2);
        int len = Math.min(pk1.length, pk2.length);
        for (int i = 0; i < len; i++) {
            if (pk1[i] == pk2[i])
                return 0;
            if (pk1[i] == null)
                return (_asc) ? -1 : 1;
            if (pk2[i] == null)
                return (_asc) ? 1 : -1;
            cmp = ((Comparable) pk1[i]).compareTo(pk2[i]);
            if (cmp != 0)
                return (_asc) ? cmp : -cmp;
        }

        cmp = pk1.length - pk2.length;
        return (_asc) ? cmp : -cmp;
    }

    /**
     * Convert the given state manager's oid to an array of (possibly null)
     * primary key values.
     */
    private static Object[] toPKValues(OpenJPAStateManager sm) {
        if (sm.getMetaData().getIdentityType() != ClassMetaData.ID_APPLICATION)
            return new Object[]{ sm.getObjectId() };

        Object[] pks = ApplicationIds.toPKValues(sm.getObjectId(),
            sm.getMetaData());
        if (pks == null)
            pks = new Object[]{ null };
        return pks;
    }
}
