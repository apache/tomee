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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class WebAppInfo extends CommonInfoObject {
    public String path;
    public String description;
    public String displayName;
    public String smallIcon;
    public String largeIcon;
    public String moduleId;
    public String host;
    public String contextRoot;
    public String defaultContextPath;
    public int sessionTimeout;
    public final Set<String> watchedResources = new TreeSet<>();
    public final Set<String> restClass = new TreeSet<>();
    public final Set<String> restApplications = new TreeSet<>();
    public final Set<String> ejbWebServices = new TreeSet<>();
    public final Set<String> ejbRestServices = new TreeSet<>();
    public final Set<ClassListInfo> webAnnotatedClasses = new LinkedHashSet<>();
    public final List<PortInfo> portInfos = new ArrayList<>();
    public final JndiEncInfo jndiEnc = new JndiEncInfo();
    public final List<ServletInfo> servlets = new ArrayList<>();
    public final List<ClassListInfo> jsfAnnotatedClasses = new ArrayList<>();
    public final Set<String> jaxRsProviders = new TreeSet<>();
    public final List<ListenerInfo> listeners = new ArrayList<>();
    public final List<FilterInfo> filters = new ArrayList<>();
}
