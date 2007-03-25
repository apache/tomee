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
package org.apache.openejb.core.security.jacc;

import org.apache.openejb.core.security.JaccProvider;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContext;
import java.security.PermissionCollection;
import java.security.CodeSource;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.Permission;
import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class BasicJaccProvider extends JaccProvider {

    private Map<String, BasicPolicyConfiguration> configurations = new HashMap<String, BasicPolicyConfiguration>();

    private final java.security.Policy systemPolicy;

    public BasicJaccProvider() {
        systemPolicy = Policy.getPolicy();
    }

    public PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException {
        BasicPolicyConfiguration configuration = (BasicPolicyConfiguration) configurations.get(contextID);

        if (configuration == null) {
            configuration = new BasicPolicyConfiguration(contextID);
            configurations.put(contextID, configuration);
        } else {
            configuration.open(remove);
        }

        return configuration;
    }

    public boolean inService(String contextID) throws PolicyContextException {
        PolicyConfiguration configuration = getPolicyConfiguration(contextID, false);
        return configuration.inService();
    }

    public PermissionCollection getPermissions(CodeSource codesource) {
        return systemPolicy == null ? null: systemPolicy.getPermissions(codesource);
    }

    public void refresh() {
    }

    public boolean implies(ProtectionDomain domain, Permission permission) {
        String contextID = PolicyContext.getContextID();

        if (contextID != null) {
            try {
                BasicPolicyConfiguration configuration = configurations.get(contextID);

                if (configuration == null || !configuration.inService()) return false;

                return configuration.implies(domain, permission);
            } catch (PolicyContextException e) {
            }
        }

        return (systemPolicy != null)? systemPolicy.implies(domain, permission): false;
    }
}
