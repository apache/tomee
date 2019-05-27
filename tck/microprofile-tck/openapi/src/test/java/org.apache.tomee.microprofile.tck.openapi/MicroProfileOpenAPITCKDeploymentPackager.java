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
package org.apache.tomee.microprofile.tck.openapi;

import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.protocol.servlet.v_2_5.ServletProtocolDeploymentPackager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.Collection;
import java.util.Map;

public class MicroProfileOpenAPITCKDeploymentPackager extends ServletProtocolDeploymentPackager {
    @Override
    public Archive<?> generateDeployment(final TestDeployment testDeployment,
                                         final Collection<ProtocolArchiveProcessor> processors) {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "microprofile-openapi.war")
                                                .merge(testDeployment.getApplicationArchive())
                                                // TODO - This doesn't seem right. This is for the JAX-RS endpoints to be CDI scanned.
                                                // This is to use CDI events to filter endpoints with configuration.
                                                // Check org.apache.geronimo.microprofile.openapi.cdi.GeronimoOpenAPIExtension.findEndpointsAndApplication()
                                                // A beans.xml should not be required.
                                                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                ;

        // MP Config in wrong place - See https://github.com/eclipse/microprofile/issues/46.
        if (testDeployment.getApplicationArchive() instanceof WebArchive) {
            final Map<ArchivePath, Node> content =
                    testDeployment.getApplicationArchive().getContent(
                            object -> object.get().matches(".*META-INF/.*"));
            content.forEach((archivePath, node) -> webArchive.addAsResource(node.getAsset(), node.getPath()));
        }

        return super.generateDeployment(
                new TestDeployment(null, webArchive, testDeployment.getAuxiliaryArchives()), processors);
    }
}
