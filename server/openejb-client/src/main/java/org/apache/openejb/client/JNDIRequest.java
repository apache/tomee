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

public class JNDIRequest implements ClusterableRequest {

    private static final long serialVersionUID = -568798775203142850L;
    private transient RequestMethodCode requestMethod;
    private transient String requestString;
    private transient String moduleId;
    private transient int serverHash;
    private transient ProtocolMetaData metaData;

    public JNDIRequest() {
    }

    public JNDIRequest(final RequestMethodCode requestMethod, final String requestString) {
        this.requestMethod = requestMethod;
        this.requestString = requestString;
    }

    @Override
    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.JNDI_REQUEST;
    }

    public RequestMethodCode getRequestMethod() {
        return requestMethod;
    }

    public String getRequestString() {
        return requestString;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(final String moduleId) {
        this.moduleId = moduleId;
    }

    public void setRequestMethod(final RequestMethodCode requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setRequestString(final String requestString) {
        this.requestString = requestString;
    }

    @Override
    public void setServerHash(final int serverHash) {
        this.serverHash = serverHash;
    }

    @Override
    public int getServerHash() {
        return serverHash;
    }

    /**
     * Changes to this method must observe the optional {@link #metaData} version
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        final int code = in.readByte();
        try {
            requestMethod = RequestMethodCode.valueOf(code);
        } catch (IllegalArgumentException iae) {
            throw new IOException("Invalid request code " + code);
        }
        requestString = in.readUTF();
        moduleId = (String) in.readObject();
        serverHash = in.readInt();
    }

    /**
     * Changes to this method must observe the optional {@link #metaData} version
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);

        out.writeByte((byte) requestMethod.getCode());
        out.writeUTF(requestString);
        out.writeObject(moduleId);
        out.writeInt(serverHash);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100);

        sb.append(requestMethod);
        sb.append(this.moduleId != null ? moduleId : "").append(":");
        sb.append(this.requestString);
        return sb.toString();
    }
}

