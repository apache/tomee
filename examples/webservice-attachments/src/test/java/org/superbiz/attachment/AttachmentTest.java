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

import junit.framework.TestCase;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;
import java.net.URL;
import java.util.Properties;

public class AttachmentTest extends TestCase {

    //START SNIPPET: setup	
    private InitialContext initialContext;

    //Random port to avoid test conflicts
    private static final int port = Integer.parseInt(System.getProperty("httpejbd.port", "" + org.apache.openejb.util.NetworkUtil.getNextAvailablePort()));

    protected void setUp() throws Exception {

        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");
        properties.setProperty("openejb.embedded.remotable", "true");

        //Just for this test we change the default port from 4204 to avoid conflicts
        properties.setProperty("httpejbd.port", "" + port);

        initialContext = new InitialContext(properties);
    }
    //END SNIPPET: setup    

    /**
     * Create a webservice client using wsdl url
     *
     * @throws Exception
     */
    //START SNIPPET: webservice
    public void testAttachmentViaWsInterface() throws Exception {
        Service service = Service.create(
                new URL("http://localhost:" + port + "/webservice-attachments/AttachmentImpl?wsdl"),
                new QName("http://superbiz.org/wsdl", "AttachmentWsService"));
        assertNotNull(service);

        AttachmentWs ws = service.getPort(AttachmentWs.class);

        // retrieve the SOAPBinding
        SOAPBinding binding = (SOAPBinding) ((BindingProvider) ws).getBinding();
        binding.setMTOMEnabled(true);

        String request = "tsztelak@gmail.com";

        // Byte array
        String response = ws.stringFromBytes(request.getBytes());
        assertEquals(request, response);

        // Data Source
        DataSource source = new ByteArrayDataSource(request.getBytes(), "text/plain; charset=UTF-8");

        // not yet supported !
        //        response = ws.stringFromDataSource(source);
        //        assertEquals(request, response);

        // Data Handler
        response = ws.stringFromDataHandler(new DataHandler(source));
        assertEquals(request, response);

    }
    //END SNIPPET: webservice

}
