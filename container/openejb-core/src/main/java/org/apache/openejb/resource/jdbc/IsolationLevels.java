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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.util.Join;

import java.util.Map;
import java.util.HashMap;

/**
 * @version $Rev$ $Date$
 */
public class IsolationLevels {

    private static Map<String,Integer> isolation = new HashMap<String,Integer>();
    static {
        isolation.put("NONE", 0);
        isolation.put("READ_COMMITTED", 2);
        isolation.put("READ_UNCOMMITTED", 1);
        isolation.put("REPEATABLE_READ", 4);
        isolation.put("SERIALIZABLE", 8);
    }

    public static int getIsolationLevel(String s) {
        if (!isolation.containsKey(s)) throw new IllegalArgumentException("No such transaction isolation level '"+s+"'.  Possible values are "+Join.join(", ", isolation.keySet()));
        int level = isolation.get(s);
        return level;
    }


}
