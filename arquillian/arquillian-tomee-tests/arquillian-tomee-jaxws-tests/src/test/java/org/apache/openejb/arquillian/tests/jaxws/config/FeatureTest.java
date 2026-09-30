/**
 *
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
package org.apache.openejb.arquillian.tests.jaxws.config;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.openejb.arquillian.tests.jaxws.fault.AuthenticatorService;
import org.apache.openejb.arquillian.tests.jaxws.fault.AuthenticatorServiceBean;
import org.apache.openejb.arquillian.tests.jaxws.fault.DummyInterceptor;
import org.apache.openejb.arquillian.tests.jaxws.fault.WrongPasswordException;
import org.apache.openejb.arquillian.tests.jaxws.fault.WrongPasswordRuntimeException;
import org.apache.openejb.server.cxf.CxfService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class FeatureTest {
    @Deployment
    public static WebArchive war() {
        return ShrinkWrap.create(WebArchive.class, "feature.war")
                .addClasses(FeatureTest.class, MyFeature.class,
                        AuthenticatorService.class, AuthenticatorServiceBean.class, DummyInterceptor.class,
                        WrongPasswordException.class, WrongPasswordRuntimeException.class)
                .addAsResource(AuthenticatorServiceBean.class.getPackage(), "handler.xml")
                .addAsWebInfResource(new StringAsset(
                        "<openejb-jar>\n" +
                        "  <ejb-deployment ejb-name=\"AuthenticatorServiceBean\">\n" +
                        "    <properties>\n" +
                        "      " + CxfService.OPENEJB_JAXWS_CXF_FEATURES + " = " + MyFeature.class.getName() + "\n" +
                        "      cxf.jaxws.features = my-feature\n" +
                        "      cxf.jaxws.properties = my-props\n" +
                        "    </properties>\n" +
                        "  </ejb-deployment>\n" +
                        "</openejb-jar>\n"), "openejb-jar.xml")
                .addAsWebInfResource(new StringAsset(
                        "<resources>\n" +
                        "  <Service id=\"my-feature\" class-name=\"" + MyFeature.class.getName() + "\" />\n" +
                        "  <Service id=\"my-props\" class-name=\"" + Properties.class.getName() + "\">\n" +
                        "    faultStackTraceEnabled = true\n" +
                        "  </Service>\n" +
                        "</resources>\n"), "resources.xml");
    }

    @Test
    public void run() {
        assertTrue(MyFeature.ok);
    }

    public static class MyFeature extends AbstractFeature {
        public static boolean ok = false;

        @Override
        public void initialize(Server server, Bus bus) {
            ok = true;
        }
    }
}
