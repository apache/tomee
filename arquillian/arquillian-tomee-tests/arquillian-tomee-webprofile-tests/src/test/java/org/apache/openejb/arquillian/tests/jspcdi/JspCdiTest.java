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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jspcdi;

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

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

// https://issues.apache.org/jira/browse/TOMEE-1909
@RunWith(Arquillian.class)
public class JspCdiTest {
    @Deployment(testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class, "JspCdi.war")
                .addClasses(TheBean.class, TheBean.Data.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebResource(new StringAsset("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                        "<jsp:root xmlns:jsp=\"http://java.sun.com/JSP/Page\" version=\"2.0\">\n" +
                        "  <jsp:directive.page contentType=\"text/html; charset=UTF-8\"\n" +
                        "                      pageEncoding=\"UTF-8\" session=\"false\" isELIgnored=\"false\" />\n" +
                        "  <jsp:output doctype-root-element=\"html\"\n" +
                        "              doctype-system=\"html\" omit-xml-declaration=\"true\" />\n" +
                        "<html xmlns:c=\"http://java.sun.com/jsp/jstl/core\">\n" +
                        "<head>\n" +
                        "  <title>test</title>\n" +
                        "  <meta http-equiv=\"Content-Style-Type\" content=\"text/html; charset=UTF-8\" />\n" +
                        "</head>\n" +
                        "<body >\n" +
                        "  <c:forEach var=\"pro\" items=\"${DATA_LIST}\">\n" +
                        "    <jsp:text>${pro.value}</jsp:text>\n" +
                        "  </c:forEach>\n" +
                        "</body>\n" +
                        "</html>\n" +
                        "</jsp:root>\n"), "index.jsp");
    }

    @ArquillianResource
    private URL base;

    @Test
    public void run() throws IOException {
        assertEquals("<!DOCTYPE html SYSTEM \"html\">\n" +
                "<html>" +
                "<head><title>test</title><meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Style-Type\"/></head>" +
                "<body>p1p2p3</body>" + // here is the important part, we have our data and not "" which would mean not resolved
                "</html>", IO.slurp(base).trim());
    }
}
