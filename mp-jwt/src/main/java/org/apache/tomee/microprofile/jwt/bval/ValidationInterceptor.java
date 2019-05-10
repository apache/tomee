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

public class ValidationInterceptor {

    public ValidationInterceptor(final Class<?> componentClass) {
    }

    /**
     * Will return null if this concept does not apply to this bean.
     */
    public static ValidationInterceptor apply(final Class<?> componentClass) {
//        final byte[] bytes = JWTBeanValidationConstraintsGenerator.generateFor(componentClass);
//        if (bytes == null) return null;
//
//        final String name = JWTBeanValidationConstraintsGenerator.getName(componentClass);
//        final Class constraintsClazz = LocalBeanProxyFactory.Unsafe.defineClass(componentClass.getClassLoader(), componentClass, name, bytes);

        return null;
    }
}
