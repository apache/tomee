package org.apache.openejb.server.cxf;

import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.validate.UsernameTokenValidator;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import java.io.IOException;

/**
 * @author Romain Manni-Bucau
 */
public class OpenEJBLoginValidator extends UsernameTokenValidator {
    @Override protected void verifyDigestPassword(UsernameToken usernameToken,
                                           RequestData data) throws WSSecurityException {
        // check password
        super.verifyDigestPassword(usernameToken, data);

        // get the plain text password
        WSPasswordCallback pwCb = new WSPasswordCallback(usernameToken.getName(),
                null, usernameToken.getPasswordType(), WSPasswordCallback.USERNAME_TOKEN, data);
        try {
            data.getCallbackHandler().handle(new Callback[]{pwCb});
        } catch (Exception e) {
            // no-op: the login will fail
        }

        // log the user
        final String user = usernameToken.getName();
        final String password = pwCb.getPassword();
        SecurityService securityService = SystemInstance.get().getComponent(SecurityService.class);
        Object token;
        try {
            securityService.disassociate();

            token = securityService.login(user, password);
            securityService.associate(token);

        } catch (LoginException e) {
            throw new SecurityException("cannot log user " + user, e);
        }
    }
}
