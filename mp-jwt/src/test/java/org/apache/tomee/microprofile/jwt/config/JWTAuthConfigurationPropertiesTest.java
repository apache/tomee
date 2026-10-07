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
package org.apache.tomee.microprofile.jwt.config;

import jakarta.enterprise.inject.spi.DeploymentException;
import org.junit.Test;

import static org.eclipse.microprofile.jwt.config.Names.CLOCK_SKEW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class JWTAuthConfigurationPropertiesTest {

    @Test
    public void nonNegativeClockSkewIsAccepted() {
        assertEquals(Integer.valueOf(0), JWTAuthConfigurationProperties.validateClockSkew(0));
        assertEquals(Integer.valueOf(60), JWTAuthConfigurationProperties.validateClockSkew(60));
    }

    @Test
    public void negativeClockSkewFailsDeployment() {
        try {
            JWTAuthConfigurationProperties.validateClockSkew(-1);
            fail("a negative clock skew must fail the deployment");
        } catch (final DeploymentException expected) {
            assertTrue(expected.getMessage().contains(CLOCK_SKEW));
        }
    }
}
