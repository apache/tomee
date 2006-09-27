package org.apache.openejb.server.telnet;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;

//import org.codehaus.groovy.runtime.InvokerHelper;
//import groovy.lang.GroovyShell;

public class GroovySh extends Command {

    public static void register() {

    }

    public void exec(Arguments args, DataInputStream in, PrintStream out) throws IOException {
/*        GroovyShell shell = new GroovyShell();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String version = InvokerHelper.getVersion();

        out.println("Lets get Groovy!");
        out.println("================");
        out.println("Version: " + version + " JVM: " + System.getProperty("java.vm.version"));
        out.println("Hit carriage return twice to execute a command");
        out.println("The command 'quit' will terminate the shell");

        int counter = 1;
        while (true) {
            StringBuffer buffer = new StringBuffer();
            while (true) {
                out.print("groovy> ");
                String line = reader.readLine();
                if (line != null) {
                    buffer.append(line);
                    buffer.append('\n');
                }
                if (line == null || line.trim().length() == 0) {
                    break;
                }
            }
            String command = buffer.toString().trim();
            if (command == null || command.equals("quit")) {
                break;
            }
            try {
                Object answer = shell.evaluate(command, "CommandLine" + counter++ +".groovy");
                out.println(InvokerHelper.inspect(answer));
            }
            catch (Exception e) {
                out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
*/    }
}

