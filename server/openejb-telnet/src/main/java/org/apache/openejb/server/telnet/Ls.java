package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.openejb.Container;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

public class Ls extends Command {

    public static void register() {
        Ls cmd = new Ls();
        Command.register("system", cmd);

    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {

        ContainerSystem containerSystem1 = (ContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        Container[] c = containerSystem1.containers();
        out.println("Containers:");

        for (int i = 0; i < c.length; i++) {
            out.print(" " + c[i].getContainerID());
            out.println("");
        }
        out.println("");

        out.println("Deployments:");

        ContainerSystem containerSystem = (ContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        DeploymentInfo[] d = containerSystem.deployments();
        for (int i = 0; i < d.length; i++) {
            out.print(" " + d[i].getDeploymentID());
            out.println("");
        }
    }

}

