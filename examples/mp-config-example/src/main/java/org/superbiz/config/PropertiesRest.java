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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/sample")
@ApplicationScoped
public class PropertiesRest {

    @Inject
    private Config config;

    @Inject
    @ConfigProperty(name = "defaultProperty", defaultValue = "ALOHA")
    private String defaultProperty;

    @Inject
    @ConfigProperty(name = "java.runtime.version")
    private String javaVersion;

    /**
     *
     * Get the default value configured on @ConfigProperty, that because
     * the property defaultProperty doesn't exists, so it will get the value
     * configured on defaultValue
     *
     * @return defaultValue from @ConfigProperty
     */
    @GET
    @Path("defaultProperty")
    public String getDefaultProperty() {
        return defaultProperty;
    }

    /**
     *
     * Get the value from property java.runtime.version, but in this case
     * it shows how you can get the value using Config class.
     *
     * @return javaVersion from Config.getValue
     */
    @GET
    @Path("javaVersion")
    public String getJavaVersionPropertyFromSystemProperties() {
        return config.getValue("java.runtime.version", String.class);
    }

    /**
     * Get the value from property java.runtime.version, but in this case
     * it shows how you can get value injected using @ConfigProperty.
     *
     * @return javaVersion injected from Config
     */
    @GET
    @Path("injectedJavaVersion")
    public String getJavaVersionWithInjection() {
        return javaVersion;
    }
}
