/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.axis.client;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import org.apache.axis.AxisEngine;
import org.apache.axis.Constants;
import org.apache.axis.client.Service;
import org.apache.axis.constants.Use;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.encoding.ser.SimpleDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleSerializerFactory;
import org.apache.axis.handlers.HandlerInfoChainFactory;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.handler.HandlerChain;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.util.Iterator;
import java.util.List;

public class SeiFactoryImpl implements SeiFactory {
    private final QName serviceName;
    private final QName portQName;
    private final String serviceEndpointClassName;
    private final OperationInfo[] operationInfos;
    private FastConstructor constructor;
    private Object serviceImpl;
    private final List typeInfo;
    private final URL location;
    private final List handlerInfos;
    private final String credentialsName;
    private HandlerInfoChainFactory handlerInfoChainFactory;
    private OperationInfo[] sortedOperationInfos;
    private Class serviceEndpointClass;

    public SeiFactoryImpl(QName serviceName,
            String portName,
            String serviceEndpointClassName,
            OperationInfo[] operationInfos,
            List typeInfo,
            URL location,
            List handlerInfos,
            String credentialsName) {
        this.serviceName = serviceName;
        this.portQName = new QName("", portName);
        this.serviceEndpointClassName = serviceEndpointClassName;
        this.operationInfos = operationInfos;
        this.typeInfo = typeInfo;
        this.location = location;
        this.handlerInfos = handlerInfos;
        this.credentialsName = credentialsName;
    }

    void initialize(Object serviceImpl, ClassLoader classLoader) throws ClassNotFoundException {
        this.serviceImpl = serviceImpl;
        Class serviceEndpointBaseClass = classLoader.loadClass(serviceEndpointClassName);
        serviceEndpointClass = enhanceServiceEndpointInterface(serviceEndpointBaseClass, classLoader);
        Class[] constructorTypes = new Class[]{classLoader.loadClass(GenericServiceEndpoint.class.getName())};
        this.constructor = FastClass.create(serviceEndpointClass).getConstructor(constructorTypes);
        this.handlerInfoChainFactory = new HandlerInfoChainFactory(handlerInfos);
        sortedOperationInfos = new OperationInfo[FastClass.create(serviceEndpointClass).getMaxIndex() + 1];
        String encodingStyle = "";
        for (int i = 0; i < operationInfos.length; i++) {
            OperationInfo operationInfo = operationInfos[i];
            Signature signature = operationInfo.getSignature();
            MethodProxy methodProxy = MethodProxy.find(serviceEndpointClass, signature);
            if (methodProxy == null) {
                throw new RuntimeException("No method proxy for operationInfo " + signature);
            }
            int index = methodProxy.getSuperIndex();
            sortedOperationInfos[index] = operationInfo;
            if (operationInfo.getOperationDesc().getUse() == Use.ENCODED) {
                encodingStyle = org.apache.axis.Constants.URI_SOAP11_ENC;
            }
        }
        //register our type descriptors
        Service service = ((ServiceImpl) serviceImpl).getService();
        AxisEngine axisEngine = service.getEngine();
        TypeMappingRegistry typeMappingRegistry = axisEngine.getTypeMappingRegistry();
        TypeMapping typeMapping = typeMappingRegistry.getOrMakeTypeMapping(encodingStyle);
        typeMapping.register(BigInteger.class, Constants.XSD_UNSIGNEDLONG, new SimpleSerializerFactory(BigInteger.class, Constants.XSD_UNSIGNEDLONG), new SimpleDeserializerFactory(BigInteger.class, Constants.XSD_UNSIGNEDLONG));
        typeMapping.register(URI.class, Constants.XSD_ANYURI, new SimpleSerializerFactory(URI.class, Constants.XSD_ANYURI), new SimpleDeserializerFactory(URI.class, Constants.XSD_ANYURI));
        //It is essential that the types be registered before the typeInfos create the serializer/deserializers.
        for (Iterator iter = typeInfo.iterator(); iter.hasNext();) {
            TypeInfo info = (TypeInfo) iter.next();
            TypeDesc.registerTypeDescForClass(info.getClazz(), info.buildTypeDesc());
        }
        TypeInfo.register(typeInfo, typeMapping);
    }

    private Class enhanceServiceEndpointInterface(Class serviceEndpointInterface, ClassLoader classLoader) {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(classLoader);
        enhancer.setSuperclass(GenericServiceEndpointWrapper.class);
        enhancer.setInterfaces(new Class[]{serviceEndpointInterface});
        enhancer.setCallbackFilter(new NoOverrideCallbackFilter(GenericServiceEndpointWrapper.class));
        enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class});
        enhancer.setUseFactory(false);
        enhancer.setUseCache(false);

        return enhancer.createClass();
    }

    public Remote createServiceEndpoint() throws ServiceException {
        //TODO figure out why this can't be called in readResolve!
//        synchronized (this) {
//            if (!initialized) {
//                initialize();
//                initialized = true;
//            }
//        }
        Service service = ((ServiceImpl) serviceImpl).getService();
        GenericServiceEndpoint serviceEndpoint = new GenericServiceEndpoint(portQName, service, location);
        Callback callback = new ServiceEndpointMethodInterceptor(serviceEndpoint, sortedOperationInfos, credentialsName);
        Callback[] callbacks = new Callback[]{NoOp.INSTANCE, callback};
        Enhancer.registerCallbacks(serviceEndpointClass, callbacks);
        try {
            return (Remote) constructor.newInstance(new Object[]{serviceEndpoint});
        } catch (InvocationTargetException e) {
            throw (ServiceException) new ServiceException("Could not construct service instance", e.getTargetException()).initCause(e);
        }
    }

    public HandlerChain createHandlerChain() {
        return handlerInfoChainFactory.createHandlerChain();
    }

//    private Object readResolve() throws ObjectStreamException {
//            SEIFactoryImpl seiFactory =  new SEIFactoryImpl(serviceName, portQName.getLocalPart(), serviceEndpointClassName, operationInfos, typeInfo, location, handlerInfos, credentialsName);
//            seiFactory.initialize();
//            return seiFactory;
//    }

    public OperationInfo[] getOperationInfos() {
        return operationInfos;
    }

    public QName getPortQName() {
        return portQName;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public URL getWSDLDocumentLocation() {
        try {
            return new URL(location.toExternalForm() + "?wsdl");
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
