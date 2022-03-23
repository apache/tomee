/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz;

import jakarta.enterprise.context.RequestScoped;
import java.security.Principal;

@RequestScoped // just to show we can be bound to the request but @ApplicationScoped is what makes sense
public class AuthBean {
    public Principal authenticate(final String username, String password) {
        if (("userA".equals(username) || "userB".equals(username)) && "test".equals(password)) {
            return new Principal() {
                @Override
                public String getName() {
                    return username;
                }

                @Override
                public String toString() {
                    return username;
                }
            };
        }
        return null;
    }

    public boolean hasRole(final Principal principal, final String role) {
        return principal != null && (
                principal.getName().equals("userA") && (role.equals("admin")
                        || role.equals("user"))
                        || principal.getName().equals("userB") && (role.equals("user"))
        );
    }
}
