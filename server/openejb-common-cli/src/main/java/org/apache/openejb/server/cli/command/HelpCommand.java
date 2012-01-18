package org.apache.openejb.server.cli.command;

import java.util.Map;

public class HelpCommand extends AbstractCommand {
    private Map<String, Class<?>> commands;

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "print this help";
    }

    @Override
    public void execute(final String cmd) {
        for (Map.Entry<String, Class<?>> command : commands.entrySet()) {
            try {
                final AbstractCommand instance = (AbstractCommand) command.getValue().newInstance();
                streamManager.writeOut(instance.name() + ": " + instance.description());
                streamManager.writeOut("\tUsage: " + instance.usage());
            } catch (Exception e) {
                // ignored = command not available
            }
        }
    }

    public void setCommands(Map<String, Class<?>> commands) {
        this.commands = commands;
    }
}
