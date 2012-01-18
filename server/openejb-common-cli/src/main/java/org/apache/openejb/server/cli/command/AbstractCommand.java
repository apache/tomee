package org.apache.openejb.server.cli.command;

import org.apache.openejb.server.cli.StreamManager;

public abstract class AbstractCommand {
    protected StreamManager streamManager;
    protected  String command;

    public abstract String name();
    public abstract Runnable executable(final String cmd);

    public void setStreamManager(StreamManager streamManager) {
        this.streamManager = streamManager;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void execute(String line) {
        executable(line).run();
    }
}
