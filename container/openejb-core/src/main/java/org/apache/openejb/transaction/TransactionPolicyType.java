/**
 *
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
package org.apache.openejb.transaction;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Enumeration of abstract transaction policies, with ordinals, for use as index to determine concrete TransactionPolicies.
 *
 * @version $Rev$ $Date$
 */
public class TransactionPolicyType implements Serializable {
    private static final long serialVersionUID = 613104784490732601L;
    private static HashMap byName = new HashMap();

    public static final TransactionPolicyType NotSupported = new TransactionPolicyType("NotSupported");
    public static final TransactionPolicyType Required = new TransactionPolicyType("Required");
    public static final TransactionPolicyType Supports = new TransactionPolicyType("Supports");
    public static final TransactionPolicyType RequiresNew = new TransactionPolicyType("RequiresNew");
    public static final TransactionPolicyType Mandatory = new TransactionPolicyType("Mandatory");
    public static final TransactionPolicyType Never = new TransactionPolicyType("Never");
    public static final TransactionPolicyType Bean = new TransactionPolicyType("Bean");

    private static final TransactionPolicyType[] values = {
        NotSupported,
        Required,
        Supports,
        RequiresNew,
        Mandatory,
        Never,
        Bean
    };

    private final int index;
    private final String name;
    private static int last = 0;

    private TransactionPolicyType(String name) {
        this.name = name;
        this.index = last++;
        byName.put(name, this);
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public static int size() {
        return values.length;
    }

    public String toString() {
        return name;
    }

    public static TransactionPolicyType getByName(String name) {
        return (TransactionPolicyType) byName.get(name);
    }

    protected Object readResolve() {
        return values[index];
    }
}
