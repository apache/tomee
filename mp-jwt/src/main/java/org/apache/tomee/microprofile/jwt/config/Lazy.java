/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.microprofile.jwt.config;

import org.apache.bval.util.Validate;

import java.util.function.Supplier;

public class Lazy<T> implements Supplier<T> {
    private T value;
    private volatile Supplier<T> init;

    public Lazy(Supplier<T> init) {
        reset(init);
    }

    public Lazy<T> reset(Supplier<T> init) {
        this.init = Validate.notNull(init);
        return this;
    }

    public synchronized Lazy<T> reset(T value) {
        this.value = value;
        this.init = null;
        return this;
    }

    @Override
    public T get() {
        if (init != null) {
            synchronized (this) {
                if (init != null) {
                    value = init.get();
                    init = null;
                }
            }
        }
        return value;
    }
}
