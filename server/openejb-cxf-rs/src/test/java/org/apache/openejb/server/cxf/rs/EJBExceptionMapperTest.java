/*
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
package org.apache.openejb.server.cxf.rs;

import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Providers;
import org.apache.openejb.loader.SystemInstance;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EJBExceptionMapperTest {
    private static final String DETAIL = "jdbc:db://internal-host/schema";

    @After
    public void reset() {
        SystemInstance.reset();
    }

    @Test
    public void defaultResponseHasNoMessage() throws Exception {
        final Response response = newMapper(null).toResponse(new EJBException(new IllegalStateException(DETAIL)));
        assertEquals(500, response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    public void defaultResponseWithoutCauseHasNoMessage() throws Exception {
        final Response response = newMapper(null).toResponse(new EJBException(DETAIL));
        assertEquals(500, response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    public void messageExposedWhenEnabled() throws Exception {
        SystemInstance.get().setProperty(EJBExceptionMapper.EXPOSE_MESSAGE, "true");
        final Response response = newMapper(null).toResponse(new EJBException(new IllegalStateException(DETAIL)));
        assertEquals(500, response.getStatus());
        assertEquals(DETAIL, response.getEntity());
    }

    @Test
    public void accessDeniedIsForbidden() throws Exception {
        final Response response = newMapper(null).toResponse(new EJBAccessException(DETAIL));
        assertEquals(403, response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    public void causeMapperIsUsed() throws Exception {
        final ExceptionMapper<IllegalArgumentException> mapper = e -> Response.status(234).entity(e.getMessage()).build();
        final Response response = newMapper(mapper).toResponse(new EJBException(new IllegalArgumentException("oops")));
        assertEquals(234, response.getStatus());
        assertEquals("oops", response.getEntity());
    }

    private static EJBExceptionMapper newMapper(final ExceptionMapper<?> causeMapper) throws Exception {
        final EJBExceptionMapper mapper = new EJBExceptionMapper();
        final Providers providers = (Providers) Proxy.newProxyInstance(
                EJBExceptionMapperTest.class.getClassLoader(), new Class<?>[]{Providers.class},
                (proxy, method, args) -> "getExceptionMapper".equals(method.getName()) ? causeMapper : null);
        final Field field = EJBExceptionMapper.class.getDeclaredField("providers");
        field.setAccessible(true);
        field.set(mapper, providers);
        return mapper;
    }
}
