package org.openejb.server.ejbd;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.openejb.client.AuthenticationRequest;
import org.openejb.client.AuthenticationResponse;
import org.openejb.client.ClientMetaData;
import org.openejb.client.RequestMethods;
import org.openejb.client.ResponseCodes;

class AuthRequestHandler implements ResponseCodes, RequestMethods {
    private final EjbDaemon daemon;
    AuthRequestHandler(EjbDaemon daemon) {
        this.daemon = daemon;

    }

    public void processRequest(ObjectInputStream in, ObjectOutputStream out) {
        AuthenticationRequest req = new AuthenticationRequest();
        AuthenticationResponse res = new AuthenticationResponse();

        try {
            req.readExternal( in );

            ClientMetaData client = new ClientMetaData();

            client.setClientIdentity( new String( (String)req.getPrinciple() ) );

            res.setIdentity( client );
            res.setResponseCode( AUTH_GRANTED );

            res.writeExternal( out );
        } catch (Throwable t) {

            return;
        }
    }
}