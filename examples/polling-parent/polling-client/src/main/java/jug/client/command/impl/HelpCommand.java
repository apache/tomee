package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import javax.ws.rs.core.Response;
import java.util.Map;

@Command(name = "help", usage = "help", description = "print this help")
public class HelpCommand extends AbstractCommand {
    private Map<String, Class<?>> commands;

    @Override
    public void execute(final String cmd) {
        for (Map.Entry<String, Class<?>> command : commands.entrySet()) {
            try {
                final Class<?> clazz = command.getValue();
                final Command annotation = clazz.getAnnotation(Command.class);
                System.out.println(annotation.name() + ": " + annotation.description());
                System.out.println("\tUsage: " + annotation.usage());
            } catch (Exception e) {
                // ignored = command not available
            }
        }
    }

    @Override
    protected Response invoke(String cmd) {
        return null;
    }

    public void setCommands(Map<String, Class<?>> commands) {
        this.commands = commands;
    }
}

