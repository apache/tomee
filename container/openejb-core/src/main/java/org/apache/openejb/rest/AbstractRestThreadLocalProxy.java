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

package org.apache.openejb.rest;

import org.apache.openejb.loader.SystemInstance;

import java.io.Serializable;

public class AbstractRestThreadLocalProxy<T> implements Serializable {

    private final ThreadLocal<T> infos = new ThreadLocal<>();
    private final Class<T> clazz;

    protected AbstractRestThreadLocalProxy(final Class<T> clazz) {
        this.clazz = clazz;
    }

    public T get() {
        T t = infos.get();
        if (t == null) {
            t = find();
        }
        return t;
    }

    public T find() {
        final RESTResourceFinder finder = SystemInstance.get().getComponent(RESTResourceFinder.class);
        if (finder != null) {
            return finder.find(clazz);
        }
        return null;
    }

    public void remove() {
        infos.remove();
    }

    public void set(final T value) {
        infos.set(value);
    }
}

