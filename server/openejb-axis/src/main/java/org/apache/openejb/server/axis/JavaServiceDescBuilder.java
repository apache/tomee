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
package org.apache.openejb.server.axis;

import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.description.FaultDesc;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.encoding.DefaultJAXRPC11TypeMappingImpl;
import org.apache.axis.encoding.DefaultSOAPEncodingTypeMappingImpl;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingImpl;
import org.apache.axis.encoding.TypeMappingRegistryImpl;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BaseDeserializerFactory;
import org.apache.axis.encoding.ser.BaseSerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.EnumDeserializerFactory;
import org.apache.axis.encoding.ser.EnumSerializerFactory;
import org.apache.axis.encoding.ser.SimpleListDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleListSerializerFactory;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.server.axis.assembler.BindingStyle;
import org.apache.openejb.server.axis.assembler.JaxRpcFaultInfo;
import org.apache.openejb.server.axis.assembler.JaxRpcOperationInfo;
import org.apache.openejb.server.axis.assembler.JaxRpcParameterInfo;
import org.apache.openejb.server.axis.assembler.JaxRpcServiceInfo;
import org.apache.openejb.server.axis.assembler.JaxRpcTypeInfo;

import javax.wsdl.OperationType;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.encoding.SerializerFactory;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class JavaServiceDescBuilder {
    private static final TypeMappingImpl SOAP_TYPE_MAPPING = DefaultSOAPEncodingTypeMappingImpl.getSingleton();
    private static final TypeMappingImpl JAXRPC_TYPE_MAPPING = DefaultJAXRPC11TypeMappingImpl.getSingleton();

    private final JaxRpcServiceInfo serviceInfo;
    private final ClassLoader classLoader;

    public JavaServiceDescBuilder(JaxRpcServiceInfo serviceInfo, ClassLoader classLoader) {
        this.serviceInfo = serviceInfo;
        this.classLoader = classLoader;
    }

    public JavaServiceDesc createServiceDesc() throws OpenEJBException {
        Class serviceEndpointInterface;
        try {
            serviceEndpointInterface = classLoader.loadClass(serviceInfo.serviceEndpointInterface);
        } catch (ClassNotFoundException e) {
            throw new OpenEJBException("Unable to load the service endpoint interface " + serviceInfo.serviceEndpointInterface, e);
        }

        JavaServiceDesc serviceDesc = new JavaServiceDesc();
        serviceDesc.setName(serviceInfo.name);
        serviceDesc.setEndpointURL(serviceInfo.endpointURL);
        serviceDesc.setWSDLFile(serviceInfo.wsdlFile);

        BindingStyle bindingStyle = serviceInfo.defaultBindingStyle;
        switch (bindingStyle) {
            case RPC_ENCODED:
                serviceDesc.setStyle(Style.RPC);
                serviceDesc.setUse(Use.ENCODED);
                break;
            case RPC_LITERAL:
                serviceDesc.setStyle(Style.RPC);
                serviceDesc.setUse(Use.LITERAL);
                break;
            case DOCUMENT_ENCODED:
                serviceDesc.setStyle(Style.DOCUMENT);
                serviceDesc.setUse(Use.ENCODED);
                break;
            case DOCUMENT_LITERAL:
                serviceDesc.setStyle(Style.DOCUMENT);
                serviceDesc.setUse(Use.LITERAL);
                break;
            case DOCUMENT_LITERAL_WRAPPED:
                serviceDesc.setStyle(Style.WRAPPED);
                serviceDesc.setUse(Use.LITERAL);
                break;
        }

        // Operations
        for (JaxRpcOperationInfo operationInfo : serviceInfo.operations) {
            OperationDesc operationDesc = buildOperationDesc(operationInfo, serviceEndpointInterface);
            serviceDesc.addOperationDesc(operationDesc);
        }

        // Type mapping registry
        TypeMappingRegistryImpl typeMappingRegistry = new TypeMappingRegistryImpl();
        typeMappingRegistry.doRegisterFromVersion("1.3");
        serviceDesc.setTypeMappingRegistry(typeMappingRegistry);

        // Type mapping
        TypeMapping typeMapping = typeMappingRegistry.getOrMakeTypeMapping(serviceDesc.getUse().getEncoding());
        serviceDesc.setTypeMapping(typeMapping);

        // Types
        for (JaxRpcTypeInfo type : serviceInfo.types) {
            registerType(type, typeMapping);
        }

        return new ReadOnlyServiceDesc(serviceDesc);
    }

    private OperationDesc buildOperationDesc(JaxRpcOperationInfo operationInfo, Class serviceEndpointInterface) throws OpenEJBException {
        OperationDesc operationDesc = new OperationDesc();
        operationDesc.setName(operationInfo.name);

        // Binding type
        switch (operationInfo.bindingStyle) {
            case RPC_ENCODED:
                operationDesc.setStyle(Style.RPC);
                operationDesc.setUse(Use.ENCODED);
                break;
            case RPC_LITERAL:
                operationDesc.setStyle(Style.RPC);
                operationDesc.setUse(Use.LITERAL);
                break;
            case DOCUMENT_ENCODED:
                operationDesc.setStyle(Style.DOCUMENT);
                operationDesc.setUse(Use.ENCODED);
                break;
            case DOCUMENT_LITERAL:
                operationDesc.setStyle(Style.DOCUMENT);
                operationDesc.setUse(Use.LITERAL);
                break;
            case DOCUMENT_LITERAL_WRAPPED:
                operationDesc.setStyle(Style.WRAPPED);
                operationDesc.setUse(Use.LITERAL);
                break;
        }

        // Operation style
        switch (operationInfo.operationStyle) {
            case NOTIFICATION:
                operationDesc.setMep(OperationType.NOTIFICATION);
                break;
            case ONE_WAY:
                operationDesc.setMep(OperationType.ONE_WAY);
                break;
            case REQUEST_RESPONSE:
                operationDesc.setMep(OperationType.REQUEST_RESPONSE);
                break;
            case SOLICIT_RESPONSE:
                operationDesc.setMep(OperationType.SOLICIT_RESPONSE);
                break;
        }

        // Build parameters
        Class[] paramTypes = new Class[operationInfo.parameters.size()];
        int i = 0;
        for (JaxRpcParameterInfo parameterInfo : operationInfo.parameters) {
            ParameterDesc parameterDesc = buildParameterDesc(parameterInfo);
            operationDesc.addParameter(parameterDesc);
            paramTypes[i++] = parameterDesc.getJavaType();
        }

        // Java method
        try {
            Method method = serviceEndpointInterface.getMethod(operationInfo.javaMethodName, paramTypes);
            operationDesc.setMethod(method);
        } catch (NoSuchMethodException e) {
            String args = "";
            for (Class paramType : paramTypes) {
                if (args.length() > 0) {
                    args += ", ";
                }
                args += paramType.getName();
            }
            throw new OpenEJBException("Mapping references non-existent method in service-endpoint: " + operationInfo.javaMethodName + "(" + args + ")");
        }

        //
        // Set return
        //
        if (operationInfo.returnQName != null) {
            operationDesc.setReturnQName(operationInfo.returnQName);
            operationDesc.setReturnType(operationInfo.returnXmlType);
            try {
                Class<?> returnClass = classLoader.loadClass(operationInfo.returnJavaType);
                operationDesc.setReturnClass(returnClass);
            } catch (ClassNotFoundException e) {
                throw new OpenEJBException();
            }
        } else if (operationInfo.operationStyle == JaxRpcOperationInfo.OperationStyle.REQUEST_RESPONSE) {
            operationDesc.setReturnQName(null);
            operationDesc.setReturnType(XMLType.AXIS_VOID);
            operationDesc.setReturnClass(void.class);
        }

        // Build faults
        for (JaxRpcFaultInfo faultInfo : operationInfo.faults) {
            FaultDesc faultDesc = buildFaultDesc(faultInfo);
            operationDesc.addFault(faultDesc);
        }

        return operationDesc;
    }

    private ParameterDesc buildParameterDesc(JaxRpcParameterInfo parameterInfo) throws OpenEJBException {
        byte mode = ParameterDesc.modeFromString(parameterInfo.mode.toString());

        boolean inHeader = parameterInfo.soapHeader && parameterInfo.mode.isIn();
        boolean outHeader = parameterInfo.soapHeader  && parameterInfo.mode.isOut();

        Class<?> javaType;
        try {
            javaType = classLoader.loadClass(parameterInfo.javaType);
        } catch (ClassNotFoundException e) {
            throw new OpenEJBException("Unable to load parameter type " + parameterInfo.javaType);
        }

        ParameterDesc parameterDesc = new ParameterDesc(parameterInfo.qname, mode, parameterInfo.xmlType, javaType, inHeader, outHeader);
        return parameterDesc;
    }

    private FaultDesc buildFaultDesc(JaxRpcFaultInfo faultInfo) throws OpenEJBException {
        FaultDesc faultDesc = new FaultDesc(faultInfo.qname, faultInfo.javaType, faultInfo.xmlType, faultInfo.complex);

        ArrayList<ParameterDesc> parameters = new ArrayList<ParameterDesc>();
        for (JaxRpcParameterInfo parameterInfo : faultInfo.parameters) {
            ParameterDesc parameterDesc = buildParameterDesc(parameterInfo);
            parameters.add(parameterDesc);
        }
        faultDesc.setParameters(parameters);

        return faultDesc;
    }

    private void registerType(JaxRpcTypeInfo type, TypeMapping typeMapping) throws OpenEJBException {
        Class javaType;
        try {
            javaType = classLoader.loadClass(type.javaType);
        } catch (ClassNotFoundException e) {
            throw new OpenEJBException("Could not load class for JaxRpc mapping " + type.javaType);
        }

        Class serializerFactoryClass;
        Class deserializerFactoryClass;
        switch (type.serializerType) {
            case ARRAY:
                serializerFactoryClass = ArraySerializerFactory.class;
                deserializerFactoryClass = ArrayDeserializerFactory.class;
                break;
            case ENUM:
                serializerFactoryClass = EnumSerializerFactory.class;
                deserializerFactoryClass = EnumDeserializerFactory.class;
                break;
            case LIST:
                serializerFactoryClass = SimpleListSerializerFactory.class;
                deserializerFactoryClass = SimpleListDeserializerFactory.class;
                break;
            default:

                Class clazz = SOAP_TYPE_MAPPING.getClassForQName(type.xmlType, null, null);
                if (null != clazz) {
                    // Built in SOAP type
                    serializerFactoryClass = SOAP_TYPE_MAPPING.getSerializer(clazz, type.xmlType).getClass();
                    deserializerFactoryClass = SOAP_TYPE_MAPPING.getDeserializer(clazz, type.xmlType, null).getClass();
                } else {
                    clazz = JAXRPC_TYPE_MAPPING.getClassForQName(type.xmlType, null, null);
                    if (null != clazz) {
                        // Built in XML schema type
                        serializerFactoryClass = JAXRPC_TYPE_MAPPING.getSerializer(clazz, type.xmlType).getClass();
                        deserializerFactoryClass = JAXRPC_TYPE_MAPPING.getDeserializer(clazz, type.xmlType, null).getClass();
                    } else {
                        // Unknown type so use the generic Java Beans serializer
                        serializerFactoryClass = BeanSerializerFactory.class;
                        deserializerFactoryClass = BeanDeserializerFactory.class;
                    }
                }
                break;
        }

        SerializerFactory serializerFactory = BaseSerializerFactory.createFactory(serializerFactoryClass, javaType, type.qname);
        DeserializerFactory deserializerFactory = BaseDeserializerFactory.createFactory(deserializerFactoryClass, javaType, type.qname);

        typeMapping.register(javaType, type.qname, serializerFactory, deserializerFactory);
    }
}
