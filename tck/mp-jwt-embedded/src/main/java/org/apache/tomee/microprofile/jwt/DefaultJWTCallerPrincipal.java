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

import java.util.Set;

/**
 * A default implementation of JWTCallerPrincipal that wraps Nimbus library used by default here
 * We could create another implementation using plain JSON-P
 *
 */
public class DefaultJWTCallerPrincipal extends JWTCallerPrincipal {

    /**
     * Create a JWTCallerPrincipal with the caller's name
     *
     * @param name - caller's name
     */
    public DefaultJWTCallerPrincipal(final String name) {
        super(name);
    }

    @Override
    public String toString(final boolean showAll) {
        return null;
    }

    @Override
    public Set<String> getClaimNames() {
        return null;
    }

    @Override
    public <T> T getClaim(final String claimName) {
        return null;
    }
}