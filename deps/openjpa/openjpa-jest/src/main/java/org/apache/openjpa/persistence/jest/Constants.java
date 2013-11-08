/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.openjpa.persistence.jest;


/**
 * Static String constants
 * 
 * @author Pinaki Poddar
 *
 */
public interface Constants {
    /**
     * Common Command Qualifiers
     */
    public static final String QUALIFIER_FORMAT      = "format";
    public static final String QUALIFIER_PLAN        = "plan";
    
    /**
     * Mime Types
     */
    public static final String MIME_TYPE_PLAIN = "text/plain";
    public static final String MIME_TYPE_JS    = "text/javascript";
    public static final String MIME_TYPE_CSS   = "text/css";
    public static final String MIME_TYPE_XML   = "text/xml";
    public static final String MIME_TYPE_JSON  = "application/json";
     
   
    /**
     * Dojo Toolkit URL and Themes
     */
    public static final String DOJO_BASE_URL = "http://ajax.googleapis.com/ajax/libs/dojo/1.5";
    public static final String DOJO_THEME    = "claro";
    
    
      
    /**
     * Root element of XML instances. Must match the name defined in <A href="jest-instance.xsd>jest-instance.xsd</A>.
     */
    public static final String ROOT_ELEMENT_INSTANCE   = "instances";
    public static final String ATTR_ID      = "id";
    public static final String ATTR_REL     = "rel";
    public static final String ATTR_SRC     = "src";
    public static final String ATTR_TYPE    = "type";
    public static final String ATTR_NAME    = "name";
    public static final String ATTR_VERSION = "version";
    public static final String ATTR_CLASS   = "class";
    public static final String ATTR_HREF    = "href";
    public static final String ATTR_STYLE   = "style";
    public static final String ATTR_NULL           = "null";
    public static final String ATTR_MEMBER_TYPE    = "member-type";
    public static final String ATTR_KEY_TYPE       = "key-type";
    public static final String ATTR_VALUE_TYPE     = "value-type";
    
    /**
     * Elements and attributes in properties XML.
     */
    public static final String ROOT_ELEMENT_PROPERTIES = "properties";
    public static final String ELEMENT_PROPERTY     = "property";
    public static final String ATTR_PROPERTY_KEY     = "name";
    public static final String ATTR_PROPERTY_VALUE     = "value";
    
    
    public static final String ROOT_ELEMENT_ERROR      = "error";
    public static final String ELEMENT_ERROR_HEADER    = "error-header";
    public static final String ELEMENT_ERROR_MESSAGE   = "error-message";
    public static final String ELEMENT_ERROR_TRACE     = "stacktrace";
    
    /**
     * Root element of XML meta-model. Must match the name defined in <A href="jest-model.xsd>jest-model.xsd</A>.
     */
    public static final String ROOT_ELEMENT_MODEL      = "metamodel";
    
    public static final String ELEMENT_INSTANCE      = "instance";
    public static final String ELEMENT_URI           = "uri";
    public static final String ELEMENT_DESCRIPTION   = "description";
    public static final String ELEMENT_REF           = "ref";
    public static final String ELEMENT_NULL_REF      = "null";
    public static final String ELEMENT_MEMBER        = "member";
    public static final String ELEMENT_ENTRY         = "entry";
    public static final String ELEMENT_ENTRY_KEY     = "key";
    public static final String ELEMENT_ENTRY_VALUE   = "value";

    
    
    /**
     * JEST resources
     */
    public static final String JEST_INSTANCE_XSD       = "jest-instance.xsd";
    
    static final String JAXP_SCHEMA_SOURCE      = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String JAXP_SCHEMA_LANGUAGE    = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    
    public static final String NULL_VALUE          = "null";


}
