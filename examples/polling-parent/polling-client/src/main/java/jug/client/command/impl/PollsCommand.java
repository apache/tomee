package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import javax.ws.rs.core.Response;

@Command(name = "polls", usage = "polls", description = "list polls")
public class PollsCommand extends AbstractCommand {
    @Override
    public Response invoke(final String cmd) {
        return client.path("api/subject/list").get();
    }
}
