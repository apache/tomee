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
package javax.security.enterprise.identitystore;


import javax.security.enterprise.CallerPrincipal;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.INVALID;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.NOT_VALIDATED;
import static javax.security.enterprise.identitystore.CredentialValidationResult.Status.VALID;

public class CredentialValidationResult {
    public static final CredentialValidationResult INVALID_RESULT = new CredentialValidationResult(INVALID);
    public static final CredentialValidationResult NOT_VALIDATED_RESULT = new CredentialValidationResult(NOT_VALIDATED);

    private final Status status;
    private final String storeId;
    private final String callerDn;
    private final String callerUniqueId;
    private final CallerPrincipal callerPrincipal;
    private final Set<String> groups;

    public enum Status {
        NOT_VALIDATED,
        INVALID,
        VALID
    }

    private CredentialValidationResult(Status status) {
        this(status, null, null, null, null, null);
    }

    public CredentialValidationResult(String callerName) {
        this(new CallerPrincipal(callerName), null);
    }

    public CredentialValidationResult(CallerPrincipal callerPrincipal) {
        this(callerPrincipal, null);
    }

    public CredentialValidationResult(String callerName, Set<String> groups) {
        this(new CallerPrincipal(callerName), groups);
    }

    public CredentialValidationResult(CallerPrincipal callerPrincipal, Set<String> groups) {
        this(null, callerPrincipal, null, null, groups);
    }

    public CredentialValidationResult(String storeId, String callerName, String callerDn, String callerUniqueId,
                                      Set<String> groups) {
        this(storeId, new CallerPrincipal(callerName), callerDn, callerUniqueId, groups);
    }

    public CredentialValidationResult(String storeId, CallerPrincipal callerPrincipal, String callerDn,
                                      String callerUniqueId, Set<String> groups) {
        this(VALID, storeId, callerPrincipal, callerDn, callerUniqueId, groups);
    }

    private CredentialValidationResult(Status status, String storeId, CallerPrincipal callerPrincipal, String callerDn,
                                       String callerUniqueId, Set<String> groups) {

        if (status != VALID && (storeId != null || callerPrincipal != null ||
                                callerDn != null || callerUniqueId != null || groups != null)) {
            throw new IllegalArgumentException("Bad status");
        }
        if (status == VALID && (callerPrincipal == null || callerPrincipal.getName().trim().isEmpty())) {
            throw new IllegalArgumentException("Null or empty CallerPrincipal");
        }

        this.status = status;
        this.storeId = storeId;
        this.callerPrincipal = callerPrincipal;
        this.callerDn = callerDn;
        this.callerUniqueId = callerUniqueId;
        this.groups = groups != null ? unmodifiableSet(new HashSet<>(groups)) : emptySet();
    }

    public Status getStatus() {
        return status;
    }

    public String getIdentityStoreId() {
        return storeId;
    }

    public CallerPrincipal getCallerPrincipal() {
        return callerPrincipal;
    }

    public String getCallerUniqueId() {
        return callerUniqueId;
    }

    public String getCallerDn() {
        return callerDn;
    }

    public Set<String> getCallerGroups() {
        return groups;
    }
}
