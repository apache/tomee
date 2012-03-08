package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import javax.ws.rs.core.Response;

import static jug.client.command.impl.PollResultCommand.RESULT_CMD;

@Command(name = RESULT_CMD, usage = RESULT_CMD + " <name>", description = "result of a poll")
public class PollResultCommand extends AbstractCommand {
    public static final String RESULT_CMD = "result";

    @Override
    protected Response invoke(String cmd) {
        if (RESULT_CMD.length() + 1 >= cmd.length()) {
            System.err.println("please specify a poll name");
            return null;
        }

        return client.path("api/subject/result/".concat(cmd.substring(RESULT_CMD.length() + 1))).get();
    }
}
