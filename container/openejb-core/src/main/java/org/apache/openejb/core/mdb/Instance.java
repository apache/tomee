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

package org.apache.openejb.core.mdb;

import org.apache.openejb.util.Pool;

import javax.enterprise.context.spi.CreationalContext;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class Instance {
    public final Object bean;
    public final Map<String, Object> interceptors;
    public final CreationalContext creationalContext;

    private Pool<Instance>.Entry poolEntry;

    public Instance(final Object bean, final Map<String, Object> interceptors, final CreationalContext creationalContext) {
        this.bean = bean;
        this.interceptors = interceptors;
        this.creationalContext = creationalContext;
    }

    public Pool<Instance>.Entry getPoolEntry() {
        return poolEntry;
    }

    public void setPoolEntry(final Pool<Instance>.Entry poolEntry) {
        this.poolEntry = poolEntry;
    }
}
