package jug.client.command.impl;

import jug.client.command.api.AbstractCommand;
import jug.client.command.api.Command;

import javax.ws.rs.core.Response;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class QueryAndPostCommand extends AbstractCommand {
    private static final Pattern PATTERN = Pattern.compile(" \\[(.*),(.*)\\]");

    @Override
    protected Response invoke(final String cmd) {
        final Matcher matcher = PATTERN.matcher(cmd.substring(getClass().getAnnotation(Command.class).name().length()));
        if (!matcher.matches() || matcher.groupCount() != 2) {
            System.err.println("'" + cmd + "' doesn't match command usage");
            return null;
        }

        return client.path(getPath()).query(getName(), matcher.group(1).trim()).post(prePost(matcher.group(2).trim()));
    }

    protected String prePost(final String post) {
        return post;
    }

    protected abstract String getName();

    protected abstract String getPath();
}
