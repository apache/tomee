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

import java.util.ArrayList;
import java.util.List;

public class ClassValidationGenerator {

    private final ClassValidationData validationData;

    public ClassValidationGenerator(final ClassValidationData validationData) {
        this.validationData = validationData;
    }

    public List<Class<?>> generate() {
        final List<Class<?>> classes = new ArrayList<>();

        if (validationData.getJwtConstraints().size() > 0) {
            {
                final JwtValidationGenerator generator = new JwtValidationGenerator(validationData.getClazz(), validationData.getJwtConstraints());
                classes.add(generator.generateAndLoad());
            }
            {
                final ReturnValidationGenerator generator = new ReturnValidationGenerator(validationData.getClazz(), validationData.getReturnConstraints());
                classes.add(generator.generateAndLoad());
            }
        }
        return classes;
    }
}
