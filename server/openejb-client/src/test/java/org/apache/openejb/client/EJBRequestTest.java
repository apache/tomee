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

import junit.framework.TestCase;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;

public class EJBRequestTest extends TestCase {

    private EJBMetaDataImpl ejb;

    static interface FooHome extends EJBHome {

        FooObject create();

        FooObject findByPrimaryKey(Integer key);
    }

    static interface FooObject extends EJBObject {

        String businessMethod(String param);
    }

    @Override
    protected void setUp() throws Exception {
        ejb = new EJBMetaDataImpl(FooHome.class, FooObject.class, Integer.class, "BMP_ENTITY", "FooBeanID", InterfaceType.BUSINESS_REMOTE, null, null);
    }

    public void testEJBHomeCreate() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_HOME_CREATE;
        final Method method = FooHome.class.getMethod("create", new Class[0]);
        final Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeFind() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_HOME_FIND;
        final Method method = FooHome.class.getMethod("findByPrimaryKey", new Class[]{Integer.class});
        final Object[] args = new Object[]{4};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeRemove1() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_HOME_REMOVE_BY_HANDLE;
        final Method method = FooHome.class.getMethod("remove", new Class[]{Handle.class});
        final Object[] args = new Object[]{null};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeRemove2() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_HOME_REMOVE_BY_PKEY;
        final Method method = FooHome.class.getMethod("remove", new Class[]{Object.class});
        final Object[] args = new Object[]{4};

        invoke(requestMethod, method, args);
    }

    public void testGetMetaData() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_HOME_GET_EJB_META_DATA;
        final Method method = FooHome.class.getMethod("getEJBMetaData", new Class[]{});
        final Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetHomeHandle() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_HOME_GET_HOME_HANDLE;
        final Method method = FooHome.class.getMethod("getHomeHandle", new Class[]{});
        final Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testBusinessMethod() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_OBJECT_BUSINESS_METHOD;
        final Method method = FooObject.class.getMethod("businessMethod", new Class[]{String.class});
        final Object[] args = new Object[]{"hola mundo"};

        invoke(requestMethod, method, args);
    }

    public void testGetEJBHome() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_OBJECT_GET_EJB_HOME;
        final Method method = FooObject.class.getMethod("getEJBHome", new Class[]{});
        final Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetHandle() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_OBJECT_GET_HANDLE;
        final Method method = FooObject.class.getMethod("getHandle", new Class[]{});
        final Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetPrimaryKey() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_OBJECT_GET_PRIMARY_KEY;
        final Method method = FooObject.class.getMethod("getPrimaryKey", new Class[]{});
        final Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testIsIdentical() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_OBJECT_IS_IDENTICAL;
        final Method method = FooObject.class.getMethod("isIdentical", new Class[]{EJBObject.class});
        final Object[] args = new Object[]{null};

        invoke(requestMethod, method, args);
    }

    public void testEJBObjectRemove() throws Exception {
        final RequestMethodCode requestMethod = RequestMethodCode.EJB_OBJECT_REMOVE;
        final Method method = FooObject.class.getMethod("remove", new Class[]{});
        final Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    private void invoke(final RequestMethodCode requestMethod, final Method method, final Object[] args) throws IOException, ClassNotFoundException {

        final EJBRequest expected = new EJBRequest(requestMethod, ejb, method, args, null, null);
        expected.getBody().setAuthentication(new JNDIContext.AuthenticationInfo("realm", "user", new char[]{'p', 'w'}));

        final EJBRequest actual = new EJBRequest();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream out = new ObjectOutputStream(baos);

        expected.writeExternal(out);
        out.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream in = new ObjectInputStream(bais);

        actual.readExternal(in);
        actual.getBody().readExternal(in);
        final JNDIContext.AuthenticationInfo authentication = actual.getBody().getAuthentication();

        assertNotNull(authentication);
        assertEquals("AuthenticationInfo.Realm", "realm", authentication.getRealm());
        assertEquals("AuthenticationInfo.User", "user", authentication.getUser());

        assertEquals("RequestType", expected.getRequestType(), actual.getRequestType());
        assertEquals("RequestMethod", expected.getRequestMethod(), actual.getRequestMethod());

        assertEquals("DeploymentId", expected.getDeploymentId(), actual.getDeploymentId());
        assertEquals("DeploymentCode", expected.getDeploymentCode(), actual.getDeploymentCode());

        assertEquals("PrimaryKey", expected.getPrimaryKey(), actual.getPrimaryKey());

        assertEquals("ClientIdentity", expected.getClientIdentity(), actual.getClientIdentity());

        assertEquals("InterfaceClass", expected.getInterfaceClass(), actual.getInterfaceClass());

        assertEquals("MethodInstance", expected.getMethodInstance(), actual.getMethodInstance());

        final Object[] expectedParams = expected.getMethodParameters();
        final Object[] actualParams = actual.getMethodParameters();

        assertNotNull("MethodParameters", actualParams);
        assertEquals("MethodParameters.length", expectedParams.length, actualParams.length);
        for (int i = 0; i < expectedParams.length; i++) {
            assertEquals("MethodParameters." + i, expectedParams[i], actualParams[i]);
        }
    }

}