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
import java.lang.reflect.Method;
import java.rmi.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.ORB;


public class EJBRequest implements ClusterableRequest {

    private transient RequestMethodCode requestMethod;
    private transient int deploymentCode = 0;
    private transient Object clientIdentity;
    private transient String deploymentId;
    private transient int serverHash;
    private transient Body body;

    // Only visible on the client side
    private transient final EJBMetaDataImpl ejbMetaData;

    public static final int SESSION_BEAN_STATELESS = 6;
    public static final int SESSION_BEAN_STATEFUL = 7;
    public static final int ENTITY_BM_PERSISTENCE = 8;
    public static final int ENTITY_CM_PERSISTENCE = 9;

    public EJBRequest() {
        body = new Body(null);
        ejbMetaData = null;
    }

    public EJBRequest(final RequestMethodCode requestMethod, final EJBMetaDataImpl ejb, final Method method, final Object[] args, final Object primaryKey) {
        body = new Body(ejb);

        this.ejbMetaData = ejb;
        this.requestMethod = requestMethod;
        setDeploymentCode(ejb.deploymentCode);
        setDeploymentId(ejb.deploymentID);
        setMethodInstance(method);
        setMethodParameters(args);
        setPrimaryKey(primaryKey);
    }

    public EJBMetaDataImpl getEjbMetaData() {
        return ejbMetaData;
    }

    public Class getInterfaceClass() {
        return body.getInterfaceClass();
    }

    public Method getMethodInstance() {
        return body.getMethodInstance();
    }

    public String getMethodName() {
        return body.getMethodName();
    }

    public Object[] getMethodParameters() {
        return body.getMethodParameters();
    }

    public Class[] getMethodParamTypes() {
        return body.getMethodParamTypes();
    }

    public Object getPrimaryKey() {
        return body.getPrimaryKey();
    }

    public void setMethodInstance(final Method methodInstance) {
        body.setMethodInstance(methodInstance);
    }

    public void setMethodParameters(final Object[] methodParameters) {
        body.setMethodParameters(methodParameters);
    }

    public void setPrimaryKey(final Object primaryKey) {
        body.setPrimaryKey(primaryKey);
    }

    public Body getBody() {
        return body;
    }

    public void setBody(final Body body) {
        this.body = body;
    }

    public byte getVersion() {
        return this.body.getVersion();
    }

    public static class Body implements java.io.Externalizable {

        private transient EJBMetaDataImpl ejb;
        private transient ORB orb;
        private transient Method methodInstance;
        private transient Class interfaceClass;
        private transient String methodName;
        private transient Class[] methodParamTypes;
        private transient Object[] methodParameters;
        private transient Object primaryKey;

        private transient String requestId;
        private byte version = EJBResponse.VERSION;

        public Body(final EJBMetaDataImpl ejb) {
            this.ejb = ejb;
        }

        public Body() {
        }

        public byte getVersion() {
            return version;
        }

        public Method getMethodInstance() {
            return methodInstance;
        }

        public Object[] getMethodParameters() {
            return methodParameters;
        }

        public Object getPrimaryKey() {
            return primaryKey;
        }

        public Class getInterfaceClass() {
            return interfaceClass;
        }

        public String getMethodName() {
            return methodName;
        }

        public Class[] getMethodParamTypes() {
            return methodParamTypes;
        }

        public void setMethodInstance(final Method methodInstance) {
            if (methodInstance == null) {
                throw new NullPointerException("methodInstance input parameter is null");
            }
            this.methodInstance = methodInstance;
            this.methodName = methodInstance.getName();
            this.methodParamTypes = methodInstance.getParameterTypes();
            final Class methodClass = methodInstance.getDeclaringClass();

            if (ejb.homeClass != null) {
                if (methodClass.isAssignableFrom(ejb.homeClass)) {
                    this.interfaceClass = ejb.homeClass;
                    return;
                }
            }

            if (ejb.remoteClass != null) {
                if (methodClass.isAssignableFrom(ejb.remoteClass)) {
                    this.interfaceClass = ejb.remoteClass;
                    return;
                }
            }

            for (final Class businessClass : ejb.businessClasses) {
                if (methodClass.isAssignableFrom(businessClass)) {
                    this.interfaceClass = businessClass;
                    return;
                }
            }
        }

        public void setMethodParameters(final Object[] methodParameters) {
            this.methodParameters = methodParameters;
        }

        public void setPrimaryKey(final Object primaryKey) {
            this.primaryKey = primaryKey;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(final String requestId) {
            this.requestId = requestId;
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {

            this.version = in.readByte();

            requestId = null;
            ClassNotFoundException result = null;
            primaryKey = null;
//            methodClass = null;
            methodName = null;
            methodInstance = null;
            try {
                requestId = (String) in.readObject();
                primaryKey = in.readObject();
                interfaceClass = (Class) in.readObject();
//                methodClass = (Class) in.readObject();
            } catch (ClassNotFoundException cnfe) {
                result = cnfe;
            }
            methodName = in.readUTF();

            try {
                readMethodParameters(in);
            } catch (ClassNotFoundException cnfe) {
                if (result == null) result = cnfe;
            }

            if (interfaceClass != null) {
                try {
                    methodInstance = interfaceClass.getMethod(methodName, methodParamTypes);
                } catch (NoSuchMethodException nsme) {
                    //if (result == null) result = nsme;
                }
            }
            if (result != null)
                throw result;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {

            out.writeByte(this.version);

            out.writeObject(requestId);

            out.writeObject(primaryKey);

            out.writeObject(interfaceClass);
//            out.writeObject(methodClass);
            out.writeUTF(methodName);

            writeMethodParameters(out, methodParamTypes, methodParameters);
        }

        protected void writeMethodParameters(final ObjectOutput out, final Class[] types, final Object[] args) throws IOException {

            out.writeByte(types.length);

            for (int i = 0; i < types.length; i++) {
                final Class type = types[i];
                Object obj = args[i];

                if (type.isPrimitive()) {
                    if (type == Byte.TYPE) {
                        out.write(B);
                        final byte bytevalue = (Byte) obj;
                        out.writeByte(bytevalue);

                    } else if (type == Character.TYPE) {
                        out.write(C);
                        final char charvalue = (Character) obj;
                        out.writeChar(charvalue);

                    } else if (type == Integer.TYPE) {
                        out.write(I);
                        final int intvalue = (Integer) obj;
                        out.writeInt(intvalue);

                    } else if (type == Boolean.TYPE) {
                        out.write(Z);
                        final boolean booleanvalue = (Boolean) obj;
                        out.writeBoolean(booleanvalue);

                    } else if (type == Long.TYPE) {
                        out.write(J);
                        final long longvalue = (Long) obj;
                        out.writeLong(longvalue);

                    } else if (type == Float.TYPE) {
                        out.write(F);
                        final float fvalue = (Float) obj;
                        out.writeFloat(fvalue);

                    } else if (type == Double.TYPE) {
                        out.write(D);
                        final double dvalue = (Double) obj;
                        out.writeDouble(dvalue);

                    } else if (type == Short.TYPE) {
                        out.write(S);
                        final short shortvalue = (Short) obj;
                        out.writeShort(shortvalue);

                    } else {
                        throw new IOException("Unkown primitive type: " + type);
                    }
                } else {
                    if (obj instanceof PortableRemoteObject && obj instanceof Remote) {
                        final Tie tie = javax.rmi.CORBA.Util.getTie((Remote) obj);
                        if (tie == null) {
                            throw new IOException("Unable to serialize PortableRemoteObject; object has not been exported: " + obj);
                        }
                        final ORB orb = getORB();
                        tie.orb(orb);
                        obj = PortableRemoteObject.toStub((Remote) obj);
                    }
                    out.write(L);
                    out.writeObject(type);
                    out.writeObject(obj);
                }
            }
        }

        static final Class[] noArgsC = new Class[0];
        static final Object[] noArgsO = new Object[0];

        /**
         * Obtain an ORB instance for this request to activate remote
         * arguments and return results.
         *
         * @return An ORB instance.
         * @throws java.io.IOException On error
         */
        protected ORB getORB() throws IOException {
            // first ORB request?  Check our various sources 
            if (orb == null) {
                try {
                    final Context initialContext = new InitialContext();
                    orb = (ORB) initialContext.lookup("java:comp/ORB");
                } catch (Throwable e) {
                    try {
                        // any orb will do if we can't get a context one. 
                        orb = ORB.init();
                    } catch (Throwable ex) {
                        throw new IOException("Unable to connect PortableRemoteObject stub to an ORB, no ORB bound to java:comp/ORB");
                    }
                }
            }
            return orb;
        }

        protected void readMethodParameters(final ObjectInput in) throws IOException, ClassNotFoundException {
            final int length = in.read();

            if (length < 1) {
                methodParamTypes = noArgsC;
                methodParameters = noArgsO;
                return;
            }

            final Class[] types = new Class[length];
            final Object[] args = new Object[length];

            for (int i = 0; i < types.length; i++) {
                Class clazz = null;
                Object obj = null;

                final int type = in.read();

                switch (type) {
                    case B:
                        clazz = Byte.TYPE;
                        obj = in.readByte();
                        break;

                    case C:
                        clazz = Character.TYPE;
                        obj = in.readChar();
                        break;

                    case I:
                        clazz = Integer.TYPE;
                        obj = in.readInt();
                        break;

                    case Z:
                        clazz = Boolean.TYPE;
                        obj = in.readBoolean();
                        break;

                    case J:
                        clazz = Long.TYPE;
                        obj = in.readLong();
                        break;

                    case F:
                        clazz = Float.TYPE;
                        obj = in.readFloat();
                        break;

                    case D:
                        clazz = Double.TYPE;
                        obj = in.readDouble();
                        break;

                    case S:
                        clazz = Short.TYPE;
                        obj = in.readShort();
                        break;

                    case L:
                        clazz = (Class) in.readObject();
                        obj = in.readObject();
                        if (obj instanceof Stub) {
                            final Stub stub = (Stub) obj;
                            final ORB orb = getORB();
                            stub.connect(orb);
                        }
                        break;
                    default:
                        throw new IOException("Unkown data type: " + type);
                }

                types[i] = clazz;
                args[i] = obj;
            }

            methodParamTypes = types;
            methodParameters = args;
        }

        private static final int I = 0;
        private static final int B = 1;
        private static final int J = 2;
        private static final int F = 3;
        private static final int D = 4;
        private static final int S = 5;
        private static final int C = 6;
        private static final int Z = 7;
        private static final int L = 8;
        private static final int A = 9;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.EJB_REQUEST;
    }

    public RequestMethodCode getRequestMethod() {
        return requestMethod;
    }

    public Object getClientIdentity() {
        return clientIdentity;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public int getDeploymentCode() {
        return deploymentCode;
    }

    public void setRequestMethod(final RequestMethodCode requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setClientIdentity(final Object clientIdentity) {
        this.clientIdentity = clientIdentity;
    }

    public void setDeploymentId(final String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public void setDeploymentCode(final int deploymentCode) {
        this.deploymentCode = deploymentCode;
    }

    @Override
    public void setServerHash(final int serverHash) {
        this.serverHash = serverHash;
    }

    @Override
    public int getServerHash() {
        return serverHash;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EJBRequest{");
        sb.append("deploymentId='");
        sb.append(deploymentId);
        sb.append("'");

        if (requestMethod != null) {
            sb.append(", type=").append(requestMethod);
        }
        if (body != null) {
            sb.append(", method='").append(body.getMethodName());
            sb.append("', primaryKey='").append(body.getPrimaryKey());
            sb.append("'");
        }
        sb.append("}");
        return sb.toString();
    }

    /*
    When the Request externalizes itself, it will reset
    the appropriate values so that this instance can be used
    again.

    There will be one request instance for each handler
    */

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        ClassNotFoundException result = null;

        deploymentId = null;
        deploymentCode = -1;
        clientIdentity = null;

        final int code = in.readByte();
        try {
            requestMethod = RequestMethodCode.valueOf(code);
        } catch (IllegalArgumentException iae) {
            throw new IOException("Invalid request code " + code);
        }
        try {
            deploymentId = (String) in.readObject();
        } catch (ClassNotFoundException cnfe) {
            result = cnfe;
        }
        deploymentCode = in.readShort();
        try {
            clientIdentity = in.readObject();
        } catch (ClassNotFoundException cnfe) {
            if (result == null) result = cnfe;
        }
        serverHash = in.readInt();
        if (result != null)
            throw result;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeByte(requestMethod.getCode());

        if (deploymentCode > 0) {
            out.writeObject(null);
        } else {
            out.writeObject(deploymentId);
        }

        out.writeShort(deploymentCode);
        out.writeObject(clientIdentity);
        out.writeInt(serverHash);
        body.writeExternal(out);
    }

}

