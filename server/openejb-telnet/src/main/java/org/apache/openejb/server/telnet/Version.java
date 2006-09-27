package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Properties;

public class Version extends Command {

    public static void register() {
        Command.register("version", Version.class);
    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {
        /*
         * Output startup message
         */
        Properties versionInfo = new Properties();

        try {
            versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
        } catch (java.io.IOException e) {
        }
        out.print("OpenEJB Remote Server ");
        out.print(versionInfo.getProperty("version"));
        out.print("    build: ");
        out.print(versionInfo.getProperty("date"));
        out.print("-");
        out.println(versionInfo.getProperty("time"));
        out.println(versionInfo.getProperty("url"));
    }
}

