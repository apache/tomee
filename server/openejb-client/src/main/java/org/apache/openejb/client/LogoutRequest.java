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

public class LogoutRequest implements Request {
    private static final long serialVersionUID = 1L;

    private transient Object securityIdentity;
    private transient ProtocolMetaData metaData;

    public LogoutRequest() {
        // no-op
    }

    public LogoutRequest(final Object securityIdentity) {
        this.securityIdentity = securityIdentity;
    }

    @Override
    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.LOGOUT_REQUEST;
    }

    public Object getSecurityIdentity() {
        return securityIdentity;
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        in.readByte(); // version, future use
        securityIdentity = in.readObject();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);
        out.writeObject(securityIdentity);
    }

    public String toString() {
        return String.valueOf(securityIdentity);
    }
}

