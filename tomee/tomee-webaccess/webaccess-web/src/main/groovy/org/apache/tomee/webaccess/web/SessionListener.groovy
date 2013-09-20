/**
 *
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

package org.apache.tomee.webaccess.web

import org.slf4j.LoggerFactory

import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

class SessionListener implements HttpSessionListener {
    private def log = LoggerFactory.getLogger(SessionListener)

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        def session = se.getSession()
        log.info("TomEE Webaccess sessionCreated -> Id: {} MaxInactiveInterval: {} seconds", session.id, session.maxInactiveInterval)
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.info("PhotoDB sessionDestroyed -> Id: {}", se.session.id)
    }
}
