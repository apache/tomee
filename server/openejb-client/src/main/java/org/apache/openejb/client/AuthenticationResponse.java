package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class AuthenticationResponse implements Response {

    private transient int responseCode = -1;
    private transient ClientMetaData identity;
    private transient ServerMetaData server;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(int code) {
        responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public ClientMetaData getIdentity() {
        return identity;
    }

    public ServerMetaData getServer() {
        return server;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setIdentity(ClientMetaData identity) {
        this.identity = identity;
    }

    public void setServer(ServerMetaData server) {
        this.server = server;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        responseCode = in.readByte();
        switch (responseCode) {
            case AUTH_GRANTED:
                identity = new ClientMetaData();
                identity.readExternal(in);
                break;
            case AUTH_REDIRECT:
                identity = new ClientMetaData();
                identity.readExternal(in);
                server = new ServerMetaData();
                server.readExternal(in);
                break;
            case AUTH_DENIED:
                break;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeByte((byte) responseCode);
        switch (responseCode) {
            case AUTH_GRANTED:
                identity.writeExternal(out);
                break;
            case AUTH_REDIRECT:
                identity.writeExternal(out);
                server.writeExternal(out);
                break;
            case AUTH_DENIED:
                break;
        }
    }

}

