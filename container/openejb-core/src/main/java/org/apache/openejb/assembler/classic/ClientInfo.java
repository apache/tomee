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

package org.apache.openejb.assembler.classic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ClientInfo extends CommonInfoObject {

    public String path;
    public String description;
    public String displayName;
    public String smallIcon;
    public String largeIcon;
    public String moduleId;
    public String mainClass;
    public final List<String> localClients = new ArrayList<>();
    public final List<String> remoteClients = new ArrayList<>();

    public String callbackHandler;
    public final Set<String> watchedResources = new TreeSet<>();

    public final JndiEncInfo jndiEnc = new JndiEncInfo();

    public final List<CallbackInfo> postConstruct = new ArrayList<>();
    public final List<CallbackInfo> preDestroy = new ArrayList<>();
}
