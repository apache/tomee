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
package org.apache.openejb.cdi;

import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.core.ThreadContextListener;
import org.apache.webbeans.context.RequestContext;

/**
 * @version $Rev$ $Date$
 */
public class RequestScopedThreadContextListener implements ThreadContextListener {


    @Override
    public void contextEntered(ThreadContext oldContext, ThreadContext newContext) {
        Request request = getRequestData(oldContext);

        if (request == null) {
            request = new Request(newContext);
        }

        request.propogate(newContext);
    }

    @Override
    public void contextExited(ThreadContext exitedContext, ThreadContext reenteredContext) {
        final Request request = getRequestData(exitedContext);

        if (request.start == exitedContext) request.complete();
    }

    private Request getRequestData(ThreadContext threadContext) {
        if (threadContext == null) return null;
        return threadContext.get(Request.class);
    }


    private static class Request {
        private final ThreadContext start;
        private final RequestContext context;

        public Request(ThreadContext start) {
            this.start = start;
            this.context = new RequestContext();
            this.context.setActive(true);
        }

        public void propogate(ThreadContext threadContext) {
            threadContext.set(Request.class, this);
            threadContext.set(RequestContext.class, context);
        }

        public void complete() {
            context.destroy();
        }
    }
}
