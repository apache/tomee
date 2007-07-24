/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;

import javax.ejb.EJBHome;

public class EJBHomeHandle implements java.io.Externalizable, javax.ejb.HomeHandle {

    protected transient EJBHomeProxy ejbHomeProxy;
    protected transient EJBHomeHandler handler;

    public EJBHomeHandle() {
    }

    public EJBHomeHandle(EJBHomeProxy proxy) {
        this.ejbHomeProxy = proxy;
        this.handler = ejbHomeProxy.getEJBHomeHandler();
    }

    protected void setEJBHomeProxy(EJBHomeProxy ejbHomeProxy) {
        this.ejbHomeProxy = ejbHomeProxy;
        this.handler = ejbHomeProxy.getEJBHomeHandler();
    }

    public EJBHome getEJBHome() throws RemoteException {
        return ejbHomeProxy;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(1);


        handler.client.writeExternal(out);

        EJBMetaDataImpl ejb = handler.ejb;
        out.writeObject(getClassName(ejb.homeClass));
        out.writeObject(getClassName(ejb.remoteClass));
        out.writeObject(getClassName(ejb.keyClass));
        out.writeByte(ejb.type);
        out.writeUTF(ejb.deploymentID);
        out.writeShort(ejb.deploymentCode);
        handler.server.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        byte version = in.readByte(); // future use

        ClientMetaData client = new ClientMetaData();
        EJBMetaDataImpl ejb = new EJBMetaDataImpl();
        ServerMetaData server = new ServerMetaData();

        client.readExternal(in);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = this.getClass().getClassLoader();
        }

        ejb.homeClass = loadClass(classLoader, (String) in.readObject());
        ejb.remoteClass = loadClass(classLoader, (String) in.readObject());
        ejb.keyClass = loadClass(classLoader, (String) in.readObject());
        ejb.type = in.readByte();
        ejb.deploymentID = in.readUTF();
        ejb.deploymentCode = in.readShort();

        server.readExternal(in);

        handler = EJBHomeHandler.createEJBHomeHandler(ejb, server, client);
        ejbHomeProxy = handler.createEJBHomeProxy();
    }

    private static String getClassName(Class clazz) {
        return (clazz == null) ? null: clazz.getName();
    }

    private static Class loadClass(ClassLoader classLoader, String homeClassName) throws ClassNotFoundException {
        return (homeClassName == null) ? null : Class.forName(homeClassName, true, classLoader);
    }
}
