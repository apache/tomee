package org.apache.openejb.server.cli.command;

import org.apache.openejb.server.groovy.OpenEJBGroovyShell;

public class GroovyCommand extends AbstractCommand {
    private OpenEJBGroovyShell shell;

    @Override
    public String name() {
        return "groovy";
    }

    @Override
    public String usage() {
        return name() + " <groovy code>";
    }

    @Override
    public String description() {
        return "execute groovy code. ejb can be accessed through their ejb name in the script.";
    }

    @Override
    public void execute(final String cmd) {
        try {
            streamManager.writeOut(streamManager.asString(shell.evaluate(cmd.substring(name().length() + 1))));
        } catch (Exception e) {
            streamManager.writeErr(e);
        }
    }

    public void setShell(OpenEJBGroovyShell shell) {
        this.shell = shell;
    }
}
