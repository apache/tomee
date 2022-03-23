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
package org.apache.tomee.overlay;

import org.apache.catalina.core.StandardContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomee.catalina.TomcatWebAppBuilder;

import jakarta.servlet.ServletContext;

// mainly to avoid classloading issue since TomcatWebAppBuilder is added on the fly to tomcat classloader
public final class Deployer {
    public static void deploy(final ServletContext ctx) {
        final TomcatWebAppBuilder builder = SystemInstance.get().getComponent(TomcatWebAppBuilder.class);
        builder.configureStart(null, StandardContext.class.cast(Reflections.get(Reflections.get(ctx, "context"), "context")));
    }

    private Deployer() {
        // no-op
    }
}
