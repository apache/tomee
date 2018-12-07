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

package org.apache.openejb.core.security.jacc;

import org.apache.openejb.core.security.JaccProvider;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class BasicJaccProvider extends JaccProvider {
    static {
        // force preloading to avoid to loop under SecurityManager
        try {
            Class.forName(PolicyContext.class.getName());
        } catch (final ClassNotFoundException e) {
            // no-op
        }
    }

    private final Map<String, BasicPolicyConfiguration> configurations = new HashMap<>();

    private final java.security.Policy systemPolicy;

    public BasicJaccProvider() {
        systemPolicy = Policy.getPolicy();
    }

    public PolicyConfiguration getPolicyConfiguration(final String contextID, final boolean remove) throws PolicyContextException {
        BasicPolicyConfiguration configuration = configurations.get(contextID);

        if (configuration == null) {
            configuration = createPolicyConfiguration(contextID);
            configurations.put(contextID, configuration);
        } else {
            configuration.open(remove);
        }

        return configuration;
    }

    protected BasicPolicyConfiguration createPolicyConfiguration(final String contextID) {
        return new BasicPolicyConfiguration(contextID);
    }

    public boolean inService(final String contextID) throws PolicyContextException {
        final PolicyConfiguration configuration = getPolicyConfiguration(contextID, false);
        return configuration.inService();
    }

    public PermissionCollection getPermissions(final CodeSource codesource) {
        return systemPolicy == null ? null : systemPolicy.getPermissions(codesource);
    }

    public void refresh() {
    }

    public boolean implies(final ProtectionDomain domain, final Permission permission) {
        final String contextID = PolicyContext.getContextID();

        if (contextID != null) {
            try {
                final BasicPolicyConfiguration configuration = configurations.get(contextID);

                if (configuration == null || !configuration.inService()) {
                    return false;
                }

                return configuration.implies(domain, permission);
            } catch (final PolicyContextException e) {
                // no-op
            }
        }

        return systemPolicy != null ? systemPolicy.implies(domain, permission) : false;
    }
}
