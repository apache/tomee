package org.apache.openejb.server.ssh;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;

public class OpenEJBShellFactory implements Factory<Command> {
    private String bind;
    private int port;
    private ThreadLocal<String> username = new ThreadLocal<String>();

    public OpenEJBShellFactory(String bind, int port) {
        this.bind = bind;
        this.port = port;
    }

    @Override
    public Command create() {
        return new OpenEJBCommands(bind, port, username.get());
    }

    public void setUsername(String username) {
        this.username.set(username);
    }
}
