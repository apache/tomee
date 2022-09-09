/*
 * Copyright 2021 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.microprofile.jwt;


import io.churchkey.shade.util.Hex;

import java.math.BigInteger;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;

import static org.junit.Assert.assertEquals;

public class CurveAsserts {

    public static void assertParamSpec(final ECParameterSpec expected, final ECParameterSpec actual) {
        assertEquals(expected.getCofactor(), actual.getCofactor());
        assertBigInt(expected.getOrder(), actual.getOrder());
        assertBigInt(expected.getCurve().getA(), actual.getCurve().getA());
        assertBigInt(expected.getCurve().getB(), actual.getCurve().getB());
        assertBigInt(expected.getGenerator().getAffineX(), actual.getGenerator().getAffineX());
        assertBigInt(expected.getGenerator().getAffineY(), actual.getGenerator().getAffineY());

        if (expected.getCurve().getField() instanceof ECFieldFp) {
            assertBigInt(((ECFieldFp) expected.getCurve().getField()).getP(), ((ECFieldFp) actual.getCurve().getField()).getP());
        }
        if (expected.getCurve().getField() instanceof ECFieldF2m) {
            assertBigInt(((ECFieldF2m) expected.getCurve().getField()).getReductionPolynomial(), ((ECFieldF2m) actual.getCurve().getField()).getReductionPolynomial());
        }

    }

    public static void assertBigInt(final BigInteger expected, final BigInteger actual) {
        final String e1 = Hex.toString(expected.toByteArray()).replaceFirst("^00", "");
        final String a1 = Hex.toString(actual.toByteArray()).replaceFirst("^00", "");
        assertEquals(e1, a1);
    }
}
