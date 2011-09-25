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
package org.apache.openejb.tomcat.catalina;

import org.apache.catalina.connector.Request;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.io.IOException;

/**
 * @author rmannibucau
 */
public class OpenEJBSecurityListener implements AsyncListener {
    private TomcatSecurityService securityService;
    private Object oldState = null;
    private Request request;

    public OpenEJBSecurityListener(TomcatSecurityService service, Request req) {
        securityService = service;
        request = req;
    }

    @Override public void onComplete(AsyncEvent asyncEvent) throws IOException {
        exit();
    }

    @Override public void onError(AsyncEvent asyncEvent) throws IOException {
        exit();
    }

    @Override public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        enter();
    }

    @Override public void onTimeout(AsyncEvent asyncEvent) throws IOException {
        exit();
    }

    public void enter() {
        if (securityService != null && request.getWrapper() != null) {
            oldState = securityService.enterWebApp(request.getWrapper().getRealm(), request.getPrincipal(), request.getWrapper().getRunAs());
        }
    }

    public void exit() {
        if (securityService != null) {
            securityService.exitWebApp(oldState);
        }
    }
}
