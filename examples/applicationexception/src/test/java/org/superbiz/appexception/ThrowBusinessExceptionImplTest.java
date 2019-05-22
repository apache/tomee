/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.appexception;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Properties;

// TODO This test case does not actually show that the bean was not destroyed.  The effect of @ApplicationException is not demonstrated
// Maybe have two methods that throw runtime exceptions and compare the behavior of both
// TODO Remote the business interface and show only POJO usage
public class ThrowBusinessExceptionImplTest {

    //START SNIPPET: setup
    private InitialContext initialContext;

    @Before
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.core.LocalInitialContextFactory");

        initialContext = new InitialContext(properties);
    }
    //END SNIPPET: setup	

    /**
     * Lookup the Counter bean via its remote home interface
     *
     * @throws Exception
     */
    //START SNIPPET: remote
    @Test(expected = ValueRequiredException.class)
    public void testCounterViaRemoteInterface() throws Exception {
        Object object = initialContext.lookup("ThrowBusinessExceptionImplRemote");

        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof ThrowBusinessException);
        ThrowBusinessException bean = (ThrowBusinessException) object;
        bean.throwValueRequiredException();
    }
    //END SNIPPET: remote

}
