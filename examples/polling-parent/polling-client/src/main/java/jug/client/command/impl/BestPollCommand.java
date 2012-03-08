package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import javax.ws.rs.core.Response;

@Command(name = "best", usage = "best", description = "find best poll")
public class BestPollCommand extends AbstractCommand {
    @Override
    protected Response invoke(String cmd) {
        return client.path("api/subject/best").get();
    }
}
