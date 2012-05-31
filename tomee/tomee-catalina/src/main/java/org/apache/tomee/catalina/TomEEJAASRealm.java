package org.apache.tomee.catalina;

import java.security.Principal;
import javax.security.auth.callback.CallbackHandler;
import org.apache.catalina.realm.JAASRealm;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;

public class TomEEJAASRealm extends JAASRealm {
    @Override
    protected Principal authenticate(String username, CallbackHandler callbackHandler) {
        final Principal principal = super.authenticate(username, callbackHandler);
        if (principal == null) {
            return null;
        }

        final TomcatSecurityService ss = (TomcatSecurityService) SystemInstance.get().getComponent(SecurityService.class);
        if (ss != null) {
            // normally we don't care about oldstate because the listener already contains one
            // which is the previous one
            // so no need to clean twice here
            if (OpenEJBSecurityListener.requests.get() != null) {
                ss.enterWebApp(this, principal, OpenEJBSecurityListener.requests.get().getWrapper().getRunAs());
            } else {
                ss.enterWebApp(this, principal, null);
            }
        }
        return principal;
    }
}
