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

    private static final long serialVersionUID = 7293643855614467349L;
    private transient int responseCode = -1;
    private transient ClientMetaData identity;
    private transient ServerMetaData server;
    private transient Throwable deniedCause;
    private transient AuthenticationRequest request;
    private transient ProtocolMetaData metaData;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(final int code) {
        this.responseCode = code;
    }

    @Override
    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    public AuthenticationRequest getRequest() {
        return request;
    }

    public void setRequest(final AuthenticationRequest request) {
        this.request = request;
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

    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }

    public void setIdentity(final ClientMetaData identity) {
        this.identity = identity;
    }

    public void setServer(final ServerMetaData server) {
        this.server = server;
    }

    public Throwable getDeniedCause() {
        return deniedCause;
    }

    public void setDeniedCause(final Throwable deniedCause) {
        this.deniedCause = deniedCause;
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        responseCode = in.readByte();
        switch (responseCode) {
            case ResponseCodes.AUTH_GRANTED:
                identity = new ClientMetaData();
                identity.setMetaData(metaData);
                identity.readExternal(in);
                break;
            case ResponseCodes.AUTH_REDIRECT:
                identity = new ClientMetaData();
                identity.setMetaData(metaData);
                identity.readExternal(in);

                server = new ServerMetaData();
                server.setMetaData(metaData);
                server.readExternal(in);
                break;
            case ResponseCodes.AUTH_DENIED:
                final ThrowableArtifact ta = new ThrowableArtifact();
                ta.setMetaData(metaData);
                ta.readExternal(in);
                deniedCause = ta.getThrowable();
                break;
        }
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeByte((byte) responseCode);
        switch (responseCode) {
            case ResponseCodes.AUTH_GRANTED:
                identity.setMetaData(metaData);
                identity.writeExternal(out);
                break;
            case ResponseCodes.AUTH_REDIRECT:
                identity.setMetaData(metaData);
                identity.writeExternal(out);

                server.setMetaData(metaData);
                server.writeExternal(out);
                break;
            case ResponseCodes.AUTH_DENIED:
                final ThrowableArtifact ta = new ThrowableArtifact(deniedCause);
                ta.setMetaData(metaData);
                ta.writeExternal(out);
                break;
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(50);

        switch (responseCode) {
            case ResponseCodes.AUTH_GRANTED: {
                sb.append("AUTH_GRANTED:");
                sb.append(null != identity ? identity.toString() : "Unknown identity");
                break;
            }
            case ResponseCodes.AUTH_REDIRECT: {
                sb.append("AUTH_REDIRECT:");
                sb.append(null != server ? server.toString() : "Unknown server");
                break;
            }
            case ResponseCodes.AUTH_DENIED: {
                sb.append("AUTH_DENIED:");
                sb.append(null != deniedCause ? deniedCause.toString() : "Unknown denial");
                break;
            }
        }
        return sb.toString();
    }

}

