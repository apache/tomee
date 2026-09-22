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
package org.apache.openejb.arquillian.tests.jaxws.context;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class PojoWebServiceContextTest {
    @Deployment
    public static WebArchive module() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(PojoWebServiceContextTest.class, MyWebservice.class)
                .setWebXML(new StringAsset("<web-app xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"6.0\">" +
                        "<servlet><servlet-name>ws</servlet-name><servlet-class>" + MyWebservice.class.getName() + "</servlet-class></servlet>" +
                        "<servlet-mapping><servlet-name>ws</servlet-name><url-pattern>/ws</url-pattern></servlet-mapping>" +
                        "</web-app>"));
    }

    @Test
    public void checkInjection() {
        assertTrue(MyWebservice.ok);
    }

    @WebService
    public static interface MyWsApi {
        String test();
    }

    @WebService
    public static class MyWebservice implements MyWsApi {
        private static boolean ok = false;

        @Resource
        private WebServiceContext ctx;

        @PostConstruct
        public void check() {
            ok = ctx != null;
        }

        @Override
        public String test() {
            return "ok";
        }
    }
}
