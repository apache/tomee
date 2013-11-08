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
package org.apache.openjpa.meta;

import java.io.Serializable;

/**
 * Describe metadata about an xml type.
 * 
 * @author Catalina Wei
 * @since 1.0.0
 */
public interface XMLMetaData extends Serializable {
    /**
     * JAXB XML binding default name
     */
    public static final String defaultName = "##default";
    public static final int XMLTYPE = 0;
    public static final int ELEMENT = 1;
    public static final int ATTRIBUTE = 2;

    /**
     * Return true if mapping on an XmlRootElement.
     */
    public boolean isXmlRootElement();

    /**
     * Return true if mapping on an XmlElement.
     */
    public boolean isXmlElement();

    /**
     * Return true if mapping on an XmlAttribute.
     */
    public boolean isXmlAttribute();
    
    /**
     * Return XMLMapping for a given field.
     * @param name the field name.
     * @return XMLMapping.
     */
    public XMLMetaData getFieldMapping(String name); 
    
    /**
     * Set type.
     */
    public void setType(Class type);

    /**
     * Return type.
     */
    public Class getType();

    /**
     * Return type code.
     */
    public int getTypeCode();

    /**
     * Return the mapping name.
     */
    public String getName();

    /**
     * Return xml element tag name or xml attribute name.
     */
    public String getXmlname();

    /**
     * Return xml namespace.
     */
    public String getXmlnamespace();

    /**
     * Set field name.
     * @param name the field name.
     */
    public void setName(String name);

    /**
     * Set xml element or attribute name.
     * @param name the element name or attribute name
     */
    public void setXmlname(String name);

    /**
     * Set namespace.
     * @param namespace
     */
    public void setXmlnamespace(String namespace);

    /**
     * Set xmltype
     * @param type XMLTYPE, ELEMENT, or ATTRIBUTE
     */
    public void setXmltype(int type);

    /**
     * Return xmltype
     * @return xmltype
     */
    public int getXmltype();
    
    public void setXmlRootElement(boolean isXmlRootElement);
    
    public void addField(String name, XMLMetaData field);
}
