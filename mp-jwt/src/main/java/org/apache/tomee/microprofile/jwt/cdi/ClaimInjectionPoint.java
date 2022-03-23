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
package org.apache.tomee.microprofile.jwt.cdi;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

public class ClaimInjectionPoint implements InjectionPoint {

    private final Bean bean;

    public ClaimInjectionPoint(final Bean bean) {
        this.bean = bean;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isDelegate() {
        return false;
    }

    @Override
    public Type getType() {
        return InjectionPoint.class;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.<Annotation>singleton(DefaultLiteral.INSTANCE);
    }

    @Override
    public Member getMember() {
        return null;
    }

    @Override
    public Bean<?> getBean() {
        return bean;
    }

    @Override
    public Annotated getAnnotated() {
        return null;
    }
}