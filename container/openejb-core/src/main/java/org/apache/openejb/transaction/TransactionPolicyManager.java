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
package org.apache.openejb.transaction;

import java.io.Serializable;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.MethodSpec;
import org.apache.openejb.dispatch.InterfaceMethodSignature;

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
