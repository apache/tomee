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
package com.apache.tomee.microprofile.tck.rest.client;

import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.BaseClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ChildClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.CustomHttpMethod;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceUsingBeanParam;
import org.eclipse.microprofile.rest.client.tck.interfaces.MissingPathParam;
import org.eclipse.microprofile.rest.client.tck.interfaces.MissingPathParamSub;
import org.eclipse.microprofile.rest.client.tck.interfaces.MissingUriTemplate;
import org.eclipse.microprofile.rest.client.tck.interfaces.MultipleHTTPMethodAnnotations;
import org.eclipse.microprofile.rest.client.tck.interfaces.MyBean;
import org.eclipse.microprofile.rest.client.tck.interfaces.RootResource;
import org.eclipse.microprofile.rest.client.tck.interfaces.SubResource;
import org.eclipse.microprofile.rest.client.tck.interfaces.TemplateMismatch;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class MicroProfileRestClientTCKArchiveProcessor implements ApplicationArchiveProcessor {
    @Override
    public void process(final Archive<?> archive, final TestClass testClass) {
        if (archive instanceof WebArchive) {
            final WebArchive war = WebArchive.class.cast(archive);

            // TODO - this could be fixed in the TCK by adding the missing classes in the test archives
            JavaArchive restClientJar = ShrinkWrap
                    .create(JavaArchive.class, "rest-client-tck-additional.jar")
                    .addClass(WiremockArquillianTest.class)
                    // missing in BeanParamTest
                    .addClass(MyBean.class)
                    .addClass(InterfaceUsingBeanParam.class)
                    // missing in CustomHttpMethodTest
                    .addClass(CustomHttpMethod.class)
                    // missing in InheritanceTest
                    .addClass(BaseClient.class)
                    .addClass(ChildClient.class)
                    // missing in InvalidInterfaceTest
                    .addClass(MultipleHTTPMethodAnnotations.class)
                    .addClass(MissingPathParam.class)
                    .addClass(MissingPathParamSub.class)
                    .addClass(MissingUriTemplate.class)
                    .addClass(TemplateMismatch.class)
                    // missing in SubResourceTest
                    .addClass(RootResource.class)
                    .addClass(SubResource.class)
                    ;

            war.addAsLibraries(restClientJar);
        }
    }
}
