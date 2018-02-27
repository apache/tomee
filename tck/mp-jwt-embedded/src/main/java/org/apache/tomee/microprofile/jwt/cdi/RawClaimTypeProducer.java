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

import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.Claims;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RawClaimTypeProducer {

    @Inject
    private MPJWTProducer producer;

    @Produces
    @Claim("")
    public Set<String> getSetOfString(final InjectionPoint ip) {
        final String name = getName(ip);
        ClaimValue<Optional<String>> cv = producer.generalClaimValueProducer(name);
        Optional<String> value = cv.getValue();
        if (value.isPresent()) {
            return new HashSet<String>() {{
                add(value.get());
            }};
        }
        return null;
    }

    @Produces
    @Claim("")
    @Named("RawClaimTypeProducer#getValue")
    public Object getValue(final InjectionPoint ip) {
        String name = getName(ip);
        ClaimValue<Optional<Object>> cv = producer.generalClaimValueProducer(name);
        Optional<Object> value = cv.getValue();
        return value.orElse(null);
    }

    @Produces
    @Claim("")
    @Named("RawClaimTypeProducer#getOptionalValue")
    public Optional getOptionalValue(final InjectionPoint ip) {
        String name = getName(ip);
        ClaimValue<Optional<Object>> cv = producer.generalClaimValueProducer(name);
        return cv.getValue();
    }

    private String getName(final InjectionPoint ip) {
        String name = null;
        for (Annotation ann : ip.getQualifiers()) {
            if (ann instanceof Claim) {
                Claim claim = (Claim) ann;
                name = claim.standard() == Claims.UNKNOWN ? claim.value() : claim.standard().name();
            }
        }
        return name;
    }
}