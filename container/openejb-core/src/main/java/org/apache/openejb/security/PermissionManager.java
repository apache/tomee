/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
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
