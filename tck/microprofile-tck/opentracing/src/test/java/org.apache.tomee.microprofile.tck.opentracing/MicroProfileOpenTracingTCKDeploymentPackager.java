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
package org.apache.tomee.microprofile.tck.opentracing;

import io.opentracing.mock.MockTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import jakarta.ws.rs.ext.Providers;
import org.eclipse.microprofile.opentracing.ClientTracingRegistrarProvider;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.protocol.servlet5.v_5.ServletProtocolDeploymentPackager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.Collection;
import java.util.Map;

import static org.apache.openejb.loader.JarLocation.jarLocation;

public class MicroProfileOpenTracingTCKDeploymentPackager extends ServletProtocolDeploymentPackager {
    @Override
    public Archive<?> generateDeployment(final TestDeployment testDeployment,
                                         final Collection<ProtocolArchiveProcessor> processors) {

        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "microprofile-opentracing.war")
                                                .merge(testDeployment.getApplicationArchive());

        // opentracing-api jar added by Geronimo Impl. Also added by TCK causes issues with same classes in different Classloaders
        final Map<ArchivePath, Node> content = webArchive.getContent(object -> object.get().matches(".*opentracing-api.*jar.*"));
        content.forEach((archivePath, node) -> webArchive.delete(archivePath));

        // TCK expects a MockTracer. Check org/eclipse/microprofile/opentracing/tck/application/TracerWebService.java:133
        webArchive.addAsLibrary(jarLocation(MockTracer.class));
        webArchive.addAsLibrary(jarLocation(ThreadLocalScopeManager.class));
        webArchive.addAsWebInfResource("META-INF/beans.xml");
        webArchive.addClass(MicroProfileOpenTracingTCKTracer.class);
        webArchive.addClass(MicroProfileOpenTrackingContextResolver.class);
        webArchive.addAsServiceProvider(Providers.class, MicroProfileOpenTrackingContextResolver.class);

        System.out.println(webArchive.toString(true));

        return super.generateDeployment(
                new TestDeployment(null, webArchive, testDeployment.getAuxiliaryArchives()), processors);
    }
}
