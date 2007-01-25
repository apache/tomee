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

import org.exolab.castor.xml.validators.*;

public class DeploymentsDescriptor extends org.exolab.castor.xml.util.XMLClassDescriptorImpl {


    private java.lang.String nsPrefix;

    private java.lang.String nsURI;

    private java.lang.String xmlName;

    private org.exolab.castor.xml.XMLFieldDescriptor identity;


    public DeploymentsDescriptor() {
        super();
        nsURI = "http://www.openejb.org/System/Configuration";
        xmlName = "Deployments";
        org.exolab.castor.xml.util.XMLFieldDescriptorImpl desc = null;
        org.exolab.castor.xml.XMLFieldHandler handler = null;
        org.exolab.castor.xml.FieldValidator fieldValidator = null;


        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_jar", "jar", org.exolab.castor.xml.NodeType.Attribute);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                Deployments target = (Deployments) object;
                return target.getJar();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    Deployments target = (Deployments) object;
                    target.setJar((java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return null;
            }
        });
        desc.setHandler(handler);
        addFieldDescriptor(desc);


        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            typeValidator.setPattern(".*\\.jar$");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);

        desc = new org.exolab.castor.xml.util.XMLFieldDescriptorImpl(java.lang.String.class, "_dir", "dir", org.exolab.castor.xml.NodeType.Attribute);
        desc.setImmutable(true);
        handler = (new org.exolab.castor.xml.XMLFieldHandler() {
            public java.lang.Object getValue(java.lang.Object object)
                    throws IllegalStateException {
                Deployments target = (Deployments) object;
                return target.getDir();
            }

            public void setValue(java.lang.Object object, java.lang.Object value)
                    throws IllegalStateException, IllegalArgumentException {
                try {
                    Deployments target = (Deployments) object;
                    target.setDir((java.lang.String) value);
                }
                catch (java.lang.Exception ex) {
                    throw new IllegalStateException(ex.toString());
                }
            }

            public java.lang.Object newInstance(java.lang.Object parent) {
                return null;
            }
        });
        desc.setHandler(handler);
        addFieldDescriptor(desc);


        fieldValidator = new org.exolab.castor.xml.FieldValidator();
        {
            StringValidator typeValidator = new StringValidator();
            typeValidator.setWhiteSpace("preserve");
            fieldValidator.setValidator(typeValidator);
        }
        desc.setValidator(fieldValidator);


    }


    public org.exolab.castor.mapping.AccessMode getAccessMode() {
        return null;
    }

    public org.exolab.castor.mapping.ClassDescriptor getExtends() {
        return null;
    }

    public org.exolab.castor.mapping.FieldDescriptor getIdentity() {
        return identity;
    }

    public java.lang.Class getJavaClass() {
        return org.apache.openejb.config.sys.Deployments.class;
    }

    public java.lang.String getNameSpacePrefix() {
        return nsPrefix;
    }

    public java.lang.String getNameSpaceURI() {
        return nsURI;
    }

    public org.exolab.castor.xml.TypeValidator getValidator() {
        return this;
    }

    public java.lang.String getXMLName() {
        return xmlName;
    }

}
