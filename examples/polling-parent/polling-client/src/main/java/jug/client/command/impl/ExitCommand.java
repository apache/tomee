package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import javax.ws.rs.core.Response;

// just to get it in the help
@Command(name = "exit", usage = "exit", description = "exit from the cli")
public class ExitCommand extends AbstractCommand {
    @Override
    public void execute(String cmd) {
        throw new UnsupportedOperationException("shouldn't be called directly");
    }

    @Override
    protected Response invoke(String cmd) {
        return null;
    }
}
