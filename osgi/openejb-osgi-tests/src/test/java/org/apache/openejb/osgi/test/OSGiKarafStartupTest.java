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

package org.apache.openejb.osgi.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.inject.Inject;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.NetworkUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.labs.paxexam.karaf.options.KarafDistributionConfigurationFilePutOption;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.ProbeBuilder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openengsb.labs.paxexam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.osgi.framework.Constants.DYNAMICIMPORT_PACKAGE;

// if you replace openengsb by karaf 3.0.0 dependency for testing use:
// import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.karafDistributionConfiguration;

@RunWith(JUnit4TestRunner.class)
public class OSGiKarafStartupTest {
    @Inject
    private CommandProcessor processor;

    @Configuration
    public Option[] configure() throws Exception {
        return options(
                karafDistributionConfiguration()
                        .frameworkUrl(
                                maven().groupId("org.apache.openejb").artifactId("apache-karafee").versionAsInProject().type("tar.gz"))
                        .name("Apache Karafee")
                        .karafVersion(System.getProperty("karaf.version")),
                new KarafDistributionConfigurationFilePutOption("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", Integer.toString(NetworkUtil.getNextAvailablePort())),
                new KarafDistributionConfigurationFilePutOption("etc/org.apache.karaf.management.cfg", "rmiServerPort", Integer.toString(NetworkUtil.getNextAvailablePort())),
                felix()

                // to debug activate next line and create a remote debug configuration in your IDE
                // , vmOption("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005")
        );
    }

    @ProbeBuilder
    public TestProbeBuilder probeConfiguration(final TestProbeBuilder probe) {
        return probe.setHeader(DYNAMICIMPORT_PACKAGE, " *,org.apache.felix.service.*;status=provisional");
    }

    @Test
    public void openejbIsStarted() {
        assertTrue(OpenEJB.isInitialized());
        assertNotNull(SystemInstance.get().getComponent(ContainerSystem.class));
    }

    @Test
    public void commandsAreInstalled() throws Exception {
        assertCommand("openejb:list", "Name");
    }

    private void assertCommand(final String cmd, final String... expected) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream psOut = new PrintStream(out);
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        final PrintStream psErr = new PrintStream(out);

        final CommandSession session = processor.createSession(System.in, psOut, psErr);
        session.execute(cmd);
        session.getConsole().flush();
        session.close();

        assertTrue(err.toString().isEmpty());
        final String sout = out.toString();
        for (String s : expected) {
            assertTrue(sout.contains(s));
        }
    }
}
