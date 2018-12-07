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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ServiceInfo extends InfoObject {

    public String service;
    public List<String> types = new ArrayList<>();
    public String description;
    public String id;
    public String displayName;
    public String className;
    public String codebase;
    public URI[] classpath;
    public String classpathAPI;
    public Properties properties;
    public final List<String> constructorArgs = new ArrayList<>();
    public Properties unsetProperties; // keep it in the model to be able to investigate it dumping Infos

    /**
     * Optional *
     */
    public String factoryMethod;

}
