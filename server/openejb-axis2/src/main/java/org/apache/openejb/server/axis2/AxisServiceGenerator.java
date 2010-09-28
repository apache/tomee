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

package org.apache.openejb.server.axis2;

import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.BindingTypeAnnot;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceProviderAnnot;
import org.apache.axis2.jaxws.description.builder.WsdlComposite;
import org.apache.axis2.jaxws.description.builder.WsdlGenerator;
import org.apache.axis2.jaxws.description.builder.converter.JavaClassToDBCConverter;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.openejb.core.webservices.JaxWsUtils;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.server.axis2.util.SimpleWsdlLocator;
import org.apache.ws.commons.schema.utils.NamespaceMap;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AxisServiceGenerator {
    private MessageReceiver messageReceiver;

    public AxisServiceGenerator() {
        this.messageReceiver = new JAXWSMessageReceiver();
    }

    public void setMessageReceiver(MessageReceiver messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    public AxisService getServiceFromClass(Class endpointClass) throws Exception {
        ServiceDescription serviceDescription = DescriptionFactory.createServiceDescription(endpointClass);
        EndpointDescription[] edArray = serviceDescription.getEndpointDescriptions();
        AxisService service = edArray[0].getAxisService();

        if (service.getNameSpacesMap() == null) {
            NamespaceMap map = new NamespaceMap();
            map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX, Java2WSDLConstants.AXIS2_XSD);
            map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX, Java2WSDLConstants.URI_2001_SCHEMA_XSD);
            service.setNameSpacesMap(map);
        }

        String endpointClassName = endpointClass.getName();
        ClassLoader classLoader = endpointClass.getClassLoader();

        service.addParameter(new Parameter(Constants.SERVICE_CLASS, endpointClassName));
        service.setClassLoader(classLoader);

        for (Iterator<AxisOperation> opIterator = service.getOperations(); opIterator.hasNext();) {
            AxisOperation operation = opIterator.next();
            operation.setMessageReceiver(this.messageReceiver);
        }

        Parameter serviceDescriptionParam = new Parameter(EndpointDescription.AXIS_SERVICE_PARAMETER, edArray[0]);
        service.addParameter(serviceDescriptionParam);

        return service;
    }

    public AxisService getServiceFromWSDL(PortData portData, Class endpointClass) throws Exception {
        if (portData.getWsdlUrl() == null) {
            throw new Exception("WSDL file is required.");
        }

        String endpointClassName = endpointClass.getName();
        ClassLoader classLoader = endpointClass.getClassLoader();

        QName serviceQName = portData.getWsdlService();
        if (serviceQName == null) {
            serviceQName = JaxWsUtils.getServiceQName(endpointClass);
        }

        QName portQName = portData.getWsdlPort();
        if (portQName == null) {
            portQName = JaxWsUtils.getPortQName(endpointClass);
        }

        Definition wsdlDefinition = readWSDL(portData.getWsdlUrl());

        Service wsdlService = wsdlDefinition.getService(serviceQName);
        if (wsdlService == null) {
            throw new Exception("Service '" + serviceQName + "' not found in WSDL");
        }

        Port port = wsdlService.getPort(portQName.getLocalPart());
        if (port == null) {
            throw new Exception("Port '" + portQName.getLocalPart() + "' not found in WSDL");
        }

        Binding binding = port.getBinding();
        List extElements = binding.getExtensibilityElements();
        Iterator extElementsIterator = extElements.iterator();
        String bindingS = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING; //this is the default.
        while (extElementsIterator.hasNext()) {
            Object o = extElementsIterator.next();
            if (o instanceof SOAPBinding) {
                SOAPBinding sp = (SOAPBinding) o;
                if (sp.getElementType().getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/soap/")) {
                    //todo:  how to we tell if it is MTOM or not.
                    bindingS = javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING;
                }
            } else if (o instanceof SOAP12Binding) {
                SOAP12Binding sp = (SOAP12Binding) o;
                if (sp.getElementType().getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/soap12/")) {
                    //todo:  how to we tell if it is MTOM or not.
                    bindingS = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING;
                }
            } else if (o instanceof HTTPBinding) {
                HTTPBinding sp = (HTTPBinding) o;
                if (sp.getElementType().getNamespaceURI().equals("http://www.w3.org/2004/08/wsdl/http")) {
                    bindingS = javax.xml.ws.http.HTTPBinding.HTTP_BINDING;
                }
            }
        }

        Class endPointClass = classLoader.loadClass(endpointClassName);
        JavaClassToDBCConverter converter = new JavaClassToDBCConverter(endPointClass);
        HashMap<String, DescriptionBuilderComposite> dbcMap = converter.produceDBC();

        DescriptionBuilderComposite dbc = dbcMap.get(endpointClassName);
        dbc.setClassLoader(classLoader);
        dbc.setWsdlDefinition(wsdlDefinition);
        dbc.setClassName(endpointClassName);
        dbc.setCustomWsdlGenerator(new WSDLGeneratorImpl(wsdlDefinition));

        if (dbc.getWebServiceAnnot() != null) { //information specified in .wsdl should overwrite annotation.
            WebServiceAnnot serviceAnnot = dbc.getWebServiceAnnot();
            serviceAnnot.setPortName(portQName.getLocalPart());
            serviceAnnot.setServiceName(serviceQName.getLocalPart());
            serviceAnnot.setTargetNamespace(serviceQName.getNamespaceURI());
            if (dbc.getBindingTypeAnnot() != null && bindingS != null && !bindingS.equals("")) {
                BindingTypeAnnot bindingAnnot = dbc.getBindingTypeAnnot();
                bindingAnnot.setValue(bindingS);
            }
        } else if (dbc.getWebServiceProviderAnnot() != null) {
            WebServiceProviderAnnot serviceProviderAnnot = dbc.getWebServiceProviderAnnot();
            serviceProviderAnnot.setPortName(portQName.getLocalPart());
            serviceProviderAnnot.setServiceName(serviceQName.getLocalPart());
            serviceProviderAnnot.setTargetNamespace(serviceQName.getNamespaceURI());
            if (dbc.getBindingTypeAnnot() != null && bindingS != null && !bindingS.equals("")) {
                BindingTypeAnnot bindingAnnot = dbc.getBindingTypeAnnot();
                bindingAnnot.setValue(bindingS);
            }
        }

        AxisService service = getService(dbcMap);

        service.setName(serviceQName.getLocalPart());
        service.setEndpointName(portQName.getLocalPart());

        for (Iterator<AxisOperation> opIterator = service.getOperations(); opIterator.hasNext();) {
            AxisOperation operation = opIterator.next();
            operation.setMessageReceiver(this.messageReceiver);
            String MEP = operation.getMessageExchangePattern();
            if (!WSDLUtil.isOutputPresentForMEP(MEP)) {
                List<MethodDescriptionComposite> mdcList = dbc.getMethodDescriptionComposite(operation.getName().toString());
                for (Iterator<MethodDescriptionComposite> mIterator = mdcList.iterator(); mIterator.hasNext();) {
                    MethodDescriptionComposite mdc = mIterator.next();
                    //TODO: JAXWS spec says need to check Holder param exist before taking a method as OneWay
                    mdc.setOneWayAnnot(true);
                }
            }
        }

        return service;
    }

    private AxisService getService(HashMap<String, DescriptionBuilderComposite> dbcMap) {
        return getEndpointDescription(dbcMap).getAxisService();
    }

    private EndpointDescription getEndpointDescription(HashMap<String, DescriptionBuilderComposite> dbcMap) {
        List<ServiceDescription> serviceDescList = DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
        if (serviceDescList == null || serviceDescList.isEmpty()) {
            throw new RuntimeException("No service");
        }
        ServiceDescription serviceDescription = serviceDescList.get(0);
        EndpointDescription[] edArray = serviceDescription.getEndpointDescriptions();
        if (edArray == null || edArray.length == 0) {
            throw new RuntimeException("No endpoint");
        }
        return edArray[0];
    }

    private static class WSDLGeneratorImpl implements WsdlGenerator {
        private Definition def;

        public WSDLGeneratorImpl(Definition def) {
            this.def = def;
        }

        public WsdlComposite generateWsdl(String implClass, EndpointDescription endpointDesc)
            throws WebServiceException {
            // Need WSDL generation code
            WsdlComposite composite = new WsdlComposite();
            composite.setWsdlFileName(implClass);
            HashMap<String, Definition> testMap = new HashMap<String, Definition>();
            testMap.put(composite.getWsdlFileName(), def);
            composite.setWsdlDefinition(testMap);
            return composite;
        }
    }

    protected Definition readWSDL(URL wsdlUrl) throws IOException, WSDLException {
        Definition wsdlDefinition = null;
        InputStream wsdlStream = null;
        try {
            wsdlStream = wsdlUrl.openStream();
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.importDocuments", true);
            reader.setFeature("javax.wsdl.verbose", false);
            SimpleWsdlLocator wsdlLocator = new SimpleWsdlLocator(wsdlUrl.toString());
            wsdlDefinition = reader.readWSDL(wsdlLocator);
        } finally {
            if (wsdlStream != null) {
                wsdlStream.close();
            }
        }
        return wsdlDefinition;
    }

    public static EndpointDescription getEndpointDescription(AxisService service) {
        Parameter param = service.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);
        return (param == null) ? null : (EndpointDescription) param.getValue();
    }

    public static boolean isSOAP11(AxisService service) {
        EndpointDescription desc = AxisServiceGenerator.getEndpointDescription(service);
        return javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING.equals(desc.getBindingType()) || javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(desc.getBindingType());
    }

}
