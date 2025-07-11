/**
 *
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
package org.apache.openejb.client;

import org.apache.openejb.client.util.JDK24Subject;

import javax.security.auth.Subject;
import java.util.Set;

public class JaasIdentityResolver implements IdentityResolver {

    @Override
    public Object getIdentity() {
        final Subject subject = JDK24Subject.currentSubject();
        if (subject == null) {
            return null;
        }

        final Set<ClientIdentityPrincipal> identityPrincipals = subject.getPrincipals(ClientIdentityPrincipal.class);
        if (identityPrincipals.isEmpty()) {
            return null;
        }

        final ClientIdentityPrincipal principal = identityPrincipals.iterator().next();
        return principal.getClientIdentity();
    }
}
