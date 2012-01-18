package org.apache.openejb.server.cli;

import jline.ConsoleReader;
import jline.FileNameCompletor;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.assembler.classic.cmd.Info2Properties;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.server.cli.command.AbstractCommand;
import org.apache.openejb.server.cli.command.Deploy;
import org.apache.openejb.server.cli.command.ExitCommand;
import org.apache.openejb.server.cli.command.GroovyCommand;
import org.apache.openejb.server.cli.command.GroovyFileCommand;
import org.apache.openejb.server.cli.command.HelpCommand;
import org.apache.openejb.server.cli.command.ListCommand;
import org.apache.openejb.server.cli.command.PropertiesCommand;
import org.apache.openejb.server.cli.command.Undeploy;
import org.apache.openejb.server.groovy.OpenEJBGroovyShell;
import org.apache.openejb.util.helper.CommandHelper;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CliRunnable implements Runnable {
    public static final String EXIT_COMMAND = "exit";
    private static final String WELCOME = "Welcome on your $bind:$port $name server";
    private static final String OS_LINE_SEP = System.getProperty("line.separator");
    private static final String NAME;
    private static final String PROMPT;
    private static final String PROMPT_SUFFIX = "> ";
    private static final List<Class<? extends AbstractCommand>> COMMAND_CLASSES
            = Arrays.asList(GroovyCommand.class, GroovyFileCommand.class,
                ListCommand.class, PropertiesCommand.class,
                Deploy.class, Undeploy.class,
                HelpCommand.class, ExitCommand.class);

    static {
        String name = "OpenEJB";
        try {
            CliRunnable.class.getClassLoader().loadClass("org.apache.tomee.loader.TomcatHook");
            name = "TomEE";
        } catch (ClassNotFoundException cnfe) {
            // ignored, we are using a simple OpenEJB server
        }
        NAME = name;
        PROMPT = NAME.toLowerCase() + PROMPT_SUFFIX;
    }

    public String lineSep;

    private OpenEJBGroovyShell shell;
    private OutputStream err;
    private OutputStream out;
    private InputStream sin;
    private String username;
    private String bind;
    private int port;
    private Map<String, Class<?>> commands;


    public CliRunnable(String bind, int port) {
        this(bind, port, PROMPT, null);
    }

    public CliRunnable(String bind, int port, String username, String sep) {
        this.bind = bind;
        this.port = port;
        this.username = username;

        if (sep != null) { // workaround to force ConsoleReader to use another line.separator
            lineSep = sep;
            System.setProperty("line.separator", sep);
            try {
                // just to force the loading of this class with the set line.separator
                // because ConsoleReader.CR is a constant and we need sometimes another value
                // not a big issue but keeping this as a workaround
                new ConsoleReader();
            } catch (IOException ignored) {
                // no-op
            } finally {
                System.setProperty("line.separator", OS_LINE_SEP);
            }
        } else {
            lineSep = OS_LINE_SEP;
        }
    }

    public void setInputStream(InputStream in) {
        sin = in;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    public void start() throws IOException {
        shell = new OpenEJBGroovyShell();
        initializeCommands();
        new Thread(this, "OpenEJB Cli").start();
    }

    private void initializeCommands() {
        commands = new HashMap<String, Class<?>>();

        for (Class<? extends AbstractCommand> cmd : COMMAND_CLASSES) {
            try {
                commands.put(cmd.newInstance().name(), cmd);
            } catch (Exception e) {
                // command ignored
            }
        }
    }

    public void destroy() {
        shell.resetLoadedClasses();
        commands.clear();
    }

    public void run() {
        try {
            final StreamManager streamManager = new StreamManager(out, err, lineSep);

            final ConsoleReader reader = new ConsoleReader(sin, streamManager.getSout());
            reader.addCompletor(new FileNameCompletor());
            // TODO : add completers with method names...?

            String line;
            streamManager.writeOut(WELCOME // simple replace for now, if it is mandatory we could bring velocity to do it
                    .replace("$bind", bind)
                    .replace("$port", Integer.toString(port))
                    .replace("$name", NAME));

            while ((line = reader.readLine(prompt())) != null) {
                // exit simply let us go out of the loop
                // do we need a command for it?
                if (EXIT_COMMAND.equals(line)) {
                    break;
                }

                Class<?> cmdClass = null;
                for (Map.Entry<String, Class<?>> cmd : commands.entrySet()) {
                    if (line.startsWith(cmd.getKey())) {
                        cmdClass = cmd.getValue();
                        break;
                    }
                }

                if (cmdClass != null) {
                    ObjectRecipe recipe = new ObjectRecipe(cmdClass);
                    recipe.setProperty("streamManager", streamManager);
                    recipe.setProperty("command", line);
                    recipe.setProperty("shell", shell);
                    recipe.setProperty("commands", commands);

                    recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
                    recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
                    recipe.allow(Option.NAMED_PARAMETERS);

                    try {
                        final AbstractCommand cmdInstance = (AbstractCommand) recipe.create();
                        cmdInstance.execute(line);
                    } catch (Exception e) {
                        streamManager.writeErr(e);
                    }
                } else {
                    streamManager.writeErr("sorry i don't understand '" + line + "'");
                }
            }
        } catch (IOException e) {
            throw new CliRuntimeException(e);
        }
    }

    private String prompt() {
        final StringBuilder prompt = new StringBuilder("");
        if (username != null) {
            prompt.append(username);
        } else {
            prompt.append(PROMPT);
        }
        prompt.append(" @ ")
            .append(bind).append(":").append(port)
            .append(PROMPT_SUFFIX);
        return prompt.toString();
    }

    private void properties() {
        final OpenEjbConfiguration config = SystemInstance.get().getComponent(OpenEjbConfiguration.class);
        Info2Properties.printConfig(config, new PrintStream(out), lineSep);
    }

    private void list(final StreamManager streamManager) {
        try {
            CommandHelper.listEJBs(lineSep).print(new PrintStream(out));
        } catch (Exception e) {
            streamManager.writeErr(e);
        }
    }

    public String getBind() {
        return bind;
    }

    public int getPort() {
        return port;
    }
}
