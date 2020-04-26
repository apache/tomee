/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.attachment;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.ejb.Stateless;
import jakarta.jws.WebService;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.io.InputStream;

/**
 * This is an EJB 3 style pojo stateless session bean
 * Every stateless session bean implementation must be annotated
 * using the annotation @Stateless
 * This EJB has a single interface: {@link AttachmentWs} a webservice interface.
 */
@Stateless
@WebService(
        portName = "AttachmentPort",
        serviceName = "AttachmentWsService",
        targetNamespace = "http://superbiz.org/wsdl",
        endpointInterface = "org.superbiz.attachment.AttachmentWs")
@BindingType(value = SOAPBinding.SOAP12HTTP_MTOM_BINDING)
public class AttachmentImpl implements AttachmentWs {

    public String stringFromBytes(byte[] data) {
        return new String(data);

    }

    public String stringFromDataSource(DataSource source) {

        try {
            InputStream inStr = source.getInputStream();
            int size = inStr.available();
            byte[] data = new byte[size];
            inStr.read(data);
            inStr.close();
            return new String(data);

        } catch (IOException e) {
            e.printStackTrace();

        }
        return "";

    }

    public String stringFromDataHandler(DataHandler handler) {

        try {
            return (String) handler.getContent();

        } catch (IOException e) {
            e.printStackTrace();

        }
        return "";

    }

}
