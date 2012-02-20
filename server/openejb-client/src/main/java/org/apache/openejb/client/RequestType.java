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
public enum RequestType {
    NOP_REQUEST((byte) -1),
    EJB_REQUEST((byte) 0),
    JNDI_REQUEST((byte) 1),
    AUTH_REQUEST((byte) 2),
    CLUSTER_REQUEST((byte) 3),
    STOP_REQUEST_Quit((byte) 'Q'),
    STOP_REQUEST_quit((byte) 'q'),
    STOP_REQUEST_Stop((byte) 'S'),
    STOP_REQUEST_stop((byte) 's');

    private final byte code;
    private final static Map<Byte, RequestType> ENUM_MAP = new HashMap<Byte, RequestType>();

    static {
        for (RequestType e : RequestType.values()) ENUM_MAP.put(e.code, e);
    }

    private RequestType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static RequestType valueOf(byte key) {
        RequestType result = ENUM_MAP.get(key);
        if (result == null) throw new IllegalArgumentException();
        return result;
    }
}
