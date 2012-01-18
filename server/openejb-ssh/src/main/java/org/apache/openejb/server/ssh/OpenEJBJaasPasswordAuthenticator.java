package org.apache.openejb.server.ssh;

import org.apache.sshd.server.jaas.JaasPasswordAuthenticator;

public class OpenEJBJaasPasswordAuthenticator extends JaasPasswordAuthenticator {
    private OpenEJBShellFactory shellFactory;

    public OpenEJBJaasPasswordAuthenticator(OpenEJBShellFactory sf) {
        this.shellFactory = sf;
    }

    @Override
    public boolean authenticate(final String username, final String password) {
        if (super.authenticate(username, password)) {
            shellFactory.setUsername(username);
            return true;
        }
        return false;
    }
}
