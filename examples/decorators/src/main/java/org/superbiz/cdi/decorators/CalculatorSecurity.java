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
package org.superbiz.cdi.decorators;

import jakarta.annotation.Resource;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.ejb.SessionContext;
import jakarta.inject.Inject;

@Decorator
public class CalculatorSecurity implements Calculator {

    @Inject
    @Delegate
    private Calculator calculator;

    @Resource
    private SessionContext sessionContext;

    @Override
    public int add(int a, int b) {
        return calculator.add(a, b);
    }

    @Override
    public int subtract(int a, int b) {
        // Caller must pass a security check to call subtract
        if (!sessionContext.isCallerInRole("Manager")) {
            throw new AccessDeniedException(sessionContext.getCallerPrincipal().getName());
        }

        return calculator.subtract(a, b);
    }

    @Override
    public int multiply(int a, int b) {
        return calculator.multiply(a, b);
    }

    @Override
    public int divide(int a, int b) {
        return calculator.divide(a, b);
    }

    @Override
    public int remainder(int a, int b) {
        return calculator.remainder(a, b);
    }

}
