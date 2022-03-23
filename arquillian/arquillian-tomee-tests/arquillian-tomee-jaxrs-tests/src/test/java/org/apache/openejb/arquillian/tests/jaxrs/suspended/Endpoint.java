/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.arquillian.tests.jaxrs.suspended;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;

@Path("touch")
@ApplicationScoped
public class Endpoint {
    private volatile AsyncResponse current;

    private volatile Thread thread;

    @GET
    public void async(@Suspended final AsyncResponse response) {
        if (current == null) {
            current = response;
        } else {
            throw new IllegalStateException("we shouldnt go here back");
        }
    }

    @GET
    @Path("check")
    public boolean set() {
        return current != null;
    }

    @POST
    @Path("answer")
    public void async(final String response) {
        thread = new RunThread(response, current);
        thread.setDaemon(true);
        thread.start();
    }

    @PreDestroy
    public void end() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }

    public static class RunThread extends Thread {
        private final String content;
        private final AsyncResponse response;

        public RunThread(final String response, final AsyncResponse current) {
            this.response = current;
            this.content = response;
        }

        @Override
        public void run() {
            response.resume(content);
        }
    }
}
