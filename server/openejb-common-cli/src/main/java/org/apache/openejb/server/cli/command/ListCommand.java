package org.apache.openejb.server.cli.command;

import org.apache.openejb.util.helper.CommandHelper;

import java.io.PrintStream;

public class ListCommand extends AbstractCommand {
    @Override
    public String name() {
        return "list";
    }

    @Override
    public Runnable executable(String cmd) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    CommandHelper.listEJBs(streamManager.getLineSep()).print(new PrintStream(streamManager.getOut()));
                } catch (Exception e) {
                    streamManager.writeErr(e);
                }
            }
        };
    }
}
