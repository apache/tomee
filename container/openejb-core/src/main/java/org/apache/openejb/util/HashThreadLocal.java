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
package org.apache.openejb.util;

import java.util.HashMap;

/*
* This variation of ThreadLocal accomplishes thread-specific storage by thread as well
* as by object.  Values are associated with both an key and a thread, which allows 
* each value to stored specific to an object and thread. 
*
* @see org.apache.openejb.resource.SharedLocalConnectionManager
* @author <a href="richard@monson-haefel.com">Richard Monson-Haefel</a>
* @version $Rev$ $Id$
*/

public class HashThreadLocal {
    HashMap keyMap = new HashMap();

    public synchronized void put(Object key, Object value) {
        FastThreadLocal threadLocal = (FastThreadLocal) keyMap.get(key);
        if (threadLocal == null) {
            threadLocal = new FastThreadLocal();
            keyMap.put(key, threadLocal);
        }
        threadLocal.set(value);
    }

    public synchronized Object get(Object key) {
        FastThreadLocal threadLocal = (FastThreadLocal) keyMap.get(key);
        if (threadLocal == null) return null;
        return threadLocal.get();
    }
}