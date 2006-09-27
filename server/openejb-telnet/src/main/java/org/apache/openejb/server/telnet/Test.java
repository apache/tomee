package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class Test extends Command {

    public static void _DONT_register() {
        Command.register("test", Test.class);
    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {
        try {

            InputStream file = new FileInputStream("print.txt");
            int b = file.read();
            while (b != -1) {
                out.write(b);
                b = file.read();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

