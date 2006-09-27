package org.apache.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;

public class EJBObjectProxyHandle implements Externalizable {

    EJBObjectHandler handler;

    public EJBObjectProxyHandle() {
    }

    public EJBObjectProxyHandle(EJBObjectHandler handler) {
        this.handler = handler;
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        handler.client.writeExternal(out);

        EJBMetaDataImpl ejb = handler.ejb;
        out.writeObject(ejb.homeClass);
        out.writeObject(ejb.remoteClass);
        out.writeObject(ejb.keyClass);
        out.writeByte(ejb.type);
        out.writeUTF(ejb.deploymentID);
        out.writeShort(ejb.deploymentCode);
        handler.server.writeExternal(out);
        out.writeObject(handler.primaryKey);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        ClientMetaData client = new ClientMetaData();
        EJBMetaDataImpl ejb = new EJBMetaDataImpl();
        ServerMetaData server = new ServerMetaData();

        client.readExternal(in);

        ejb.homeClass = (Class) in.readObject();
        ejb.remoteClass = (Class) in.readObject();
        ejb.keyClass = (Class) in.readObject();
        ejb.type = in.readByte();
        ejb.deploymentID = in.readUTF();
        ejb.deploymentCode = in.readShort();

        server.readExternal(in);
        Object primaryKey = in.readObject();

        handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey);

    }

    private Object readResolve() throws ObjectStreamException {
        return handler.createEJBObjectProxy();
    }

}
