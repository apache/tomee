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
package org.apache.tomee.embedded;

import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

public class SecurityConstaintBuilder {
    private final SecurityConstraint securityConstraint = new SecurityConstraint();

    public SecurityConstaintBuilder authConstraint(final boolean authConstraint) {
        securityConstraint.setAuthConstraint(authConstraint);
        return this;
    }

    public SecurityConstaintBuilder setDisplayName(final String displayName) {
        securityConstraint.setDisplayName(displayName);
        return this;
    }

    public SecurityConstaintBuilder setUserConstraint(final String userConstraint) {
        securityConstraint.setUserConstraint(userConstraint);
        return this;
    }

    public SecurityConstaintBuilder addAuthRole(final String authRole) {
        securityConstraint.addAuthRole(authRole);
        return this;
    }

    public SecurityConstaintBuilder addCollection(final String name, final String pattern, final String... methods) {
        final SecurityCollection collection = new SecurityCollection();
        collection.setName(name);
        collection.addPattern(pattern);
        for (final String httpMethod : methods) {
            collection.addMethod(httpMethod);
        }
        securityConstraint.addCollection(collection);
        return this;
    }

    public SecurityConstraint build() {
        return securityConstraint;
    }
}
