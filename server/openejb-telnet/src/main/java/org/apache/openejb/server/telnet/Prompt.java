package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Prompt extends Command {

    public static void _DONT_register() {
        Command.register("prompt", Prompt.class);
    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {
        try {
            if (args.count() == 0) return;

            TextConsole.PROMPT = args.get(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

