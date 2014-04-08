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
package org.apache.openejb.server.webservices.saaj;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;

public class SoapFactoryImpl extends SOAPFactory {
    
    private SOAPFactory getSOAPFactory() throws SOAPException {
        SOAPFactory factory = 
            (SOAPFactory) SaajFactoryFinder.find("javax.xml.soap.SOAPFactory");
        return factory;
        
    }
    public Detail createDetail() throws SOAPException {
        return getSOAPFactory().createDetail();
    }

    public SOAPElement createElement(Name arg0) throws SOAPException {
        return getSOAPFactory().createElement(arg0);
    }

    public SOAPElement createElement(String arg0) throws SOAPException {
        return getSOAPFactory().createElement(arg0);
    }

    public SOAPElement createElement(String arg0, String arg1, String arg2) throws SOAPException {
        return getSOAPFactory().createElement(arg0, arg1, arg2);
    }

    public SOAPFault createFault() throws SOAPException {
        return getSOAPFactory().createFault();
    }

    public SOAPFault createFault(String arg0, QName arg1) throws SOAPException {
        return getSOAPFactory().createFault(arg0, arg1);
    }

    public Name createName(String arg0) throws SOAPException {
        return getSOAPFactory().createName(arg0);
    }

    public Name createName(String arg0, String arg1, String arg2) throws SOAPException {
        return getSOAPFactory().createName(arg0, arg1, arg2);
    }
      
}
