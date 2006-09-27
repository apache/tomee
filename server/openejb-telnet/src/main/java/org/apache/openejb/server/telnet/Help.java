package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Help extends Command {

    public static void register() {
        Command.register("help", Help.class);
    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {
        HashMap hash = Command.commands;
        Set set = hash.keySet();
        Iterator cmds = set.iterator();
        while (cmds.hasNext()) {
            out.print(" " + cmds.next());
            out.println("");
        }

    }

}

