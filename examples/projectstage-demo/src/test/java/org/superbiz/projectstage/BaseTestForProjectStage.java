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
package org.superbiz.projectstage;

import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.spi.config.ConfigSourceProvider;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.Manager;
import org.superbiz.ManagerFactory;
import org.superbiz.projectstage.util.ProjectStageProducer;

import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public abstract class BaseTestForProjectStage {

    @Inject
    protected Manager manager;

    protected static WebArchive war(final String projectStageName) {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(ProjectStageProducer.class, BaseTestForProjectStage.class, Manager.class, ManagerFactory.class)
                .addAsResource(new StringAsset("org.apache.deltaspike.ProjectStage = " + projectStageName), ArchivePaths.create(ProjectStageProducer.CONFIG_PATH))
                .addAsServiceProvider(ConfigSourceProvider.class, ProjectStageProducer.class)
                .addAsLibraries(JarLocation.jarLocation(ProjectStage.class))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void checkManagerValue() {
        assertEquals(ProjectStageProducer.value("org.apache.deltaspike.ProjectStage"), manager.name());
    }
}
