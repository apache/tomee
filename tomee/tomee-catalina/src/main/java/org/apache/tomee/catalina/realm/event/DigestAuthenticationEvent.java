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
package org.apache.tomee.catalina.realm.event;

public class DigestAuthenticationEvent extends BaseAuthenticationEvent {

    private final String username;
    private final String digest;
    private final String nonce;
    private final String nc;
    private final String cnonce;
    private final String qop;
    private final String realm;
    private final String md5a2;

    public DigestAuthenticationEvent(final String username, final String digest, final String nonce, final String nc,
                                     final String cnonce, final String qop, final String realm, final String md5a2) {

        this.username = username;
        this.digest = digest;
        this.nonce = nonce;
        this.nc = nc;
        this.cnonce = cnonce;
        this.qop = qop;
        this.realm = realm;
        this.md5a2 = md5a2;
    }

    public String getUsername() {
        return username;
    }

    public String getDigest() {
        return digest;
    }

    public String getNonce() {
        return nonce;
    }

    public String getNc() {
        return nc;
    }

    public String getCnonce() {
        return cnonce;
    }

    public String getQop() {
        return qop;
    }

    public String getRealm() {
        return realm;
    }

    public String getMd5a2() {
        return md5a2;
    }
}
