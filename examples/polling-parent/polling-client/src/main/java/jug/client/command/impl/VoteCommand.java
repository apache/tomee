package jug.client.command.impl;

import jug.client.command.api.Command;
import jug.domain.Value;

@Command(name = "vote", usage = "vote [<subject name>, +1|-1]", description = "vote for a subject")
public class VoteCommand extends QueryAndPostCommand {
    @Override
    protected String getName() {
        return "subject";
    }

    @Override
    protected String getPath() {
        return "api/subject/vote";
    }

    @Override
    protected String prePost(final String post) {
        if ("+1".equals(post) || "like".equals(post)) {
            return Value.I_LIKE.name();
        }
        if ("-1".equals(post)) {
            return Value.I_DONT_LIKE.name();
        }
        throw new IllegalArgumentException("please use +1 or -1 and not '" + post + "'");
    }
}
