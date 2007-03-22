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

    private transient Object principal;
    private transient Object credentials;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(Object principal, Object credentials) {
        this.principal = principal;
        this.credentials = credentials;
    }

    public byte getRequestType() {
        return RequestMethodConstants.AUTH_REQUEST;
    }

    public Object getPrincipal() {
        return principal;
    }

    public Object getCredentials() {
        return credentials;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        principal = in.readObject();
        credentials = in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(principal);
        out.writeObject(credentials);
    }
}

