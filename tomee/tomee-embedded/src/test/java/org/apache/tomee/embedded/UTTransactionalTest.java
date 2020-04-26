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
package org.apache.tomee.embedded;

import org.apache.openejb.loader.IO;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UTTransactionalTest {
    @Test
    public void run() throws IOException {
        try (final Container c = new Container(new Configuration()
                .http(NetworkUtil.getNextAvailablePort())
                //.property("openejb.container.additional.exclude", "org.apache.tomee.embedded.")
                .property("openejb.additional.include", "tomee-"))
                .deployClasspathAsWebApp()) {
            assertEquals("IllegalStateException", IO.slurp(new URL("http://localhost:" + c.getConfiguration().getHttpPort() + "/UTTransactionalTest")));
        }
    }

    @ApplicationScoped
    @Transactional
    public static class Bean {
        @Resource
        private UserTransaction ut;

        public void noUt() {
            try {
                ut.begin();
            } catch (final NotSupportedException | SystemException e) {
                fail();
            }
        }
    }

    @WebServlet(urlPatterns = "/UTTransactionalTest", loadOnStartup = 1)
    public static class Endpoint extends HttpServlet {
        @Inject
        private Bean bean;

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            try {
                bean.noUt();
                resp.getWriter().write("ok");
            } catch (final IllegalStateException ise) {
                resp.getWriter().write("IllegalStateException");
            }
        }
    }
}
