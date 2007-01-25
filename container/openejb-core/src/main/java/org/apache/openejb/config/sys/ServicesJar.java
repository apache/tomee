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

package org.apache.openejb.config.sys;

//---------------------------------/

import java.util.Vector;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

public class ServicesJar implements java.io.Serializable {


    private java.util.Vector _serviceProviderList;


    public ServicesJar() {
        super();
        _serviceProviderList = new Vector();
    }


    public void addServiceProvider(org.apache.openejb.config.sys.ServiceProvider vServiceProvider)
            throws java.lang.IndexOutOfBoundsException {
        _serviceProviderList.addElement(vServiceProvider);
    }

    public void addServiceProvider(int index, org.apache.openejb.config.sys.ServiceProvider vServiceProvider)
            throws java.lang.IndexOutOfBoundsException {
        _serviceProviderList.insertElementAt(vServiceProvider, index);
    }

    public java.util.Enumeration enumerateServiceProvider() {
        return _serviceProviderList.elements();
    }

    public org.apache.openejb.config.sys.ServiceProvider getServiceProvider(int index)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _serviceProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }

        return (org.apache.openejb.config.sys.ServiceProvider) _serviceProviderList.elementAt(index);
    }

    public org.apache.openejb.config.sys.ServiceProvider[] getServiceProvider() {
        int size = _serviceProviderList.size();
        org.apache.openejb.config.sys.ServiceProvider[] mArray = new org.apache.openejb.config.sys.ServiceProvider[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (org.apache.openejb.config.sys.ServiceProvider) _serviceProviderList.elementAt(index);
        }
        return mArray;
    }

    public int getServiceProviderCount() {
        return _serviceProviderList.size();
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

    public void removeAllServiceProvider() {
        _serviceProviderList.removeAllElements();
    }

    public org.apache.openejb.config.sys.ServiceProvider removeServiceProvider(int index) {
        java.lang.Object obj = _serviceProviderList.elementAt(index);
        _serviceProviderList.removeElementAt(index);
        return (org.apache.openejb.config.sys.ServiceProvider) obj;
    }

    public void setServiceProvider(int index, org.apache.openejb.config.sys.ServiceProvider vServiceProvider)
            throws java.lang.IndexOutOfBoundsException {

        if ((index < 0) || (index > _serviceProviderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        _serviceProviderList.setElementAt(vServiceProvider, index);
    }

    public void setServiceProvider(org.apache.openejb.config.sys.ServiceProvider[] serviceProviderArray) {

        _serviceProviderList.removeAllElements();
        for (int i = 0; i < serviceProviderArray.length; i++) {
            _serviceProviderList.addElement(serviceProviderArray[i]);
        }
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.sys.ServicesJar) Unmarshaller.unmarshal(org.apache.openejb.config.sys.ServicesJar.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
