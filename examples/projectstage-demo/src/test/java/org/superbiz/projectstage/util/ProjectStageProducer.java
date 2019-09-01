/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.projectstage.util;

import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class ProjectStageProducer implements ConfigSourceProvider {

    public static final String CONFIG_PATH = "project-stage.properties";

    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(ProjectStageProducer.class.getResourceAsStream("/project-stage.properties"));
        } catch (IOException e) {
            // no-op
        }
    }

    @Override
    public List<ConfigSource> getConfigSources() {
        return new ArrayList<ConfigSource>() {{
            add(new ConfigSource() {
                @Override
                public int getOrdinal() {
                    return 0;
                }

                @Override
                public String getPropertyValue(final String key) {
                    return value(key);
                }

                @Override
                public String getConfigName() {
                    return "test-project-stage";
                }

                @Override
                public Map<String, String> getProperties() {
                    return Collections.emptyMap();
                }

                @Override
                public boolean isScannable() {
                    return false;
                }
            });
        }};
    }

    public static String value(final String key) {
        return PROPERTIES.getProperty(key);
    }
}
