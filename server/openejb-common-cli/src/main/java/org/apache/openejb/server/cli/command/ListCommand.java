package org.apache.openejb.server.cli.command;

import org.apache.openejb.util.helper.CommandHelper;

import java.io.PrintStream;

public class ListCommand extends AbstractCommand {
    @Override
    public String name() {
        return "list";
    }

    @Override
    public String description() {
        return "list available ejbs";
    }

    @Override
    public void execute(final String cmd) {
        try {
            CommandHelper.listEJBs(streamManager.getLineSep()).print(new PrintStream(streamManager.getOut()));
        } catch (Exception e) {
            streamManager.writeErr(e);
        }
    }
}
