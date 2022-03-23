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

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class EjbJarInfo extends CommonInfoObject {
    public final Properties properties = new Properties();

    public String moduleName;
    public String moduleId;
    public URI moduleUri;
    public String path;
    public boolean webapp;

    @XmlElements({ // needed for unmarshalling
        @XmlElement(name = "stateless", type = StatelessBeanInfo.class),
        @XmlElement(name = "entity", type = EntityBeanInfo.class),
        @XmlElement(name = "stateful", type = StatefulBeanInfo.class),
        @XmlElement(name = "singleton", type = SingletonBeanInfo.class),
        @XmlElement(name = "message-driven", type = MessageDrivenBeanInfo.class),
        @XmlElement(name = "managed-bean", type = ManagedBeanInfo.class)
    })
    public final List<EnterpriseBeanInfo> enterpriseBeans = new ArrayList<EnterpriseBeanInfo>();

    public final List<SecurityRoleInfo> securityRoles = new ArrayList<>();
    public final List<MethodPermissionInfo> methodPermissions = new ArrayList<>();
    public final List<MethodTransactionInfo> methodTransactions = new ArrayList<>();
    public final List<MethodConcurrencyInfo> methodConcurrency = new ArrayList<>();
    public final List<InterceptorInfo> interceptors = new ArrayList<>();
    public final List<InterceptorBindingInfo> interceptorBindings = new ArrayList<>();
    public final List<MethodInfo> excludeList = new ArrayList<>();
    public final List<ApplicationExceptionInfo> applicationException = new ArrayList<>();
    public final List<PortInfo> portInfos = new ArrayList<>();
    public final Set<String> watchedResources = new TreeSet<>();
    public final JndiEncInfo moduleJndiEnc = new JndiEncInfo();

    public BeansInfo beans;
    public Set<String> mbeans = new TreeSet<>();
    public final List<IdPropertiesInfo> pojoConfigurations = new ArrayList<>();
}
