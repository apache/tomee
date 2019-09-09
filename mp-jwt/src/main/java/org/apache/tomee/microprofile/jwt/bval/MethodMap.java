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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Given a class like the following:
 *
 * public class Green {
 *
 *    {@literal @}ReturnValidation("bar")
 *    public void sage() {
 *    }
 * }
 *
 * We will have generated a class like the following
 *
 */
public class MethodMap {

    private final Map<Method, Method> mapping = new HashMap<>();

    public static MethodMap of(final Class<?> original, final Class<?> generated) {
        final MethodMap map = new MethodMap();
        /**
         * The generated class will have several methods annotated with @Name,
         * where name indicates the name of the matching original method.
         */
        final Map<String, Method> methods = Stream.of(original.getMethods())
                .collect(Collectors.toMap(Method::toString, Function.identity()));

        for (final Method generatedMethod : generated.getMethods()) {
            final Generated name = generatedMethod.getAnnotation(Generated.class);

            if (name == null) continue;

            final String toString = name.value();
            final Method originalMethod = methods.get(toString);

            if (originalMethod == null) {
                throw new IllegalStateException(String.format("Cannot find method '%s' on class '%s'", toString, original.getName()));
            }

            map.mapping.put(originalMethod, generatedMethod);
        }

        return map;
    }

    public Method get(final Method orinalMethod) {
        return mapping.get(orinalMethod);
    }
}
