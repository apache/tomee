/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.tomee.gradle.embedded;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TomEEEmbeddedExtension {
    public static final String NAME = "tomee-embedded";

    private boolean skipDefaultRepository = false;
    private String tomeeVersion;

    public boolean isSkipDefaultRepository() {
        return skipDefaultRepository;
    }

    public void setSkipDefaultRepository(final boolean skipDefaultRepository) {
        this.skipDefaultRepository = skipDefaultRepository;
    }

    public String getTomeeVersion() {
        if (tomeeVersion == null) {
            tomeeVersion = "7.0.0";
            try {
                try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("META-INF/maven/org.apache.tomee.gradle/tomee-embedded/pom.properties")) {
                    final Properties p = new Properties();
                    p.load(is);
                    tomeeVersion = p.getProperty("version", tomeeVersion);
                }
            } catch (IOException e) {
                // no-op
            }
        }
        return tomeeVersion;
    }

    public void setTomeeVersion(final String tomeeVersion) {
        this.tomeeVersion = tomeeVersion;
    }
}
