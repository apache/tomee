package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import javax.ws.rs.core.Response;

import static jug.client.command.impl.ShowPollCommand.SHOW_POLL_CMD;

@Command(name = SHOW_POLL_CMD, usage = SHOW_POLL_CMD + " <name>", description = "show a poll")
public class ShowPollCommand extends AbstractCommand {
    public static final String SHOW_POLL_CMD = "show-poll";

    @Override
    protected Response invoke(String cmd) {
        if (SHOW_POLL_CMD.length() + 1 >= cmd.length()) {
            System.err.println("please specify a poll name");
            return null;
        }

        return client.path("api/subject/find/".concat(cmd.substring(SHOW_POLL_CMD.length() + 1))).get();
    }
}
