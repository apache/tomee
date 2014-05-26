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
package org.apache.tomee.webapp.installer.test;

import org.apache.tomee.installer.Alerts;
import org.apache.tomee.installer.InstallerInterface;
import org.apache.tomee.installer.PathsInterface;
import org.apache.tomee.installer.Status;
import org.apache.tomee.webapp.installer.Runner;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class RunnerTest {

    private Object getValue(final String key, final List<Map<String, String>> runnerResults) {
        for (final Map<String, String> result : runnerResults) {
            if (key.equals(result.get("key"))) {
                return result.get("value");
            }
        }
        return null;
    }

    @Test
    public void testInstaller() {
        {
            final PathsInterface paths = EasyMock.createNiceMock(PathsInterface.class);
            paths.reset();
            EasyMock.expectLastCall();
            paths.verify();
            EasyMock.expectLastCall().andReturn(Boolean.TRUE).anyTimes();

            final InstallerInterface installer = EasyMock.createNiceMock(InstallerInterface.class);
            installer.getPaths();
            EasyMock.expectLastCall().andReturn(paths).anyTimes();
            installer.reset();
            EasyMock.expectLastCall();
            installer.getStatus();
            EasyMock.expectLastCall().andReturn(Status.NONE);
            installer.getStatus();
            EasyMock.expectLastCall().andReturn(Status.NONE);
            installer.getStatus();
            EasyMock.expectLastCall().andReturn(Status.REBOOT_REQUIRED);
            installer.getAlerts();
            EasyMock.expectLastCall().andReturn(new Alerts()).anyTimes();


            final Runner runner = new Runner(installer);
            EasyMock.replay(paths, installer);
            Assert.assertEquals("NONE", getValue("status", runner.execute(false)));
            Assert.assertEquals("NONE", getValue("status", runner.execute(false)));
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(true)));
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(false)));
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(true)));
            EasyMock.verify(paths, installer);
        }
        {
            final InstallerInterface installer = EasyMock.createStrictMock(InstallerInterface.class);
            final Runner runner = new Runner(installer);
            EasyMock.replay(installer);
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(false)));
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(true)));
            EasyMock.verify(installer);
        }

    }

}
