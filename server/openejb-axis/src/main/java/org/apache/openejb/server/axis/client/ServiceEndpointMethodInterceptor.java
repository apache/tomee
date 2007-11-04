/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
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

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.axis.client.Call;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.utils.JavaUtils;
import org.apache.openejb.server.webservices.saaj.SaajUniverse;

import javax.wsdl.OperationType;
import javax.xml.rpc.holders.Holder;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ServiceEndpointMethodInterceptor implements MethodInterceptor {
    private final GenericServiceEndpoint stub;
    private final OperationInfo[] operations;
    private final String credentialsName;

    public ServiceEndpointMethodInterceptor(GenericServiceEndpoint stub, OperationInfo[] operations, String credentialsName) {
        this.stub = stub;
        this.operations = operations;
        this.credentialsName = credentialsName;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        SaajUniverse universe = new SaajUniverse();
        universe.set(SaajUniverse.AXIS1);
        try {
            return doIntercept(method, objects, methodProxy);
        } finally {
            universe.unset();
        }
    }

    private Object doIntercept(Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        int index = methodProxy.getSuperIndex();
        OperationInfo operationInfo = operations[index];
        if (operationInfo == null) {
            throw new RuntimeException("Operation not mapped: " + method.getName() + " index: " + index + "\n OperationInfos: " + Arrays.asList(operations));
        }
        stub.checkCachedEndpoint();

        Call call = stub.createCall();

        operationInfo.prepareCall(call);

        stub.setUpCall(call);
        if (credentialsName != null) {
            throw new UnsupportedOperationException("Client side auth is not implementd");
//            Subject subject = ContextManager.getNextCaller();
//            if (subject == null) {
//                throw new IllegalStateException("Subject missing but authentication turned on");
//            } else {
//                Set creds = subject.getPrivateCredentials(NamedUsernamePasswordCredential.class);
//                boolean found = false;
//                for (Iterator iterator = creds.iterator(); iterator.hasNext();) {
//                    NamedUsernamePasswordCredential namedUsernamePasswordCredential = (NamedUsernamePasswordCredential) iterator.next();
//                    if (credentialsName.equals(namedUsernamePasswordCredential.getName())) {
//                        call.setUsername(namedUsernamePasswordCredential.getUsername());
//                        call.setPassword(new String(namedUsernamePasswordCredential.getPassword()));
//                        found = true;
//                        break;
//                    }
//                }
//                if (!found) {
//                    throw new IllegalStateException("no NamedUsernamePasswordCredential found for name " + credentialsName);
//                }
//            }
        }
        Object response = null;
        List parameterDescs = operationInfo.getOperationDesc().getParameters();
        Object[] unwrapped = extractFromHolders(objects, parameterDescs, operationInfo.getOperationDesc().getNumInParams());
        if (operationInfo.getOperationDesc().getMep() == OperationType.REQUEST_RESPONSE) {
            try {
                response = call.invoke(unwrapped);
            } catch (RemoteException e) {
                throw operationInfo.unwrapFault(e);
            }

            if (response instanceof RemoteException) {
                throw operationInfo.unwrapFault((RemoteException) response);
            } else {
                stub.extractAttachments(call);
                Map outputParameters = call.getOutputParams();
                putInHolders(outputParameters, objects, parameterDescs);
                Class returnType = operationInfo.getOperationDesc().getReturnClass();
                //return type should never be null... but we are not objecting if wsdl-return-value-mapping is not set.
                if (response == null || returnType == null || returnType.isAssignableFrom(response.getClass())) {
                    return response;
                } else {
                    return JavaUtils.convert(response, returnType);
                }
            }
        } else if (operationInfo.getOperationDesc().getMep() == OperationType.ONE_WAY) {
            //one way
            call.invokeOneWay(unwrapped);
            return null;
        } else {
            throw new RuntimeException("Invalid messaging style: " + operationInfo.getOperationDesc().getMep());
        }
    }

    private Object[] extractFromHolders(Object[] objects, List parameterDescs, int inParameterCount) throws JavaUtils.HolderException {
        if (objects.length != parameterDescs.size()) {
            throw new IllegalArgumentException("Mismatch parameter count: expected: " + parameterDescs.size() + ", actual: " + objects.length);
        }
        Object[] unwrapped = new Object[inParameterCount];
        int j = 0;
        for (int i = 0; objects != null && i < objects.length; i++) {
            Object parameter = objects[i];
            ParameterDesc parameterDesc = (ParameterDesc) parameterDescs.get(i);

            if (parameterDesc.getMode() == ParameterDesc.INOUT) {
                unwrapped[j++] = JavaUtils.getHolderValue(parameter);
            } else if (parameterDesc.getMode() == ParameterDesc.IN) {
                unwrapped[j++] = parameter;
            }
        }
        return unwrapped;
    }

    private void putInHolders(Map outputParameters, Object[] objects, List parameterDescs) throws JavaUtils.HolderException {
        for (int i = 0; i < objects.length; i++) {
            Object parameter = objects[i];
            ParameterDesc parameterDesc = (ParameterDesc) parameterDescs.get(i);
            if ((parameterDesc.getMode() == ParameterDesc.INOUT) || (parameterDesc.getMode() == ParameterDesc.OUT)) {
                Object returned = outputParameters.get(parameterDesc.getQName());
                if (returned instanceof Holder) {
                    //TODO this must be a bug somewhere!!!!
                    returned = JavaUtils.getHolderValue(returned);
                }
                JavaUtils.setHolderValue(parameter, returned);
            }
        }
    }
}
