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

import org.apache.openejb.util.proxy.ProxyGenerationException;
import org.apache.tomee.microprofile.jwt.bval.data.Colors;
import org.apache.tomee.microprofile.jwt.bval.data.Colors$$JwtConstraints;
import org.apache.tomee.microprofile.jwt.bval.data.Shapes;
import org.apache.tomee.microprofile.jwt.bval.data.Shapes$$JwtConstraints;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationGeneratorTest {

    @Test
    public void testGetConstrainedMethods() throws Exception {

        final Set<Method> methods = OldValidationGenerator.getConstrainedMethods(Colors.class);
        final Map<String, Method> map = methods.stream().collect(Collectors.toMap(Method::getName, method -> method));

        Assert.assertTrue(map.containsKey("red"));
        Assert.assertTrue(map.containsKey("blue"));
        Assert.assertFalse(map.containsKey("green"));
    }

    @Test
    public void testSimple() throws Exception {
        assertGeneration(Colors.class, Colors$$JwtConstraints.class);
    }

    @Test
    public void test() throws Exception {
        assertGeneration(Shapes.class, Shapes$$JwtConstraints.class);
    }

    private void assertGeneration(final Class<?> target, final Class<?> expectedClass) throws IOException, ProxyGenerationException {
        final String actual = Asmifier.asmify(OldValidationGenerator.generateFor(target));
        final String expected = Asmifier.asmify(Asmifier.readClassFile(expectedClass));

        Assert.assertEquals(expected, actual);
    }

}