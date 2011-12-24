package org.apache.openejb.karaf.command;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.openejb.assembler.Deployer;

import java.io.File;

@Command(scope = "openejb", name = "deploy", description = "deploy a JEE file/directory")
public class Deploy extends DeploymentCommand {
    @Argument(index = 0, name = "path", description = "location of the archive file/directory", required = true, multiValued = false)
    private String path;

    @Override
    protected Object doExecute() throws Exception {
        if (!new File(path).exists()) {
            System.out.println(path + " doesn't exist");
            return null;
        }

        lookup(Deployer.class, "openejb/DeployerBusinessRemote").deploy(path, properties(path));

        return null;
    }
}
