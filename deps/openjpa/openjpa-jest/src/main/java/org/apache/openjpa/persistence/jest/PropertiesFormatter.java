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

import java.util.Arrays;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Formats a key-value pair in a HTML Document.
 * 
 * @author Pinaki Poddar
 *
 */
class PropertiesFormatter extends XMLFormatter {
    public Document createXML(String title, String tkey, String tvalue, Map<String,Object> properties) {
        Element root = newDocument(Constants.ROOT_ELEMENT_PROPERTIES);
        for (Map.Entry<String,Object> entry : properties.entrySet()) {
            Element property = root.getOwnerDocument().createElement("property");
            Object value = entry.getValue();
            String v = value == null 
                     ? Constants.NULL_VALUE 
                     : value.getClass().isArray() ? Arrays.toString((Object[])value) : value.toString();
                     property.setAttribute(Constants.ATTR_PROPERTY_KEY, entry.getKey());
                     property.setAttribute(Constants.ATTR_PROPERTY_VALUE, v);
            root.appendChild(property);
        }
        return root.getOwnerDocument();
    }
}
