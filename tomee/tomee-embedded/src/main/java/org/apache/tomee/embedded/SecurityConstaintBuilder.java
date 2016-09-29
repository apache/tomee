/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    public SecurityConstaintBuilder displayName(final String displayName) {
        securityConstraint.setDisplayName(displayName);
        return this;
    }

    public SecurityConstaintBuilder userConstraint(final String constraint) {
        securityConstraint.setUserConstraint(constraint);
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

    public void setAuthConstraint(final boolean authConstraint) {
        securityConstraint.setAuthConstraint(authConstraint);
    }

    public void setDisplayName(final String displayName) {
        securityConstraint.setDisplayName(displayName);
    }

    public void setUserConstraint(final String userConstraint) {
        securityConstraint.setUserConstraint(userConstraint);
    }

    public void setAuthRole(final String authRole) { // easier for config
        addAuthRole(authRole);
    }

    // name:pattern:method1/method2
    public void setCollection(final String value) { // for config
        final String[] split = value.split(":");
        if (split.length != 3 && split.length != 2) {
            throw new IllegalArgumentException("Can't parse " + value + ", syntax is: name:pattern:method1/method2");
        }
        addCollection(split[0], split[1], split.length == 2 ? new String[0] : split[2].split("/"));
    }

    public SecurityConstraint build() {
        return securityConstraint;
    }
}
