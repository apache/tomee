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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.assembler.spring;

/**
 * @org.apache.xbean.XBean element="assembly"
 */
public class AssemblyInfo {
    MethodPermissionInfo[] methodPermissions;
    public MethodTransactionInfo[] methodTransactions;
    public RoleMapping[] roleMappings;

    /**
     * @org.apache.xbean.FlatCollection childElement="permission"
     */
    public MethodPermissionInfo[] getMethodPermissions() {
        return methodPermissions;
    }

    public void setMethodPermissions(MethodPermissionInfo[] methodPermissions) {
        this.methodPermissions = methodPermissions;
    }

    /**
     * @org.apache.xbean.FlatCollection childElement="transaction"
     */
    public MethodTransactionInfo[] getMethodTransactions() {
        return methodTransactions;
    }

    public void setMethodTransactions(MethodTransactionInfo[] methodTransactions) {
        this.methodTransactions = methodTransactions;
    }

    /**
     * @org.apache.xbean.FlatCollection childElement="roleMapping"
     */
    public RoleMapping[] getRoleMappings() {
        return roleMappings;
    }

    public void setRoleMappings(RoleMapping[] roleMappings) {
        this.roleMappings = roleMappings;
    }
}
