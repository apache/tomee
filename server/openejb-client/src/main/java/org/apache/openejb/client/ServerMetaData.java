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
package org.apache.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.Arrays;

public class ServerMetaData implements Externalizable {

    private static final long serialVersionUID = -915541900332460068L;
    private transient URI[] locations;
    private transient URI location;
    private transient ProtocolMetaData metaData;

    public ServerMetaData() {
    }

    public ServerMetaData(final URI... locations) {
        this.locations = locations;
        location = locations[0];
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    public void merge(final ServerMetaData toMerge) {
        locations = toMerge.locations;
    }

    public URI getLocation() {
        return location;
    }

    public URI[] getLocations() {
        return locations;
    }

    public int buildHash() {
        int locationsHash = 0;
        for (final URI location : this.locations) {
            locationsHash += location.hashCode();
        }
        return locationsHash;
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte();

        locations = (URI[]) in.readObject();
        location = locations[0];
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeObject(locations);
    }

    public String toString() {
        return Arrays.toString(locations);
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ServerMetaData that = (ServerMetaData) o;

        return !(location != null ? !location.equals(that.location) : that.location != null);

    }

    public int hashCode() {
        return (location != null ? location.hashCode() : 0);
    }
}

