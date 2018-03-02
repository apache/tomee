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
package org.apache.tomee.microprofile;

import org.apache.openejb.component.ClassLoaderEnricher;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.Service;

import java.net.URL;
import java.util.Properties;

/**
 * This is used as an optional service in org.apache.tomee.catalina.TomcatLoader
 */
@SuppressWarnings("unused")
public class MicroProfileService implements Service {
    @Override
    public void init(final Properties props) throws Exception {
        enrichClassLoaderWithMicroProfile();
    }

    private void enrichClassLoaderWithMicroProfile() {
        final ClassLoaderEnricher enricher = SystemInstance.get().getComponent(ClassLoaderEnricher.class);
        if (null != enricher) {
            final MicroProfileClassLoaderEnricher classLoaderEnricher = new MicroProfileClassLoaderEnricher();
            for (final URL url : classLoaderEnricher.enrichment(null)) {
                enricher.addUrl(url);
            }
        }
        SystemInstance.get().removeObserver(this);
    }
}
