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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.asyncpost;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

@Singleton
@Lock(LockType.READ)
public class SlowStarter {

    @EJB
    private Executor executor;

    private Future construct;

    private String color;
    private String shape;

    @PostConstruct
    private void construct() throws Exception {
        construct = executor.submit(new Callable() {
            @Override
            public Object call() throws Exception {
                Thread.sleep(SECONDS.toMillis(10));
                SlowStarter.this.color = "orange";
                SlowStarter.this.shape = "circle";
                return null;
            }
        });
    }

    @AroundTimeout
    @AroundInvoke
    private Object guaranteeConstructionComplete(InvocationContext context) throws Exception {
        construct.get();
        return context.proceed();
    }

    public String getColor() {
        return color;
    }

    public String getShape() {
        return shape;
    }
}
