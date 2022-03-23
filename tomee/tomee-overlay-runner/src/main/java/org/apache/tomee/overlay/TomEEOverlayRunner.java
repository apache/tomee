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

import org.apache.tomee.loader.TomcatEmbedder;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import java.io.File;
import java.util.Properties;
import java.util.Set;

public class TomEEOverlayRunner implements ServletContainerInitializer {
    @Override
    public void onStartup(final Set<Class<?>> classes, final ServletContext ctx) throws ServletException {
        if (System.getProperty("openejb.embedder.source") != null) {
            ctx.log("TomEE already initialized");
            return;
        }

        ctx.log("Embedded TomEE starting");

        final Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.setProperty("openejb.system.apps", Boolean.toString(Boolean.getBoolean("openejb.system.apps")));
        properties.setProperty("tomee.war", new File(ctx.getRealPath("WEB-INF")).getParentFile().getAbsolutePath());
        properties.setProperty("openejb.embedder.source", getClass().getSimpleName());

        TomcatEmbedder.embed(properties, ServletContainerInitializer.class.getClassLoader());

        ctx.log("Embedded TomEE started");

        Deployer.deploy(ctx);

        ctx.log("Application '" + ctx.getContextPath() + "' TomEE-ised");
    }
}
