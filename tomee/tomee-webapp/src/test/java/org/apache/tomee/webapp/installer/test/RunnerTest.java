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

    private Object getValue(String key, List<Map<String, String>> runnerResults) {
        for (Map<String, String> result : runnerResults) {
            if (key.equals(result.get("key"))) {
                return result.get("value");
            }
        }
        return null;
    }

    @Test
    public void testInstaller() {
        {
            PathsInterface paths = EasyMock.createNiceMock(PathsInterface.class);
            paths.reset();
            EasyMock.expectLastCall();
            paths.verify();
            EasyMock.expectLastCall().andReturn(Boolean.TRUE).anyTimes();

            InstallerInterface installer = EasyMock.createNiceMock(InstallerInterface.class);
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


            Runner runner = new Runner(installer);
            EasyMock.replay(paths, installer);
            Assert.assertEquals("NONE", getValue("status", runner.execute(false)));
            Assert.assertEquals("NONE", getValue("status", runner.execute(false)));
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(true)));
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(false)));
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(true)));
            EasyMock.verify(paths, installer);
        }
        {
            InstallerInterface installer = EasyMock.createStrictMock(InstallerInterface.class);
            Runner runner = new Runner(installer);
            EasyMock.replay(installer);
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(false)));
            Assert.assertEquals("REBOOT_REQUIRED", getValue("status", runner.execute(true)));
            EasyMock.verify(installer);
        }

    }

}
