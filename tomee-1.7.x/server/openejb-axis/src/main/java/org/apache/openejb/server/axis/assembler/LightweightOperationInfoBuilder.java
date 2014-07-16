/**
 *
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
package org.apache.openejb.server.axis.assembler;

import org.apache.openejb.OpenEJBException;

import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.List;

public class LightweightOperationInfoBuilder {
    private final String operationName;
    private final Message inputMessage;
    private final Message outputMessage;
    private final Method method;

    private JaxRpcOperationInfo operationInfo;

    public LightweightOperationInfoBuilder(BindingOperation bindingOperation, Method method) throws OpenEJBException{
        if (bindingOperation == null) {
            throw new OpenEJBException("No BindingOperation supplied for method " + method.getName());
        }

        Operation operation = bindingOperation.getOperation();
        this.operationName = operation.getName();
        this.inputMessage = operation.getInput().getMessage();
        this.outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();
        this.method = method;
    }

    public JaxRpcOperationInfo buildOperationInfo() throws OpenEJBException {
        if (operationInfo != null) {
            return operationInfo;
        }

        operationInfo = new JaxRpcOperationInfo();
        operationInfo.name = operationName;
        operationInfo.bindingStyle = BindingStyle.RPC_ENCODED;
        operationInfo.javaMethodName = method.getName();

        // Verify we have the right number of args for this method
        Class[] methodParamTypes = method.getParameterTypes();
        List inputParts = inputMessage.getOrderedParts(null);
        if (methodParamTypes.length != inputParts.size()) {
            throw new OpenEJBException("mismatch in parameter counts: method has " + methodParamTypes.length + " whereas the input message has " + inputParts.size());
        }

        // Map parameters
        int i = 0;
        for (Object inputPart : inputParts) {
            Part part = (Part) inputPart;

            JaxRpcParameterInfo parameter = new JaxRpcParameterInfo();
            parameter.qname = new QName("", part.getName());
            parameter.mode = JaxRpcParameterInfo.Mode.IN;

            if (part.getTypeName() == null) {
                parameter.xmlType = part.getElementName();
            } else {
                parameter.xmlType = part.getTypeName();
            }

            parameter.javaType = methodParamTypes[i++].getName();
            parameter.soapHeader = false;

            operationInfo.parameters.add(parameter);
        }

        // Lightweight can't have multiple return values
        if (outputMessage != null && outputMessage.getParts().size() > 1) {
            throw new OpenEJBException("Lightweight mapping has at most one part in the (optional) output message, not: " + outputMessage.getParts().size());
        }

        // Map return type mapping
        if (outputMessage != null && outputMessage.getParts().size() == 1) {
            Part part = (Part) outputMessage.getParts().values().iterator().next();

            // return qname
            if (part.getElementName() == null) {
                operationInfo.returnQName = new QName(part.getName());
            } else {
                operationInfo.returnQName = part.getElementName();
            }

            // return xml schema type
            if (part.getTypeName() == null) {
                operationInfo.returnXmlType = part.getElementName();
            } else {
                operationInfo.returnXmlType = part.getTypeName();
            }

            // return java type
            operationInfo.returnJavaType = method.getReturnType().getName();
        }

        //TODO add faults
//        TFault[] faults = tOperation.getFaultArray();
//        for (int i = 0; i < faults.length; i++) {
//            TFault fault = faults[i];
//            QName faultQName = new QName("", fault.getName());
//            String className = ;
//            QName faultTypeQName = ;
//            boolean isComplex = ;
//            FaultDesc faultDesc = new FaultDesc(faultQName, className, faultTypeQName, isComplex)
//        }
        return operationInfo;
    }
}