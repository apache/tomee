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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A request that leaves a UserTransaction behind must not poison the next request served on the
 * same pooled Tomcat exec thread.
 *
 * @see <a href="https://issues.apache.org/jira/browse/TOMEE-4652">TOMEE-4652</a>
 */
public class UserTransactionLeakTest {
    @Test
    public void transactionDoesNotLeakToNextRequest() throws IOException {
        try (final Container c = new Container(new Configuration()
                .http(NetworkUtil.getNextAvailablePort())
                // a single exec thread guarantees both requests land on the same thread
                .property("connector.attributes.maxThreads", "1")
                .property("connector.attributes.minSpareThreads", "1")
                .property("openejb.additional.include", "tomee-"))
                .deployClasspathAsWebApp()) {

            final String base = "http://localhost:" + c.getConfiguration().getHttpPort();

            // the leaker: begins a transaction and never completes it
            final String leaker = IO.slurp(new URL(base + "/leak"));
            assertTrue(leaker, leaker.startsWith("begun"));

            // the victim: must see a clean thread, not the transaction above
            final String victim = IO.slurp(new URL(base + "/status"));

            // guard: the whole point is thread reuse, so fail loudly if that did not happen
            assertEquals("both requests must share the exec thread for this test to mean anything",
                    threadOf(leaker), threadOf(victim));

            assertEquals("STATUS_NO_TRANSACTION", victim.substring(0, victim.indexOf(" on ")));

            // and must still be able to run a transaction of its own
            assertEquals("committed", IO.slurp(new URL(base + "/commit")));
        }
    }

    private static String threadOf(final String response) {
        final int marker = response.indexOf(" on ");
        assertTrue("no thread name in response: " + response, marker > 0);
        final String rest = response.substring(marker + " on ".length());
        final int end = rest.indexOf(' ');
        return end < 0 ? rest : rest.substring(0, end);
    }

    @WebServlet(urlPatterns = "/leak", loadOnStartup = 1)
    public static class Leaker extends HttpServlet {
        @Resource
        private UserTransaction ut;

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            try {
                ut.setTransactionTimeout(120); // must not leak to the next request either
                ut.begin();
                resp.getWriter().write("begun on " + Thread.currentThread().getName());
            } catch (final Exception e) {
                resp.getWriter().write("failed: " + e.getMessage());
            }
        }
    }

    @WebServlet(urlPatterns = "/status", loadOnStartup = 1)
    public static class StatusReporter extends HttpServlet {
        @Resource
        private UserTransaction ut;

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            try {
                final int status = ut.getStatus();
                resp.getWriter().write((status == Status.STATUS_NO_TRANSACTION
                        ? "STATUS_NO_TRANSACTION" : "leaked status " + status)
                        + " on " + Thread.currentThread().getName());
            } catch (final Exception e) {
                resp.getWriter().write("failed: " + e.getMessage());
            }
        }
    }

    @WebServlet(urlPatterns = "/commit", loadOnStartup = 1)
    public static class Committer extends HttpServlet {
        @Resource
        private UserTransaction ut;

        @Override
        protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
            try {
                // would throw NotSupportedException ("Nested Transactions are not supported")
                // if the leaked transaction were still associated with this thread
                ut.begin();
                ut.commit();
                resp.getWriter().write("committed");
            } catch (final Exception e) {
                resp.getWriter().write("failed: " + e.getClass().getSimpleName() + " " + e.getMessage());
            }
        }
    }
}
