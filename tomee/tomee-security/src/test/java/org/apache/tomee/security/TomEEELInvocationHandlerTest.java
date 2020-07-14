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
package org.apache.tomee.security;

import org.junit.Assert;
import org.junit.Test;

import javax.el.ELProcessor;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import javax.security.enterprise.identitystore.PasswordHash;
import java.lang.reflect.InvocationHandler;
import java.util.Map;

public class TomEEELInvocationHandlerTest extends AbstractTomEESecurityTest {

    @Test
    public void canCreateInvocationHandler() {
        final DatabaseIdentityStoreDefinition annotation =
            Color.class.getAnnotation(DatabaseIdentityStoreDefinition.class);

        final ELProcessor elProcessor = new ELProcessor();
        elProcessor.getELManager().addELResolver(bm().getELResolver());

        final InvocationHandler handler = new TomEEELInvocationHandler(annotation, elProcessor);
        final DatabaseIdentityStoreDefinition proxiedAnnotation = TomEEELInvocationHandler.of(DatabaseIdentityStoreDefinition.class, annotation, bm());

        Assert.assertEquals("select password from caller where name = ?", proxiedAnnotation.callerQuery());
        Assert.assertEquals(90, proxiedAnnotation.priority());

        Assert.assertEquals("90", proxiedAnnotation.priorityExpression());

    }

    private BeanManager bm() {
        return CDI.current().getBeanManager();
    }

    @DatabaseIdentityStoreDefinition(dataSourceLookup = "jdbc/securityAPIDB",
                                     callerQuery = "select password from caller where name = ?",
                                     groupsQuery = "select group_name from caller_groups where caller_name = ?",
                                     hashAlgorithm = CleartextPasswordHash.class,
                                     priority = 30, priorityExpression = "90")
    public static class Color {

    }

    public static class CleartextPasswordHash implements PasswordHash {

        @Override
        public void initialize(Map<String, String> parameters) {

        }

        @Override
        public String generate(char[] password) {
            return new String(password);
        }

        @Override
        public boolean verify(char[] password, String hashedPassword) {
            return (password != null && password.length > 0 && hashedPassword != null
                    && hashedPassword.length() > 0
                    && hashedPassword.equals(new String(password)));
        }
    }

}
