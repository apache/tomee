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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Formats error stack trace.
 * 
 * @author Pinaki Poddar
 *
 */
class ExceptionFormatter extends XMLFormatter {
    /**
     * Creates a XML Document with given header and stack trace of the given error.
     * @param header
     * @param e
     */
    public Document createXML(String header, Throwable e) {
        Element root = newDocument(Constants.ROOT_ELEMENT_ERROR);
        Document doc = root.getOwnerDocument();
        Element errorHeader  = doc.createElement(Constants.ELEMENT_ERROR_HEADER);
        Element errorMessage = doc.createElement(Constants.ELEMENT_ERROR_MESSAGE);
        Element stackTrace   = doc.createElement(Constants.ELEMENT_ERROR_TRACE);

        errorHeader.setTextContent(header);
        errorMessage.appendChild(doc.createCDATASection(e.getMessage()));
        
        StringWriter buf = new StringWriter();
        e.printStackTrace(new PrintWriter(buf, true));
        stackTrace.appendChild(doc.createCDATASection(buf.toString()));
        
        root.appendChild(errorHeader);
        root.appendChild(errorMessage);
        root.appendChild(stackTrace);
        
        return doc;
    }

}
