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

import javax.security.enterprise.identitystore.IdentityStore.ValidationType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.PROVIDE_GROUPS;
import static javax.security.enterprise.identitystore.IdentityStore.ValidationType.VALIDATE;

@Retention(RUNTIME)
@Target(TYPE)
public @interface DatabaseIdentityStoreDefinition {
    String dataSourceLookup() default "java:comp/DefaultDataSource";

    String callerQuery() default "";

    String groupsQuery() default "";

    Class<? extends PasswordHash> hashAlgorithm() default Pbkdf2PasswordHash.class;

    String[] hashAlgorithmParameters() default {};

    int priority() default 70;

    String priorityExpression() default "";

    ValidationType[] useFor() default {
            VALIDATE,
            PROVIDE_GROUPS
    };

    String useForExpression() default "";
}
