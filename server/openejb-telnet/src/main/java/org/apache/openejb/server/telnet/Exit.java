package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Exit extends Command {

    public static void register() {
        Command.register("exit", Exit.class);
    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {
        throw new UnsupportedOperationException();
    }

}

