/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tomee.application.composer.internal;

import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.FinderFactory;
import org.apache.openejb.config.WebModule;
import org.apache.openejb.jee.WebApp;
import org.apache.tomee.application.composer.component.Web;

import java.net.URL;
import java.util.Collections;
import java.util.List;

public final class Bundler {
    public static WebModule createWebModule(final Web web, final ClassLoader loader,
                                            final List<URL> jarList,
                                            final String path) {
        final String contextRoot = web == null ? "" : web.value();
        final WebModule webModule = new WebModule(
                new WebApp(),
                contextRoot,
                loader,
                path,
                contextRoot);

        webModule.setUrls(jarList);
        webModule.setAddedUrls(Collections.<URL>emptyList());
        webModule.setRarUrls(Collections.<URL>emptyList());
        webModule.setScannableUrls(jarList);
        try {
            webModule.setFinder(FinderFactory.createFinder(webModule));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }

        DeploymentLoader.addBeansXmls(webModule);

        return webModule;
    }

    private Bundler() {
        // no-op
    }
}
