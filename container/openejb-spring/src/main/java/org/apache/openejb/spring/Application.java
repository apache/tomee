/**
 *
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
package org.apache.openejb.spring;

import java.util.List;
import java.util.Collections;
import java.io.IOException;
import java.io.File;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ConfigurationFactory;
import org.springframework.core.io.Resource;

@Exported
public class Application extends AbstractApplication {
    private final ConfigurationFactory configurationFactory = new ConfigurationFactory();
    private Resource jarFile;

    public Resource getJarFile() {
        return jarFile;
    }

    public void setJarFile(Resource jarFile) {
        this.jarFile = jarFile;
    }

    protected List<AppInfo> loadApplications() throws OpenEJBException {
        File file;
        try {
            file = jarFile.getFile();
        } catch (IOException e) {
            throw new OpenEJBException("Can not load application " + jarFile);
        }
        AppInfo appInfo = configurationFactory.configureApplication(file);
        return Collections.singletonList(appInfo);
    }
}
