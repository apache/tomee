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
import java.rmi.RemoteException;
import java.util.Arrays;

public class ServerMetaData implements Externalizable {

    private transient URI[] locations;
    private transient ConnectionFactoryStrategy connectionFactoryStrategy;

    public ServerMetaData() {
    }

    public ServerMetaData(URI ... locations)  {
        this.locations = locations;
    }

    public void merge(ServerMetaData toMerge) {
        locations = toMerge.locations;
    }
    
    public URI[] getLocations() {
        return locations;
    }

    public int buildHash() {
        int locationsHash = 0;
        for (URI location : locations) {
            locationsHash += location.hashCode();
        }
        return locationsHash;
    }

    public Connection connect(Request request) throws RemoteException {
        ConnectionFactoryStrategy factoryStrategy = getConnectionFactoryStrategy();
        return factoryStrategy.connect(locations, request);
    }
    
    protected ConnectionFactoryStrategy getConnectionFactoryStrategy() {
        if (null == connectionFactoryStrategy) {
            connectionFactoryStrategy = new StickToLastServerConnectionFactoryStrategy(); 
        }
        return connectionFactoryStrategy;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte version = in.readByte(); // future use
        
        locations = (URI[]) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeObject(locations);
    }

    public String toString() {
        return Arrays.toString(locations);
    }

}

