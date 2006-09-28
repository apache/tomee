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
import java.net.URISyntaxException;

public class ServerMetaData implements Externalizable {

    private transient URI location;

    public ServerMetaData() {
    }

    public ServerMetaData(URI location)  {
        this.location = location;
    }

    public int getPort() {
        return location.getPort();
    }

    public String getHost() {
        return location.getHost();
    }


    public URI getLocation() {
        return location;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String uri = (String) in.readObject();
        try {
            location = new URI(uri);
        } catch (URISyntaxException e) {
            throw (IOException)new IOException("cannot create uri from '"+uri+"'").initCause(e);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(location.toString());
    }

}

