/*
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
package javax.security.enterprise.authentication.mechanism.http;

import javax.security.enterprise.credential.Credential;

public class AuthenticationParameters {
    private Credential credential;
    private boolean newAuthentication;
    private boolean rememberMe;

    public static AuthenticationParameters withParams() {
        return new AuthenticationParameters();
    }

    public AuthenticationParameters credential(Credential credential) {
        setCredential(credential);
        return this;
    }

    public AuthenticationParameters newAuthentication(boolean newAuthentication) {
        setNewAuthentication(newAuthentication);
        return this;
    }

    public AuthenticationParameters rememberMe(boolean rememberMe) {
        setRememberMe(rememberMe);
        return this;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    public boolean isNewAuthentication() {
        return newAuthentication;
    }

    public void setNewAuthentication(boolean newAuthentication) {
        this.newAuthentication = newAuthentication;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
