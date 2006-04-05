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
package org.openejb.transaction;

import java.io.Serializable;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.Iterator;
import java.util.Map;

import org.openejb.EJBInterfaceType;
import org.openejb.MethodSpec;
import org.openejb.dispatch.InterfaceMethodSignature;

/**
 * @version $Revision$ $Date$
 */
public final class TransactionPolicyManager implements Serializable {
    private static final long serialVersionUID = -2039826921336518779L;
    private final TransactionPolicyType[][] transactionPolicyType;
    private final boolean beanManaged;

    public TransactionPolicyManager(boolean beanManaged, SortedMap transactionPolicies, InterfaceMethodSignature[] signatures) {
        this.beanManaged = beanManaged;
        transactionPolicyType = buildTransactionPolicyIndex(transactionPolicies, signatures);
    }

    public TransactionPolicyManager(TransactionPolicyType[][] transactionPolicyType) {
        this.transactionPolicyType = transactionPolicyType;
        beanManaged = false;
    }

    public TransactionPolicy getTransactionPolicy(EJBInterfaceType invocationType, int operationIndex) {
        TransactionPolicyType transactionPolicyType = getTransactionPolicyType(invocationType, operationIndex);
        TransactionPolicy transactionPolicy = TransactionPolicies.getTransactionPolicy(transactionPolicyType);
        return transactionPolicy;
    }

    public TransactionPolicyType getTransactionPolicyType(EJBInterfaceType invocationType, int operationIndex) {
        if (beanManaged) {
            return TransactionPolicyType.Bean;
        } else {
            return transactionPolicyType[invocationType.getOrdinal()][operationIndex];
        }
    }

    private static TransactionPolicyType[][] buildTransactionPolicyIndex(SortedMap transactionPolicies, InterfaceMethodSignature[] signatures) {
        TransactionPolicyType[][] transactionPolicyType = new TransactionPolicyType[EJBInterfaceType.MAX_ORDINAL][];
        transactionPolicyType[EJBInterfaceType.HOME.getOrdinal()] = mapPolicies("Home", signatures, transactionPolicies);
        transactionPolicyType[EJBInterfaceType.REMOTE.getOrdinal()] = mapPolicies("Remote", signatures, transactionPolicies);
        transactionPolicyType[EJBInterfaceType.LOCALHOME.getOrdinal()] = mapPolicies("LocalHome", signatures, transactionPolicies);
        transactionPolicyType[EJBInterfaceType.LOCAL.getOrdinal()] = mapPolicies("Local", signatures, transactionPolicies);
        transactionPolicyType[EJBInterfaceType.WEB_SERVICE.getOrdinal()] = mapPolicies("ServiceEndpoint", signatures, transactionPolicies);
        transactionPolicyType[EJBInterfaceType.TIMEOUT.getOrdinal()] = new TransactionPolicyType[signatures.length];
        Arrays.fill(transactionPolicyType[EJBInterfaceType.TIMEOUT.getOrdinal()], TransactionPolicyType.Supports); //we control the transaction from the top of the stack.

        return transactionPolicyType;
    }

    private static TransactionPolicyType[] mapPolicies(String intfName, InterfaceMethodSignature[] signatures, SortedMap transactionPolicies) {
        TransactionPolicyType[] policies = new TransactionPolicyType[signatures.length];
        for (int index = 0; index < signatures.length; index++) {
            InterfaceMethodSignature signature = signatures[index];
            policies[index] = getTransactionPolicy(transactionPolicies, intfName, signature);
        }
        return policies;
    }

    public static TransactionPolicyType getTransactionPolicy(SortedMap transactionPolicies, String methodIntf, InterfaceMethodSignature signature) {
        if (transactionPolicies != null) {
            for (Iterator iterator = transactionPolicies.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry entry = (Map.Entry) iterator.next();
                MethodSpec methodSpec = (MethodSpec) entry.getKey();
                TransactionPolicyType transactionPolicyType = (TransactionPolicyType) entry.getValue();

                if (methodSpec.matches(methodIntf, signature.getMethodName(), signature.getParameterTypes())) {
                    return transactionPolicyType;
                }
            }
        }

        //default
        return TransactionPolicyType.Required;
    }
}
