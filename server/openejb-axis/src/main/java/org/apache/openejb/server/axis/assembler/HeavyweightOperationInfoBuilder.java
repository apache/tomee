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
import org.apache.openejb.jee.ConstructorParameterOrder;
import org.apache.openejb.jee.ExceptionMapping;
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.JavaXmlTypeMapping;
import org.apache.openejb.jee.MethodParamPartsMapping;
import org.apache.openejb.jee.PackageMapping;
import org.apache.openejb.jee.ServiceEndpointMethodMapping;
import org.apache.openejb.jee.WsdlMessageMapping;
import org.apache.openejb.jee.WsdlReturnValueMapping;
import static org.apache.openejb.server.axis.assembler.JaxRpcParameterInfo.Mode;

import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.rpc.holders.BigDecimalHolder;
import javax.xml.rpc.holders.BigIntegerHolder;
import javax.xml.rpc.holders.BooleanHolder;
import javax.xml.rpc.holders.BooleanWrapperHolder;
import javax.xml.rpc.holders.ByteArrayHolder;
import javax.xml.rpc.holders.ByteHolder;
import javax.xml.rpc.holders.ByteWrapperHolder;
import javax.xml.rpc.holders.CalendarHolder;
import javax.xml.rpc.holders.DoubleHolder;
import javax.xml.rpc.holders.DoubleWrapperHolder;
import javax.xml.rpc.holders.FloatHolder;
import javax.xml.rpc.holders.FloatWrapperHolder;
import javax.xml.rpc.holders.IntHolder;
import javax.xml.rpc.holders.IntegerWrapperHolder;
import javax.xml.rpc.holders.LongHolder;
import javax.xml.rpc.holders.LongWrapperHolder;
import javax.xml.rpc.holders.ObjectHolder;
import javax.xml.rpc.holders.QNameHolder;
import javax.xml.rpc.holders.ShortHolder;
import javax.xml.rpc.holders.ShortWrapperHolder;
import javax.xml.rpc.holders.StringHolder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HeavyweightOperationInfoBuilder{
    private final JavaWsdlMapping mapping;
    private final ServiceEndpointMethodMapping methodMapping;

    private final XmlSchemaInfo schemaInfo;
    private final BindingStyle bindingStyle;

    //
    // Used to map exception class constructor args
    //
    private final Map<QName, String> publicTypes = new HashMap<QName, String>();
    private final Map<String, String> anonymousTypes = new HashMap<String, String>();

    //
    // Track in and out parameter names so we can verify that
    // everything has been mapped and mapped correctly
    //
    private final Set<String> inParamNames = new HashSet<String>();
    private final Set<String> outParamNames = new HashSet<String>();

    //
    // Track the wrapper elements - used by HeavyweightTypeInfoBuilder
    //
    private final Set<QName> wrapperElementQNames = new HashSet<QName>();

    private final String operationName;
    private final JaxRpcOperationInfo.OperationStyle operationStyle;
    private final Message inputMessage;
    private final Message outputMessage;
    private final Collection<Fault> faults = new ArrayList<Fault>();

    private JaxRpcOperationInfo operationInfo;

    public HeavyweightOperationInfoBuilder(BindingOperation bindingOperation, ServiceEndpointMethodMapping methodMapping, JavaWsdlMapping mapping, XmlSchemaInfo schemaInfo) throws OpenEJBException {
        Operation operation = bindingOperation.getOperation();
        this.operationName = operation.getName();
        this.operationStyle = JaxRpcOperationInfo.OperationStyle.valueOf(operation.getStyle().toString());
        this.outputMessage = operation.getOutput() == null ? null : operation.getOutput().getMessage();
        this.inputMessage = operation.getInput().getMessage();

        // faults
        for (Object o : operation.getFaults().values()) {
            faults.add((Fault) o);
        }

        this.mapping = mapping;
        this.methodMapping = methodMapping;
        this.schemaInfo = schemaInfo;

        // index types - used to process build exception class constructor args
        for (JavaXmlTypeMapping javaXmlTypeMapping : mapping.getJavaXmlTypeMapping()) {
            String javaClassName = javaXmlTypeMapping.getJavaType();
            if (javaXmlTypeMapping.getAnonymousTypeQname() != null) {
                String anonymousTypeQName = javaXmlTypeMapping.getAnonymousTypeQname();
                anonymousTypes.put(anonymousTypeQName, javaClassName);
            } else if (javaXmlTypeMapping.getRootTypeQname() != null) {
                QName qname = javaXmlTypeMapping.getRootTypeQname();
                publicTypes.put(qname, javaClassName);
            }
        }

        // BindingStyle
        if (methodMapping.getWrappedElement() != null) {
            bindingStyle = BindingStyle.DOCUMENT_LITERAL_WRAPPED;
        } else {
            BindingInput bindingInput = bindingOperation.getBindingInput();

            SOAPOperation soapOperation = JaxRpcServiceInfoBuilder.getExtensibilityElement(SOAPOperation.class, bindingOperation.getExtensibilityElements());
            String styleString = soapOperation.getStyle();
            if (styleString == null) {
                SOAPBinding soapBinding = JaxRpcServiceInfoBuilder.getExtensibilityElement(SOAPBinding.class, bindingInput.getExtensibilityElements());
                styleString = soapBinding.getStyle();
            }

            SOAPBody soapBody = JaxRpcServiceInfoBuilder.getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());
            String useString = soapBody.getUse();

            bindingStyle = BindingStyle.getBindingStyle(styleString, useString);
        }
    }

    public Set<QName> getWrapperElementQNames() throws OpenEJBException {
        buildOperationInfo();
        return Collections.unmodifiableSet(wrapperElementQNames);
    }

    public JaxRpcOperationInfo buildOperationInfo() throws OpenEJBException {
        if (operationInfo != null) {
            return operationInfo;
        }

        operationInfo = new JaxRpcOperationInfo();
        operationInfo.name = operationName;

        // Binding style rpc/encoded, doc/lit, wrapped, etc.
        operationInfo.bindingStyle = bindingStyle;

        // Operation style one way, request response, etc/
        operationInfo.operationStyle = operationStyle;

        // Java method name
        operationInfo.javaMethodName = methodMapping.getJavaMethodName();

        //
        // Map the parameters
        //
        mapParameters();

        //
        // Map return
        //
        if (methodMapping.getWsdlReturnValueMapping() != null) {
            mapReturnType();
        }

        // Validate output mapping is complete
        if (outputMessage != null && bindingStyle.isWrapped()) {
            Part inputPart = getWrappedPart(outputMessage);

            QName wrapperName = inputPart.getElementName();
            XmlElementInfo wraperElement = schemaInfo.elements.get(wrapperName);
            XmlTypeInfo wrapperType = schemaInfo.types.get(wraperElement.xmlType);

            Set<String> expectedOutParams = new HashSet<String>();
            for (XmlElementInfo expectedOutParam : wrapperType.elements.values()) {
                expectedOutParams.add(expectedOutParam.qname.getLocalPart());
            }
            if (!outParamNames.equals(expectedOutParams)) {
                throw new OpenEJBException("Not all wrapper children were mapped to parameters or a return value for operation " + operationName);
            }
        } else if (null != outputMessage) {
            if (!outParamNames.equals(outputMessage.getParts().keySet())) {
                throw new OpenEJBException("Not all output message parts were mapped to parameters or a return value for operation " + operationName);
            }
        }

        //
        // Map faults (exception)
        //
        for (Fault fault : faults) {
            JaxRpcFaultInfo faultInfo = mapFaults(fault);
            operationInfo.faults.add(faultInfo);
        }

        return operationInfo;
    }

    private JaxRpcParameterInfo[] mapParameters() throws OpenEJBException {
        List<MethodParamPartsMapping> paramMappings = methodMapping.getMethodParamPartsMapping();

        //
        // Map the ParameterDesc instance in an array so they can be ordered properly
        // before they are added to the the OperationDesc.
        //
        JaxRpcParameterInfo[] parameterInfos = new JaxRpcParameterInfo[paramMappings.size()];
        for (MethodParamPartsMapping paramMapping : paramMappings) {
            JaxRpcParameterInfo parameterInfo = mapParameter(paramMapping);
            parameterInfos[paramMapping.getParamPosition().intValue()] = parameterInfo;
        }

        //
        // verify that all parameters were mapped and we don't have nulls in the parameter array
        //
        for (int i = 0; i < parameterInfos.length; i++) {
            if (parameterInfos[i] == null) {
                throw new OpenEJBException("There is no mapping for parameter number " + i + " for operation " + operationName);
            }
        }

        //
        // Verify that all parameter names were mapped
        //
        if (bindingStyle.isWrapped()) {
            // verify that all child elements have a parameter mapping
            Part inputPart = getWrappedPart(inputMessage);

            QName wrapperName = inputPart.getElementName();
            XmlElementInfo wrapperElement = schemaInfo.elements.get(wrapperName);
            XmlTypeInfo wrapperType = schemaInfo.types.get(wrapperElement.xmlType);

            Set<String> expectedInParams = new HashSet<String>();
            for (XmlElementInfo expectedInParam : wrapperType.elements.values()) {
                expectedInParams.add(expectedInParam.qname.getLocalPart());
            }
            if (!inParamNames.equals(expectedInParams)) {
                throw new OpenEJBException("Not all wrapper children were mapped for operation name" + operationName);
            }
        } else {
            // verify all input message parts are mapped
            if (!inParamNames.equals(inputMessage.getParts().keySet())) {
                throw new OpenEJBException("Not all input message parts were mapped for operation name" + operationName);
            }
        }

        return parameterInfos;
    }

    private JaxRpcParameterInfo mapParameter(MethodParamPartsMapping paramMapping) throws OpenEJBException {
        WsdlMessageMapping wsdlMessageMappingType = paramMapping.getWsdlMessageMapping();
        QName wsdlMessageQName = wsdlMessageMappingType.getWsdlMessage();
        String wsdlMessagePartName = wsdlMessageMappingType.getWsdlMessagePartName();

        Mode mode = Mode.valueOf(wsdlMessageMappingType.getParameterMode());
        if ((mode == Mode.OUT || mode == Mode.INOUT) && outputMessage == null) {
            throw new OpenEJBException("Mapping for output parameter " + wsdlMessagePartName + " found, but no output message for operation " + operationName);
        }

        //
        // Determine the param qname and xml schema type
        //
        QName paramQName;
        QName paramXmlType;
        if (mode == Mode.IN || mode == Mode.INOUT) {
            //
            // IN or INOUT Parameter
            //
            if (!wsdlMessageQName.equals(inputMessage.getQName())) {
                throw new OpenEJBException("QName of input message: " + inputMessage.getQName() +
                        " does not match mapping message QName: " + wsdlMessageQName + " for operation " + operationName);
            }

            Part part = null;
            XmlElementInfo inParameter = null;
            if (bindingStyle.isWrapped()) {
                Part inPart = getWrappedPart(inputMessage);

                // operation name == wraper element name
                QName name = inPart.getElementName();
                if (!name.getLocalPart().equals(operationName)) {
                    throw new OpenEJBException("message " + inputMessage.getQName() + " refers to a global element named " +
                            name.getLocalPart() + ", which is not equal to the operation name " + operationName);
                }
                inParameter = getWrapperChild(inPart, wsdlMessagePartName);

                paramQName = new QName("", inParameter.qname.getLocalPart());
                paramXmlType = inParameter.xmlType;
            } else if (bindingStyle.isRpc()) {
                part = inputMessage.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new OpenEJBException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in input message for operation " + operationName);
                }

                paramQName = new QName("", part.getName());
                
                // RPC can only use type
                paramXmlType = part.getTypeName();
            } else {
                part = inputMessage.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new OpenEJBException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in input message for operation " + operationName);
                }
                // Document should use element, but type is allowed
                paramQName = getPartName(part);
                paramXmlType = paramQName;
            }
            inParamNames.add(wsdlMessagePartName);

            //
            // Verify INOUT parameter output message is consistent with input message
            //
            if (mode == Mode.INOUT) {
                if (bindingStyle.isWrapped()) {
                    // Verify output message supports this inout parameter
                    Part outPart = getWrappedPart(outputMessage);
                    XmlElementInfo outParameter = getWrapperChild(outPart, wsdlMessagePartName);
                    if (inParameter.xmlType != outParameter.xmlType) {
                        throw new OpenEJBException("The wrapper children " + wsdlMessagePartName + " do not have the same type for operation " + operationName);
                    }
                } else if (bindingStyle.isRpc()) {
                    // Verify output message supports this inout parameter
                    Part outPart = outputMessage.getPart(wsdlMessagePartName);
                    if (outPart == null) {
                        throw new OpenEJBException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for INOUT parameter of operation " + operationName);
                    }
                    // TODO this cannot happen.
                    if (!part.getName().equals(outPart.getName())) {
                        throw new OpenEJBException("Mismatched input part name: " + part.getName() + " and output part name: " + outPart.getName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                    if (!(part.getElementName() == null ? outPart.getElementName() == null : part.getElementName().equals(outPart.getElementName()))) {
                        throw new OpenEJBException("Mismatched input part element name: " + part.getElementName() + " and output part element name: " + outPart.getElementName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                    if (!(part.getTypeName() == null ? outPart.getTypeName() == null : part.getTypeName().equals(outPart.getTypeName()))) {
                        throw new OpenEJBException("Mismatched input part type name: " + part.getTypeName() + " and output part type name: " + outPart.getTypeName() + " for INOUT parameter for wsdlMessagePartName " + wsdlMessagePartName + " for operation " + operationName);
                    }
                } else {
                    part = outputMessage.getPart(wsdlMessagePartName);
                    if (part == null) {
                        throw new OpenEJBException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                    }
                    // todo this seem strange... shouldn't the name and type be the same as the in binding above
                    paramQName = getPartName(part);
                    paramXmlType = paramQName;
                }
                outParamNames.add(wsdlMessagePartName);
            }
        } else {
            //
            // OUT only Parameter
            //
            if (!wsdlMessageQName.equals(outputMessage.getQName())) {
                throw new OpenEJBException("QName of output message: " + outputMessage.getQName() +
                        " does not match mapping message QName: " + wsdlMessageQName + " for operation " + operationName);
            }

            if (bindingStyle.isWrapped()) {
                Part outPart = getWrappedPart(outputMessage);
                XmlElementInfo outParameter = getWrapperChild(outPart, wsdlMessagePartName);

                paramQName = new QName("", outParameter.qname.getLocalPart());
                paramXmlType = outParameter.xmlType;
            } else if (bindingStyle.isRpc()) {
                Part part = outputMessage.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new OpenEJBException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }

                paramQName = new QName("", part.getName());

                // RPC can only use type
                paramXmlType = part.getTypeName();
            } else {
                Part part = outputMessage.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new OpenEJBException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }

                paramQName = getPartName(part);
                paramXmlType = paramQName;
            }
            outParamNames.add(wsdlMessagePartName);
        }

        //
        // Determine the param java type
        //
        String paramJavaType;
        if (mode == Mode.IN) {
            // IN only prarmeters don't have holders
            paramJavaType = paramMapping.getParamType();
        } else if (rpcHolderClasses.containsKey(paramMapping.getParamType())) {
            // This is a standard type with a built in holder class
            paramJavaType = rpcHolderClasses.get(paramMapping.getParamType());
        } else {
            // holderClass == ${packageName}.holders.${typeName}Holder
            String packageName;
            String typeName;

            PackageMapping packageMapping = mapping.getPackageMappingMap().get(paramXmlType.getNamespaceURI());
            if (packageMapping != null) {
                packageName = packageMapping.getPackageType();

                // Type name is typeQName local part, but make sure it is capitalized correctly
                typeName = paramXmlType.getLocalPart();
                typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
            } else {
                // a.b.foo.Bar >>> a.b.foo.holders.BarHolder
                String paramJavaTypeName = paramMapping.getParamType();
                int lastDot = paramJavaTypeName.lastIndexOf(".");
                packageName = paramJavaTypeName.substring(0, lastDot);
                typeName = paramJavaTypeName.substring(lastDot + 1);
            }

            paramJavaType = packageName + ".holders." + typeName + "Holder";
        }

        //
        // Build JaxRpcParameterInfo
        //
        JaxRpcParameterInfo parameterInfo = new JaxRpcParameterInfo();
        parameterInfo.qname = paramQName;
        parameterInfo.xmlType = paramXmlType;
        parameterInfo.javaType = paramJavaType;
        parameterInfo.mode = Mode.valueOf(wsdlMessageMappingType.getParameterMode());
        parameterInfo.soapHeader = wsdlMessageMappingType.getSoapHeader() != null;

        return parameterInfo;
    }

    private void mapReturnType() throws OpenEJBException {
        if (outputMessage == null) {
            throw new OpenEJBException("No output message, but a mapping for it for operation " + operationName);
        }

        // verify mapped return value qname matches expected output message name
        WsdlReturnValueMapping wsdlReturnValueMapping = methodMapping.getWsdlReturnValueMapping();
        if (!wsdlReturnValueMapping.getWsdlMessage().equals(outputMessage.getQName())) {
            throw new OpenEJBException("OutputMessage has QName: " + outputMessage.getQName() + " but mapping specifies: " + wsdlReturnValueMapping.getWsdlMessage() + " for operation " + operationName);
        }

        //
        // Determind return type qname and xml schema type
        //
        QName returnQName = null;
        QName returnXmlType = null;
        if (wsdlReturnValueMapping.getWsdlMessagePartName() != null) {
            String wsdlMessagePartName = wsdlReturnValueMapping.getWsdlMessagePartName();
            if (outParamNames.contains(wsdlMessagePartName)) {
                throw new OpenEJBException("output message part " + wsdlMessagePartName + " has both an INOUT or OUT mapping and a return value mapping for operation " + operationName);
            }

            if (bindingStyle.isWrapped()) {
                Part outPart = getWrappedPart(outputMessage);
                XmlElementInfo returnParticle = getWrapperChild(outPart, wsdlMessagePartName);

                returnQName = new QName("", returnParticle.qname.getLocalPart());
                returnXmlType = returnParticle.xmlType;
            } else if (bindingStyle.isRpc()) {
                Part part = outputMessage.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new OpenEJBException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }

                returnQName = new QName("", part.getName());

                // RPC can only use type
                returnXmlType = part.getTypeName();
            } else {
                Part part = outputMessage.getPart(wsdlMessagePartName);
                if (part == null) {
                    throw new OpenEJBException("No part for wsdlMessagePartName " + wsdlMessagePartName + " in output message for operation " + operationName);
                }

                returnQName = getPartName(part);
                returnXmlType = returnQName;
            }

            outParamNames.add(wsdlMessagePartName);
        } else {
            // what does this mean????
        }

        operationInfo.returnQName = returnQName;
        operationInfo.returnXmlType = returnXmlType;
        operationInfo.returnJavaType = wsdlReturnValueMapping.getMethodReturnValue();
    }

    private JaxRpcFaultInfo mapFaults(Fault fault) throws OpenEJBException {
        Message message = fault.getMessage();
        ExceptionMapping exceptionMapping = mapping.getExceptionMappingMap().get(message.getQName());
        if (exceptionMapping == null) {
            throw new OpenEJBException("No exception mapping for fault " + fault.getName() + " and fault message " + message.getQName() + " for operation " + operationName);
        }

        // TODO investigate whether there are other cases in which the namespace of faultQName can be determined.
        // this is weird, but I can't figure out what it should be.
        // if part has an element rather than a type, it should be part.getElementName() (see below)
        Part part;
        if (exceptionMapping.getWsdlMessagePartName() != null) {
            // According to schema documentation, this will only be set when several headerfaults use the same message.
            String headerFaultMessagePartName = exceptionMapping.getWsdlMessagePartName();
            part = message.getPart(headerFaultMessagePartName);
        } else {
            part = (Part) message.getOrderedParts(null).iterator().next();
        }

        // Determine the fault qname and xml schema type
        QName faultQName;
        XmlTypeInfo faultTypeInfo;
        if (part.getElementName() != null) {
            XmlElementInfo elementInfo = schemaInfo.elements.get(part.getElementName());
            if (elementInfo == null) {
                throw new OpenEJBException("Can not find element: " + part.getElementName() + ", known elements: " + schemaInfo.elements.keySet());
            }
            faultTypeInfo = schemaInfo.types.get(elementInfo.xmlType);
            if (faultTypeInfo == null) {
                throw new OpenEJBException("Can not find type " + elementInfo.xmlType + " for element " + elementInfo.qname + ", known types: " + schemaInfo.types.keySet());
            }

            faultQName = part.getElementName();
        } else if (part.getTypeName() != null) {
            faultTypeInfo = schemaInfo.types.get(part.getTypeName());
            if (faultTypeInfo == null) {
                throw new OpenEJBException("Can not find type: " + part.getTypeName() + ", known elements: " + schemaInfo.types.keySet());
            }

            faultQName = new QName("", fault.getName());
        } else {
            throw new OpenEJBException("Neither type nor element name supplied for part: " + part);
        }

        //
        // Build the fault info
        //
        JaxRpcFaultInfo faultInfo = new JaxRpcFaultInfo();
        faultInfo.qname = faultQName;
        faultInfo.xmlType = faultTypeInfo.qname;
        faultInfo.javaType = exceptionMapping.getExceptionType();
        faultInfo.complex = faultTypeInfo.simpleBaseType == null;

        //
        // Map exception class constructor args
        //
        if (exceptionMapping.getConstructorParameterOrder() != null) {
            if (faultTypeInfo.simpleBaseType != null) {
                throw new OpenEJBException("ConstructorParameterOrder can only be set for complex types, not " + faultTypeInfo.qname);
            }

            Map<String, XmlElementInfo> elements = new HashMap<String, XmlElementInfo>();
            for (XmlElementInfo element : faultTypeInfo.elements.values()) {
                elements.put(element.qname.getLocalPart(), element);
            }

            ConstructorParameterOrder constructorParameterOrder = exceptionMapping.getConstructorParameterOrder();
            for (int i = 0; i < constructorParameterOrder.getElementName().size(); i++) {
                String paramName = constructorParameterOrder.getElementName().get(i);

                // get the parameter element
                XmlElementInfo paramElementInfo = elements.get(paramName);
                if (paramElementInfo == null) {
                    throw new OpenEJBException("Can not find element " + paramName + " in fault type " + faultTypeInfo.qname + ", known elements: " + elements.keySet());
                }

                // Java Type
                String paramJavaType = null;
                XmlTypeInfo paramTypeInfo = schemaInfo.types.get(paramElementInfo.xmlType);
                if (paramTypeInfo != null) {
                    if (paramTypeInfo.anonymous) {
                        paramJavaType = anonymousTypes.get(paramTypeInfo.qname.getLocalPart());
                    } else {
                        paramJavaType = publicTypes.get(paramTypeInfo.qname);
                    }
                }
                // if we don't have a java type yet, check the simple types
                if (paramJavaType == null) {
                    paramJavaType = qnameToJavaType.get(paramElementInfo.xmlType);
                }
                if (paramJavaType == null) {
                    throw new OpenEJBException("No class mapped for element type: " + paramElementInfo.xmlType);
                }

                JaxRpcParameterInfo parameterInfo = new JaxRpcParameterInfo();
                parameterInfo.qname = paramElementInfo.qname;
                parameterInfo.mode = Mode.OUT;
                // todo could be a soap header
                parameterInfo.soapHeader = false;
                parameterInfo.xmlType = paramElementInfo.xmlType;
                parameterInfo.javaType = paramJavaType;

                faultInfo.parameters.add(parameterInfo);
            }
        }
        return faultInfo;
    }


    private QName getPartName(Part part) {
        return part.getElementName() == null ? part.getTypeName() : part.getElementName();
    }

    private Part getWrappedPart(Message message) throws OpenEJBException {
        // a wrapped element can only have one part
        Collection parts = message.getParts().values();
        if (parts.size() != 1) {
            throw new OpenEJBException("message " + message.getQName() + " has " + parts.size() +
                    " parts and should only have one as wrapper style mapping is specified for operation " +
                    operationName);
        }
        return (Part) parts.iterator().next();
    }

    private XmlElementInfo getWrapperChild(Part part, String wsdlMessagePartName) throws OpenEJBException {
        // get the part name
        QName elementName = part.getElementName();
        wrapperElementQNames.add(elementName);

        // get the wrapper element
        XmlElementInfo wrapperElement = schemaInfo.elements.get(elementName);
        if (wrapperElement == null) {
            throw new OpenEJBException("No global element named " + elementName + " for operation " + operationName);
        }

        // get the wrapper type
        XmlTypeInfo wrapperType = schemaInfo.types.get(wrapperElement.xmlType);
        if (wrapperType == null) {
            throw new OpenEJBException("Can not find type " + wrapperElement.xmlType + " for element " + wrapperElement.qname + ", known types: " + schemaInfo.types.keySet());
        }

        // get the part type
        for (XmlElementInfo wrapperChild : wrapperType.elements.values()) {
            if (wrapperChild.qname.getLocalPart().equals(wsdlMessagePartName)) {
                return wrapperChild;
            }
        }
        throw new OpenEJBException("Global element named " + elementName + " does not define a child element named " + wsdlMessagePartName + " required by the operation " + operationName);
    }

    //see jaxrpc 1.1 4.2.1
    private static final Map<QName, String> qnameToJavaType = new HashMap<QName, String>();

    static {
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "integer"), BigInteger.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "int"), int.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "long"), long.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "short"), short.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "decimal"), BigDecimal.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "float"), float.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "double"), double.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "boolean"), boolean.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "byte"), byte.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedInt"), long.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedShort"), int.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "unsignedByte"), short.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "QName"), QName.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "dateTime"), Calendar.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "date"), Calendar.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "time"), Calendar.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "anyURI"), URI.class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "base64Binary"), byte[].class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "hexBinary"), byte[].class.getName());
        qnameToJavaType.put(new QName("http://www.w3.org/2001/XMLSchema", "anySimpleType"), String.class.getName());
    }


    /**
     * Supporting the Document/Literal Wrapped pattern
     *
     * See http://www-106.ibm.com/developerworks/webservices/library/ws-whichwsdl/ for a nice explanation and example
     *
     * wrapped-element tag is used
     * WSDL message with a single part
     * part uses the 'element' attribute to point to an elemement in the types section
     * the element type and the element's name match the operation name
     */

    // standard holder classes by type
    private static final Map<String,String> rpcHolderClasses = new HashMap<String, String>();

    static {
        rpcHolderClasses.put(BigDecimal.class.getName(), BigDecimalHolder.class.getName());
        rpcHolderClasses.put(BigInteger.class.getName(), BigIntegerHolder.class.getName());
        rpcHolderClasses.put(boolean.class.getName(), BooleanHolder.class.getName());
        rpcHolderClasses.put(Boolean.class.getName(), BooleanWrapperHolder.class.getName());
        rpcHolderClasses.put(byte[].class.getName(), ByteArrayHolder.class.getName());
        rpcHolderClasses.put(byte.class.getName(), ByteHolder.class.getName());
        rpcHolderClasses.put(Byte.class.getName(), ByteWrapperHolder.class.getName());
        rpcHolderClasses.put(Calendar.class.getName(), CalendarHolder.class.getName());
        rpcHolderClasses.put(double.class.getName(), DoubleHolder.class.getName());
        rpcHolderClasses.put(Double.class.getName(), DoubleWrapperHolder.class.getName());
        rpcHolderClasses.put(float.class.getName(), FloatHolder.class.getName());
        rpcHolderClasses.put(Float.class.getName(), FloatWrapperHolder.class.getName());
        rpcHolderClasses.put(int.class.getName(), IntHolder.class.getName());
        rpcHolderClasses.put(Integer.class.getName(), IntegerWrapperHolder.class.getName());
        rpcHolderClasses.put(long.class.getName(), LongHolder.class.getName());
        rpcHolderClasses.put(Long.class.getName(), LongWrapperHolder.class.getName());
        rpcHolderClasses.put(Object.class.getName(), ObjectHolder.class.getName());
        rpcHolderClasses.put(QName.class.getName(), QNameHolder.class.getName());
        rpcHolderClasses.put(short.class.getName(), ShortHolder.class.getName());
        rpcHolderClasses.put(Short.class.getName(), ShortWrapperHolder.class.getName());
        rpcHolderClasses.put(String.class.getName(), StringHolder.class.getName());
    }
}