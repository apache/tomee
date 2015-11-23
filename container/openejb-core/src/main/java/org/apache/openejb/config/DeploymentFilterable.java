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

package org.apache.openejb.config;

/**
 * @version $Rev$
 */
public interface DeploymentFilterable {
    String DEPLOYMENTS_CLASSPATH_PROPERTY = "openejb.deployments.classpath";
    String SEARCH_CLASSPATH_FOR_DEPLOYMENTS_PROPERTY = DEPLOYMENTS_CLASSPATH_PROPERTY;
    String CLASSPATH_INCLUDE = "openejb.deployments.classpath.include";
    String CLASSPATH_EXCLUDE = "openejb.deployments.classpath.exclude";
    String PACKAGE_INCLUDE = "openejb.deployments.package.include";
    String PACKAGE_EXCLUDE = "openejb.deployments.package.exclude";
    String CLASSPATH_REQUIRE_DESCRIPTOR = RequireDescriptors.PROPERTY;
    String CLASSPATH_FILTER_DESCRIPTORS = "openejb.deployments.classpath.filter.descriptors";
    String CLASSPATH_FILTER_SYSTEMAPPS = "openejb.deployments.classpath.filter.systemapps";
}
