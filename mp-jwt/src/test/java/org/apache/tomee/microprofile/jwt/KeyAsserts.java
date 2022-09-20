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


import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.Assert.assertEquals;

public class KeyAsserts {
    public static void assertRsaPrivateKey(final RSAPrivateCrtKey expected, final RSAPrivateCrtKey actual) {
        assertEquals(expected.getPublicExponent(), actual.getPublicExponent());
        assertEquals(expected.getCrtCoefficient(), actual.getCrtCoefficient());
        assertEquals(expected.getPrimeExponentP(), actual.getPrimeExponentP());
        assertEquals(expected.getPrimeExponentQ(), actual.getPrimeExponentQ());
        assertEquals(expected.getPrimeP(), actual.getPrimeP());
        assertEquals(expected.getPrimeQ(), actual.getPrimeQ());
        assertEquals(expected.getPrivateExponent(), actual.getPrivateExponent());
        assertEquals(expected.getModulus(), actual.getModulus());
    }

    public static void assertRsaPublicKey(final RSAPublicKey expected, final RSAPublicKey actual) {
        assertEquals(expected.getPublicExponent(), actual.getPublicExponent());
        assertEquals(expected.getModulus(), actual.getModulus());
    }

    public static void assertDsaPrivateKey(final DSAPrivateKey expected, final DSAPrivateKey actual) {
        assertEquals(expected.getParams().getG(), actual.getParams().getG());
        assertEquals(expected.getParams().getQ(), actual.getParams().getQ());
        assertEquals(expected.getParams().getP(), actual.getParams().getP());
        assertEquals(expected.getX(), actual.getX());
    }

    public static void assertDsaPublicKey(final DSAPublicKey expected, final DSAPublicKey actual) {
        assertEquals(expected.getParams().getG(), actual.getParams().getG());
        assertEquals(expected.getParams().getQ(), actual.getParams().getQ());
        assertEquals(expected.getParams().getP(), actual.getParams().getP());
        assertEquals(expected.getY(), actual.getY());
    }

    public static void assertEcPrivateKey(final ECPrivateKey expected, final ECPrivateKey actual) {
        assertEquals("d", expected.getS(), actual.getS());
        CurveAsserts.assertParamSpec(expected.getParams(), actual.getParams());
    }

    public static void assertEcPublicKey(final ECPublicKey expected, final ECPublicKey actual) {
        assertEquals("x", expected.getW().getAffineX(), actual.getW().getAffineX());
        assertEquals("y", expected.getW().getAffineY(), actual.getW().getAffineY());
        CurveAsserts.assertParamSpec(expected.getParams(), actual.getParams());
    }
}
