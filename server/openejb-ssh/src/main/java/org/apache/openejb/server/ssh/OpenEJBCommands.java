package org.apache.openejb.server.ssh;

import org.apache.openejb.server.cli.CliRunnable;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class OpenEJBCommands extends CliRunnable implements Command, Runnable {
    private ExitCallback cbk;

    public OpenEJBCommands(String bind, int port) {
        super(bind, port);
    }

    @Override
    public void setInputStream(InputStream in) {
        super.setInputStream(in);
    }

    @Override
    public void setOutputStream(OutputStream out) {
        super.setOutputStream(out);
    }

    @Override
    public void setErrorStream(OutputStream err) {
        super.setErrorStream(err);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void setExitCallback(ExitCallback callback) {
        cbk = callback;
    }

    @Override
    public void start(Environment env) throws IOException {
        start();
    }

    @Override
    public void run() {
        try {
            super.run();
        } finally {
            cbk.onExit(0);
        }
    }
}
