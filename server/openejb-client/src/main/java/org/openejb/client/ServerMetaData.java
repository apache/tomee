package org.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.net.URISyntaxException;

public class ServerMetaData implements Externalizable {

    private transient URI location;

    public ServerMetaData() {
    }

    public ServerMetaData(String host, int port) throws URISyntaxException {
        this.location = new URI("foo://"+host+":"+port);
    }

    public ServerMetaData(URI location)  {
        this.location = location;
    }

    public int getPort() {
        return location.getPort();
    }

    public String getHost() {
        return location.getHost();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        String uri = (String) in.readObject();
        try {
            location = new URI(uri);
        } catch (URISyntaxException e) {
            throw (IOException)new IOException("cannot create uri from '"+uri+"'").initCause(e);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(location.toString());
    }

}

