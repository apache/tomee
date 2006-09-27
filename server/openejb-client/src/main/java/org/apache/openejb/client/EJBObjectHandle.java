package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public class EJBObjectHandle implements java.io.Externalizable, javax.ejb.Handle {

    protected transient EJBObjectProxy ejbObjectProxy;
    protected transient EJBObjectHandler handler;

    public EJBObjectHandle() {
    }

    public EJBObjectHandle(EJBObjectProxy proxy) {
        this.ejbObjectProxy = proxy;
        this.handler = ejbObjectProxy.getEJBObjectHandler();
    }

    protected void setEJBObjectProxy(EJBObjectProxy ejbObjectProxy) {
        this.ejbObjectProxy = ejbObjectProxy;
        this.handler = ejbObjectProxy.getEJBObjectHandler();
    }

    public EJBObject getEJBObject() throws RemoteException {
        return ejbObjectProxy;
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
        ejbObjectProxy = handler.createEJBObjectProxy();
    }

}