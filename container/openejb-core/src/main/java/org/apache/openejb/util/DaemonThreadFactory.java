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
package org.apache.openejb.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static org.apache.openejb.util.Join.join;

/**
* @version $Rev$ $Date$
*/
public class DaemonThreadFactory implements ThreadFactory {

    private final String name;
    private AtomicInteger ids = new AtomicInteger(0);

    public DaemonThreadFactory(Object... name) {
        this.name = join(" ", name).trim();
    }

    public DaemonThreadFactory(Class... clazz) {
        this(asStrings(clazz));
    }

    private static Object[] asStrings(Class[] clazz) {
        final String[] strings = new String[clazz.length];
        int i = 0;
        for (Class c : clazz) {
            strings[i++] = c.getSimpleName();
        }
        return strings;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable, name + " - " + ids.incrementAndGet());
        t.setDaemon(true);
        return t;
    }
}
