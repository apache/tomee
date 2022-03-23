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

import jakarta.ejb.EJBHome;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class EJBMetaDataImpl implements jakarta.ejb.EJBMetaData, java.io.Externalizable {

    private static final long serialVersionUID = -858340852654709679L;

    public static final byte STATEFUL = (byte) 6;

    public static final byte STATELESS = (byte) 7;

    public static final byte BMP_ENTITY = (byte) 8;

    public static final byte CMP_ENTITY = (byte) 9;

    public static final byte SINGLETON = (byte) 10;

    protected transient byte type;

    protected transient String deploymentID;
    protected transient int deploymentCode;

    protected transient Class homeClass;

    protected transient Class remoteClass;

    protected final transient List<Class> businessClasses = new ArrayList<Class>();

    protected transient Class mainInterface;

    protected final transient Set<String> asynchronousMethods = new HashSet<String>();

    protected final transient Properties properties = new Properties();

    protected transient Class keyClass;

    protected transient EJBHome ejbHomeProxy;

    protected transient InterfaceType interfaceType;

    // only used for business objects;
    protected transient Object primaryKey;

    private transient ProtocolMetaData metaData;

    public EJBMetaDataImpl() {

    }

    public EJBMetaDataImpl(final Class homeInterface,
                           final Class remoteInterface,
                           final String typeOfBean,
                           final InterfaceType interfaceType,
                           final List<Class> businessInterfaces,
                           final Set<String> asynchronousMethodSignatures) {
        this.interfaceType = interfaceType;

        if ("STATEFUL".equalsIgnoreCase(typeOfBean)) {
            this.type = STATEFUL;
        } else if ("STATELESS".equalsIgnoreCase(typeOfBean)) {
            this.type = STATELESS;
        } else if ("SINGLETON".equalsIgnoreCase(typeOfBean)) {
            this.type = SINGLETON;
        } else if ("BMP_ENTITY".equalsIgnoreCase(typeOfBean)) {
            this.type = BMP_ENTITY;
        } else if ("CMP_ENTITY".equalsIgnoreCase(typeOfBean)) {
            this.type = CMP_ENTITY;
        }
        this.homeClass = homeInterface;
        this.remoteClass = remoteInterface;
        if (businessInterfaces != null) {
            this.businessClasses.addAll(businessInterfaces);
        }
        if (asynchronousMethodSignatures != null) {
            this.asynchronousMethods.addAll(asynchronousMethodSignatures);
        }
    }

    public EJBMetaDataImpl(final Class homeInterface,
                           final Class remoteInterface,
                           final Class primaryKeyClass,
                           final String typeOfBean,
                           final InterfaceType interfaceType,
                           final List<Class> businessInterfaces,
                           final Set<String> asynchronousMethodSignatures) {
        this(homeInterface, remoteInterface, typeOfBean, interfaceType, businessInterfaces, asynchronousMethodSignatures);
        if (type == CMP_ENTITY || type == BMP_ENTITY) {
            this.keyClass = primaryKeyClass;
        }
    }

    public EJBMetaDataImpl(final Class homeInterface,
                           final Class remoteInterface,
                           final Class primaryKeyClass,
                           final String typeOfBean,
                           final String deploymentID,
                           final InterfaceType interfaceType,
                           final List<Class> businessInterfaces,
                           final Set<String> asynchronousMethodSignatures) {
        this(homeInterface, remoteInterface, primaryKeyClass, typeOfBean, interfaceType, businessInterfaces, asynchronousMethodSignatures);
        this.deploymentID = deploymentID;
    }

    public EJBMetaDataImpl(final Class homeInterface,
                           final Class remoteInterface,
                           final Class primaryKeyClass,
                           final String typeOfBean,
                           final String deploymentID,
                           final int deploymentCode,
                           final InterfaceType interfaceType,
                           final List<Class> businessInterfaces,
                           final Set<String> asynchronousMethodSignatures) {
        this(homeInterface, remoteInterface, primaryKeyClass, typeOfBean, deploymentID, interfaceType, businessInterfaces, asynchronousMethodSignatures);
        this.deploymentCode = deploymentCode;
    }

    public void setMetaData(final ProtocolMetaData metaData) {
        this.metaData = metaData;
    }

    @Override
    public Class getPrimaryKeyClass() {
        if (type != BMP_ENTITY && type != CMP_ENTITY) {

            throw new java.lang.UnsupportedOperationException();
        }
        return keyClass;
    }

    @Override
    public EJBHome getEJBHome() {
        return ejbHomeProxy;
    }

    @Override
    public Class getHomeInterfaceClass() {
        return homeClass;
    }

    @Override
    public boolean isStatelessSession() {
        return type == STATELESS;
    }

    public boolean isStatefulSession() {
        return type == STATEFUL;
    }

    public boolean isSingletonSession() {
        return type == SINGLETON;
    }

    @Override
    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }

    @Override
    public boolean isSession() {
        return (type == STATEFUL || type == STATELESS || type == SINGLETON);
    }

    public boolean isAsynchronousMethod(final Method method) {
        if (asynchronousMethods.size() == 0) {
            return false;
        }
        return asynchronousMethods.contains(generateMethodSignature(method));
    }

    public void addAsynchronousMethod(final Method method) {
        asynchronousMethods.add(generateMethodSignature(method));
    }

    protected void setEJBHomeProxy(final EJBHomeProxy home) {
        ejbHomeProxy = home;
    }

    public String getDeploymentID() {
        return deploymentID;
    }

    public Class getHomeClass() {
        return homeClass;
    }

    public List<Class> getBusinessClasses() {
        return businessClasses;
    }

    public Class getMainInterface() {
        return mainInterface;
    }

    public Properties getProperties() {
        return properties;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(final Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // write out the version of the serialized data for future use
        out.writeByte(3);

        out.writeObject(homeClass);
        out.writeObject(remoteClass);
        out.writeObject(keyClass);
        out.writeObject(ejbHomeProxy);
        out.writeByte(type);
        out.writeUTF(deploymentID);
        out.writeShort((short) deploymentCode);
        out.writeShort((short) businessClasses.size());
        for (final Class clazz : businessClasses) {
            out.writeObject(clazz);
        }
        if (businessClasses.size() > 0) {
            out.writeObject(primaryKey);
        }
        out.writeObject(mainInterface);

        out.writeByte(interfaceType.ordinal());

        out.writeInt(asynchronousMethods.size());
        for (final String asynchronousMethod : asynchronousMethods) {
            out.writeObject(asynchronousMethod);
        }

        if (properties.size() == 0) {
            out.writeBoolean(false);
        } else {
            out.writeBoolean(true);
            final ByteArrayOutputStream tmp = new ByteArrayOutputStream();
            properties.store(tmp, "");
            tmp.close();
            final byte[] bytes = tmp.toByteArray();
            final int length = bytes.length;
            out.writeInt(length);
            out.write(bytes);
        }

    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        final byte version = in.readByte(); // future use

        homeClass = (Class) in.readObject();
        remoteClass = (Class) in.readObject();
        keyClass = (Class) in.readObject();
        ejbHomeProxy = (EJBHome) in.readObject();
        type = in.readByte();
        deploymentID = in.readUTF();
        deploymentCode = in.readShort();

        for (int i = in.readShort(); i > 0; i--) {
            businessClasses.add((Class) in.readObject());
        }
        if (businessClasses.size() > 0) {
            primaryKey = in.readObject();
        }
        if (version > 2) {
            mainInterface = (Class) in.readObject();
        }
        if (version > 1) {
            final byte typeIndex = in.readByte();
            interfaceType = InterfaceType.values()[typeIndex];
        }
        for (int i = in.readInt(); i > 0; i--) {
            asynchronousMethods.add((String) in.readObject());
        }

        final boolean hasProperties = in.readBoolean();
        if (hasProperties) {
            final int bufferLength = in.readInt();
            final byte[] buffer = new byte[bufferLength];
            in.read(buffer);
            final ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            properties.load(bais);
        }
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        switch (type) {
            case STATEFUL:
                sb.append("STATEFUL:");
                break;
            case STATELESS:
                sb.append("STATELESS:");
                break;
            case SINGLETON:
                sb.append("SINGLETON:");
                break;
            case CMP_ENTITY:
                sb.append("CMP_ENTITY:");
                break;
            case BMP_ENTITY:
                sb.append("BMP_ENTITY:");
                break;
        }
        sb.append(deploymentID).append(":");
        if (homeClass != null) {
            sb.append(homeClass.getName());
        } else if (businessClasses.size() != 0) {
            for (final Class clazz : businessClasses) {
                sb.append(clazz.getName()).append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
            if (type == STATEFUL) {
                sb.append(":").append(primaryKey);
            }
        }
        return sb.toString();
    }

    public void loadProperties(final Properties properties) {
        for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey() instanceof String) {
                final String key = (String) entry.getKey();
                if (key.startsWith("openejb.client.")) {
                    this.properties.put(key, entry.getValue());
                }
            }
        }
    }

    private String generateMethodSignature(final Method method) {
        final StringBuilder buffer = new StringBuilder(method.getName());
        for (final Class<?> parameterType : method.getParameterTypes()) {
            buffer.append(parameterType.getName());
        }
        return buffer.toString();
    }
}
