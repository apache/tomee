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

public class LogoutResponse implements Response {
    private static final long serialVersionUID = 7293643855614467349L;

    private transient int responseCode = -1;
    private transient Throwable deniedCause;
    private transient ProtocolMetaData metaData;

    public LogoutResponse() {
    }

    public LogoutResponse(final int code) {
        this.responseCode = code;
    }

    @Override
    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    public Throwable getDeniedCause() {
        return deniedCause;
    }

    public void setDeniedCause(final Throwable deniedCause) {
        this.deniedCause = deniedCause;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        in.readByte(); // version, future use
        responseCode = in.readByte();
        switch (responseCode) {
            case ResponseCodes.LOGOUT_SUCCESS:
                deniedCause = null;
                break;
            case ResponseCodes.LOGOUT_FAILED:
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
            case ResponseCodes.LOGOUT_FAILED:
                final ThrowableArtifact ta = new ThrowableArtifact(deniedCause);
                ta.setMetaData(metaData);
                ta.writeExternal(out);
                break;
            case ResponseCodes.LOGOUT_SUCCESS:
            default:
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(50);

        switch (responseCode) {
            case ResponseCodes.LOGOUT_SUCCESS: {
                sb.append("LOGOUT_SUCCESS");
                break;
            }
            case ResponseCodes.LOGOUT_FAILED: {
                sb.append("LOGOUT_FAILED:");
                sb.append(null != deniedCause ? deniedCause.toString() : "Unknown denial");
                break;
            }
        }

        return sb.toString();
    }
}

