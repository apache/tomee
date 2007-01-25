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

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

public class ServiceProvider implements java.io.Serializable {


    private java.lang.String _id;

    private java.lang.String _providerType;

    private java.lang.String _displayName;

    private java.lang.String _description;

    private java.lang.String _className;

    private java.lang.String _constructor;

    private java.lang.String _content = "";

    private org.apache.openejb.config.sys.PropertiesFile _propertiesFile;

    private org.apache.openejb.config.sys.Lookup _lookup;


    public ServiceProvider() {
        super();
        setContent("");
    }


    public String getConstructor() {
        return _constructor;
    }

    public java.lang.String getClassName() {
        return this._className;
    }

    public java.lang.String getContent() {
        return this._content;
    }

    public java.lang.String getDescription() {
        return this._description;
    }

    public java.lang.String getDisplayName() {
        return this._displayName;
    }

    public java.lang.String getId() {
        return this._id;
    }

    public org.apache.openejb.config.sys.Lookup getLookup() {
        return this._lookup;
    }

    public org.apache.openejb.config.sys.PropertiesFile getPropertiesFile() {
        return this._propertiesFile;
    }

    public java.lang.String getProviderType() {
        return this._providerType;
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

    public void setConstructor(String constructor) {
        this._constructor = constructor;
    }

    public void setClassName(java.lang.String className) {
        this._className = className;
    }

    public void setContent(java.lang.String content) {
        this._content = content;
    }

    public void setDescription(java.lang.String description) {
        this._description = description;
    }

    public void setDisplayName(java.lang.String displayName) {
        this._displayName = displayName;
    }

    public void setId(java.lang.String id) {
        this._id = id;
    }

    public void setLookup(org.apache.openejb.config.sys.Lookup lookup) {
        this._lookup = lookup;
    }

    public void setPropertiesFile(org.apache.openejb.config.sys.PropertiesFile propertiesFile) {
        this._propertiesFile = propertiesFile;
    }

    public void setProviderType(java.lang.String providerType) {
        this._providerType = providerType;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.config.sys.ServiceProvider) Unmarshaller.unmarshal(org.apache.openejb.config.sys.ServiceProvider.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
