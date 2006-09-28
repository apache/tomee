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


/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class EJBOperation {
    public static final EJBOperation INACTIVE = new EJBOperation(0, "INACTIVE");
    public static final EJBOperation SETCONTEXT = new EJBOperation(1, "SETCONTEXT");
    public static final EJBOperation EJBCREATE = new EJBOperation(2, "EJBCREATE");
    public static final EJBOperation EJBPOSTCREATE = new EJBOperation(3, "EJBPOSTCREATE");
    public static final EJBOperation EJBREMOVE = new EJBOperation(4, "EJBREMOVE");
    public static final EJBOperation EJBACTIVATE = new EJBOperation(5, "EJBACTIVATE");
    public static final EJBOperation EJBLOAD = new EJBOperation(6, "EJBLOAD");
    public static final EJBOperation BIZMETHOD = new EJBOperation(7, "BIZMETHOD");
    public static final EJBOperation ENDPOINT = new EJBOperation(8, "ENDPOINT");
    public static final EJBOperation TIMEOUT = new EJBOperation(9, "TIMEOUT");
    public static final EJBOperation EJBFIND = new EJBOperation(10, "EJBFIND");
    public static final EJBOperation EJBHOME = new EJBOperation(11, "EJBHOME");

    private static final EJBOperation[] values = {
        INACTIVE, SETCONTEXT, EJBCREATE, EJBPOSTCREATE, EJBREMOVE,
        EJBACTIVATE, EJBLOAD, BIZMETHOD, ENDPOINT, TIMEOUT,
        EJBFIND, EJBHOME
    };

    public static final int MAX_ORDINAL = values.length;

    private final int ordinal;
    private final String description;

    private EJBOperation(int ordinal, String description) {
        this.ordinal = ordinal;
        this.description = description;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public String toString() {
        return description;
    }
}
