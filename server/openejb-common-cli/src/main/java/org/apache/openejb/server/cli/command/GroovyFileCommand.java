package org.apache.openejb.server.cli.command;

import org.apache.openejb.server.groovy.OpenEJBGroovyShell;

import java.io.File;

public class GroovyFileCommand extends AbstractCommand {
    private OpenEJBGroovyShell shell;

    @Override
    public String name() {
        return "groovy file";
    }

    @Override
    public String usage() {
        return name() + " <groovy file path>";
    }

    @Override
    public String description() {
        return "execute groovy code contained in a file. ejb can be accessed through their ejb name in the script.";
    }

    @Override
    public void execute(final String cmd) {
        try {
            streamManager.writeOut(streamManager.asString(shell.evaluate(new File(cmd.substring(name().length() + 1)))));
        } catch (Exception e) {
            streamManager.writeErr(e);
        }
    }

    public void setShell(OpenEJBGroovyShell shell) {
        this.shell = shell;
    }
}
