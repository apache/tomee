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
package org.superbiz.stateless.basic;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.ejb.Stateless;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

@Stateless
public class CalculatorBean {

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

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public int divide(int a, int b) {
        return a / b;
    }

    public int remainder(int a, int b) {
        return a % b;
    }

}