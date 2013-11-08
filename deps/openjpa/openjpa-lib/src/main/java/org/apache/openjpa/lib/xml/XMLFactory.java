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
package org.apache.openjpa.lib.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * The XMLFactory produces validating and non-validating DOM level 2
 * and SAX level 2 parsers and XSL transformers through JAXP. It uses
 * caching to avoid repeatedly paying the relatively expensive runtime costs
 * associated with resolving the correct XML implementation through the
 * JAXP configuration mechanisms.
 *
 * @author Abe White
 * @nojavadoc
 */
public class XMLFactory {

    // cache parsers and transformers in all possible configurations
    private static SAXParserFactory[] _saxFactories = null;
    private static DocumentBuilderFactory[] _domFactories = null;
    private static ErrorHandler _validating;

    static {
        _saxFactories = new SAXParserFactory[4];
        _domFactories = new DocumentBuilderFactory[4];

        SAXParserFactory saxFactory;
        DocumentBuilderFactory domFactory;
        int arrIdx;
        for (int validating = 0; validating < 2; validating++) {
            for (int namespace = 0; namespace < 2; namespace++) {
                arrIdx = factoryIndex(validating == 1, namespace == 1);

                saxFactory = SAXParserFactory.newInstance();
                saxFactory.setValidating(validating == 1);
                saxFactory.setNamespaceAware(namespace == 1);
                _saxFactories[arrIdx] = saxFactory;

                domFactory = DocumentBuilderFactory.newInstance();
                domFactory.setValidating(validating == 1);
                domFactory.setNamespaceAware(namespace == 1);
                _domFactories[arrIdx] = domFactory;
            }
        }
        _validating = new ValidatingErrorHandler();
    }

    /**
     * Return a SAXParser with the specified configuration.
     */
    public static SAXParser getSAXParser(boolean validating,
        boolean namespaceAware) {
        SAXParser sp;
        try {
            sp = _saxFactories[factoryIndex(validating, namespaceAware)].
                newSAXParser();
        } catch (ParserConfigurationException pce) {
            throw new NestableRuntimeException(pce);
        } catch (SAXException se) {
            throw new NestableRuntimeException(se);
        }

        if (validating) {
            try {
                sp.getXMLReader().setErrorHandler(_validating);
            } catch (SAXException se) {
                throw new NestableRuntimeException(se);
            }
        }

        return sp;
    }

    /**
     * Return a DocumentBuilder with the specified configuration.
     */
    public static DocumentBuilder getDOMParser(boolean validating,
        boolean namespaceAware) {
        DocumentBuilder db;
        try {
            db = _domFactories[factoryIndex(validating, namespaceAware)].
                newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new NestableRuntimeException(pce);
        }

        if (validating)
            db.setErrorHandler(_validating);
        return db;
    }

    /**
     * Return a new DOM Document.
     */
    public static Document getDocument() {
        return getDOMParser(false, false).newDocument();
    }

    /**
     * Return the array index of the factory with the given properties.
     */
    private static int factoryIndex(boolean validating,
        boolean namespaceAware) {
        int arrayIndex = 0;
        if (validating)
            arrayIndex += 2;
        if (namespaceAware)
            arrayIndex += 1;
        return arrayIndex;
    }
}
