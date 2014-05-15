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

package org.apache.openejb.assembler.classic.util;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.IdPropertiesInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;

import java.util.Collection;
import java.util.Properties;

public final class PojoUtil {
    private PojoUtil() {
        // no-op
    }

    public static Properties findConfiguration(final Collection<IdPropertiesInfo> infos, final String id) {
        for (final IdPropertiesInfo info : infos) {
            if (id.equals(info.id)) {
                return info.properties;
            }
        }
        return null;
    }

    public static Collection<IdPropertiesInfo> findPojoConfig(final Collection<IdPropertiesInfo> pojoConfigurations, final AppInfo appInfo, final WebAppInfo webApp) {
        if (pojoConfigurations == null) {
            for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
                if (ejbJarInfo.moduleId.equals(webApp.moduleId)) {
                    return ejbJarInfo.pojoConfigurations;
                }
            }

            // useless normally but we had some code where modulName was the webapp moduleId
            for (final EjbJarInfo ejbJarInfo : appInfo.ejbJars) {
                if (ejbJarInfo.moduleName.equals(webApp.moduleId)) {
                    return ejbJarInfo.pojoConfigurations;
                }
            }
        }
        return pojoConfigurations;
    }
}
