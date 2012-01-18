package org.apache.openejb.server.cli.command;

import org.apache.openejb.server.groovy.OpenEJBGroovyShell;

public class GroovyCommand extends AbstractCommand {
    private OpenEJBGroovyShell shell;

    @Override
    public String name() {
        return "groovy";
    }

    @Override
    public Runnable executable(final String cmd) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    streamManager.writeOut(streamManager.asString(shell.evaluate(cmd.substring(name().length() + 1))));
                } catch (Exception e) {
                    streamManager.writeErr(e);
                }
            }
        };
    }

    public void setShell(OpenEJBGroovyShell shell) {
        this.shell = shell;
    }
}
