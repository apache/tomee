package org.apache.openejb.server.cli.command;

import org.apache.openejb.server.cli.StreamManager;
import org.apache.openejb.server.groovy.OpenEJBGroovyShell;

import java.io.File;

public class GroovyFileCommand extends AbstractCommand {
    private OpenEJBGroovyShell shell;

    @Override
    public String name() {
        return "groovy file";
    }

    @Override
    public Runnable executable(final String cmd) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    streamManager.writeOut(streamManager.asString(shell.evaluate(new File(cmd.substring(name().length() + 1)))));
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
