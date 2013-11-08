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
package org.apache.openjpa.lib.meta;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Custom non-validating SAX parser which can be used to get the version and
 * schema location attributes from the root node.
 *
 * @author Jeremy Bauer
 * @nojavadoc
 */
public class XMLVersionParser extends XMLMetaDataParser {

    public static final String VERSION_1_0 = "1.0";
    public static final String VERSION_2_0 = "2.0";

    static private final String VERSION_ATTR = "version";
    static private final String XSI_NS =
        "http://www.w3.org/2001/XMLSchema-instance";
    static private final String SCHEMA_LOCATION = "schemaLocation";

    private String _rootElement;
    private String _version;
    private String _schemaLocation;
    
    public XMLVersionParser(String rootElement) {
        _rootElement = rootElement;
        setCaching(false);
        setValidating(false);
        setParseText(false);
    }

    @Override
    protected void endElement(String name) throws SAXException {

    }

    @Override
    protected boolean startElement(String name, Attributes attrs)
            throws SAXException {
        if (name.equals(_rootElement)) {
            // save the version and schema location attributes
            _version = attrs.getValue("", VERSION_ATTR);
            _schemaLocation = attrs.getValue(XSI_NS, SCHEMA_LOCATION);
            // ignore remaining content
            ignoreContent(true);
        }
        return false;
    }

    /**
     * Get the string value of the version attribute on the root element
     * @return doc version
     */
    public String getVersion() {
        return _version;
    }
    
    /**
     * Get the string value of the schema location attribute on the root element
     * @return doc schema location
     */
    public String getSchemaLocation() {
        return _schemaLocation;
    }
}
