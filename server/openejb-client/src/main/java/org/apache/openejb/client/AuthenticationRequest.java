package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class AuthenticationRequest implements Request {

    private transient Object principle;
    private transient Object credentials;

    public AuthenticationRequest() {
    }

    public AuthenticationRequest(Object principle, Object credentials) {
        this.principle = principle;
        this.credentials = credentials;
    }

    public byte getRequestType() {
        return AUTH_REQUEST;
    }

    public Object getPrinciple() {
        return principle;
    }

    public Object getCredentials() {
        return credentials;
    }

    public void setPrinciple(Object principle) {
        this.principle = principle;
    }

    public void setCredentials(Object credentials) {
        this.credentials = credentials;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        principle = in.readObject();
        credentials = in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(principle);
        out.writeObject(credentials);
    }
}

