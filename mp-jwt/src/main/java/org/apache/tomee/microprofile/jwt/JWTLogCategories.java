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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt;

import org.apache.openejb.util.LogCategory;

public class JWTLogCategories {

    /**
     * Root log level for all MicroProfile JWT code
     */
    private static final LogCategory JWT = LogCategory.OPENEJB
            .createChild("microprofile")
            .createChild("jwt");

    /**
     * Configuration issues are logged on error
     * Individual settings are logged in
     */
    public static final LogCategory CONFIG = JWT.createChild("configuration");

    /**
     * Key resolution and key values are logged on debug and info
     * allowing users to know exactly what key is being used for verification
     */
    public static final LogCategory KEYS = CONFIG.createChild("keys");

    /**
     * Expresses if a token is valid or not
     */
    public static final LogCategory VALIDATION = JWT.createChild("validation");

    /**
     * Allows more fine-grained reporting of a user's Bean Validation constraints
     */
    public static final LogCategory CONSTRAINT = VALIDATION.createChild("constraint");

    /**
     * Where full JWTs are logged
     */
    public static final LogCategory TOKENS = JWT.createChild("tokens");

    /**
     * Encoded form of the JWT. This is a separate logging category so it can be shut off entirely.
     * A user will typically enable either this or the "decoded" category, not both
     */
    public static final LogCategory TOKENS_ENCODED = TOKENS.createChild("encoded");

    /**
     * Decoded form of the JWT. This is a separate logging category so it can be shut off entirely.
     * A user will typically enable either this or the "encoded" category, not both
     */
    public static final LogCategory TOKENS_DECODED = TOKENS.createChild("decoded");

    private JWTLogCategories() {
    }
}
