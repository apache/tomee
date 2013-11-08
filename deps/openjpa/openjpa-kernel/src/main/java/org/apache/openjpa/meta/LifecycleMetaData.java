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

import java.io.Serializable;
import java.util.Arrays;

import org.apache.openjpa.event.LifecycleCallbacks;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.InternalException;

/**
 * Information about lifecycle events for a managed type.
 *
 * @author Steve Kim
 * @author Abe White
 */
public class LifecycleMetaData
    implements Serializable {

    public static final int IGNORE_NONE = 0;
    public static final int IGNORE_HIGH = 2 << 0;
    public static final int IGNORE_LOW = 2 << 1;

    private static final LifecycleCallbacks[] EMPTY_CALLBACKS =
        new LifecycleCallbacks[0];
    private static final Localizer _loc = Localizer.forPackage
        (LifecycleMetaData.class);

    private final ClassMetaData _meta;
    private LifecycleCallbacks[][] _declared = null;
    private LifecycleCallbacks[][] _super = null;
    private LifecycleCallbacks[][] _all = null;
    private int[] _high = null;
    private int[] _superHigh = null;
    private boolean _resolved = false;
    private boolean _ignoreSystem = false;
    private int _ignoreSups = 0;
    private boolean _activated = false;

    /**
     * Construct with owning metadata.
     */
    LifecycleMetaData(ClassMetaData meta) {
        _meta = meta;
    }

    /**
     * Whether the LifeCycleMetaData has had any callbacks or listeners registered.  Used
     * for a quick test to determine whether to attempt to fire any events.
     * @return boolean 
     */
    public boolean is_activated() {
        return _activated;
    }

    /**
     * Whether to exclude system listeners from events.
     */
    public boolean getIgnoreSystemListeners() {
        return _ignoreSystem;
    }

    /**
     * Whether to exclude system listeners from events.
     */
    public void setIgnoreSystemListeners(boolean ignore) {
        _ignoreSystem = ignore;
    }

    /**
     * Whether to exclude superclass callbacks from events.
     */
    public int getIgnoreSuperclassCallbacks() {
        return _ignoreSups;
    }

    /**
     * Whether to exclude superclass callbacks from events.
     */
    public void setIgnoreSuperclassCallbacks(int ignore) {
        _ignoreSups = ignore;
    }

    /**
     * Return the declared callbacks for the given event type.
     */
    public LifecycleCallbacks[] getDeclaredCallbacks(int eventType) {
        return (_declared == null || _declared[eventType] == null)
            ? EMPTY_CALLBACKS : _declared[eventType];
    }

    /**
     * Return all callbacks for the given event type, including superclass
     * callbacks if appropriate.
     */
    public LifecycleCallbacks[] getCallbacks(int eventType) {
        resolve();
        return (_all == null || _all[eventType] == null)
            ? EMPTY_CALLBACKS : _all[eventType];
    }

    /**
     * Set the callbacks for the given event type.
     *
     * @param highPriority the first N given callbacks are high priority;
     * high priority callbacks will be returned before
     * non-high-priority superclass callbacks
     */
    public void setDeclaredCallbacks(int eventType,
        LifecycleCallbacks[] callbacks, int highPriority) {
        if (_resolved)
            throw new InternalException(_loc.get("lifecycle-resolved",
                _meta, Arrays.asList(callbacks)));

        if (_declared == null) {
            _declared = new LifecycleCallbacks
                [LifecycleEvent.ALL_EVENTS.length][];
            _high = new int[LifecycleEvent.ALL_EVENTS.length];
        }
        _declared[eventType] = callbacks;
        _high[eventType] = highPriority;
        _activated = true;
    }

    /**
     * Return the callbacks for the non-PC superclass.
     */
    public LifecycleCallbacks[] getNonPCSuperclassCallbacks
        (int eventType) {
        return (_super == null || _super[eventType] == null)
            ? EMPTY_CALLBACKS : _super[eventType];
    }

    /**
     * Set the callbacks for the given event type for non-persistent
     * superclass. Note these callbacks will only be used where the
     * non-persistent superclass is the direct ancestor of the described class.
     *
     * @param highPriority the first N given callbacks are high priority;
     * high priority callbacks will be returned before
     * non-high-priority superclass callbacks
     */
    public void setNonPCSuperclassCallbacks(int eventType,
        LifecycleCallbacks[] callbacks, int highPriority) {
        if (_resolved)
            throw new InternalException(_loc.get("lifecycle-resolved",
                _meta, Arrays.asList(callbacks)));

        if (_super == null) {
            _super = new LifecycleCallbacks
                [LifecycleEvent.ALL_EVENTS.length][];
            _superHigh = new int[LifecycleEvent.ALL_EVENTS.length];
        }
        _super[eventType] = callbacks;
        _superHigh[eventType] = highPriority;
        _activated = true;
    }

    /**
     * Resolve all callbacks.
     */
    void resolve() {
        if (!_resolved) {
            _all = combineCallbacks();
            _resolved = true;
        }
    }

    /**
     * Combine our callbacks with superclass callbacks as necessary.
     * This method has the side effect of manipulating the _high array to
     * reflect the combined callbacks rather than the declared ones.
     */
    private LifecycleCallbacks[][] combineCallbacks() {
        if (_ignoreSups == (IGNORE_HIGH | IGNORE_LOW))
            return _declared;

        LifecycleMetaData supMeta = (_meta.getPCSuperclass() == null) ? null
            : _meta.getPCSuperclassMetaData().getLifecycleMetaData();
        if (supMeta == null && _super == null)
            return _declared;

        if (supMeta != null) {
            supMeta.resolve();
            if (supMeta._all == null)
                return _declared;
            if (_declared == null && _ignoreSups == 0) {
                _high = supMeta._high;
                _activated = true;
                return supMeta._all;
            }
            // don't hold strong refs onto redundant info
            _super = null;
            _superHigh = null;
        }

        LifecycleCallbacks[][] all = new LifecycleCallbacks
            [LifecycleEvent.ALL_EVENTS.length][];
        LifecycleCallbacks[] decs, sups;
        int supStart, supEnd, supHigh;
        int count;
        for (int i = 0; i < all.length; i++) {
            decs = getDeclaredCallbacks(i);
            if (supMeta == null) {
                sups = (_super[i] == null) ? EMPTY_CALLBACKS : _super[i];
                supHigh = (_superHigh == null) ? 0 : _superHigh[i];
            } else {
                sups = supMeta.getCallbacks(i);
                supHigh = (supMeta._high == null) ? 0 : supMeta._high[i];
            }
            supStart = ((_ignoreSups & IGNORE_HIGH) != 0) ? supHigh : 0;
            supEnd = ((_ignoreSups & IGNORE_LOW) != 0) ? supHigh : sups.length;

            if (supEnd - supStart == 0)
                all[i] = decs;
            else if (decs.length == 0) {
                if (supEnd - supStart == sups.length)
                    all[i] = sups;
                else {
                    all[i] = new LifecycleCallbacks[supEnd - supStart];
                    System.arraycopy(sups, supStart, all[i], 0, all[i].length);
                }
                if (_high == null)
                    _high = new int[all.length];
                _high[i] = supHigh - supStart;
            } else {
                all[i] =
                    new LifecycleCallbacks[decs.length + supEnd - supStart];
                count = 0;

                // add superclass high priority callbacks first
                if ((_ignoreSups & IGNORE_HIGH) == 0)
                    for (int j = 0; j < supHigh; j++)
                        all[i][count++] = sups[j];
                // then our high priority
                for (int j = 0; j < _high[i]; j++)
                    all[i][count++] = decs[j];
                // then superclass low priority
                if ((_ignoreSups & IGNORE_LOW) == 0)
                    for (int j = supHigh; j < sups.length; j++)
                        all[i][count++] = sups[j];
                // then our low priority
                for (int j = _high[i]; j < decs.length; j++)
                    all[i][count++] = decs[j];

                if ((_ignoreSups & IGNORE_HIGH) == 0)
                    _high[i] += supHigh;
			}
		}
		return all;
	}
}
