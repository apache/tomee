package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;

public class Stop extends Command {

    public static void register() {
        Command.register("stop", Stop.class);
    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {
        try {
            String addr = Thread.currentThread().getName();
            InetAddress client = InetAddress.getByName(addr);

        } catch (SecurityException e) {
            out.println("Permission denied. " + e.getMessage());
        } catch (Exception e) {
            out.println("Error occured. " + e.getMessage());
        }
        try {
            Thread.sleep(2000);
        } catch (Throwable t) {
        }
    }

}

