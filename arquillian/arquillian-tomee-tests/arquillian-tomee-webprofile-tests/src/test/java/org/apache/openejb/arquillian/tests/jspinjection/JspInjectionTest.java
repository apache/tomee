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
package org.apache.openejb.arquillian.tests.jspinjection;

import org.apache.openejb.arquillian.common.IO;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class JspInjectionTest {
    @Deployment(testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class, "JspInjectionTest.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsManifestResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<Context path=\"/foo\" privileged=\"true\">\n" +
                        "    <Environment name=\"foo\" value=\"42\" type=\"java.lang.Integer\" override=\"false\"/>\n" +
                        "</Context>"), "context.xml")
                .addAsWebResource(new StringAsset(
                        "<%@ page import=\"" + Resource.class.getName() + "\" %>\n" +
                        "<%@ page contentType=\"text/html;charset=UTF-8\" language=\"java\" session=\"false\" %>\n" +
                        "<%!\n" +
                        "    @Resource(name = \"foo\")\n" +
                        "    private Integer foo;\n" +
                        "%>\n" +
                        "<html>\n" +
                        " <body>\n" +
                        "  foo = <%= foo %>\n" +
                        " </body>\n" +
                        "</html>"), "index.jsp");
    }

    @ArquillianResource
    private URL base;

    @Test
    public void check() throws IOException {
        assertEquals(
                "<html>" +
                " <body>" +
                "  foo = 42" +
                " </body>" +
                "</html>", IO.slurp(base).replace("\r", "").replace("\n", ""));
    }
}
