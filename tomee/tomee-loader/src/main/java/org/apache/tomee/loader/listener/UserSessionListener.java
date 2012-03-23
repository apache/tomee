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

package org.apache.tomee.loader.listener;

import org.apache.tomee.loader.service.ServletsService;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class UserSessionListener implements HttpSessionListener {
    public static final String USER_CONTEXT = "UserSessionListener_USER_CONTEXT";

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        final ServletsService servletsService = new ServletsService();
        httpSessionEvent.getSession().setAttribute(USER_CONTEXT, servletsService);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        final ServletsService servletsService = (ServletsService) httpSessionEvent.getSession().getAttribute(USER_CONTEXT);
        if (servletsService == null) {
            return; //do nothing
        }
        servletsService.close();
    }
}
