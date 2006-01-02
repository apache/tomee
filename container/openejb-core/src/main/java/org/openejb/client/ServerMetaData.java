package org.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServerMetaData implements Externalizable {

    private transient int port;

    private transient InetAddress address;

    public ServerMetaData() {

    }

    public ServerMetaData(String host, int port) throws UnknownHostException {
        this.setAddress(InetAddress.getByName(host));
        this.setPort(port);
    }

    public int getPort() {
        return port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        StringBuffer IP = new StringBuffer(15);

        IP.append(in.readByte()).append('.');
        IP.append(in.readByte()).append('.');
        IP.append(in.readByte()).append('.');
        IP.append(in.readByte());

//        System.out.println(IP.toString());        
        try {
            setAddress(InetAddress.getByName(IP.toString()));
        } catch (java.net.UnknownHostException e) {
            throw new IOException("Cannot read in the host address " + IP + ": The host is unknown");
        }

        setPort(in.readInt());

    }

    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] addr = getAddress().getAddress();

        out.writeByte(addr[0]);
        out.writeByte(addr[1]);
        out.writeByte(addr[2]);
        out.writeByte(addr[3]);

        out.writeInt(getPort());
    }

}

