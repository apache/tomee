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
package org.apache.openjpa.event;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;

import org.apache.openjpa.lib.util.Localizer;
import org.apache.openjpa.util.UserException;

/**
 * Event type to hold the IDs of additions, updates, and
 * deletes. This event type is also fully serializable for remote communication.
 *
 * @since 0.3.0
 * @author Patrick Linskey
 * @author Abe White
 */
public class RemoteCommitEvent
    implements Externalizable {

    /**
     * Names of added classes, updated and deleted Object IDs.
     */
    public static final int PAYLOAD_OIDS = 0;

    /**
     * Names of added classes, added, updated and deleted Object IDs.
     */
    public static final int PAYLOAD_OIDS_WITH_ADDS = 1;

    /**
     * Names of added, updated, and deleted classes only.
     */
    public static final int PAYLOAD_EXTENTS = 2;

    /**
     * The local {@link BrokerFactory} detected that local data is out of date
     * with the data store. Stale object IDs will be in t he updated set,
     * although it is possible that records were actually deleted, rather than
     * updated.
     *
     * @since 1.0.0
     */
    public static final int PAYLOAD_LOCAL_STALE_DETECTION = 3;

    private static final Localizer s_loc = Localizer.forPackage
        (RemoteCommitEvent.class);

    private int _payload = PAYLOAD_OIDS;
    private Collection _addIds = null;
    private Collection _addClasses = null;
    private Collection _updates = null;
    private Collection _deletes = null;

    /**
     * Constructor used during externalization.
     */
    public RemoteCommitEvent() {
    }

    /**
     * Constructor. All collections will be proxied with unmodifiable views.
     *
     * @param payloadType PAYLOAD constant for type of data in this event
     * @param addIds set of object IDs for added instances, or null
     * @param addClasses set of class names for added instances
     * @param updates set of class names or object IDs for updated instances
     * @param deletes set of class names or object IDs for deleted instances
     */
    public RemoteCommitEvent(int payloadType, Collection addIds,
        Collection addClasses, Collection updates, Collection deletes) {
        _payload = payloadType;
        if (addIds != null)
            _addIds = Collections.unmodifiableCollection(addIds);
        if (addClasses != null)
            _addClasses = Collections.unmodifiableCollection(addClasses);
        if (updates != null)
            _updates = Collections.unmodifiableCollection(updates);
        if (deletes != null)
            _deletes = Collections.unmodifiableCollection(deletes);
    }

    /**
     * The event PAYLOAD constant.
     */
    public int getPayloadType() {
        return _payload;
    }

    /**
     * When the event type is PAYLOAD_OIDS_WITH_ADDS, return the set of
     * object IDs for added objects. This will only be callable when the
     * backward compatability property transmitAddObjectIds is true.
     */
    public Collection getPersistedObjectIds() {
        if (_payload != PAYLOAD_OIDS_WITH_ADDS) {
            if (_payload == PAYLOAD_OIDS)
                throw new UserException(s_loc.get("no-added-oids"));
            throw new UserException(s_loc.get("extent-only-event"));
        }
        return (_addIds == null) ? Collections.EMPTY_LIST : _addIds;
    }

    /**
     * When the event type is not PAYLOAD_EXTENTS, return the set of
     * object IDs for updated objects. When the event type is
     * PAYLOAD_LOCAL_STALE_DETECTION, items in this list may actually have
     * been deleted from the database.
     */
    public Collection getUpdatedObjectIds() {
        if (_payload == PAYLOAD_EXTENTS)
            throw new UserException(s_loc.get("extent-only-event"));
        return (_updates == null) ? Collections.EMPTY_LIST : _updates;
    }

    /**
     * When the event type is not PAYLOAD_EXTENTS, return the set of
     * object IDs for deleted objects.
     */
    public Collection getDeletedObjectIds() {
        if (_payload == PAYLOAD_EXTENTS)
            throw new UserException(s_loc.get("extent-only-event"));
        return (_deletes == null) ? Collections.EMPTY_LIST : _deletes;
    }

    /**
     * For all event types, return the set of class names for
     * the classes of inserted objects.
     */
    public Collection getPersistedTypeNames() {
        return (_addClasses == null) ? Collections.EMPTY_LIST : _addClasses;
    }

    /**
     * When the event type is PAYLOAD_EXTENTS, return the set of class
     * names for the classes of updated objects.
     */
    public Collection getUpdatedTypeNames() {
        if (_payload != PAYLOAD_EXTENTS)
            throw new UserException(s_loc.get("nonextent-event"));
        return (_updates == null) ? Collections.EMPTY_LIST : _updates;
    }

    /**
     * When the event type is PAYLOAD_EXTENTS, return the set of class
     * names for the classes of deleted objects.
     */
    public Collection getDeletedTypeNames() {
        if (_payload != PAYLOAD_EXTENTS)
            throw new UserException(s_loc.get("nonextent-event"));
        return (_deletes == null) ? Collections.EMPTY_LIST : _deletes;
    }

    public void writeExternal(ObjectOutput out)
        throws IOException {
        out.writeInt(_payload);
        out.writeObject(_addClasses);
        if (_payload == PAYLOAD_OIDS_WITH_ADDS)
            out.writeObject(_addIds);
        out.writeObject(_updates);
        out.writeObject(_deletes);
    }

    public void readExternal(ObjectInput in)
        throws IOException {
        try {
            _payload = in.readInt();
            _addClasses = (Collection) in.readObject();
            if (_payload == PAYLOAD_OIDS_WITH_ADDS)
                _addIds = (Collection) in.readObject();
            _updates = (Collection) in.readObject();
            _deletes = (Collection) in.readObject();
        } catch (ClassNotFoundException cnfe) {
            // ### do something
		}
	}
}
