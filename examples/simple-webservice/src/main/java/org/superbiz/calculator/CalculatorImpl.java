/**
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
package org.superbiz.calculator;

import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import java.util.Date;

/**
 * This is an EJB 3 style pojo stateless session bean
 * Every stateless session bean implementation must be annotated
 * using the annotation @Stateless
 * This EJB has a 2 interfaces:
 * <ul>
 * <li>CalculatorWs a webservice interface</li>
 * <li>CalculatorLocal a local interface</li>
 * </ul>
 */
//START SNIPPET: code
@Stateless
@WebService(
        portName = "CalculatorPort",
        serviceName = "CalculatorWsService",
        targetNamespace = "http://superbiz.org/wsdl",
        endpointInterface = "org.superbiz.calculator.CalculatorWs")
@HandlerChain(file = "handler.xml")
public class CalculatorImpl implements CalculatorWs, CalculatorLocal {

    public int sum(int add1, int add2) {
        return add1 + add2;
    }

    public int multiply(int mul1, int mul2) {
        return mul1 * mul2;
    }

    public int factorial(
            int number,
            Holder<String> userId,
            Holder<String> returnCode,
            Holder<Date> datetime) {

        if (number == 0) {
            returnCode.value = "Can not calculate factorial for zero.";
            return -1;
        }

        returnCode.value = userId.value;
        datetime.value = new Date();
        return (int) factorial(number);
    }

    // return n!
    // precondition: n >= 0 and n <= 20

    private static long factorial(long n) {
        if (n < 0) throw new RuntimeException("Underflow error in factorial");
        else if (n > 20) throw new RuntimeException("Overflow error in factorial");
        else if (n == 0) return 1;
        else return n * factorial(n - 1);
    }
}
//END SNIPPET: code
