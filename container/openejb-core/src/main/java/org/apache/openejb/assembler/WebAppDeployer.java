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
package org.apache.openejb.assembler;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.loader.SystemInstance;

import java.io.File;

public interface WebAppDeployer {
    AppInfo deploy(String contextRoot, File file);
    void reload(String appId);

    public static final class Helper {
        private Helper() {
            // no-op
        }

        public static boolean isWebApp(final File file) {
            return (file.getName().endsWith(".war") || new File(file, "WEB-INF").exists())
                        && SystemInstance.get().getComponent(WebAppDeployer.class) != null;
        }
    }
}
