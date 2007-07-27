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
package org.apache.openejb.assembler.classic.cmd;

import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.loader.SystemInstance;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import java.io.File;

/**
 * @version $Rev$ $Date$
 */
@Stateless(name = "openejb/ConfigurationInfo")
@Remote(ConfigurationInfo.class)
public class ConfigurationInfoEjb implements ConfigurationInfo {

    public OpenEjbConfiguration getOpenEjbConfiguration(File tmpFile) throws UnauthorizedException {
        if (tmpFile.exists()) {
            return SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        }

        throw new UnauthorizedException("Machine not authorized to see this server's configuration properties");
    }

}

