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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class AuthenticationResponse implements Response {

    private transient int responseCode = -1;
    private transient ClientMetaData identity;
    private transient ServerMetaData server;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(int code) {
        responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ClientMetaData getIdentity() {
        return identity;
    }

    public ServerMetaData getServer() {
        return server;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setIdentity(ClientMetaData identity) {
        this.identity = identity;
    }

    public void setServer(ServerMetaData server) {
        this.server = server;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte version = in.readByte(); // future use

        responseCode = in.readByte();
        switch (responseCode) {
            case ResponseCodes.AUTH_GRANTED:
                identity = new ClientMetaData();
                identity.readExternal(in);
                break;
            case ResponseCodes.AUTH_REDIRECT:
                identity = new ClientMetaData();
                identity.readExternal(in);
                server = new ServerMetaData();
                server.readExternal(in);
                break;
            case ResponseCodes.AUTH_DENIED:
                break;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeByte((byte) responseCode);
        switch (responseCode) {
            case ResponseCodes.AUTH_GRANTED:
                identity.writeExternal(out);
                break;
            case ResponseCodes.AUTH_REDIRECT:
                identity.writeExternal(out);
                server.writeExternal(out);
                break;
            case ResponseCodes.AUTH_DENIED:
                break;
        }
    }

}

