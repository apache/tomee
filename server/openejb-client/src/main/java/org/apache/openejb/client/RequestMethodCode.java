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
package org.apache.openejb.client;

import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public enum RequestMethodCode {
    EJB_HOME_GET_EJB_META_DATA(1),
    EJB_HOME_GET_HOME_HANDLE(2),
    EJB_HOME_REMOVE_BY_HANDLE(3),
    EJB_HOME_REMOVE_BY_PKEY(4),

    EJB_HOME_FIND(9),
    EJB_HOME_CREATE(10),

    EJB_OBJECT_GET_EJB_HOME(14),
    EJB_OBJECT_GET_HANDLE(15),
    EJB_OBJECT_GET_PRIMARY_KEY(16),
    EJB_OBJECT_IS_IDENTICAL(17),
    EJB_OBJECT_REMOVE(18),

    EJB_OBJECT_BUSINESS_METHOD(23),
    EJB_HOME_METHOD(24),

    JNDI_LOOKUP(27),
    JNDI_LIST(28),
    JNDI_LIST_BINDINGS(29),

    FUTURE_CANCEL(35);

    private final int code;
    private final static Map<Integer, RequestMethodCode> ENUM_MAP = new HashMap<Integer, RequestMethodCode>();

    static {
        for (RequestMethodCode e : RequestMethodCode.values()) ENUM_MAP.put(e.code, e);
    }

    private RequestMethodCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RequestMethodCode valueOf(int key) {
        RequestMethodCode result = ENUM_MAP.get(key);
        if (result == null) throw new IllegalArgumentException();
        return result;
    }
}
