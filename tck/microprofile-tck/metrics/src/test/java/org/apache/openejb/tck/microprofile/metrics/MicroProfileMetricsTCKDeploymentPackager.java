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
package org.apache.openejb.tck.microprofile.metrics;

import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.protocol.servlet.v_2_5.ServletProtocolDeploymentPackager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.Collection;

/**
 * Metrics TCK provides Archives in JAR format. Arquillian transforms them in EAR. To simplify, override the behavior
 * and wrap them into a WAR.
 */
public class MicroProfileMetricsTCKDeploymentPackager extends ServletProtocolDeploymentPackager {
    @Override
    public Archive<?> generateDeployment(final TestDeployment testDeployment,
                                         final Collection<ProtocolArchiveProcessor> processors) {
        final Archive<?> applicationArchive = testDeployment.getApplicationArchive();
        if (applicationArchive instanceof JavaArchive) {
            final WebArchive wrapperWar =
                    ShrinkWrap.create(WebArchive.class, "microprofile-metrics.war").addAsLibrary(applicationArchive);
            return super.generateDeployment(new TestDeploymentDelegate(testDeployment, wrapperWar), processors);
        }

        return super.generateDeployment(testDeployment, processors);
    }

    private static class TestDeploymentDelegate extends TestDeployment {
        private TestDeployment testDeployment;
        private Archive<?> archive;

        public TestDeploymentDelegate(final TestDeployment testDeployment, final Archive<?> archive) {
            super(null, archive, testDeployment.getAuxiliaryArchives());
            this.testDeployment = testDeployment;
            this.archive = archive;
        }

        @Override
        public TargetDescription getTargetDescription() {
            return testDeployment.getTargetDescription();
        }

        @Override
        public ProtocolDescription getProtocolDescription() {
            return testDeployment.getProtocolDescription();
        }

        @Override
        public String getDeploymentName() {
            return testDeployment.getDeploymentName();
        }

        @Override
        public Archive<?> getArchiveForEnrichment() {
            return testDeployment.getArchiveForEnrichment();
        }

        @Override
        public Archive<?> getApplicationArchive() {
            return archive;
        }

        @Override
        public Collection<Archive<?>> getAuxiliaryArchives() {
            return testDeployment.getAuxiliaryArchives();
        }
    }
}
