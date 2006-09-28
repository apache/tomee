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

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

public class SecurityService implements java.io.Serializable, org.apache.openejb.alt.config.Service {


    private java.lang.String _id;

    private java.lang.String _provider;

    private java.lang.String _jar;

    private java.lang.String _content = "";


    public SecurityService() {
        super();
        setContent("");
    }


    public java.lang.String getContent() {
        return this._content;
    }

    public java.lang.String getId() {
        return this._id;
    }

    public java.lang.String getJar() {
        return this._jar;
    }

    public java.lang.String getProvider() {
        return this._provider;
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

    public void setContent(java.lang.String content) {
        this._content = content;
    }

    public void setId(java.lang.String id) {
        this._id = id;
    }

    public void setJar(java.lang.String jar) {
        this._jar = jar;
    }

    public void setProvider(java.lang.String provider) {
        this._provider = provider;
    }

    public static java.lang.Object unmarshal(java.io.Reader reader)
            throws org.exolab.castor.xml.MarshalException, org.exolab.castor.xml.ValidationException {
        return (org.apache.openejb.alt.config.sys.SecurityService) Unmarshaller.unmarshal(org.apache.openejb.alt.config.sys.SecurityService.class, reader);
    }

    public void validate()
            throws org.exolab.castor.xml.ValidationException {
        org.exolab.castor.xml.Validator validator = new org.exolab.castor.xml.Validator();
        validator.validate(this);
    }

}
