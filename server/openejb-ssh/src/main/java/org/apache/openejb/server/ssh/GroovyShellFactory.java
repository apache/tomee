package org.apache.openejb.server.ssh;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;

public class GroovyShellFactory implements Factory<Command> {
    @Override
    public Command create() {
        return new GroovyCommand();
    }
}
