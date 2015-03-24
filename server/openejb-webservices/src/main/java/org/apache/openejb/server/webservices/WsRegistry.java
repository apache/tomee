/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.webservices;

import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.server.httpd.HttpListener;

import java.util.List;

public interface WsRegistry {
    List<String> setWsContainer(HttpListener httpListener,
                                ClassLoader classLoader,
                                String context, String virtualHost, ServletInfo servletInfo,
                                String realmName, String transportGuarantee, String authMethod,
                                String moduleId) throws Exception;

    void clearWsContainer(String context, String virtualHost, ServletInfo servletInfo, String moduleId);

    List<String> addWsContainer(HttpListener inputListener,
                                ClassLoader classLoader,
                                String context,
                                String virtualHost,
                                String path,
                                String realmName,
                                String transportGuarantee, // ignored
                                String authMethod,
                                String moduleId) throws Exception;

    void removeWsContainer(String path, String moduleId);
}
