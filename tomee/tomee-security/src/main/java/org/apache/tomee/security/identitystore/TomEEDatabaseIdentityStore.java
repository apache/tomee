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
package org.apache.tomee.security.identitystore;

import javax.annotation.PostConstruct;
import javax.el.ELProcessor;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import javax.security.enterprise.identitystore.IdentityStore;
import javax.security.enterprise.identitystore.IdentityStorePermission;
import javax.security.enterprise.identitystore.PasswordHash;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class TomEEDatabaseIdentityStore implements IdentityStore {

    private final Pattern elExpressionPattern = Pattern.compile("^[#$]\\{(.+)}$");

    @Inject
    private BeanManager beanManager;

    @Inject
    private Supplier<DatabaseIdentityStoreDefinition> definitionSupplier;
    private DatabaseIdentityStoreDefinition definition;

    private Set<ValidationType> validationTypes;

    private PasswordHash passwordHash;

    @PostConstruct
    private void init() throws Exception {
        definition = definitionSupplier.get();
        validationTypes = new HashSet<>(asList(definition.useFor()));

        passwordHash = getInstance(definition.hashAlgorithm());

        final ELProcessor elProcessor = new ELProcessor();
        elProcessor.getELManager().addELResolver(beanManager.getELResolver());

        passwordHash.initialize(stream(definition.hashAlgorithmParameters())
                    .flatMap(s -> toStream(eval(elProcessor, s, String.class)))
                    .collect(toMap(s -> (String) s.substring(0, s.indexOf('=')) ,
                                   s -> (String) eval(elProcessor, s.substring(s.indexOf('=') + 1), String.class)))
                               );
    }

    @Override
    public CredentialValidationResult validate(final Credential credential) {
        if (!(credential instanceof UsernamePasswordCredential)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }

        final UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) credential;
        final List<String> passwords = query(definition.callerQuery(), usernamePasswordCredential.getCaller());

        if (passwords.isEmpty()) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        if (passwordHash.verify(usernamePasswordCredential.getPassword().getValue(), passwords.get(0))) {
            Set<String> groups = emptySet();
            if (validationTypes.contains(ValidationType.PROVIDE_GROUPS)) {
                groups = new HashSet<>(getGroups(usernamePasswordCredential.getCaller()));
            }

            return new CredentialValidationResult(usernamePasswordCredential.getCaller(), groups);
        }

        return CredentialValidationResult.INVALID_RESULT;
    }

    @Override
    public Set<String> getCallerGroups(final CredentialValidationResult validationResult) {

        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(new IdentityStorePermission("getGroups"));
        }

        return getGroups(validationResult.getCallerPrincipal().getName());
    }

    private Set<String> getGroups(final String username) {
        if (username == null) {
            return emptySet();
        }
        return new HashSet<>(query(definition.groupsQuery(), username));
    }

    @Override
    public int priority() {
        return definition.priority();
    }

    @Override
    public Set<ValidationType> validationTypes() {
        return validationTypes;
    }

    private List<String> query(final String query, final String parameter) {
        final List<String> result = new ArrayList<>();

        final DataSource dataSource = lookup(definition.dataSourceLookup()); // todo instance field?
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, parameter);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        result.add(resultSet.getString(1));
                    }
                }
            }
        } catch (final SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return result;
    }

    public static DataSource lookup(final String name) {
        if (name == null || name.trim().equals("")) {
            throw new RuntimeException(
                "Can't lookup datasource because dataSourceLookup is null/empty in DatabaseIdentityStoreDefinition.");
        }

        Context ctx = null;
        try {
            ctx = new InitialContext();

            // todo is it the best way to look it up
            return (DataSource) ctx.lookup("java:openejb/Resource/" + name);

        } catch (final NamingException ne) {
            throw new RuntimeException("Can't find datasource with name in DatabaseIdentityStoreDefinition.", ne);

        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ne) {
                    // ignore
                }
            }
        }
    }

    private Object eval(final ELProcessor processor, final String expression, final Class<?> expectedType) {
        // expression maybe #{expression} instead of ${expression}
        // the ELProcessor anyways wraps it with ${}

        final Matcher matcher = elExpressionPattern.matcher(expression);

        if (!matcher.matches()) {
            return expression;
        }

        final String sanitizedExpression = matcher.replaceAll("$1");
        return processor.getValue(sanitizedExpression, expectedType);
    }

    private Stream<String> toStream(final Object raw) {
        if (raw instanceof String[]) {
            return stream((String[])raw);
        }
        if (raw instanceof Stream<?>) {
            return ((Stream<String>) raw).map(String::toString);
        }

        return Stream.of(raw.toString());
    }

    private <T extends PasswordHash> T getInstance(final Class<T> beanType) {
        final Bean<T> bean = (Bean<T>) beanManager.getBeans(beanType).iterator().next();

        // This should create the instance and put it in the context
        return (T) beanManager.getReference(bean, beanType, beanManager.createCreationalContext(bean));
    }
}
