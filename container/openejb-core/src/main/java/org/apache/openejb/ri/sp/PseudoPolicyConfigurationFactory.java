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
package org.apache.openejb.ri.sp;

import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import java.security.PermissionCollection;
import java.security.Permission;

/**
 * @version $Rev$ $Date$
 */
public class PseudoPolicyConfigurationFactory extends PolicyConfigurationFactory {

    public static void install() {
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", PseudoPolicyConfigurationFactory.class.getName()) ;
    }

    public PolicyConfiguration getPolicyConfiguration(final String contextID, boolean remove) throws PolicyContextException {
        return new PolicyConfiguration(){
            public String getContextID() throws PolicyContextException {
                return contextID;
            }

            public void addToRole(String roleName, PermissionCollection permissions) throws PolicyContextException {
            }

            public void addToRole(String roleName, Permission permission) throws PolicyContextException {
            }

            public void addToUncheckedPolicy(PermissionCollection permissions) throws PolicyContextException {
            }

            public void addToUncheckedPolicy(Permission permission) throws PolicyContextException {
            }

            public void addToExcludedPolicy(PermissionCollection permissions) throws PolicyContextException {
            }

            public void addToExcludedPolicy(Permission permission) throws PolicyContextException {
            }

            public void removeRole(String roleName) throws PolicyContextException {
            }

            public void removeUncheckedPolicy() throws PolicyContextException {
            }

            public void removeExcludedPolicy() throws PolicyContextException {
            }

            public void linkConfiguration(PolicyConfiguration link) throws PolicyContextException {
            }

            public void delete() throws PolicyContextException {
            }

            public void commit() throws PolicyContextException {
            }

            public boolean inService() throws PolicyContextException {
                return false;
            }
        };
    }

    public boolean inService(String contextID) throws PolicyContextException {
        return true;
    }
}
