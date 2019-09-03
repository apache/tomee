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
package org.apache.tomee.microprofile.jwt.bval.blue;

import org.apache.tomee.microprofile.jwt.bval.Asserts;
import org.apache.tomee.microprofile.jwt.bval.ClassValidationData;
import org.apache.tomee.microprofile.jwt.bval.JwtValidationGenerator;
import org.apache.tomee.microprofile.jwt.bval.ReturnValidationGenerator;
import org.apache.tomee.microprofile.jwt.bval.ValidationGenerator;
import org.junit.Test;

/**
 * Test every kind of parameter and return type
 */
public class BlueValidationGeneratorTest {

    @Test
    public void testJwtGenerator() throws Exception {

        final ClassValidationData data = new ClassValidationData(Blue.class);
        final ValidationGenerator generator = new JwtValidationGenerator(data.getClazz(), data.getJwtConstraints());

        Asserts.assertBytecode(generator.generate(), Blue$$JwtConstraints.class);
    }

    @Test
    public void testReturnGenerator() throws Exception {

        final ClassValidationData data = new ClassValidationData(Blue.class);
        final ValidationGenerator generator = new ReturnValidationGenerator(data.getClazz(), data.getReturnConstraints());

        Asserts.assertBytecode(generator.generate(), Blue$$ReturnConstraints.class);
    }

}