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

package org.apache.openejb.jee;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

import org.apache.openejb.jee.JaxbJavaee.HandlerChainsNamespaceFilter;

/**
 * @version $Rev$ $Date$
 */
public class HandlerChainsStringQNameAdapter extends XmlAdapter<String, QName> {

    private HandlerChainsNamespaceFilter xmlFilter;

    @Override
    public QName unmarshal(String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return new QName(XMLConstants.NULL_NS_URI, "");
        }
        int colonIndex = value.indexOf(":");
        if (colonIndex == -1) {
            return new QName(XMLConstants.NULL_NS_URI, value);
        }
        String prefix = value.substring(0, colonIndex);
        String localPart = (colonIndex == (value.length() - 1)) ? "" : value.substring(colonIndex + 1);
        String nameSpaceURI = xmlFilter.lookupNamespaceURI(prefix);
        if (nameSpaceURI == null) {
            nameSpaceURI = XMLConstants.NULL_NS_URI;
        }
        return new QName(nameSpaceURI, localPart, prefix);
    }

    @Override
    public String marshal(QName name) throws Exception {
        String localPart = name.getLocalPart();
        if (localPart == null || localPart.isEmpty()) {
            return "";
        }
        if (localPart.equals("*")) {
            return localPart;
        }
        String prefix = name.getPrefix();
        if (prefix == null || prefix.isEmpty()) {
            return localPart;
        }
        return prefix + ":" + localPart;
    }

    public void setHandlerChainsNamespaceFilter(HandlerChainsNamespaceFilter xmlFilter) {
        this.xmlFilter = xmlFilter;
    }
}
