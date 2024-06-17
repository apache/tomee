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
package org.apache.tomee.security.cdi.openid.storage.impl;

import org.apache.tomee.security.cdi.openid.storage.OpenIdStorageHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SessionBasedOpenIdStorageHandler extends OpenIdStorageHandler {
    @Override
    public String get(HttpServletRequest request, HttpServletResponse response, String key) {
        return (String) request.getSession().getAttribute(PREFIX + key);
    }

    @Override
    public void set(HttpServletRequest request, HttpServletResponse response, String key, String value) {
        request.getSession().setAttribute(PREFIX + key, value);
    }

    @Override
    public void delete(HttpServletRequest request, HttpServletResponse response, String key) {
        request.getSession().removeAttribute(PREFIX + key);
    }
}
