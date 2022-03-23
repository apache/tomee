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
package org.apache.openejb.arquillian.openejb;

import org.apache.openejb.loader.IO;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class SessionDestroyTest {
    @Deployment(name = "app", managed = false, testable = false)
    public static Archive<?> app() {
        return ShrinkWrap.create(WebArchive.class).addClasses(SessionTestManager.class, SessionListener.class);
    }

    @ArquillianResource
    private Deployer deployer;

    private static String id;

    @Test
    @InSequence(1)
    public void deploy() {
        reset();
        deployer.deploy("app");
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("app")
    public void doTest(@ArquillianResource final URL url) throws IOException {
        id = IO.slurp(new URL(url.toExternalForm() + "create"));
        assertNotNull(SessionListener.created);
        assertEquals(id, SessionListener.created);
    }

    @Test
    @InSequence(3)
    public void undeployAndAsserts() {
        deployer.undeploy("app");
        assertNotNull(SessionListener.destroyed);
        assertEquals(id, SessionListener.destroyed);
        reset();
    }

    private void reset() {
        SessionListener.destroyed = null;
        SessionListener.created = null;
        id = null;
    }

    @WebServlet("/create")
    public static class SessionTestManager extends HttpServlet {
        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            req.getSession().setAttribute("test", "ok");
            resp.getWriter().write(req.getSession().getId());
        }
    }

    @WebListener
    public static class SessionListener implements HttpSessionListener {
        private static String created;
        private static String destroyed;

        @Override
        public void sessionCreated(final HttpSessionEvent httpSessionEvent) {
            created = httpSessionEvent.getSession().getId();
        }

        @Override
        public void sessionDestroyed(final HttpSessionEvent httpSessionEvent) {
            destroyed = httpSessionEvent.getSession().getId();
        }
    }
}
