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
package org.apache.tomee.microprofile.jwt.jaxrs;

import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.cxf.rs.event.ExtensionProviderRegistration;
import org.apache.tomee.microprofile.jwt.MPJWTFilter;

/**
 * OpenEJB/TomEE hack to register a new provider on the fly
 * Could be package in tomee only or done in another way
 *
 * As soon as Roberto is done with the packaging, we can remove all this and providers are going to be scanned automatically
 */
public class MPJWPProviderRegistration {

    public void registerProvider(@Observes final ExtensionProviderRegistration event) {
        event.getProviders().add(new MPJWTFilter.MPJWTExceptionMapper());
        event.getProviders().add(new MPJWTSecurityAnnotationsInterceptorsFeature());
    }

}