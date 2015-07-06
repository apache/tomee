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

public class AuthenticationRequest implements Request {

    /**
     * Never change this, use #metaData for version
     */
    private static final long serialVersionUID = 7009531340198948330L;

    private transient String realm;
    private transient String username;
    private transient String credentials;
    private transient long timeout;
    private transient ProtocolMetaData metaData;
    private transient Object logoutIdentity = null;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(final String principal, final String credentials) {
        this(null, principal, credentials, 0);
    }

    public AuthenticationRequest(final String principal, final String credentials, final long timeout) {
        this(null, principal, credentials, timeout);
    }

    public AuthenticationRequest(final String securityRealm, final String username, final String password) {
        this(securityRealm, username, password, 0);
    }

    public AuthenticationRequest(final String realm, final String principal, final String credentials, final long timeout) {
        this.realm = realm;
        this.username = principal;
        this.credentials = credentials;
        this.timeout = timeout;
    }

    @Override
    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.AUTH_REQUEST;
    }

    public String getRealm() {
        return realm;
    }

    public String getUsername() {
        return username;
    }

    public String getCredentials() {
        return credentials;
    }

    public long getTimeout() {
        return timeout;
    }

    public Object getLogoutIdentity() {
        return logoutIdentity;
    }

    public void setLogoutIdentity(final Object logoutIdentity) {
        this.logoutIdentity = logoutIdentity;
    }

    /**
     * Changes to this method must observe the optional {@link #metaData} version
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        in.readByte(); // Not used @see #metaData

        realm = (String) in.readObject();
        username = (String) in.readObject();
        credentials = (String) in.readObject();

        if (null == metaData || metaData.isAtLeast(4, 7)) {
            timeout = in.readLong();
            logoutIdentity = in.readObject();
        }
    }

    /**
     * Changes to this method must observe the optional {@link #metaData} version
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // Not used, but must be written @see #metaData
        out.writeByte(1);

        out.writeObject(realm);
        out.writeObject(username);
        out.writeObject(credentials);
        out.writeLong(timeout);
        out.writeObject(logoutIdentity);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(50);
        sb.append(null != realm ? realm : "Undefined realm").append(':');
        sb.append(null != username ? username : "Undefined user").append(':');
        sb.append(null != credentials ? credentials : "Undefined credentials").append(':');
        sb.append(null != logoutIdentity ? logoutIdentity : "Undefined logoutIdentity").append(':');
        sb.append(timeout);
        return sb.toString();
    }
}

