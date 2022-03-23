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

import org.apache.openejb.util.Join;
import org.junit.Assert;

import java.io.IOException;

public class Asserts {

    public static void assertBytecode(final byte[] actualBytes, final Class<?> expectedClass) throws IOException {
        final String actual = Asmifier.asmify(actualBytes);
        final String expected = withoutGenerics(Asmifier.asmify(Asmifier.readClassFile(expectedClass)));

        Assert.assertEquals(expected, actual);
    }

    /**
     * When we generate the bytecode generics are not included.  This does
     * not affect things at runtime, but it does require us to adapt our testing.
     *
     * Specifically, we must yank the generic information from the expected bytecode
     *
     * This method will find lines like this:
     *
     * cw.visitMethod(ACC_PUBLIC, "emerald", "(Ljava/util/List;)Lorg/eclipse/microprofile/jwt/JsonWebToken;", "(Ljava/util/List<Ljava/net/URI;>;)Lorg/eclipse/microprofile/jwt/JsonWebToken;", null)
     *
     * and turn them into:
     *
     * cw.visitMethod(ACC_PUBLIC, "emerald", "(Ljava/util/List;)Lorg/eclipse/microprofile/jwt/JsonWebToken;", null, null)
     *
     */
    public static String withoutGenerics(final String asmify) {
        final String[] lines = asmify.split("\n");
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            if (!line.contains(".visitMethod(ACC_PUBLIC,")) continue;

            final String[] parts = line.split(",");
            parts[3] = " null";

            lines[i] = Join.join(",", parts);
        }
        return Join.join("\n", lines) + "\n";
    }
}
