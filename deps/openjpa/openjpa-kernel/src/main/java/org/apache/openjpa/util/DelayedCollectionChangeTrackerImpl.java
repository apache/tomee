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
package org.apache.openjpa.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A collection change tracker used by delay loaded collections.
 *
 * @nojavadoc
 */
public class DelayedCollectionChangeTrackerImpl
    extends CollectionChangeTrackerImpl {

    public DelayedCollectionChangeTrackerImpl(Collection coll, boolean dups,
            boolean order,boolean autoOff) {
        super(coll, dups, order, autoOff);
    }

    protected void add(Object elem) {
        if (rem == null || !rem.remove(elem)) {
            if (add == null) {
                if (_dups || _order)
                    add = new ArrayList();
                else
                    add = newSet();
            }
            add.add(elem);
        } else {
            if (change == null)
                change = newSet();
            change.add(elem);
        }
    }

    protected void remove(Object elem) {
        if (add == null || !add.remove(elem)) {
            if (rem == null)
                rem = newSet();
            rem.add(elem);
        }
    }

    protected void change(Object elem) {
        throw new InternalException();
    }
}
