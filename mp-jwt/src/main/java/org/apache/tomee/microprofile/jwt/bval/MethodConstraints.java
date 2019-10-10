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

import org.apache.bval.jsr.descriptor.ConstraintD;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MethodConstraints implements Comparable<MethodConstraints> {

    /**
     * Names of annotations that contain constraints
     */
    final Set<Class<?>> annotations = new HashSet<>();

    final Method method;

    public MethodConstraints(final Method method) {
        Objects.requireNonNull(method, "method cannot be null");
        this.method = method;
    }

    public void add(final ConstraintD<?> constraint) {
        annotations.add(constraint.getAnnotation().annotationType());
    }

    public Set<Class<?>> getAnnotations() {
        return annotations;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public int compareTo(final MethodConstraints that) {
        final String signatureA = signature(that.method);
        final String signatureB = signature(this.method);
        return signatureB.compareTo(signatureA);
    }

    private String signature(final Method method) {
        final String desc = method.toString();
        return desc.substring(desc.indexOf(method.getName()));
    }

    @Override
    public String toString() {
        return signature(method);
    }
}
