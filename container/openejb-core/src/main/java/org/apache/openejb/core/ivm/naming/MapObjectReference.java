/*
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

package org.apache.openejb.core.ivm.naming;

import javax.naming.NamingException;
import java.util.Map;

public class MapObjectReference extends Reference {

    private final ThreadLocal<Map<String, Object>> obj;
    private final String key;

    public MapObjectReference(final ThreadLocal<Map<String, Object>> obj, final String key) {
        this.obj = obj;
        this.key = key;
    }

    public Object getObject() throws NamingException {
        final Map<String, Object> theMap = obj.get();
        return theMap == null ? null : theMap.get(key);
    }
}
