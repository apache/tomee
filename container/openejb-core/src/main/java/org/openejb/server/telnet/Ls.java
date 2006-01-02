package org.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.openejb.Container;
import org.openejb.DeploymentInfo;
import org.openejb.OpenEJB;

public class Ls extends Command {

    public static void register() {
        Ls cmd = new Ls();
        Command.register("system", cmd);

    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException{

        Container[] c = OpenEJB.containers();
        out.println("Containers:");

        for (int i=0; i < c.length; i++){
            out.print(" "+c[i].getContainerID());
            out.println("");
        }
        out.println("");

        out.println("Deployments:");

        DeploymentInfo[] d = OpenEJB.deployments();
        for (int i=0; i < d.length; i++){
            out.print(" "+d[i].getDeploymentID());
            out.println("");
        }
    }

}

