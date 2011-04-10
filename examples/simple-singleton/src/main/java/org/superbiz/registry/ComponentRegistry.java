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
package org.superbiz.registry;

//START SNIPPET: code

import javax.ejb.Lock;
import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static javax.ejb.LockType.READ;
import static javax.ejb.LockType.WRITE;

@Singleton
@Lock(READ)
public class ComponentRegistry {

    private final Map<Class, Object> components = new HashMap<Class, Object>();

    public <T> T getComponent(Class<T> type) {
        return (T) components.get(type);
    }

    public Collection<?> getComponents() {
        return new ArrayList(components.values());
    }

    @Lock(WRITE)
    public <T> T setComponent(Class<T> type, T value) {
        return (T) components.put(type, value);
    }

    @Lock(WRITE)
    public <T> T removeComponent(Class<T> type) {
        return (T) components.remove(type);
    }

}
//END SNIPPET: code
