package jug.client.command.impl;

import jug.client.command.api.Command;

@Command(name = "new-poll", usage = "new-poll [<name>, <question>]", description = "create a new poll")
public class NewPollCommand extends QueryAndPostCommand {
    @Override
    protected String getName() {
        return "name";
    }

    @Override
    protected String getPath() {
        return "api/subject/create";
    }
}
