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
package org.superbiz.remote;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;

@Stateless(name = "Calculator", description = "Calculator", mappedName = "Calculator")
@Remote(Calculator.class)
public class DefaultCalculator implements Calculator {
    @Override
    public int sum(int add1, int add2) {
        return add1 + add2;
    }

    @Override
    public int multiply(int mul1, int mul2) {
        return mul1 * mul2;
    }

    @Override
    public String echo(final String input) throws BusinessException {
        if ("CHECKED".equals(input)) {
            throw new BusinessException("This is a checked exception");
        }

        if ("RUNTIME".equals(input)) {
            throw new RuntimeException("This is a runtime exception");
        }

        if (input == null) {
            return "Input was null";
        }

        return "Input was: " + input;
    }



}
