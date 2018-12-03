/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistry;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class ReadOnlyServiceDesc extends JavaServiceDesc {
    private JavaServiceDesc serviceDesc;

    public ReadOnlyServiceDesc(JavaServiceDesc serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    /**
     *
     * @return
     */
    @Override
    public Class getImplClass() {
        return serviceDesc.getImplClass();
    }

    /**
     *
     * @param implClass implClass
     */
    @Override
    public void setImplClass(Class implClass) {
        serviceDesc.setImplClass(implClass);
    }

    /**
     *
     * @return
     */
    @Override
    public ArrayList getStopClasses() {
        return serviceDesc.getStopClasses();
    }

    /**
     *
     * @param stopClasses stopClasses
     */
    @Override
    public void setStopClasses(ArrayList stopClasses) {
    }

    @Override
    public void loadServiceDescByIntrospection() {
        serviceDesc.loadServiceDescByIntrospection();
    }

    /**
     *
     * @param implClass implClass
     */
    @Override
    public void loadServiceDescByIntrospection(Class implClass) {
        serviceDesc.loadServiceDescByIntrospection(implClass);
    }

    /**
     *
     * @param cls cls
     * @param tm tm
     */
    @Override
    public void loadServiceDescByIntrospection(Class cls, TypeMapping tm) {
        serviceDesc.loadServiceDescByIntrospection(cls, tm);
    }

    /**
     *
     * @return
     */
    @Override
    public Style getStyle() {
        return serviceDesc.getStyle();
    }

    /**
     *
     * @param style style
     */
    @Override
    public void setStyle(Style style) {
    }

    /**
     *
     * @return
     */
    @Override
    public Use getUse() {
        return serviceDesc.getUse();
    }

    /**
     *
     * @param use
     */
    @Override
    public void setUse(Use use) {
    }

    /**
     *
     * @return
     */
    @Override
    public String getWSDLFile() {
        return serviceDesc.getWSDLFile();
    }

    /**
     *
     * @param wsdlFileName Name of the WSDL
     */
    @Override
    public void setWSDLFile(String wsdlFileName) {
    }

    /**
     *
     * @return List of Allowed Methods
     */
    @Override
    public List getAllowedMethods() {
        return serviceDesc.getAllowedMethods();
    }

    /**
     *
     * @param allowedMethods
     */
    @Override
    public void setAllowedMethods(List allowedMethods) {
    }

    /**
     *
     * @return TypeMapping
     */
    @Override
    public TypeMapping getTypeMapping() {
        return serviceDesc.getTypeMapping();
    }

    /**
     *
     * @param tm Set TypeMapping
     */
    @Override
    public void setTypeMapping(TypeMapping tm) {
    }

    /**
     *
     * @return Name
     */
    @Override
    public String getName() {
        return serviceDesc.getName();
    }

    /**
     *
     * @param name Set Name
     */
    @Override
    public void setName(String name) {
    }

    /**
     *
     * @return Documentation
     */
    @Override
    public String getDocumentation() {
        return serviceDesc.getDocumentation();
    }

    /**
     *
     * @param documentation Set Documentation
     */
    @Override
    public void setDocumentation(String documentation) {
    }

    /**
     *
     * @param operation Remove Operation
     */
    @Override
    public void removeOperationDesc(OperationDesc operation) {
    }

    /**
     *
     * @param operation Add Operation
     */
    @Override
    public void addOperationDesc(OperationDesc operation) {
    }

    /**
     *
     * @return List Of Operations
     */
    @Override
    public ArrayList getOperations() {
        return serviceDesc.getOperations();
    }

    /**
     *
     * @param methodName Method Name
     * @return Array Of Operation By Name
     */
    @Override
    public OperationDesc[] getOperationsByName(String methodName) {
        return serviceDesc.getOperationsByName(methodName);
    }

    /**
     *
     * @param methodName Method Name
     * @return Operation By Name
     */
    @Override
    public OperationDesc getOperationByName(String methodName) {
        return serviceDesc.getOperationByName(methodName);
    }

    /**
     *
     * @param qname QName
     * @return OperationDesc by Element QName
     */
    @Override
    public OperationDesc getOperationByElementQName(QName qname) {
        return serviceDesc.getOperationByElementQName(qname);
    }

    /**
     *
     * @param qname QName
     * @return Array Of OperationDesc by QName
     */
    @Override
    public OperationDesc[] getOperationsByQName(QName qname) {
        return serviceDesc.getOperationsByQName(qname);
    }

    /**
     *
     * @param namespaces List of Namespaces
     */
    @Override
    public void setNamespaceMappings(List namespaces) {
    }

    /**
     *
     * @return Default Namespace
     */
    @Override
    public String getDefaultNamespace() {
        return serviceDesc.getDefaultNamespace();
    }

    /**
     *
     * @param namespace Set Namespace
     */
    @Override
    public void setDefaultNamespace(String namespace) {
    }

    /**
     *
     * @param name Name of the property
     * @param value Value of the property
     */
    @Override
    public void setProperty(String name, Object value) {
        serviceDesc.setProperty(name, value);
    }

    /**
     *
     * @param name 
     * @return
     */
    @Override
    public Object getProperty(String name) {
        return serviceDesc.getProperty(name);
    }

    /**
     *
     * @return Endpoint URL
     */
    @Override
    public String getEndpointURL() {
        return serviceDesc.getEndpointURL();
    }

    /**
     *
     * @param endpointURL Endpoint URL
     */
    @Override
    public void setEndpointURL(String endpointURL) {
    }

    /**
     *
     * @return TypeMappingRegistry
     */
    @Override
    public TypeMappingRegistry getTypeMappingRegistry() {
        return serviceDesc.getTypeMappingRegistry();
    }

    /**
     *
     * @param tmr
     */
    @Override
    public void setTypeMappingRegistry(TypeMappingRegistry tmr) {
    }

    /**
     *
     * @return boolean 
     */
    @Override
    public boolean isInitialized() {
        return serviceDesc.isInitialized();
    }

    /**
     *
     * @return boolean
     */
    @Override
    public boolean isWrapped() {
        return serviceDesc.isWrapped();
    }

    /**
     *
     * @return List of Disallowed Methods
     */
    @Override
    public List getDisallowedMethods() {
        return serviceDesc.getDisallowedMethods();
    }

    /**
     *
     * @param disallowedMethods Set List of Disallowed Methods
     */
    @Override
    public void setDisallowedMethods(List disallowedMethods) {
    }
}
