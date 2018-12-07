/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.openejb.server.rest;

import org.apache.openejb.server.httpd.HttpListener;


public interface RsRegistry {
    AddressInfo createRsHttpListener(String appId, String webContext, HttpListener listener, ClassLoader classLoader, String path, String virtualHost, String auth, String realm);

    HttpListener removeListener(String appId, String context);

    public static class AddressInfo {
        public String base;
        public String complete;

        public AddressInfo(String base, String complete) {
            this.base = base;
            this.complete = complete;
        }
    }
}
