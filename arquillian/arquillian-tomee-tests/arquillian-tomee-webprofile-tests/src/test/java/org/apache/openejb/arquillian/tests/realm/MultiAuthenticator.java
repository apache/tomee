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
package org.apache.openejb.arquillian.tests.realm;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.tomee.catalina.realm.event.UserPasswordAuthenticationEvent;

import java.util.Arrays;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;

@RequestScoped
public class MultiAuthenticator {
    private boolean stacked = false;

    public void authenticate(@Observes final UserPasswordAuthenticationEvent event) {
        if (!"secret".equals(event.getCredential())) return; // not authenticated
        event.setPrincipal(new GenericPrincipal(event.getUsername(), "", Arrays.asList(event.getUsername())));
    }

    public void stacked(@Observes final UserPasswordAuthenticationEvent event) {
        stacked = true;
    }

    public boolean isStacked() {
        return stacked;
    }
}
