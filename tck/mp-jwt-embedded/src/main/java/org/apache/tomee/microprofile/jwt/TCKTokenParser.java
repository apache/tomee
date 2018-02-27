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

import org.apache.tomee.microprofile.jwt.config.JWTAuthContextInfo;
import org.apache.tomee.microprofile.jwt.principal.DefaultJWTCallerPrincipalFactory;
import org.apache.tomee.microprofile.jwt.principal.JWTCallerPrincipalFactory;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.jwt.tck.util.ITokenParser;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

/**
 * MP-JWT TCK harness class to parse a token string
 */
public class TCKTokenParser implements ITokenParser {

    @Override
    public JsonWebToken parse(final String bearerToken, final String issuer, final PublicKey publicKey) throws Exception {
        final JWTAuthContextInfo authContextInfo = new JWTAuthContextInfo((RSAPublicKey) publicKey, issuer);
        final JWTCallerPrincipalFactory factory = DefaultJWTCallerPrincipalFactory.instance();
        return factory.parse(bearerToken, authContextInfo);
    }

}
