package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;
import jug.client.util.ClientNameHolder;

import javax.ws.rs.core.Response;
import java.util.Map;

@Command(name = "client", usage = "client <name>", description = "change client")
public class SwitchClientCommand extends AbstractCommand {
    private Map<String, Class<?>> commands;

    @Override
    public void execute(final String cmd) {
        if (cmd.length() <= "client ".length()) {
            System.err.println("please specify a client name (client1 or client2)");
            return;
        }

        final String client = cmd.substring(7);
        ClientNameHolder.setCurrent(client);
    }

    @Override
    protected Response invoke(String cmd) {
        return null;
    }
}

