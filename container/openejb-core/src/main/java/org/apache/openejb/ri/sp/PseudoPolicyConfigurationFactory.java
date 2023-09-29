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

package org.apache.openejb.ri.sp;

import jakarta.security.jacc.PolicyContext;
import org.apache.openejb.util.JavaSecurityManagers;

import jakarta.security.jacc.PolicyConfiguration;
import jakarta.security.jacc.PolicyConfigurationFactory;
import jakarta.security.jacc.PolicyContextException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class PseudoPolicyConfigurationFactory extends PolicyConfigurationFactory {

    public static void install() {
        JavaSecurityManagers.setSystemProperty("jakarta.security.jacc.PolicyConfigurationFactory.provider", PseudoPolicyConfigurationFactory.class.getName());
    }

    public PolicyConfiguration getPolicyConfiguration(final String contextID, final boolean remove) throws PolicyContextException {
        return new DummyPolicyConfiguration(contextID);
    }

    @Override
    public PolicyConfiguration getPolicyConfiguration(final String contextID) {
        return new DummyPolicyConfiguration(contextID);
    }

    @Override
    public PolicyConfiguration getPolicyConfiguration() {
        final String contextID = PolicyContext.getContextID();
        if (contextID == null) {
            return null;
        }
        return new DummyPolicyConfiguration(contextID);
    }

    public boolean inService(final String contextID) throws PolicyContextException {
        return true;
    }

    private static class DummyPolicyConfiguration implements PolicyConfiguration {
        private final String contextID;

        public DummyPolicyConfiguration(final String contextID) {this.contextID = contextID;}

        public String getContextID() throws PolicyContextException {
            return contextID;
        }

        public void addToRole(final String roleName, final PermissionCollection permissions) throws PolicyContextException {
        }

        public void addToRole(final String roleName, final Permission permission) throws PolicyContextException {
        }

        public void addToUncheckedPolicy(final PermissionCollection permissions) throws PolicyContextException {
        }

        public void addToUncheckedPolicy(final Permission permission) throws PolicyContextException {
        }

        public void addToExcludedPolicy(final PermissionCollection permissions) throws PolicyContextException {
        }

        public void addToExcludedPolicy(final Permission permission) throws PolicyContextException {
        }

        @Override
        public Map<String, PermissionCollection> getPerRolePermissions() {
            return null;
        }

        @Override
        public PermissionCollection getUncheckedPermissions() {
            return null;
        }

        @Override
        public PermissionCollection getExcludedPermissions() {
            return null;
        }

        public void removeRole(final String roleName) throws PolicyContextException {
        }

        public void removeUncheckedPolicy() throws PolicyContextException {
        }

        public void removeExcludedPolicy() throws PolicyContextException {
        }

        public void linkConfiguration(final PolicyConfiguration link) throws PolicyContextException {
        }

        public void delete() throws PolicyContextException {
        }

        public void commit() throws PolicyContextException {
        }

        public boolean inService() throws PolicyContextException {
            return false;
        }
    }
}
