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

import org.apache.commons.lang.StringUtils;

/**
 * Contains metadata about an xml element or attribute
 *
 * @author Catalina Wei
 * @since 1.0.0
 */
public class XMLFieldMetaData implements XMLMetaData {

    private String _name;
    private String _xmlname = null;
    private String _xmlnamespace = null;
    private Class _decType = Object.class;
    private int _decCode = JavaTypes.OBJECT;
    private Class _type = Object.class;
    private int _code = JavaTypes.OBJECT;
    private int _xmltype;    

    public XMLFieldMetaData() {        
    }
    
    public XMLFieldMetaData(Class type, String name) {
        setType(type);
        _name = name;
    }
    
    public Class getType() {
        return (_type == null) ? _decType : _type;
    }

    public void setType(Class type) {
        _type = type;
        if (type != null)
            setTypeCode(JavaTypes.getTypeCode(type));
    }

    public int getTypeCode() {
        return (_type == null) ? _decCode : _code;
    }

    // set JavaTypes code
    public void setTypeCode(int code) {
        _code = code;
    }
    
    public void setName(String name) {
        _name = name;
    }
    
    public String getName() {
        return _name;
    }
    
    public void setXmlname(String name) {
        _xmlname = name;
    }
    
    public String getXmlname() {
        return _xmlname;
    }
    
    public void setXmlnamespace(String name) {
        // avoid JAXB XML bind default name
        if (!StringUtils.equals(defaultName, name))
            _xmlnamespace = name;
    }
    
    public String getXmlnamespace() {
        return _xmlnamespace;
    }
    
    public void setXmltype(int type) {
        _xmltype = type;
    }
    
    public int getXmltype() {
        return _xmltype;
    }
    
    public boolean isXmlRootElement() {
        return false;
    }
    
    public boolean isXmlElement() {
        return _xmltype == ELEMENT;
    }
    
    public boolean isXmlAttribute() {
        return _xmltype == ATTRIBUTE;
    }
    
    public XMLMetaData getFieldMapping(String name) {
        return null;
    }
    
    public void setXmlRootElement(boolean isXmlRootElement) {
    }

    public void addField(String name, XMLMetaData field) {
    }
}
