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
package org.apache.openejb.security;

import java.io.Serializable;
import java.security.Permission;
import javax.security.jacc.EJBMethodPermission;

import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.dispatch.InterfaceMethodSignature;


/**
 * Create a sparse matrix of pre-created EJB permissions.
 * <p/>
 * TODO: This matrix isn't sparse enough for the likes of certain cheeseheads.
 *
 * @version $Revision$ $Date$
 */
public final class PermissionManager implements Serializable {
    private final Permission[][] permissions = new Permission[EJBInterfaceType.MAX_ORDINAL][];

    public PermissionManager(String ejbName, InterfaceMethodSignature[] signatures) {
        permissions[EJBInterfaceType.HOME.getOrdinal()] = mapPermissions(ejbName, "Home", signatures);
        permissions[EJBInterfaceType.REMOTE.getOrdinal()] = mapPermissions(ejbName, "Remote", signatures);
        permissions[EJBInterfaceType.LOCALHOME.getOrdinal()] = mapPermissions(ejbName, "LocalHome", signatures);
        permissions[EJBInterfaceType.LOCAL.getOrdinal()] = mapPermissions(ejbName, "Local", signatures);
        permissions[EJBInterfaceType.WEB_SERVICE.getOrdinal()] = mapPermissions(ejbName, "ServiceEndpoint", signatures);
    }

    /**
     * Return the permission for that invocation type and operation index.
     * Note that the permissions matrix is sparse and it may return null.
     *
     * @param invocationType the invocation type
     * @param operationIndex the operation index
     * @return
     */
    public Permission getPermission(EJBInterfaceType invocationType, int operationIndex) {
        Permission[] pArray = permissions[invocationType.getOrdinal()];

        if (pArray == null) return null;

        return pArray[operationIndex];
    }

    private static Permission[] mapPermissions(String ejbName, String intfName, InterfaceMethodSignature[] signatures) {
        Permission[] permissions = new Permission[signatures.length];
        for (int index = 0; index < signatures.length; index++) {
            InterfaceMethodSignature signature = signatures[index];
            permissions[index] = new EJBMethodPermission(ejbName, signature.getMethodName(), intfName, signature.getParameterTypes());
        }
        return permissions;
    }
}
