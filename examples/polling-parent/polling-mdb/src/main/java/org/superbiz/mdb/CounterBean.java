/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.superbiz.mdb;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@Startup
public class CounterBean {
    private Map<Integer, AtomicInteger> logs = new TreeMap<>();

    public void add(Integer beanId) {
        if(!this.logs.containsKey(beanId)) {
            this.logs.put(beanId, new AtomicInteger(0));
        }
        this.logs.get(beanId).incrementAndGet();
    }

    public Map<Integer, AtomicInteger> getUsage() {
        Map<Integer, AtomicInteger> copy = new TreeMap<>();
        copy.putAll(this.logs);
        return copy;
    }
}

