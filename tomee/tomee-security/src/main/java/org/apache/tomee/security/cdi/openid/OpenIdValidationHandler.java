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
package org.apache.tomee.security.cdi.openid;

import org.apache.tomee.security.http.openid.model.TomEEOpenIdCredential;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.credential.Credential;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@ApplicationScoped
public class OpenIdValidationHandler implements IdentityStore {
    @Inject private Instance<Supplier<OpenIdAuthenticationMechanismDefinition>> definition;
    @Inject private TomEEOpenIdContext openIdContext;

    @PostConstruct
    public void init() {
        if (definition.isUnsatisfied()) {
            throw new IllegalStateException("OpenIdContext is not available if no @OpenIdAuthenticationMechanismDefinition is defined");
        }
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (!(credential instanceof TomEEOpenIdCredential openIdCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        String callerNameClaim = definition.get().get().claimsDefinition().callerNameClaim();
        String groupsClaim = definition.get().get().claimsDefinition().callerGroupsClaim();

        String callerName = null;
        List<String> groups = Collections.emptyList();

        if (openIdContext.getAccessToken().isJWT()) {
            callerName = openIdContext.getAccessToken().getJwtClaims().getStringClaim(callerNameClaim).orElse(null);
            groups = openIdContext.getAccessToken().getJwtClaims().getArrayStringClaim(groupsClaim);
        }

        if (callerName == null) {
            callerName = openIdContext.getIdentityToken().getJwtClaims().getStringClaim(callerNameClaim).orElse(null);
        }

        if (groups.isEmpty()) {
            groups = openIdContext.getIdentityToken().getJwtClaims().getArrayStringClaim(groupsClaim);
        }

        if (callerName == null) {
            callerName = openIdContext.getClaims().getStringClaim(callerNameClaim).orElse(null);
        }

        if (groups.isEmpty()) {
            groups = openIdContext.getClaims().getArrayStringClaim(groupsClaim);
        }

        if (callerName == null) {
            callerName = openIdContext.getSubject();
        }

        return new CredentialValidationResult(callerName, new HashSet<>(groups));
    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        return validationResult.getCallerGroups();
    }
}
