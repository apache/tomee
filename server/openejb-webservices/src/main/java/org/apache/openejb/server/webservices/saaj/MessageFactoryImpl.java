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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class MessageFactoryImpl extends MessageFactory {
    private MessageFactory getMessageFactory() throws SOAPException {
        MessageFactory factory = 
            (MessageFactory) SaajFactoryFinder.find("javax.xml.soap.MessageFactory");
        return factory;
    }
    
    public SOAPMessage createMessage() throws SOAPException {
        return getMessageFactory().createMessage();        
    }

    public SOAPMessage createMessage(MimeHeaders arg0, InputStream arg1) throws IOException, SOAPException {
        return getMessageFactory().createMessage(arg0, arg1);
    }
     
}