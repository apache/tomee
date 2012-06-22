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

package org.apache.tomee.webapp.listener;

import org.apache.tomee.webapp.application.SessionData;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashMap;
import java.util.Map;

public class UserSessionListener implements HttpSessionListener {
    private static final String CONTEXT_KEY = "UserSessionListener_USER_CONTEXT";
    private static final String OBJS_KEY = "objects";

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        httpSessionEvent.getSession().setAttribute(CONTEXT_KEY, new SessionData());

        //this i used by the old tomee gui
        //TODO: remove me once the new gui is ready
        httpSessionEvent.getSession().setAttribute(OBJS_KEY, new HashMap<String, Object>());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        //do nothing
    }

    public static SessionData getServiceContext(HttpSession session) {
        return (SessionData) session.getAttribute(CONTEXT_KEY);
    }

    public static Map<String, Object> getObjects(HttpSession session) {
        return (Map<String, Object>) session.getAttribute(OBJS_KEY);
    }
}
