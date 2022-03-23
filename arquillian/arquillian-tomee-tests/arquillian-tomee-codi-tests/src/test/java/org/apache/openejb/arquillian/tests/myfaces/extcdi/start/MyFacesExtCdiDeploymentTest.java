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
package org.apache.openejb.arquillian.tests.myfaces.extcdi.start;

import org.apache.myfaces.extensions.cdi.core.api.projectstage.ProjectStage;
import org.apache.myfaces.extensions.cdi.jsf.impl.projectstage.JsfProjectStageProducer;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon30.WebAppVersionType;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.inject.Inject;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class MyFacesExtCdiDeploymentTest {

    @Inject
    private ProjectStage projectStage;

    @Deployment
    public static WebArchive getArchive() {
        final WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class).version(WebAppVersionType._3_0);
        // web.xml params is not supported by default
        // descriptor.contextParam(ProjectStage.PROJECT_STAGE_PARAM_NAME, ProjectStage.SystemTest.name());

        return ShrinkWrap.create(WebArchive.class, "MyFacesExtCdiDeploymentTest.war")
                .addAsLibraries(JarLocation.jarLocation(JsfProjectStageProducer.class)) // codi
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void testProjectStage() throws Exception {
        assertEquals(ProjectStage.Production, projectStage);
    }
}

