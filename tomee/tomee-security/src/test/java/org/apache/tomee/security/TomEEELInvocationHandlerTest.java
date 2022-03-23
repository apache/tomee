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

import org.apache.tomee.security.identitystore.TomEEDatabaseIdentityStore;
import org.junit.Assert;
import org.junit.Test;

import jakarta.el.ELProcessor;
import jakarta.el.ELResolver;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Named;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.security.enterprise.identitystore.PasswordHash;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.tomee.security.identitystore.TomEEDatabaseIdentityStore.eval;
import static org.apache.tomee.security.identitystore.TomEEDatabaseIdentityStore.toStream;

public class TomEEELInvocationHandlerTest extends AbstractTomEESecurityTest {

    @Test
    public void canCreateInvocationHandler() {
        final DatabaseIdentityStoreDefinition annotation = Color.class.getAnnotation(DatabaseIdentityStoreDefinition.class);

        final ELProcessor elProcessor = new ELProcessor();
        final ELResolver elResolver = bm().getELResolver();
        elProcessor.getELManager().addELResolver(elResolver);

        // small trick because of the @Vetoed bellow - OWB won't pick it up
        // so we will register one ourselves into the processor so it is resolved
        elProcessor.defineBean("color", new Color());

        final DatabaseIdentityStoreDefinition proxiedAnnotation = TomEEELInvocationHandler.of(DatabaseIdentityStoreDefinition.class, annotation, elProcessor);

        Assert.assertEquals("select password from caller where name = ?", proxiedAnnotation.callerQuery());
        Assert.assertEquals(90, proxiedAnnotation.priority());

        Assert.assertEquals("90", proxiedAnnotation.priorityExpression());
        Assert.assertArrayEquals(new IdentityStore.ValidationType[] {IdentityStore.ValidationType.VALIDATE}, proxiedAnnotation.useFor());

        Assert.assertEquals("select group_name from caller_groups where caller_name = ?", proxiedAnnotation.groupsQuery());
        final String[] hashAlgorithmParameters = proxiedAnnotation.hashAlgorithmParameters();
        Assert.assertArrayEquals(new String[]{
            "Pbkdf2PasswordHash.Iterations=3072",
            "${color.dyna}"
        }, hashAlgorithmParameters);

        final Set<String> evaluatedHashParameters = stream(hashAlgorithmParameters)
            .flatMap(s -> toStream(eval(elProcessor, s, Object.class))).collect(toSet());

        System.out.println(evaluatedHashParameters);

        final Map<String, String> parametersMap = evaluatedHashParameters.stream()
            .collect(toMap(s -> (String) s.substring(0, s.indexOf('=')),
                           s -> (String) eval(elProcessor, s.substring(s.indexOf('=') + 1), String.class)));

        System.out.println(parametersMap);
    }

    private BeanManager bm() {
        return CDI.current().getBeanManager();
    }
    @Vetoed // so we don't break the other tests with this
    @Named // see expression language
    @DatabaseIdentityStoreDefinition(dataSourceLookup = "jdbc/securityAPIDB",
                                     callerQuery = "select password from caller where name = ?",
                                     groupsQuery = "${color.groupsQuery}",
                                     hashAlgorithm = CleartextPasswordHash.class,
                                     priority = 30,
                                     priorityExpression = "90",
                                     useForExpression = "#{'VALIDATE'}",
                                     hashAlgorithmParameters = {
                                         "Pbkdf2PasswordHash.Iterations=3072",
                                         "${color.dyna}"
                                     })
    public static class Color {

        public String getGroupsQuery() {
            return "select group_name from caller_groups where caller_name = ?";
        }

        public String[] getDyna() {
            return new String[]{"Pbkdf2PasswordHash.Algorithm=PBKDF2WithHmacSHA512", "Pbkdf2PasswordHash.SaltSizeBytes=64"};
        }
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
