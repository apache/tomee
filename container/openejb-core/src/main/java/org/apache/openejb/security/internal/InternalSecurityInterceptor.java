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

package org.apache.openejb.security.internal;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.Assembler;
import org.apache.openejb.spi.SecurityService;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

public class InternalSecurityInterceptor {
    public static final String OPENEJB_INTERNAL_BEANS_SECURITY_ENABLED = "openejb.internal.beans.security.enabled";

    private static final String[] ROLES = new String[]{"openejb-admin", "tomee-admin"};

    @AroundInvoke
    public Object invoke(final InvocationContext ic) throws Exception {
        if (SystemInstance.get().isDefaultProfile() || !SystemInstance.get().getOptions().get(OPENEJB_INTERNAL_BEANS_SECURITY_ENABLED, true)) {
            return ic.proceed();
        }

        final SecurityService<?> ss = SystemInstance.get().getComponent(Assembler.class).getSecurityService();
        for (final String role : ROLES) {
            if (ss.isCallerInRole(role)) {
                return ic.proceed();
            }
        }

        throw new SecurityException("to invoke this EJB you need to get the right permission");
    }
}
