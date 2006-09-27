package org.apache.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ClientMetaData implements Externalizable {

    transient Object clientIdentity;

    public ClientMetaData() {
    }

    public ClientMetaData(Object identity) {
        this.clientIdentity = identity;
    }

    public Object getClientIdentity() {
        return clientIdentity;
    }

    public void setClientIdentity(Object clientIdentity) {
        this.clientIdentity = clientIdentity;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.clientIdentity = in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(clientIdentity);
    }
}
