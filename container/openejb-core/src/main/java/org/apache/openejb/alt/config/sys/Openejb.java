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
/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 0.9.5.3</a>, using an XML
 * Schema.
 * $Id$
 */

package org.apache.openejb.alt.config.sys;

//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

public class Openejb implements java.io.Serializable {


    private java.lang.String _content = "";

    private java.util.Vector _containerList;

    private java.util.Vector _jndiProviderList;

    private org.apache.openejb.alt.config.sys.SecurityService _securityService;

    private org.apache.openejb.alt.config.sys.TransactionService _transactionService;

    private org.apache.openejb.alt.config.sys.ConnectionManager _connectionManager;

    private org.apache.openejb.alt.config.sys.ProxyFactory _proxyFactory;

    private java.util.Vector _connectorList;

    private java.util.Vector _resourceList;

    private java.util.Vector _deploymentsList;


    public Openejb() {
        super();
        setContent("");
        _containerList = new Vector();
        _jndiProviderList = new Vector();
        _connectorList = new Vector();
        _resourceList = new Vector();
        _deploymentsList = new Vector();
    }


    public void addConnector(org.apache.openejb.alt.config.sys.Connector vConnector)
            throws java.lang.IndexOutOfBoundsException {
        _connectorList.addElement(vConnector);
    }

    public void addConnector(int index, org.apache.openejb.alt.config.sys.Connector vConnector)
            throws java.lang.IndexOutOfBoundsException {
        _connectorList.insertElementAt(vConnector, index);
    }

    public void addContainer(org.apache.openejb.alt.config.sys.Container vContainer)
            throws java.lang.IndexOutOfBoundsException {
        _containerList.addElement(vContainer);
    }

    public void addContainer(int index, org.apache.openejb.alt.config.sys.Container vContainer)
            throws java.lang.IndexOutOfBoundsException {
        _containerList.insertElementAt(vContainer, index);
    }

    public void addDeployments(org.apache.openejb.alt.config.sys.Deployments vDeployments)
            throws java.lang.IndexOutOfBoundsException {
        _deploymentsList.addElement(vDeployments);
    }

    public void addDeployments(int index, org.apache.openejb.alt.config.sys.Deployments vDeployments)
            throws java.lang.IndexOutOfBoundsException {
        _deploymentsList.insertElementAt(vDeployments, index);
    }

    public void addJndiProvider(org.apache.openejb.alt.config.sys.JndiProvider vJndiProvider)
            throws java.lang.IndexOutOfBoundsException {
        _jndiProviderList.addElement(vJndiProvider);
    }

    public void addJndiProvider(int index, org.apache.openejb.alt.config.sys.JndiProvider vJndiProvider)
            throws java.lang.IndexOutOfBoundsException {
        _jndiProviderList.insertElementAt(vJndiProvider, index);
    }

    public void addResource(org.apache.openejb.alt.config.sys.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {
        _resourceList.addElement(vResource);
    }

    public void addResource(int index, org.apache.openejb.alt.config.sys.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {
        _resourceList.insertElementAt(vResource, index);
    }

    public java.util.Enumeration enumerateConnector() {
        return _connectorList.elements();
    }

    public java.util.Enumeration enumerateContainer() {
        return _containerList.elements();
    }

    public java.util.Enumeration enumerateDeployments() {
        return _deploymentsList.elements();
    }

    public java.util.Enumeration enumerateJndiProvider() {
        return _jndiProviderList.elements();
    }

    public java.util.Enumeration enumerateResource() {
        return _resourceList.elements();
    }

    public org.apache.openejb.alt.config.sys.ConnectionManager getConnectionManager() {
        return this._connectionManager;
    }

    public org.apache.openejb.alt.config.sys.Connector getConnector(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _connectorList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.alt.config.sys.Connector) _connectorList.elementAt(index);
    }

    public org.apache.openejb.alt.config.sys.Connector[] getConnector() {
        int size = _connectorList.size();
        org.apache.openejb.alt.config.sys.Connector[] mArray = new org.apache.openejb.alt.config.sys.Connector[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.alt.config.sys.Connector) _connectorList.elementAt(index);
        }
        return mArray;
    }

    public int getConnectorCount() {
        return _connectorList.size();
    }

    public org.apache.openejb.alt.config.sys.Container getContainer(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _containerList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.alt.config.sys.Container) _containerList.elementAt(index);
    }

    public org.apache.openejb.alt.config.sys.Container[] getContainer() {
        int size = _containerList.size();
        org.apache.openejb.alt.config.sys.Container[] mArray = new org.apache.openejb.alt.config.sys.Container[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.alt.config.sys.Container) _containerList.elementAt(index);
        }
        return mArray;
    }

    public int getContainerCount() {
        return _containerList.size();
    }

    public java.lang.String getContent() {
        return this._content;
    }

    public org.apache.openejb.alt.config.sys.Deployments getDeployments(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _deploymentsList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.alt.config.sys.Deployments) _deploymentsList.elementAt(index);
    }

    public org.apache.openejb.alt.config.sys.Deployments[] getDeployments() {
        int size = _deploymentsList.size();
        org.apache.openejb.alt.config.sys.Deployments[] mArray = new org.apache.openejb.alt.config.sys.Deployments[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.alt.config.sys.Deployments) _deploymentsList.elementAt(index);
        }
        return mArray;
    }

    public int getDeploymentsCount() {
        return _deploymentsList.size();
    }

    public org.apache.openejb.alt.config.sys.JndiProvider getJndiProvider(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _jndiProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.alt.config.sys.JndiProvider) _jndiProviderList.elementAt(index);
    }

    public org.apache.openejb.alt.config.sys.JndiProvider[] getJndiProvider() {
        int size = _jndiProviderList.size();
        org.apache.openejb.alt.config.sys.JndiProvider[] mArray = new org.apache.openejb.alt.config.sys.JndiProvider[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.alt.config.sys.JndiProvider) _jndiProviderList.elementAt(index);
        }
        return mArray;
    }

    public int getJndiProviderCount() {
        return _jndiProviderList.size();
    }

    public org.apache.openejb.alt.config.sys.ProxyFactory getProxyFactory() {
        return this._proxyFactory;
    }

    public org.apache.openejb.alt.config.sys.Resource getResource(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _resourceList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.alt.config.sys.Resource) _resourceList.elementAt(index);
    }

    public org.apache.openejb.alt.config.sys.Resource[] getResource() {
        int size = _resourceList.size();
        org.apache.openejb.alt.config.sys.Resource[] mArray = new org.apache.openejb.alt.config.sys.Resource[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.alt.config.sys.Resource) _resourceList.elementAt(index);
        }
        return mArray;
    }

    public int getResourceCount() {
        return _resourceList.size();
    }

    public org.apache.openejb.alt.config.sys.SecurityService getSecurityService() {
        return this._securityService;
    }

    public org.apache.openejb.alt.config.sys.TransactionService getTransactionService() {
        return this._transactionService;
    }

    public boolean isValid() {
        try {
            validate();
        }
        catch (org.exolab.castor.xml.ValidationException vex) {
            return false;
        }
        return true;
    }

    public void marshal(java.io.Writer out)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, out);
    }

    public void marshal(org.xml.sax.ContentHandler handler)
            throws java.io.IOException, org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {

        Marshaller.marshal(this, handler);
    }

    public void removeAllConnector() {
        _connectorList.removeAllElements();
    }

    public void removeAllContainer() {
        _containerList.removeAllElements();
    }

    public void removeAllDeployments() {
        _deploymentsList.removeAllElements();
    }

    public void removeAllJndiProvider() {
        _jndiProviderList.removeAllElements();
    }

    public void removeAllResource() {
        _resourceList.removeAllElements();
    }

    public org.apache.openejb.alt.config.sys.Connector removeConnector(int index) {
        java.lang.Object obj = _connectorList.elementAt(index);
        _connectorList.removeElementAt(index);
        return (org.apache.openejb.alt.config.sys.Connector) obj;
    }

    public org.apache.openejb.alt.config.sys.Container removeContainer(int index) {
        java.lang.Object obj = _containerList.elementAt(index);
        _containerList.removeElementAt(index);
        return (org.apache.openejb.alt.config.sys.Container) obj;
    }

    public org.apache.openejb.alt.config.sys.Deployments removeDeployments(int index) {
        java.lang.Object obj = _deploymentsList.elementAt(index);
        _deploymentsList.removeElementAt(index);
        return (org.apache.openejb.alt.config.sys.Deployments) obj;
    }

    public org.apache.openejb.alt.config.sys.JndiProvider removeJndiProvider(int index) {
        java.lang.Object obj = _jndiProviderList.elementAt(index);
        _jndiProviderList.removeElementAt(index);
        return (org.apache.openejb.alt.config.sys.JndiProvider) obj;
    }

    public org.apache.openejb.alt.config.sys.Resource removeResource(int index) {
        java.lang.Object obj = _resourceList.elementAt(index);
        _resourceList.removeElementAt(index);
        return (org.apache.openejb.alt.config.sys.Resource) obj;
    }

    public void setConnectionManager(org.apache.openejb.alt.config.sys.ConnectionManager connectionManager) {
        this._connectionManager = connectionManager;
    }

    public void setConnector(int index, org.apache.openejb.alt.config.sys.Connector vConnector)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _connectorList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _connectorList.setElementAt(vConnector, index);
    }

    public void setConnector(org.apache.openejb.alt.config.sys.Connector[] connectorArray) {

        _connectorList.removeAllElements();
        for (int i = 0; i < connectorArray.length; i++) {
            _connectorList.addElement(connectorArray[i]);
        }
    }

    public void setContainer(int index, org.apache.openejb.alt.config.sys.Container vContainer)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _containerList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _containerList.setElementAt(vContainer, index);
    }

    public void setContainer(org.apache.openejb.alt.config.sys.Container[] containerArray) {

        _containerList.removeAllElements();
        for (int i = 0; i < containerArray.length; i++) {
            _containerList.addElement(containerArray[i]);
        }
    }

    public void setContent(java.lang.String content) {
        this._content = content;
    }

    public void setDeployments(int index, org.apache.openejb.alt.config.sys.Deployments vDeployments)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _deploymentsList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _deploymentsList.setElementAt(vDeployments, index);
    }

    public void setDeployments(org.apache.openejb.alt.config.sys.Deployments[] deploymentsArray) {

        _deploymentsList.removeAllElements();
        for (int i = 0; i < deploymentsArray.length; i++) {
            _deploymentsList.addElement(deploymentsArray[i]);
        }
    }

    public void setJndiProvider(int index, org.apache.openejb.alt.config.sys.JndiProvider vJndiProvider)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _jndiProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _jndiProviderList.setElementAt(vJndiProvider, index);
    }

    public void setJndiProvider(org.apache.openejb.alt.config.sys.JndiProvider[] jndiProviderArray) {

        _jndiProviderList.removeAllElements();
        for (int i = 0; i < jndiProviderArray.length; i++) {
            _jndiProviderList.addElement(jndiProviderArray[i]);
        }
    }

    public void setProxyFactory(org.apache.openejb.alt.config.sys.ProxyFactory proxyFactory) {
        this._proxyFactory = proxyFactory;
    }

    public void setResource(int index, org.apache.openejb.alt.config.sys.Resource vResource)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _resourceList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _resourceList.setElementAt(vResource, index);
    }

    public void setResource(org.apache.openejb.alt.config.sys.Resource[] resourceArray) {

        _resourceList.removeAllElements();
        for (int i = 0; i < resourceArray.length; i++) {
            _resourceList.addElement(resourceArray[i]);
        }
    }

    public void setSecurityService(org.apache.openejb.alt.config.sys.SecurityService securityService) {
        this._securityService = securityService;
    }

    public void setTransactionService(org.apache.openejb.alt.config.sys.TransactionService transactionService) {
        this._transactionService = transactionService;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.alt.config.sys.Openejb) Unmarshaller.unmarshal(org.apache.openejb.alt.config.sys.Openejb.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
