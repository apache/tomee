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
import jakarta.xml.soap.Detail;
import jakarta.xml.soap.Name;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;

public class SoapFactoryImpl extends SOAPFactory {

    private SOAPFactory getSOAPFactory() throws SOAPException {
        SOAPFactory factory =
            (SOAPFactory) SaajFactoryFinder.find("jakarta.xml.soap.SOAPFactory");
        return factory;

    }

    @Override
    public Detail createDetail() throws SOAPException {
        return getSOAPFactory().createDetail();
    }

    @Override
    public SOAPElement createElement(Name arg0) throws SOAPException {
        return getSOAPFactory().createElement(arg0);
    }

    @Override
    public SOAPElement createElement(String arg0) throws SOAPException {
        return getSOAPFactory().createElement(arg0);
    }

    @Override
    public SOAPElement createElement(String arg0, String arg1, String arg2) throws SOAPException {
        return getSOAPFactory().createElement(arg0, arg1, arg2);
    }

    @Override
    public SOAPFault createFault() throws SOAPException {
        return getSOAPFactory().createFault();
    }

    @Override
    public SOAPFault createFault(String arg0, QName arg1) throws SOAPException {
        return getSOAPFactory().createFault(arg0, arg1);
    }

    @Override
    public Name createName(String arg0) throws SOAPException {
        return getSOAPFactory().createName(arg0);
    }

    @Override
    public Name createName(String arg0, String arg1, String arg2) throws SOAPException {
        return getSOAPFactory().createName(arg0, arg1, arg2);
    }

}
