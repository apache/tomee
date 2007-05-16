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
        return (EJBObject) ejbObjectProxy;
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        handler.client.writeExternal(out);

        EJBMetaDataImpl ejb = handler.ejb;
        out.writeObject(getClassName(ejb.homeClass));
        out.writeObject(getClassName(ejb.remoteClass));
        out.writeObject(getClassName(ejb.keyClass));
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
        Object primaryKey = in.readObject();

        handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primaryKey);
        ejbObjectProxy = handler.createEJBObjectProxy();
    }

    private static String getClassName(Class clazz) {
        return (clazz == null) ? null: clazz.getName();
    }

    private static Class loadClass(ClassLoader classLoader, String homeClassName) throws ClassNotFoundException {
        return (homeClassName == null) ? null : Class.forName(homeClassName, true, classLoader);
    }
}