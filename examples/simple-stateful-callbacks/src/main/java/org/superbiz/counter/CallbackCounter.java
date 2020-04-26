/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.counter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.PostActivate;
import jakarta.ejb.PrePassivate;
import jakarta.ejb.Stateful;
import jakarta.ejb.StatefulTimeout;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Stateful
@StatefulTimeout(value = 1, unit = TimeUnit.SECONDS)
public class CallbackCounter implements Serializable {

    private int count = 0;

    @PrePassivate
    public void prePassivate() {
        ExecutionChannel.getInstance().notifyObservers("prePassivate");
    }

    @PostActivate
    public void postActivate() {
        ExecutionChannel.getInstance().notifyObservers("postActivate");
    }

    @PostConstruct
    public void postConstruct() {
        ExecutionChannel.getInstance().notifyObservers("postConstruct");
    }

    @PreDestroy
    public void preDestroy() {
        ExecutionChannel.getInstance().notifyObservers("preDestroy");
    }

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        ExecutionChannel.getInstance().notifyObservers(ctx.getMethod().getName());
        return ctx.proceed();
    }

    public int count() {
        return count;
    }

    public int increment() {
        return ++count;
    }

    public int reset() {
        return (count = 0);
    }
}
