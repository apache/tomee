package org.apache.openejb.karaf.command;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

@Command(scope = "openejb", name = "undeploy", description = "undeploy a JEE file/directory")
public class Undeploy extends DeploymentCommand {
    @Argument(index = 0, name = "path", description = "location of the archive file/directory", required = true, multiValued = false)
    private String path;

    @Override
    protected Object doExecute() throws Exception {
        final String moduleId = moduleId(path);
        for (AppContext app : SystemInstance.get().getComponent(ContainerSystem.class).getAppContexts()) {
            if (moduleId.equals(app.getId())) { // module id is used only to check the correct app is deployed
                lookup(Deployer.class, "openejb/DeployerBusinessRemote").undeploy(path);
                return null;
            }
        }

        System.out.println(path + " application not found");

        return null;
    }
}
