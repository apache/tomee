/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.core;

import java.util.HashMap;
import java.util.Properties;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.loader.SystemInstance;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * @version $Rev:$ $Date:$
 */


public class InheritedAppExceptionTest {

    public static class AE1 extends RuntimeException {
    }
    public static class AE2 extends AE1 {
    }
    public static class AE3 extends AE2 {
    }
    public static class AE4 extends AE3 {
    }
    public static class AE5 extends AE4 {
    }
    public static class AE6 extends AE5 {
    }
    public static class AE7 extends AE6 {
    }

    @Test
    public void testRollback() throws Exception {
        SystemInstance.init(new Properties());
        BeanContext cdi = new BeanContext("foo", null, new ModuleContext("foo",null, "bar", new AppContext("foo", SystemInstance.get(), null, null, null, false), null), Object.class, null, new HashMap<String, String>());
        cdi.addApplicationException(AE1.class, true, true);
        cdi.addApplicationException(AE3.class, true, false);
        cdi.addApplicationException(AE6.class, false, true);

        assertEquals(ExceptionType.APPLICATION_ROLLBACK, cdi.getExceptionType(new AE1()));
        assertEquals(ExceptionType.APPLICATION_ROLLBACK, cdi.getExceptionType(new AE2()));
        assertEquals(ExceptionType.APPLICATION_ROLLBACK, cdi.getExceptionType(new AE3()));
        assertEquals(ExceptionType.SYSTEM, cdi.getExceptionType(new AE4()));
        assertEquals(ExceptionType.SYSTEM, cdi.getExceptionType(new AE5()));
        assertEquals(ExceptionType.APPLICATION, cdi.getExceptionType(new AE6()));
        assertEquals(ExceptionType.APPLICATION, cdi.getExceptionType(new AE7()));
    }
}
