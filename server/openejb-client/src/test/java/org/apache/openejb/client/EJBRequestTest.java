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

import java.io.*;
import java.lang.reflect.Method;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import junit.framework.TestCase;
import org.omg.CORBA.UserException;

public class EJBRequestTest extends TestCase {
    private EJBMetaDataImpl ejb;

    static interface FooHome extends EJBHome {
        FooObject create();
        FooObject findByPrimaryKey(Integer key);
    }
    static interface FooObject extends EJBObject{
        String businessMethod(String param) throws UserException;
    }


    protected void setUp() throws Exception {
        ejb = new EJBMetaDataImpl(FooHome.class, FooObject.class, Integer.class, "BMP_ENTITY", "FooBeanID", null);
    }

    public void testEJBHomeCreate() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_HOME_CREATE;
        Method method = FooHome.class.getMethod("create", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeFind() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_HOME_FIND;
        Method method = FooHome.class.getMethod("findByPrimaryKey", new Class[]{Integer.class});
        Object[] args = new Object[]{new Integer(4)};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeRemove1() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_HOME_REMOVE_BY_HANDLE;
        Method method = FooHome.class.getMethod("remove", new Class[]{Handle.class});
        Object[] args = new Object[]{null};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeRemove2() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_HOME_REMOVE_BY_PKEY;
        Method method = FooHome.class.getMethod("remove", new Class[]{Object.class});
        Object[] args = new Object[]{new Integer(4)};

        invoke(requestMethod, method, args);
    }

    public void testGetMetaData() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_HOME_GET_EJB_META_DATA;
        Method method = FooHome.class.getMethod("getEJBMetaData", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetHomeHandle() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_HOME_GET_HOME_HANDLE;
        Method method = FooHome.class.getMethod("getHomeHandle", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testBusinessMethod() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_OBJECT_BUSINESS_METHOD;
        Method method = FooObject.class.getMethod("businessMethod", new Class[]{String.class});
        Object[] args = new Object[]{"hola mundo"};

        invoke(requestMethod, method, args);
    }

    public void testGetEJBHome() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_OBJECT_GET_EJB_HOME;
        Method method = FooObject.class.getMethod("getEJBHome", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetHandle() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_OBJECT_GET_HANDLE;
        Method method = FooObject.class.getMethod("getHandle", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetPrimaryKey() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_OBJECT_GET_PRIMARY_KEY;
        Method method = FooObject.class.getMethod("getPrimaryKey", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testIsIdentical() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_OBJECT_IS_IDENTICAL;
        Method method = FooObject.class.getMethod("isIdentical", new Class[]{EJBObject.class});
        Object[] args = new Object[]{null};

        invoke(requestMethod, method, args);
    }

    public void testEJBObjectRemove() throws Exception {
        int requestMethod = RequestMethodConstants.EJB_OBJECT_REMOVE;
        Method method = FooObject.class.getMethod("remove", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    private void invoke(int requestMethod, Method method, Object[] args) throws IOException, ClassNotFoundException {
        EJBRequest expected = new EJBRequest(requestMethod, ejb, method, args, null);

        EJBRequest actual = new EJBRequest();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        expected.writeExternal(out);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);

        actual.readExternal(in);
        actual.getBody().readExternal(in);


        assertEquals("RequestType", expected.getRequestType(), actual.getRequestType());
        assertEquals("RequestMethod", expected.getRequestMethod(), actual.getRequestMethod());

        assertEquals("DeploymentId", expected.getDeploymentId(), actual.getDeploymentId());
        assertEquals("DeploymentCode", expected.getDeploymentCode(), actual.getDeploymentCode());

        assertEquals("PrimaryKey", expected.getPrimaryKey(), actual.getPrimaryKey());

        assertEquals("ClientIdentity", expected.getClientIdentity(), actual.getClientIdentity());

        assertEquals("InterfaceClass", expected.getInterfaceClass(), actual.getInterfaceClass());

        assertEquals("MethodInstance", expected.getMethodInstance(), actual.getMethodInstance());

        Object[] expectedParams = expected.getMethodParameters();
        Object[] actualParams = actual.getMethodParameters();

        assertNotNull("MethodParameters",actualParams);
        assertEquals("MethodParameters.length", expectedParams.length, actualParams.length);
        for (int i = 0; i < expectedParams.length; i++) {
            assertEquals("MethodParameters."+i, expectedParams[i], actualParams[i]);
        }
    }






}