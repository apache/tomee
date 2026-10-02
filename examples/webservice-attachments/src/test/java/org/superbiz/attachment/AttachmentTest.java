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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class AttachmentTest {

    //START SNIPPET: setup
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "webservice-attachments.war")
                .addClasses(AttachmentImpl.class, AttachmentWs.class);
    }
    //END SNIPPET: setup

    /**
     * Create a webservice client using wsdl url
     *
     * @throws Exception
     */
    //START SNIPPET: webservice
    @Test
    public void testAttachmentViaWsInterface() throws Exception {
        Service service = Service.create(
                new URL(base.toExternalForm() + "webservices/AttachmentImpl?wsdl"),
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
