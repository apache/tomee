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
package org.superbiz.calculator.ws;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;
import javax.jws.soap.SOAPBinding.Style;
import javax.jws.soap.SOAPBinding.Use;
import javax.xml.ws.Holder;
import java.util.Date;

//END SNIPPET: code

/**
 * This is an EJB 3 webservice interface
 * A webservice interface must be annotated with the @WebService
 * annotation.
 */
//START SNIPPET: code
@WebService(
        name = "CalculatorWs",
        targetNamespace = "http://superbiz.org/wsdl")
public interface CalculatorWs {

    public int sum(int add1, int add2);

    public int multiply(int mul1, int mul2);

    // because of CXF bug, BARE must be used instead of default WRAPPED

    @SOAPBinding(use = Use.LITERAL, parameterStyle = ParameterStyle.BARE, style = Style.DOCUMENT)
    public int factorial(
            int number,
            @WebParam(name = "userid", header = true, mode = WebParam.Mode.IN) Holder<String> userId,
            @WebParam(name = "returncode", header = true, mode = WebParam.Mode.OUT) Holder<String> returnCode,
            @WebParam(name = "datetime", header = true, mode = WebParam.Mode.INOUT) Holder<Date> datetime);

}
//END SNIPPET: code