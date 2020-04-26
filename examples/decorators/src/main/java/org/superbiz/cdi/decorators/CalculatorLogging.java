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

import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.inject.Inject;
import java.util.logging.Logger;

@Decorator
public class CalculatorLogging implements Calculator {

    private Logger logger = Logger.getLogger("Calculator");

    @Inject
    @Delegate
    private Calculator calculator;

    @Override
    public int add(int a, int b) {
        logger.fine(String.format("add(%s, %s)", a, b));
        return calculator.add(a, b);
    }

    @Override
    public int subtract(int a, int b) {
        return calculator.subtract(a, b);
    }

    @Override
    public int multiply(int a, int b) {
        logger.finest(String.format("multiply(%s, %s)", a, b));
        return calculator.multiply(a, b);
    }

    @Override
    public int divide(int a, int b) {
        return calculator.divide(a, b);
    }

    @Override
    public int remainder(int a, int b) {
        logger.info(String.format("remainder(%s, %s)", a, b));
        return calculator.remainder(a, b);
    }
}
