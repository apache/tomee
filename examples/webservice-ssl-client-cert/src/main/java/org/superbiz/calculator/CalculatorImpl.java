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
package org.superbiz.calculator;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

/**
 * This is an EJB 3 style pojo stateless session bean
 * Every stateless session bean implementation must be annotated
 * using the annotation @Stateless
 * This EJB has a single interface: CalculatorWs a webservice interface.
 */
//START SNIPPET: code
@DeclareRoles(value = {"Administrator"})
@Stateless
@WebService(
        portName = "CalculatorPort",
        serviceName = "CalculatorWsService",
        targetNamespace = "http://superbiz.org/wsdl",
        endpointInterface = "org.superbiz.calculator.CalculatorWs")
public class CalculatorImpl implements CalculatorWs {

    public int sum(int add1, int add2) {
        return add1 + add2;
    }

    public int multiply(int mul1, int mul2) {
        return mul1 * mul2;
    }

}
//END SNIPPET: code