package org.apache.openejb.arquillian.tests.realm;

import org.apache.catalina.realm.GenericPrincipal;
import org.apache.tomee.catalina.realm.event.UserPasswordAuthenticationEvent;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import java.util.Arrays;

@RequestScoped
public class MultiAuthenticator {
    private boolean stacked = false;

    public void authenticate(@Observes final UserPasswordAuthenticationEvent event) {
        if (!"secret".equals(event.getCredential())) {
            return; // not authenticated
        }
        event.setPrincipal(new GenericPrincipal(event.getUsername(), "", Arrays.asList(event.getUsername())));
    }

    public void stacked(@Observes final UserPasswordAuthenticationEvent event) {
        stacked = true;
    }

    public boolean isStacked() {
        return stacked;
    }
}
