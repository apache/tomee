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
package org.superbiz.example.jaxws;

import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.apache.openejb.testng.PropertiesBuilder;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

@EnableServices("jax-ws")
@RunWith(ApplicationComposer.class)
public class MeetingPlannerTest {
    private static final int JAX_WS_PORT = NetworkUtil.getNextAvailablePort();

    @Configuration
    public Properties configuration() {
        return new PropertiesBuilder().p("httpejbd.port", Integer.toString(JAX_WS_PORT)).build();
    }

    @Module
    @Classes(cdi = true, value = {MeetingPlannerImpl.class, LazyAgenda.class})
    public WebApp war() {
        return new WebApp()
                .contextRoot("/demo")
                .addServlet("jaxws", MeetingPlannerImpl.class.getName(), "/meeting-planner");
    }

    @Test
    public void bookPort() throws Exception {
        final Service service = Service.create(
                new URL("http://127.0.0.1:" + JAX_WS_PORT + "/demo/meeting-planner?wsdl"),
                new QName("http://jaxws.example.superbiz.org/", "MeetingPlannerImplService"));
        final MeetingPlanner planner = service.getPort(MeetingPlanner.class);
        assertTrue(planner.book(new Date(System.currentTimeMillis() + 1000000)));
    }

    @Test
    public void bookDispatch() throws Exception {
        final Service service = Service.create(
                new URL("http://127.0.0.1:" + JAX_WS_PORT + "/demo/meeting-planner?wsdl"),
                new QName("http://jaxws.example.superbiz.org/", "MeetingPlannerImplService"));
        final Dispatch<Source> dispatch = service.createDispatch(new QName("http://jaxws.example.superbiz.org/", "MeetingPlannerImplPort"), Source.class, Service.Mode.PAYLOAD);

        Date currentDate = new Date();
        LocalDateTime nowPlusTwoDays = LocalDateTime.from(currentDate.toInstant().atZone(ZoneId.systemDefault())).plusDays(2);
        String dateArgument = nowPlusTwoDays.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String request = "<ns1:book xmlns:ns1=\"http://jaxws.example.superbiz.org/\"><arg0>"+ dateArgument +"</arg0></ns1:book>";
        Source invoke = dispatch.invoke(new StreamSource(new StringReader(request)));
        String result = sourceToXMLString(invoke);

        assertTrue(result.contains("<return>true</return>"));
    }

    private String sourceToXMLString(Source result) {
        String xmlResult = null;
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            OutputStream out = new ByteArrayOutputStream();
            StreamResult streamResult = new StreamResult();
            streamResult.setOutputStream(out);
            transformer.transform(result, streamResult);
            xmlResult = streamResult.getOutputStream().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return xmlResult;
    }
}
