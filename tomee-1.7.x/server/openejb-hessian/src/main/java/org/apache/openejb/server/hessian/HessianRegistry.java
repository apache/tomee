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
package org.apache.openejb.server.hessian;

import org.apache.openejb.loader.SystemInstance;

import java.net.URISyntaxException;

public interface HessianRegistry {
    static final String HESSIAN = SystemInstance.get().getProperty("openejb.hessian.subcontext", "/hessian/");

    String deploy(ClassLoader loader, HessianServer server,
                  String host, String app,
                  String authMethod, String transportGuarantee, String realmName,
                  String name) throws URISyntaxException;

    public void undeploy(String host, String app, String name);
}
