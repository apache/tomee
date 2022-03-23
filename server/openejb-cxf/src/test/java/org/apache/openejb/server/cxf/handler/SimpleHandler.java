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
package org.apache.openejb.server.cxf.handler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.MessageContext;

import org.apache.openejb.server.cxf.CdiHandlersTest;

@RequestScoped // otherwise can't test pre/post hooks
public class SimpleHandler implements Handler {
    public static boolean pre = false;
    public static boolean post = false;
    public static boolean handled = false;
    public static boolean close = false;

    public static void reset() {
        handled = false;
        close = false;
        pre = false;
        post = false;
    }

    @Inject
    private CdiHandlersTest.ACdiSimpleTaste cdi;

    @PostConstruct
    public void post() {
        post = true;
    }

    @PreDestroy
    public void pre() {
        pre = true;
    }

    @Override
    public void close(final MessageContext messageContext) {
        close = cdi != null && "ok".equals(cdi.ok());
    }

    @Override
    public boolean handleFault(final MessageContext messageContext) {
        return false;
    }

    @Override
    public boolean handleMessage(final MessageContext messageContext) {
        handled = cdi != null && "ok".equals(cdi.ok());
        return handled;
    }
}
