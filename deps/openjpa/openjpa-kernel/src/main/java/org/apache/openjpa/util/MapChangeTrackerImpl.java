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

import java.util.Map;

/**
 * Default {@link MapChangeTracker}.
 *
 * @author Abe White
 * @nojavadoc
 */
public class MapChangeTrackerImpl
    extends AbstractChangeTracker
    implements MapChangeTracker {

    private final Map _map;
    private boolean _keys = true;

    /**
     * Constructor; supply delegate map.
     */
    public MapChangeTrackerImpl(Map map, boolean autoOff) {
        _map = map;
        this.setAutoOff(autoOff);
    }

    public boolean getTrackKeys() {
        return _keys;
    }

    public void setTrackKeys(boolean keys) {
        _keys = keys;
    }

    public void added(Object key, Object val) {
        if (_keys)
            super.added(key);
        else
            super.added(val);
    }

    public void removed(Object key, Object val) {
        if (_keys)
            super.removed(key);
        else
            super.removed(val);
    }

    public void changed(Object key, Object oldVal, Object newVal) {
        if (_keys)
            super.changed(key);
        else {
            super.removed(oldVal);
            super.added(newVal);
        }
    }

    protected void add(Object obj) {
        // if the key was previously removed and now added back, mark
        // it as a change; otherwise it's a new addition
        if (rem != null && rem.remove(obj)) {
            if (change == null)
                change = newSet();
            change.add(obj);
        } else {
            // after a point it becomes inefficient to track changes
            if (getAutoOff() && getAdded().size() + getChanged().size()
                + getRemoved().size() >= _map.size())
                stopTracking();
            else {
                if (add == null)
                    add = newSet();
                add.add(obj);
            }
        }
    }

    protected void remove(Object obj) {
        // no longer a change, if it was before
        if (change != null)
            change.remove(obj);

        // if it was a new addition, just forget it; otherwise remember
        // that it was removed
        if (add == null || !add.remove(obj)) {
            // after a point it becomes inefficient to track changes
            if (getAutoOff() && getAdded().size() + getChanged().size()
                + getRemoved().size() >= _map.size())
                stopTracking();
            else {
                if (rem == null)
                    rem = newSet();
                rem.add(obj);
            }
        }
    }

    protected void change(Object key) {
        // if the key is already changed or the key is newly added, nothing
        // to do
        if ((change != null && change.contains(key))
            || (add != null && add.contains(key)))
            return;

        // after a point it becomes inefficient to track changes
        if (getAutoOff() && getAdded().size() + getChanged().size()
            + getRemoved().size() >= _map.size())
            stopTracking();
        else {
            // record the change
            if (change == null)
                change = newSet();
            change.add(key);
        }
    }
}
