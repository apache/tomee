/*
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
package org.apache.openejb.arquillian.tests.jaxws;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;


@RunWith(Arquillian.class)
@Ignore
public class WebServiceContextEJBTest {
    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        final WebAppDescriptor webAppDescriptor = Descriptors.create(WebAppDescriptor.class)
                .createServlet()
                .servletName("HelloService")
                .servletClass(HelloService.class.getName())
                .up()
                .createServletMapping()
                .servletName("HelloService")
                .urlPattern("/ws/Hello")
                .up();

        return ShrinkWrap.create(WebArchive.class, "ROOT.war")
                .addClasses(HelloService.class)
                .addAsWebInfResource(new StringAsset(webAppDescriptor.exportAsString()), "web.xml");
    }

    @Test
    public void invoke() throws Exception {
        final Service service = Service.create(new URL(url.toExternalForm() + "/ws/Hello?wsdl"), new QName("http://jaxws.tests.arquillian.openejb.apache.org/", "Hello"));
        final QName portQName = new QName("http://jaxws.tests.arquillian.openejb.apache.org/", "HelloService");

        final Dispatch<Source> dispatch = service.createDispatch(portQName, Source.class, Service.Mode.PAYLOAD);
        final String request =
                "      <jax:sayHello xmlns:jax=\"http://jaxws.tests.arquillian.openejb.apache.org/\">\n" +
                "         <name>tomee</name>\n" +
                "      </jax:sayHello>\n";
        final Source response = dispatch.invoke(new StreamSource(new StringReader(request)));

        final TransformerFactory factory = TransformerFactory.newInstance();
        final Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final StreamResult streamResult = new StreamResult();
        streamResult.setOutputStream(os);
        transformer.transform(response, streamResult);

        final String result = new String(os.toByteArray(), StandardCharsets.UTF_8);
        System.out.println(result);
        Assert.assertTrue(result.contains("<return>Hello, tomee, test header is set to null</return>"));
    }

    @WebService(name = "Hello", targetNamespace = "http://jaxws.tests.arquillian.openejb.apache.org/", serviceName = "Hello", portName = "HelloService")
    @SOAPBinding(style = SOAPBinding.Style.DOCUMENT, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED, use = SOAPBinding.Use.LITERAL)
    @Stateless
    public static class HelloService {

        @Resource
        private WebServiceContext context;

        @WebMethod
        public String sayHello(final @WebParam(name="name") String name) {
            final MessageContext messageContext = context.getMessageContext();
            final HttpServletRequest request = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);

            final String testHeader = request.getHeader("XX-Test");
            return "Hello, " + name + ", test header is set to " + testHeader;
        }
    }
}
