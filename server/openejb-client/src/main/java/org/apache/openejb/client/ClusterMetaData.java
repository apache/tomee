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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.client;

import java.io.Externalizable;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @version $Rev$ $Date$
 */
public class ClusterMetaData implements Externalizable {
    private URI[] locations;
    private long version;
    private String connectionStrategy;
    private URI lastLocation;

    public ClusterMetaData() {
    }

    public ClusterMetaData(long version, URI... locations) {
        this.locations = locations;
        this.version = version;
    }

    public URI getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(URI lastLocation) {
        this.lastLocation = lastLocation;
    }

    public URI[] getLocations() {
        return locations;
    }

    public long getVersion() {
        return version;
    }

    public void setConnectionStrategy(String connectionStrategy) {
        this.connectionStrategy = connectionStrategy;
    }

    public String getConnectionStrategy() {
        return connectionStrategy;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        in.readByte(); // for future use to identify format of the data.

        version = in.readLong();
        connectionStrategy = (String) in.readObject();

        int length = in.readInt();
        locations = new URI[length];

        for (int i = 0; i < locations.length; i++) {
            Object o = in.readObject();
            try {
                locations[i] = new URI((String)o);
            } catch (URISyntaxException e) {
                throw (IOException) new IOException().initCause(e);
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeLong(version);
        out.writeObject(connectionStrategy);
        out.writeInt(locations.length);
        for (URI uri : locations) {
            out.writeObject(uri.toString());
        }
    }
}
