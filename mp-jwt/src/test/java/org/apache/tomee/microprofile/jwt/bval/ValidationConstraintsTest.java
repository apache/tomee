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
package org.apache.tomee.microprofile.jwt.bval;

import org.apache.tomee.microprofile.jwt.JsonWebTokenValidator;
import org.apache.tomee.microprofile.jwt.Tokens;
import org.apache.tomee.microprofile.jwt.bval.data.Colors;
import org.apache.tomee.microprofile.jwt.bval.data.Shapes;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@Ignore
public class ValidationConstraintsTest {

    @Test
    public void testValidate() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Colors.class);

        final Method red = Colors.class.getMethod("red");


        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"iss\":\"http://something.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        final Set<ConstraintViolation<Object>> violations = constraints.validate(red, jwt);

        assertEquals(1, violations.size());
        final ConstraintViolation<Object> next = violations.iterator().next();
        assertEquals("The 'iss' claim must be 'http://foo.bar.com'", next.getMessage());
    }

    @Test
    public void testAudienceIsJoe() throws Exception {
        final ValidationConstraints constraints = ValidationConstraints.of(Shapes.class);

        final Method method = Shapes.class.getMethod("square");
        final JsonWebTokenValidator validator = JsonWebTokenValidator.builder()
                .publicKey(Tokens.getPublicKey())
                .build();

        final String claims = "{" +
                "  \"sub\":\"Jane Awesome\"," +
                "  \"aud\":[\"jim\"]," +
                "  \"iss\":\"http://foo.bar.com\"," +
                "  \"groups\":[\"manager\",\"user\"]," +
                "  \"exp\":2552047942" +
                "}";
        final String token = Tokens.asToken(claims);

        final JsonWebToken jwt = validator.validate(token);

        final Set<ConstraintViolation<Object>> violations = constraints.validate(method, jwt);

        assertEquals(1, violations.size());
        final ConstraintViolation<Object> next = violations.iterator().next();
        assertEquals("The 'aud' claim must contain 'joe'", next.getMessage());
    }


}