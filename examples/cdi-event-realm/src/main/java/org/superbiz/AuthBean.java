/**
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
package org.superbiz;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.tomee.catalina.realm.event.UserPasswordAuthenticationEvent;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import java.util.Arrays;

@RequestScoped
public class AuthBean {

    public void authenticate(@Observes final UserPasswordAuthenticationEvent event) {
        final String username = event.getUsername();
        final String password = event.getCredential();

        if (!"secret".equals(password)) return;

        if ("userA".equals(username)) {
            event.setPrincipal(new GenericPrincipal(username, "", Arrays.asList("admin", "user")));

        } else if ("userB".equals(username)) {
            event.setPrincipal(new GenericPrincipal(username, "", Arrays.asList("user")));

        }

        // no else, the user is not going to be authenticated
    }

}
