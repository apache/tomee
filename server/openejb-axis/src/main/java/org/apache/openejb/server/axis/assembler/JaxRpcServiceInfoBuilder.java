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
import org.apache.openejb.jee.JavaWsdlMapping;
import org.apache.openejb.jee.PortComponent;
import org.apache.openejb.jee.ServiceEndpointInterfaceMapping;
import org.apache.openejb.jee.ServiceEndpointMethodMapping;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JaxRpcServiceInfoBuilder {
    private final JavaWsdlMapping javaWsdlMapping;
    private final XmlSchemaInfo schemaInfo;
    private final PortComponent portComponent;
    private final Port port;
    private final String wsdlFile;
    private final ClassLoader classLoader;
    private JaxRpcServiceInfo serviceInfo;

    public JaxRpcServiceInfoBuilder(final JavaWsdlMapping javaWsdlMapping, final XmlSchemaInfo schemaInfo, final PortComponent portComponent, final Port port, final String wsdlFile, final ClassLoader classLoader) {
        this.javaWsdlMapping = javaWsdlMapping;
        this.schemaInfo = schemaInfo;
        this.portComponent = portComponent;
        this.port = port;
        this.wsdlFile = wsdlFile;
        this.classLoader = classLoader;
    }

    public JaxRpcServiceInfo createServiceInfo() throws OpenEJBException {
        final Class serviceEndpointInterface;
        try {
            serviceEndpointInterface = classLoader.loadClass(portComponent.getServiceEndpointInterface());
        } catch (final ClassNotFoundException e) {
            throw new OpenEJBException("Unable to load the service-endpoint interface for port-component " + portComponent.getPortComponentName(), e);
        }

        serviceInfo = new JaxRpcServiceInfo();
        serviceInfo.name = portComponent.getWsdlPort().toString();
        serviceInfo.serviceEndpointInterface = portComponent.getServiceEndpointInterface();
        serviceInfo.endpointURL = getAddressLocation(port.getExtensibilityElements());
        serviceInfo.wsdlFile = wsdlFile;
        serviceInfo.defaultBindingStyle = getStyle(port.getBinding());

        // The service is using lightweight mappings if there are no Java to XML service mappings
        final boolean isLightweight = javaWsdlMapping.getServiceEndpointInterfaceMapping().isEmpty();

        //
        // Map Operations
        //
        final Set wrapperElementQNames = buildOperations(port.getBinding(), serviceEndpointInterface, isLightweight);

        //
        // Map Types
        //
        final Collection<JaxRpcTypeInfo> types;
        if (isLightweight) {
            final LightweightTypeInfoBuilder builder = new LightweightTypeInfoBuilder(javaWsdlMapping, schemaInfo, classLoader);
            types = builder.buildTypeInfo();
        } else {
            final HeavyweightTypeInfoBuilder builder = new HeavyweightTypeInfoBuilder(javaWsdlMapping, schemaInfo, classLoader, wrapperElementQNames, serviceInfo.operations, serviceInfo.defaultBindingStyle.isEncoded());
            types = builder.buildTypeInfo();
        }
        serviceInfo.types.addAll(types);

        return serviceInfo;
    }

    private Set<QName> buildOperations(final Binding binding, final Class serviceEndpointInterface, final boolean lightweight) throws OpenEJBException {
        final Set<QName> wrappedElementQNames = new HashSet<QName>();


        for (final Object op : binding.getBindingOperations()) {
            final BindingOperation bindingOperation = (BindingOperation) op;
            final String operationName = bindingOperation.getOperation().getName();

            if (lightweight) {
                // Lightweight mappings are solely based on the Java method
                final Method method = getMethodForOperation(operationName, serviceEndpointInterface);

                // Build the operation info using the method
                final LightweightOperationInfoBuilder operationInfoBuilder = new LightweightOperationInfoBuilder(bindingOperation, method);
                final JaxRpcOperationInfo operationInfo = operationInfoBuilder.buildOperationInfo();
                serviceInfo.operations.add(operationInfo);
            } else {
                // Heavyweight mappings are solely based on the Java to XML mapping declarations
                final ServiceEndpointMethodMapping methodMapping = getMethodMappingForOperation(operationName, serviceEndpointInterface);

                // Build the operation info using the Java to XML method mapping
                final HeavyweightOperationInfoBuilder operationInfoBuilder = new HeavyweightOperationInfoBuilder(bindingOperation, methodMapping, javaWsdlMapping, schemaInfo);
                final JaxRpcOperationInfo operationInfo = operationInfoBuilder.buildOperationInfo();
                serviceInfo.operations.add(operationInfo);

                // remember wrapped elements for type mapping
                final Set<QName> wrappedElementQNamesForOper = operationInfoBuilder.getWrapperElementQNames();
                wrappedElementQNames.addAll(wrappedElementQNamesForOper);
            }
        }

        return wrappedElementQNames;
    }

    private BindingStyle getStyle(final Binding binding) throws OpenEJBException {
        final SOAPBinding soapBinding = getExtensibilityElement(SOAPBinding.class, binding.getExtensibilityElements());
        final String styleString = soapBinding.getStyle();

        final BindingInput bindingInput = ((BindingOperation) binding.getBindingOperations().get(0)).getBindingInput();
        final SOAPBody soapBody = getExtensibilityElement(SOAPBody.class, bindingInput.getExtensibilityElements());
        final String useString = soapBody.getUse();

        final BindingStyle bindingStyle = BindingStyle.getBindingStyle(styleString, useString);
        return bindingStyle;
    }

    private String getAddressLocation(final List extensibilityElements) throws OpenEJBException {
        final SOAPAddress soapAddress = getExtensibilityElement(SOAPAddress.class, extensibilityElements);
        final String locationURIString = soapAddress.getLocationURI();
        return locationURIString;
    }

    private Method getMethodForOperation(final String operationName, final Class serviceEndpointInterface) throws OpenEJBException {
        Method found = null;
        for (final Method method : serviceEndpointInterface.getMethods()) {
            if (method.getName().equals(operationName)) {
                if (found != null) {
                    throw new OpenEJBException("Overloaded methods are not allowed in lightweight mappings");
                }
                found = method;
            }
        }
        if (found == null) {
            throw new OpenEJBException("No method found for operation named " + operationName);
        }
        return found;
    }

    private ServiceEndpointMethodMapping getMethodMappingForOperation(final String operationName, final Class serviceEndpointInterface) throws OpenEJBException {
        // get mapping for service endpoint interface
        final ServiceEndpointInterfaceMapping interfaceMapping = javaWsdlMapping.getServiceEndpointInterfaceMappingMap().get(serviceEndpointInterface.getName());
        if (interfaceMapping == null) {
            throw new OpenEJBException("No java-wsdl mapping found for the service interface " + serviceEndpointInterface);
        }

        // match by operation name
        for (final ServiceEndpointMethodMapping methodMapping : interfaceMapping.getServiceEndpointMethodMapping()) {
            if (operationName.equals(methodMapping.getWsdlOperation())) {
                return methodMapping;
            }
        }

        // failed - throw nice exception message
        final StringBuffer availOps = new StringBuffer(128);
        for (final ServiceEndpointMethodMapping methodMapping : interfaceMapping.getServiceEndpointMethodMapping()) {
            if (availOps.length() > 0) availOps.append(",");
            availOps.append(methodMapping.getWsdlOperation());
        }
        throw new OpenEJBException("No method found for operation named '" + operationName + "'. Available operations: " + availOps);
    }

    public static <T extends ExtensibilityElement> T getExtensibilityElement(final Class<T> clazz, final List extensibilityElements) throws OpenEJBException {
        for (final Object o : extensibilityElements) {
            final ExtensibilityElement extensibilityElement = (ExtensibilityElement) o;
            if (clazz.isAssignableFrom(extensibilityElement.getClass())) {
                return clazz.cast(extensibilityElement);
            }
        }
        throw new OpenEJBException("No element of class " + clazz.getName() + " found");
    }
}