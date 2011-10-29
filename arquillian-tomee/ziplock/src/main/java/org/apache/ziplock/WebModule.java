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
package org.apache.ziplock;

import org.apache.openejb.jee.WebApp;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @version $Rev$ $Date$
 */
public class WebModule {

    private final WebApp webApp = new WebApp();

    private final WebArchive archive;

    public WebModule(WebArchive archive) {
        this.archive = archive;
    }

    public WebModule(String name) {
        this(ShrinkWrap.create(WebArchive.class, name));
    }

    public WebModule(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    public WebArchive getArchive() {
        return archive;
    }
}
