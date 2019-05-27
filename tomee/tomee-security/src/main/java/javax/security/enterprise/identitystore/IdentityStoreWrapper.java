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

import javax.security.enterprise.credential.Credential;
import java.util.Set;

public class IdentityStoreWrapper implements IdentityStore {
    private final IdentityStore identityStore;

    public IdentityStoreWrapper(IdentityStore identityStore) {
        this.identityStore = identityStore;
    }

    public IdentityStore getWrapped() {
        return identityStore;
    }

    @Override
    public CredentialValidationResult validate(Credential credential) {
        return getWrapped().validate(credential);
    }

    @Override
    public Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        return getWrapped().getCallerGroups(validationResult);
    }

    @Override
    public int priority() {
        return getWrapped().priority();
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return getWrapped().validationTypes();
    }
}
