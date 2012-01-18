package org.apache.openejb.server.cli.command;

import org.apache.openejb.server.cli.CliRunnable;

// just to get it in the help
public class ExitCommand extends AbstractCommand {
    @Override
    public String name() {
        return CliRunnable.EXIT_COMMAND ;
    }

    @Override
    public void execute(String cmd) {
        throw new UnsupportedOperationException("shouldn't be called directly");
    }

    @Override
    public String description() {
        return "let you exit";
    }
}
