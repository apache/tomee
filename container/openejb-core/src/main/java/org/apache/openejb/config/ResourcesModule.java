/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import java.io.File;
import java.net.URI;
import java.util.Set;

// a fake module type to wrap a xml resource file in a module
// to be able to deploy only resources
public class ResourcesModule extends Module implements DeploymentModule {
    @Override
    public String getModuleId() {
        return null;
    }

    @Override
    public URI getModuleUri() {
        return null;
    }

    @Override
    public String getJarLocation() {
        return null;
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public ValidationContext getValidation() {
        return null;
    }

    @Override
    public Set<String> getWatchedResources() {
        return null;
    }
}
