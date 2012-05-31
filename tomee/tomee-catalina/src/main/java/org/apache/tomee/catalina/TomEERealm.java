package org.apache.tomee.catalina;

import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.CallbackHandler;
import org.apache.catalina.realm.CombinedRealm;
import org.apache.catalina.realm.JAASRealm;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.ietf.jgss.GSSContext;

public class TomEERealm extends CombinedRealm {
    @Override
    public Principal authenticate(String username, String password) {
        return logInTomEE(super.authenticate(username, password));
    }

    @Override
    public Principal authenticate(X509Certificate[] certs) {
        return logInTomEE(super.authenticate(certs));
    }

    @Override
    public Principal authenticate(String username, String clientDigest,
                                  String nonce, String nc, String cnonce, String qop,
                                  String realmName, String md5a2) {
        return logInTomEE(super.authenticate(username, clientDigest, nonce, nc, cnonce, qop, realmName, md5a2));
    }

    @Override
    public Principal authenticate(GSSContext gssContext, boolean storeCreds) {
        return logInTomEE(super.authenticate(gssContext, storeCreds));
    }

    private Principal logInTomEE(final Principal pcp) {
        if (pcp == null) {
            return null;
        }

        final TomcatSecurityService ss = (TomcatSecurityService) SystemInstance.get().getComponent(SecurityService.class);
        if (ss != null) {
            // normally we don't care about oldstate because the listener already contains one
            // which is the previous one
            // so no need to clean twice here
            if (OpenEJBSecurityListener.requests.get() != null) {
                ss.enterWebApp(this, pcp, OpenEJBSecurityListener.requests.get().getWrapper().getRunAs());
            } else {
                ss.enterWebApp(this, pcp, null);
            }
        }
        return pcp;
    }
}
