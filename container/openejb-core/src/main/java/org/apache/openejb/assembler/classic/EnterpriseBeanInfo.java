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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public abstract class EnterpriseBeanInfo extends InfoObject {

    public static final int ENTITY = 0;

    public static final int STATEFUL = 1;

    public static final int STATELESS = 2;

    public static final int MESSAGE = 3;

    public static final int SINGLETON = 4;

    public static final int MANAGED = 5;

    public int type;

    public final Properties properties = new Properties();

    public String codebase;
    public String description;
    public String displayName;
    public String smallIcon;
    public String largeIcon;
    public String ejbDeploymentId;
    public String home;
    public String remote;
    public String localHome;
    public String local;
    public String proxy;
    public final List<String> businessLocal = new ArrayList<>();
    public final List<String> businessRemote = new ArrayList<>();
    public final List<String> parents = new ArrayList<>();
    public boolean localbean;

    public String ejbClass;
    public String ejbName;

    public String transactionType;
    public String concurrencyType;
    public final JndiEncInfo jndiEnc = new JndiEncInfo();
    public NamedMethodInfo timeoutMethod;

    public String runAs;
    public String runAsUser;

    public final List<SecurityRoleReferenceInfo> securityRoleReferences = new ArrayList<>();

    public final List<CallbackInfo> aroundInvoke = new ArrayList<>();

    public final List<CallbackInfo> postConstruct = new ArrayList<>();
    public final List<CallbackInfo> preDestroy = new ArrayList<>();

    public final List<CallbackInfo> aroundTimeout = new ArrayList<>();

    public final List<NamedMethodInfo> asynchronous = new ArrayList<>();
    public Set<String> asynchronousClasses = new HashSet<>();

    public String containerId;
    public String serviceEndpoint;

    public List<JndiNameInfo> jndiNamess = new ArrayList<>();

    public List<String> jndiNames = new ArrayList<>();
    public boolean loadOnStartup;
    public final List<String> dependsOn = new ArrayList<>();

    public TimeoutInfo statefulTimeout;
    public List<MethodScheduleInfo> methodScheduleInfos = new ArrayList<>();

    public boolean restService;
    public boolean passivable;
}
