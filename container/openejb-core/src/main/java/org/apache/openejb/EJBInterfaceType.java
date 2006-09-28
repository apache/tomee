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
package org.apache.openejb;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Type-safe enum describing
 *
 *
 * @version $Revision$ $Date$
 */
public final class EJBInterfaceType implements Serializable {
    private final transient String name;
    private final transient boolean local;
    private final transient int transactionPolicyKey;

    private EJBInterfaceType(String name, boolean local, int transactionPolicyKey) {
        this.name = name;
        this.local = local;
        this.transactionPolicyKey = transactionPolicyKey;
    }

    /**
     * Keep these in the same order since MethodHelper relies in the ordinal number of the enum.
     */
    public static final EJBInterfaceType REMOTE = new EJBInterfaceType("Remote", false, 0);
    public static final EJBInterfaceType HOME = new EJBInterfaceType("Home", false, 0);
    public static final EJBInterfaceType LOCAL = new EJBInterfaceType("Local", true, 1);
    public static final EJBInterfaceType LOCALHOME = new EJBInterfaceType("LocalHome", true, 1);
    public static final EJBInterfaceType WEB_SERVICE = new EJBInterfaceType("Web-Service", false, 2);
    public static final EJBInterfaceType TIMEOUT = new EJBInterfaceType("ejbTimeout", true, 3);
    //lifecycle should never go through tx interceptor, so -1 as index should produce an error if it tries.
    public static final EJBInterfaceType LIFECYCLE = new EJBInterfaceType("container-lifecycle", true, -1);

    private static final EJBInterfaceType[] VALUES = {
        REMOTE, HOME, LOCAL, LOCALHOME, WEB_SERVICE, TIMEOUT, LIFECYCLE
    };

    public static int MAX_ORDINAL = VALUES.length;

    public boolean isLocal() {
        return local;
    }

    public String toString() {
        return name;
    }

    public int getTransactionPolicyKey() {
        return transactionPolicyKey;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public static int getMaxTransactionPolicyKey() {
        return maxTransactionPolicyKey;
    }

    private static int nextOrdinal;
    private final int ordinal = nextOrdinal++;

    Object readResolve() throws ObjectStreamException {
        return VALUES[ordinal];
    }

    private static int maxTransactionPolicyKey = 0;

    // verify that all are defined and the ids match up
    static {
        assert (VALUES.length == nextOrdinal) : "VALUES is missing a value";
        for (int i = 0; i < VALUES.length; i++) {
            EJBInterfaceType value = VALUES[i];
            assert (value.ordinal == i) : "Ordinal mismatch for " + value;
            if (maxTransactionPolicyKey < value.transactionPolicyKey) {
                maxTransactionPolicyKey = value.transactionPolicyKey;
            }
        }
    }
}
