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
import java.lang.invoke.MethodHandles;
import java.util.EnumSet;
import java.util.Set;

import static java.lang.invoke.MethodType.methodType;
import static java.util.Collections.emptySet;
import static javax.security.enterprise.identitystore.CredentialValidationResult.NOT_VALIDATED_RESULT;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;

public interface IdentityStore {
    Set<ValidationType> DEFAULT_VALIDATION_TYPES = EnumSet.of(VALIDATE, PROVIDE_GROUPS);

    default CredentialValidationResult validate(Credential credential) {
        try {
            return CredentialValidationResult.class.cast(
                    MethodHandles.lookup()
                                 .bind(this, "validate", methodType(CredentialValidationResult.class, credential.getClass()))
                                 .invoke(credential));
        } catch (NoSuchMethodException e) {
            return NOT_VALIDATED_RESULT;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    default Set<String> getCallerGroups(CredentialValidationResult validationResult) {
        return emptySet();
    }

    default int priority() {
        return 100;
    }

    default Set<ValidationType> validationTypes() {
        return DEFAULT_VALIDATION_TYPES;
    }

    enum ValidationType {
        VALIDATE, PROVIDE_GROUPS
    }
}
