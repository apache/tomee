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
package org.superbiz.deltaspike.config;

import org.apache.deltaspike.core.impl.config.DefaultConfigSourceProvider;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.openjpa.lib.conf.MapConfigurationProvider;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ConfigTest {

    @Inject
    private Counter counter;

    @Deployment
    public static WebArchive jar() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(Counter.class, MyConfigSource.class, MapConfigurationProvider.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsResource(new ClassLoaderAsset("my-app-config.properties"), "my-app-config.properties")
                .addAsLibraries(JarLocation.jarLocation(ConfigSourceProvider.class))
                .addAsLibraries(JarLocation.jarLocation(DefaultConfigSourceProvider.class))
                .addAsServiceProvider(ConfigSourceProvider.class, MyConfigSourceProvider.class);
    }

    @Test
    public void check() {
        assertNotNull(counter);
        counter.loop();
    }
}
