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
package org.apache.tomee.tests;

import junit.framework.Assert;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

@RunWith(Arquillian.class)
public class SimpleTest {

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war").addClasses(
                OrangeServlet.class,
                BrownBean.class
        );
    }

    @Test
    public void testShouldBeAbleToAccessServletAndEjb() throws Exception {
        final String output = get("http://localhost:9080/test/Test?hello");
        Assert.assertTrue(output.contains("hello"));
    }

    private String get(String url) throws IOException {
        return get(new URL(url));
    }

    private String get(URL url) throws IOException {
        final InputStream is = url.openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead = -1;

        final byte[] buffer = new byte[8192];

        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        return new String(os.toByteArray(), "UTF-8");
    }

    @WebServlet(urlPatterns = "/*")
    public static class OrangeServlet extends HttpServlet {

        private BrownBean brownBean;

        public OrangeServlet() {
        }

        @EJB
        public void setBrownBean(BrownBean brownBean) {
            this.brownBean = brownBean;
        }

        @Override
        public void init(ServletConfig config) throws ServletException {
            assert brownBean != null;
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            brownBean.doService(request, response);
        }

        @Override
        public void destroy() {
        }
    }

    @Singleton
    public static class BrownBean {

        public void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/plain");
            PrintWriter writer = response.getWriter();
            writer.print(request.getQueryString());
        }

    }
}
