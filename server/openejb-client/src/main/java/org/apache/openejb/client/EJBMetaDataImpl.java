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
import java.util.List;
import java.util.ArrayList;

import javax.ejb.EJBHome;

public class EJBMetaDataImpl implements javax.ejb.EJBMetaData, java.io.Externalizable {

    public static final byte STATEFUL = (byte) 6;

    public static final byte STATELESS = (byte) 7;

    public static final byte BMP_ENTITY = (byte) 8;

    public static final byte CMP_ENTITY = (byte) 9;

    protected transient byte type;

    protected transient String deploymentID;
    protected transient int deploymentCode;

    protected transient Class homeClass;

    protected transient Class remoteClass;

    protected final transient List<Class> businessClasses = new ArrayList<Class>();

    protected transient Class keyClass;

    protected transient EJBHomeProxy ejbHomeProxy;

    public EJBMetaDataImpl() {

    }

    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, String typeOfBean, List<Class> businessInterfaces) {
        if ("STATEFUL".equalsIgnoreCase(typeOfBean)){
            this.type = STATEFUL;
        } else if ("STATELESS".equalsIgnoreCase(typeOfBean)){
            this.type = STATELESS;
        } else if ("BMP_ENTITY".equalsIgnoreCase(typeOfBean)){
            this.type = BMP_ENTITY;
        } else if ("CMP_ENTITY".equalsIgnoreCase(typeOfBean)){
            this.type = CMP_ENTITY;
        }
        this.homeClass = homeInterface;
        this.remoteClass = remoteInterface;
        if (businessInterfaces != null){
            this.businessClasses.addAll(businessInterfaces);
        }
    }

    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, String typeOfBean, List<Class> businessInterfaces) {
        this(homeInterface, remoteInterface, typeOfBean, businessInterfaces);
        if (type == CMP_ENTITY || type == BMP_ENTITY) {
            this.keyClass = primaryKeyClass;
        }
    }

    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, String typeOfBean, String deploymentID, List<Class> businessInterfaces) {
        this(homeInterface, remoteInterface, primaryKeyClass, typeOfBean, businessInterfaces);
        this.deploymentID = deploymentID;
    }

    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, String typeOfBean, String deploymentID, int deploymentCode, List<Class> businessInterfaces) {
        this(homeInterface, remoteInterface, primaryKeyClass, typeOfBean, deploymentID, businessInterfaces);
        this.deploymentCode = deploymentCode;
    }

    public Class getPrimaryKeyClass() {
        if (type != BMP_ENTITY && type != CMP_ENTITY) {

            throw new java.lang.UnsupportedOperationException();
        }
        return keyClass;
    }

    public EJBHome getEJBHome() {
        return ejbHomeProxy;
    }

    public Class getHomeInterfaceClass() {
        return homeClass;
    }

    public boolean isStatelessSession() {
        return type == STATELESS;
    }

    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }

    public boolean isSession() {
        return (type == STATEFUL || type == STATELESS);
    }

    protected void setEJBHomeProxy(EJBHomeProxy home) {
        ejbHomeProxy = home;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(homeClass);
        out.writeObject(remoteClass);
        out.writeObject(keyClass);
        out.writeObject(ejbHomeProxy);
        out.writeByte(type);
        out.writeUTF(deploymentID);
        out.writeShort((short) deploymentCode);
        out.writeShort((short) businessClasses.size());
        for (Class clazz : businessClasses) {
            out.writeObject(clazz);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        homeClass = (Class) in.readObject();
        remoteClass = (Class) in.readObject();
        keyClass = (Class) in.readObject();
        ejbHomeProxy = (EJBHomeProxy) in.readObject();
        type = in.readByte();
        deploymentID = in.readUTF();
        deploymentCode = in.readShort();

        for (int i = in.readShort(); i > 0; i--) {
            businessClasses.add((Class) in.readObject());
        }
    }

}