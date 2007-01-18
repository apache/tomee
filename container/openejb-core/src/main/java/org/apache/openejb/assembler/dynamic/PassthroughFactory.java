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
package org.apache.openejb.assembler.dynamic;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.openejb.assembler.classic.ServiceInfo;

public class PassthroughFactory {
    private static final Map<String,Object> values = new ConcurrentHashMap<String,Object>();
    private static final AtomicInteger sequence = new AtomicInteger();

    public static void add(ServiceInfo info, Object item) {
        info.className = PassthroughFactory.class.getName();
        info.constructorArgs.add("id");
        info.factoryMethod = "create";
        info.properties = new Properties();

        String id = add(item);
        info.properties.setProperty("id", id);
    }

    public static String add(Object item) {
        String id = Integer.toString(sequence.getAndIncrement());
        values.put(id, item);
        return id;
    }

    public static void remove(ServiceInfo info) {
        String id = info.properties.getProperty("id");
        remove(id);
    }

    public static void remove(String id) {
        values.remove(id);
    }

    public static Object create(String id) {
        return values.get(id);
    }
}
