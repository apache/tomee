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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomee.security.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;

import java.security.Principal;

/**
 * Jakarta Security 4.0 §1.2.4: the container must expose a {@code java.security.Principal}
 * bean with the {@code @Default} qualifier that resolves to the current caller.
 *
 * <p>Producer is {@code @Dependent} so each injection sees the principal at the moment
 * of resolution -- the caller identity changes per-request.
 */
@ApplicationScoped
public class CallerPrincipalProducer {

    @Inject
    private SecurityContext securityContext;

    @Produces
    @Dependent
    @Default
    public Principal callerPrincipal() {
        return securityContext.getCallerPrincipal();
    }
}
