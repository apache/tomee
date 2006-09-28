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

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import org.apache.openejb.transaction.TransactionPolicyType;

/**
 * @version $Revision$ $Date$
 */
public class MethodTransaction implements Comparable {

    private static final int AFTER_OTHER = 1;
    private static final int BEFORE_OTHER = -1;
    private final static Map transactionPolicyMap;

    static {
        transactionPolicyMap = new HashMap();
        transactionPolicyMap.put("NotSupported", TransactionPolicyType.NotSupported);
        transactionPolicyMap.put("Required", TransactionPolicyType.Required);
        transactionPolicyMap.put("Supports", TransactionPolicyType.Supports);
        transactionPolicyMap.put("RequiresNew", TransactionPolicyType.RequiresNew);
        transactionPolicyMap.put("Mandatory", TransactionPolicyType.Mandatory);
        transactionPolicyMap.put("Never", TransactionPolicyType.Never);
        transactionPolicyMap.put("Stateless", TransactionPolicyType.Bean);
        transactionPolicyMap.put("Stateful", TransactionPolicyType.Bean);
    }

    private final TransactionPolicyType transactionPolicyType;
    private final String methodIntf;
    private final String methodName;
    private final String[] parameterTypes;

    public MethodTransaction(TransactionPolicyType transactionPolicyType, String methodIntf, String methodName, String[] parameterTypes) {
        this.transactionPolicyType = transactionPolicyType;
        this.methodIntf = methodIntf;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
    }

    public TransactionPolicyType getTransactionPolicyType() {
        return transactionPolicyType;
    }

    public String getMethodIntf() {
        return methodIntf;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public int compareTo(Object o) {
        if (!(o instanceof MethodTransaction)) {
            return -1;
        }
        if (this == o) {
            return 0;
        }
        MethodTransaction other = (MethodTransaction) o;
        if (parameterTypes != null) {
            if (other.parameterTypes == null) {
                //parameter types always come before no param types
                return BEFORE_OTHER;
            }
            //both have param types
            if (methodIntf != null) {
                if (other.methodIntf == null) {
                    //method intf comes before no method intf.
                    return BEFORE_OTHER;
                }
                //both have method interfaces
                int intfOrder = methodIntf.compareTo(other.methodIntf);
                if (intfOrder != 0) {
                    return intfOrder;
                }
                //same interfaces
                return compareMethod(other);
            }
            if (other.methodIntf != null) {
                //they have method intf, we don't, they are first
                return AFTER_OTHER;
            }
            //neither has methodIntf: sort by method name
            return compareMethod(other);
        }
        //we don't have param types
        if (other.parameterTypes != null) {
            //they do, they are first
            return AFTER_OTHER;
        }
        //neither has param types.
        //explicit method name comes first
        if (!methodName.equals("*")) {
            if (other.methodName.equals("*")) {
                return BEFORE_OTHER;
            }
            //both explicit method names.
            //explicit method interface comes first
            if (methodIntf != null) {
                if (other.methodIntf == null) {
                    return BEFORE_OTHER;
                }
                //both explicit method intf. sort by intf, then methodName
                int intfOrder = methodIntf.compareTo(other.methodIntf);
                if (intfOrder != 0) {
                    return intfOrder;
                }
                //same interfaces
                return methodName.compareTo(other.methodName);
            }
            if (other.methodIntf != null) {
                //they have explicit method inft, we dont, they are first
                return AFTER_OTHER;
            }
            //neither have explicit method intf.
            return methodName.compareTo(other.methodName);
        }
        //we don't have explicit method name
        if (!other.methodName.equals("*")) {
            //they do, they are first
            return AFTER_OTHER;
        }
        //neither has explicit method name
        if (methodIntf != null) {
            if (other.methodIntf == null) {
                return BEFORE_OTHER;
            }
            return methodIntf.compareTo(other.methodIntf);
        }
        if (other.methodIntf != null) {
            return AFTER_OTHER;
        }
        //neither has methodIntf or explicit methodName.  problem.
        throw new IllegalStateException("Cannot compare " + this + " and " + other);
    }

    private int compareMethod(MethodTransaction other) {
        int methodOrder = methodName.compareTo(other.methodName);
        if (methodOrder != 0) {
            return methodOrder;
        }
        //same method name, sort by params lexicographically
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i == other.parameterTypes.length) {
                //the other list is shorter, they are first
                return AFTER_OTHER;
            }
            int paramOrder = parameterTypes[i].compareTo(other.parameterTypes[i]);
            if (paramOrder != 0) {
                return paramOrder;
            }
        }
        //our list is shorter, we are first
        return BEFORE_OTHER;
    }

    public String toString() {
        StringBuffer result = new StringBuffer("MethodTransaction: interface ").append(methodIntf).append(", methodName ").append(methodName).append(", parameters: ");
        if (parameterTypes != null) {
            for (int i = 0; i < parameterTypes.length; i++) {
                String parameterType = parameterTypes[i];
                result.append(parameterType).append(", ");
            }
        }
        result.append("transaction attribute: ").append(transactionPolicyType);
        return result.toString();
    }

    public boolean matches(String methodIntf, String methodName, String[] parameterTypes) {
        assert methodIntf != null;
        assert methodName != null;
        assert parameterTypes != null;
        if (this.methodIntf != null && !methodIntf.equals(this.methodIntf)) {
            //definitely wrong interface
            return false;
        }
        //our interface is not specified or matches.
        if (this.methodName.equals("*")) {
            return true;
        }
        if (!methodName.equals(this.methodName)) {
            //explicitly different method names
            return false;
        }
        //same method names.
        if (this.parameterTypes == null) {
            return true;
        }
        return Arrays.equals(parameterTypes, this.parameterTypes);
    }

}
