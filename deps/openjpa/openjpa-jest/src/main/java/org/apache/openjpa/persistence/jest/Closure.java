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

package org.apache.openjpa.persistence.jest;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.meta.JavaTypes;

/**
 * Computes closure of a collection of managed objects.
 * 
 * @author Pinaki Poddar
 *
 */
public class Closure implements Iterable<OpenJPAStateManager> {
    private Set<OpenJPAStateManager> _visited = new LinkedHashSet<OpenJPAStateManager>();
    
    public Closure(OpenJPAStateManager root) {
        this(Collections.singleton(root));
    }
    
    public Closure(Collection<OpenJPAStateManager> roots) {
        for (OpenJPAStateManager sm : roots) {
            visit(sm);
        }
    }
    
    private void visit(OpenJPAStateManager sm) {
        if (sm == null)
            return;
        boolean isVisited = !_visited.add(sm);
        if (isVisited) return;
        BitSet loaded = sm.getLoaded();
        FieldMetaData[] fmds = sm.getMetaData().getFields();
        for (FieldMetaData fmd : fmds) {
            int idx = fmd.getIndex();
            if (!loaded.get(idx))
                continue;
            if (fmd.getElement().getTypeMetaData() == null && fmd.getValue().getTypeMetaData() == null)
                continue;
            switch (fmd.getDeclaredTypeCode()) {
                case JavaTypes.PC:
                    visit(toStateManager(sm.fetch(idx)));
                    break;
                case JavaTypes.ARRAY:
                    Object[] values = (Object[])sm.fetch(idx);
                    for (Object o : values)
                        visit(toStateManager(o));
                    break;
                case JavaTypes.COLLECTION:
                    Collection<?> members = (Collection<?>)sm.fetch(idx);
                    for (Object o : members)
                        visit(toStateManager(o));
                    break;
                case JavaTypes.MAP:
                    Map<?,?> map = (Map<?,?>)sm.fetch(idx);
                    for (Map.Entry<?,?> entry : map.entrySet()) {
                        visit(toStateManager(entry.getKey()));
                        visit(toStateManager(entry.getValue()));
                    }
                    break;
                default:
            }
        }
    }
    
    OpenJPAStateManager toStateManager(Object o) {
        if (o instanceof PersistenceCapable) {
            return (OpenJPAStateManager)((PersistenceCapable)o).pcGetStateManager();
        }
        return null;
    }

    public Iterator<OpenJPAStateManager> iterator() {
        return _visited.iterator();
    }
    
    String ior(OpenJPAStateManager sm) {
        return sm.getMetaData().getDescribedType().getSimpleName()+'-'+sm.getObjectId();
    }
}
