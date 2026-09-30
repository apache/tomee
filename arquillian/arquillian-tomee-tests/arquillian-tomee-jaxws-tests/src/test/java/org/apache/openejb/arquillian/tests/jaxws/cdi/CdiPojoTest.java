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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxws.cdi;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;
import jakarta.jws.WebService;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CdiPojoTest {
    @ArquillianResource
    private URL base;

    @Deployment(testable = false)
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(CdiPojoTest.class, MyWebservice.class, ACdiTaste.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml") // ACdiTaste has no bean defining annotation
                .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                        "<servlet><servlet-name>ws</servlet-name><servlet-class>" + MyWebservice.class.getName() + "</servlet-class></servlet>" +
                        "<servlet-mapping><servlet-name>ws</servlet-name><url-pattern>/ws</url-pattern></servlet-mapping>" +
                        "</web-app>"));
    }

    @Test
    public void checkInjection() throws MalformedURLException {
        final MyWsApi api = Service.create(new URL(base.toExternalForm() + "ws?wsdl"),
            new QName("http://cdi.jaxws.tests.arquillian.openejb.apache.org/", "MyWebserviceService"))
            .getPort(MyWsApi.class);
        assertEquals("ok", api.test());
    }

    public static class ACdiTaste {
        public String ok() {
            return "ok";
        }
    }

    @WebService
    public static interface MyWsApi {
        String test();
    }

    @WebService
    public static class MyWebservice implements MyWsApi {
        @Inject
        private ACdiTaste cdi;

        @Override
        public String test() {
            if (cdi == null) {
                return "cdi injection failed";
            }
            return cdi.ok();
        }
    }
}
