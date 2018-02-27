/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.tomee.microprofile.jwt;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

@Dependent
public class JWTAuthContextInfoProvider {

    @Produces
    Optional<JWTAuthContextInfo> getOptionalContextInfo() {
        JWTAuthContextInfo contextInfo = new JWTAuthContextInfo();

        // todo use MP Config to load the configuration
        contextInfo.setIssuedBy("https://server.example.com");
        RSAPublicKey pk = null;
        try {
            pk = (RSAPublicKey) KeyUtils.decodePublicKey("-----BEGIN RSA PUBLIC KEY-----\n" +
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlivFI8qB4D0y2jy0CfEq\n" +
                    "Fyy46R0o7S8TKpsx5xbHKoU1VWg6QkQm+ntyIv1p4kE1sPEQO73+HY8+Bzs75XwR\n" +
                    "TYL1BmR1w8J5hmjVWjc6R2BTBGAYRPFRhor3kpM6ni2SPmNNhurEAHw7TaqszP5e\n" +
                    "UF/F9+KEBWkwVta+PZ37bwqSE4sCb1soZFrVz/UT/LF4tYpuVYt3YbqToZ3pZOZ9\n" +
                    "AX2o1GCG3xwOjkc4x0W7ezbQZdC9iftPxVHR8irOijJRRjcPDtA6vPKpzLl6CyYn\n" +
                    "sIYPd99ltwxTHjr3npfv/3Lw50bAkbT4HeLFxTx4flEoZLKO/g0bAoV2uqBhkA9x\n" +
                    "nQIDAQAB\n" +
                    "-----END RSA PUBLIC KEY-----\n");

        } catch (final Exception e) {
            e.printStackTrace();
            // todo better handling
            throw new RuntimeException(e);
        }
        contextInfo.setSignerKey(pk);

        return Optional.of(contextInfo);
    }

    @Produces
    JWTAuthContextInfo getContextInfo() {
        return getOptionalContextInfo().get();
    }
}