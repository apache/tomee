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
package org.apache.openejb.data.handler;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.PageRecord;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Status;
import jakarta.transaction.UserTransaction;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.executable.ExecutableValidator;
import org.apache.openejb.bval.ValidatorUtil;
import org.apache.openejb.data.meta.MethodMetadata;
import org.apache.openejb.data.meta.RepositoryMetadata;
import org.apache.openejb.data.proxy.EntityManagerLookup;
import org.apache.openejb.data.query.AnnotatedQueryExecutor;
import org.apache.openejb.data.query.CriteriaQueryBuilder;
import org.apache.openejb.data.query.FindAnnotationExecutor;
import org.apache.openejb.data.query.MethodNameParser;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import javax.naming.NamingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class RepositoryInvocationHandler implements InvocationHandler {

    private static final Logger LOGGER = Logger.getLogger(RepositoryInvocationHandler.class.getName());

    private final RepositoryMetadata metadata;
    private final ConcurrentHashMap<Method, MethodMetadata> methodCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Method, String> jpqlCache = new ConcurrentHashMap<>();
    private final String findAllJpql;
    private final String deleteAllJpql;
    private final String countAllJpql;

    public RepositoryInvocationHandler(final RepositoryMetadata metadata) {
        this.metadata = metadata;
        final String entityName = metadata.getEntityClass().getSimpleName();
        this.findAllJpql = "SELECT e FROM " + entityName + " e";
        this.deleteAllJpql = "DELETE FROM " + entityName;
        this.countAllJpql = "SELECT COUNT(e) FROM " + entityName + " e";
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // Object methods
        if (method.getDeclaringClass().equals(Object.class)) {
            return switch (method.getName()) {
                case "toString" -> "Jakarta Data Repository Proxy [" + metadata.getRepositoryInterface().getName() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> method.invoke(this, args);
            };
        }

        // Default methods
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }

        try {
            // Bean Validation: validate parameters
            validateParameters(proxy, method, args);

            final EntityManager em = EntityManagerLookup.lookup(metadata.getDataStore());
            final Object result = executeInTransaction(em, method, args);

            // Bean Validation: validate return value
            validateReturnValue(proxy, method, result);

            return result;
        } catch (final jakarta.data.exceptions.DataException de) {
            // Already a Jakarta Data exception, pass through
            throw de;
        } catch (final ConstraintViolationException cve) {
            // Bean Validation exception, pass through
            throw cve;
        } catch (final Exception e) {
            throw mapException(e);
        }
    }

    private void validateParameters(final Object proxy, final Method method, final Object[] args) {
        if (args == null || args.length == 0) {
            return;
        }
        try {
            final ExecutableValidator execValidator = getExecutableValidator();
            if (execValidator == null) {
                return;
            }
            final Set<ConstraintViolation<Object>> violations =
                execValidator.validateParameters(proxy, method, args);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } catch (final ConstraintViolationException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.log(Level.FINE, "Bean Validation not available for parameter validation", e);
        }
    }

    private void validateReturnValue(final Object proxy, final Method method, final Object result) {
        if (method.getReturnType() == void.class) {
            return;
        }
        try {
            final ExecutableValidator execValidator = getExecutableValidator();
            if (execValidator == null) {
                return;
            }
            final Set<ConstraintViolation<Object>> violations =
                execValidator.validateReturnValue(proxy, method, result);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } catch (final ConstraintViolationException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.log(Level.FINE, "Bean Validation not available for return value validation", e);
        }
    }

    private ExecutableValidator getExecutableValidator() {
        try {
            return ValidatorUtil.validator().forExecutables();
        } catch (final Exception e) {
            return null;
        }
    }

    private Object executeInTransaction(final EntityManager em, final Method method, final Object[] args) throws Exception {
        final UserTransaction ut = lookupUserTransaction();
        if (ut == null) {
            return doInvoke(em, method, args);
        }

        final boolean startedTx = ut.getStatus() != Status.STATUS_ACTIVE;
        if (startedTx) {
            ut.begin();
        }
        try {
            final Object result = doInvoke(em, method, args);
            if (startedTx) {
                ut.commit();
            }
            return result;
        } catch (final Exception e) {
            if (startedTx) {
                try {
                    ut.rollback();
                } catch (final Exception rollbackEx) {
                    LOGGER.log(Level.WARNING, "Rollback failed", rollbackEx);
                }
            }
            throw mapException(e);
        }
    }

    private UserTransaction lookupUserTransaction() {
        try {
            final ContainerSystem containerSystem = SystemInstance.get().getComponent(ContainerSystem.class);
            if (containerSystem != null) {
                return (UserTransaction) containerSystem.getJNDIContext().lookup("comp/UserTransaction");
            }
        } catch (final NamingException e) {
            LOGGER.log(Level.FINE, "UserTransaction not available via JNDI", e);
        }
        return null;
    }

    private Object doInvoke(final EntityManager em, final Method method, final Object[] args) {
        // Check built-in method names from BasicRepository/CrudRepository FIRST
        // These have well-known semantics and their inherited annotations (like @Find on findById)
        // may not work correctly with the generic annotation handlers.
        final String methodName = method.getName();
        switch (methodName) {
            case "save": return handleSave(em, args);
            case "saveAll": return handleSaveAll(em, args);
            case "insert", "insertAll": return handleInsert(em, args);
            case "update", "updateAll": return handleUpdate(em, args);
            case "findById": return handleFindById(em, method, args);
            case "findAll": return handleFindAll(em, method, args);
            case "delete": handleDelete(em, args); return null;
            case "deleteById": {
                // Check if this is a custom @Delete + @By method (not the built-in CrudRepository.deleteById)
                if (method.isAnnotationPresent(Delete.class) && isCustomDeleteById(method)) {
                    return handleDeleteAnnotation(em, method, args);
                }
                handleDeleteById(em, args);
                return null;
            }
            case "deleteAll": handleDeleteAll(em, args); return null;
            case "count", "countAll": return handleCount(em);
            case "existsById": return handleExistsById(em, args);
            default: break;
        }

        // Check annotations for custom methods
        if (method.isAnnotationPresent(Query.class)) {
            final String jpql = jpqlCache.computeIfAbsent(method,
                m -> AnnotatedQueryExecutor.buildJpql(m, metadata.getEntityClass(), em));
            return AnnotatedQueryExecutor.execute(em, method, args, jpql);
        }

        if (method.isAnnotationPresent(Insert.class)) {
            return handleInsert(em, args);
        }

        if (method.isAnnotationPresent(Update.class)) {
            return handleUpdate(em, args);
        }

        if (method.isAnnotationPresent(Delete.class)) {
            return handleDeleteAnnotation(em, method, args);
        }

        if (method.isAnnotationPresent(Save.class)) {
            return handleSave(em, args);
        }

        if (method.isAnnotationPresent(Find.class)) {
            final String jpql = jpqlCache.computeIfAbsent(method,
                m -> FindAnnotationExecutor.buildJpql(m, metadata.getEntityClass()));
            return FindAnnotationExecutor.execute(em, method, args, metadata.getEntityClass(), jpql);
        }

        return handleCustomMethod(em, method, args);
    }

    // -- Built-in CRUD operations --

    @SuppressWarnings("unchecked")
    private <T> T handleSave(final EntityManager em, final Object[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("save requires exactly one argument");
        }
        final Object result = applyToEntities(args[0], e -> em.merge(e));
        em.flush();
        return (T) result;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> handleSaveAll(final EntityManager em, final Object[] args) {
        final List<T> result = new ArrayList<>();
        for (final Object entity : asIterable(args[0])) {
            result.add((T) em.merge(entity));
        }
        em.flush();
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T handleInsert(final EntityManager em, final Object[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("insert requires exactly one argument");
        }
        return (T) applyToEntities(args[0], e -> {
            clearVersionField(em, e);
            em.persist(e);
            em.flush();
            return e;
        });
    }

    private void clearVersionField(final EntityManager em, final Object entity) {
        try {
            final jakarta.persistence.metamodel.EntityType<?> entityType =
                em.getMetamodel().entity(entity.getClass());
            if (entityType.hasVersionAttribute()) {
                for (final jakarta.persistence.metamodel.SingularAttribute<?, ?> attr : entityType.getSingularAttributes()) {
                    if (attr.isVersion()) {
                        final java.lang.reflect.Field field = entity.getClass().getDeclaredField(attr.getName());
                        field.setAccessible(true);
                        if (field.getType() == long.class) {
                            field.setLong(entity, 0L);
                        } else if (field.getType() == int.class) {
                            field.setInt(entity, 0);
                        } else if (field.getType() == Long.class || field.getType() == Integer.class) {
                            field.set(entity, null);
                        }
                        break;
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.FINE, "Could not clear version field", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T handleUpdate(final EntityManager em, final Object[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("update requires exactly one argument");
        }
        return (T) applyToEntities(args[0], e -> mergeWithVersionCheck(em, e));
    }

    /**
     * Performs an update with version checking. The strategy depends on the JPA provider:
     * <ul>
     *   <li>Hibernate/EclipseLink: standard {@code em.merge()} handles @Version correctly</li>
     *   <li>OpenJPA: requires a workaround — reflection-based field copying with
     *       OPTIMISTIC_FORCE_INCREMENT because OpenJPA restricts setting @Version fields</li>
     * </ul>
     */
    private Object mergeWithVersionCheck(final EntityManager em, final Object entity) {
        final Object id = getEntityId(em, entity);
        if (id == null) {
            return em.merge(entity);
        }

        final Object existing = em.find(entity.getClass(), id);
        if (existing == null) {
            throw new jakarta.data.exceptions.OptimisticLockingFailureException(
                "Entity not found for update: " + entity);
        }

        // Check version before merge
        checkVersionMatch(em, entity, existing);

        // Providers other than OpenJPA handle @Version correctly via em.merge()
        if (!isOpenJpa(em)) {
            return em.merge(entity);
        }

        // OpenJPA workaround: copy fields via reflection, force version increment
        try {
            final jakarta.persistence.metamodel.EntityType<?> entityType =
                em.getMetamodel().entity(entity.getClass());

            for (final jakarta.persistence.metamodel.Attribute<?, ?> attr : entityType.getAttributes()) {
                if (attr instanceof jakarta.persistence.metamodel.SingularAttribute<?, ?> sa) {
                    if (sa.isId() || sa.isVersion()) {
                        continue;
                    }
                }
                final java.lang.reflect.Field field = findField(entity.getClass(), attr.getName());
                field.setAccessible(true);
                final java.lang.reflect.Field existingField = findField(existing.getClass(), attr.getName());
                existingField.setAccessible(true);
                existingField.set(existing, field.get(entity));
            }
            em.lock(existing, jakarta.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT);
            em.flush();
            em.refresh(existing);
            // Copy updated fields (including version) back to the input entity
            for (final jakarta.persistence.metamodel.Attribute<?, ?> attr : entityType.getAttributes()) {
                try {
                    final java.lang.reflect.Field f = findField(entity.getClass(), attr.getName());
                    f.setAccessible(true);
                    final java.lang.reflect.Field ef = findField(existing.getClass(), attr.getName());
                    ef.setAccessible(true);
                    f.set(entity, ef.get(existing));
                } catch (final NoSuchFieldException ignored) {
                    // skip
                }
            }
            return entity;
        } catch (final jakarta.data.exceptions.OptimisticLockingFailureException e) {
            throw e;
        } catch (final Exception e) {
            return em.merge(entity);
        }
    }

    private static boolean isOpenJpa(final EntityManager em) {
        return em.getClass().getName().startsWith("org.apache.openjpa.")
            || em.getDelegate().getClass().getName().startsWith("org.apache.openjpa.");
    }

    private Object handleFindById(final EntityManager em, final Method method, final Object[] args) {
        final Object id = args[0];
        final Object entity = em.find(metadata.getEntityClass(), id);
        if (Optional.class.isAssignableFrom(method.getReturnType())) {
            return Optional.ofNullable(entity);
        }
        return entity;
    }

    private Object handleFindAll(final EntityManager em, final Method method, final Object[] args) {
        final TypedQuery<Object> query = em.createQuery(findAllJpql, Object.class);
        CriteriaQueryBuilder.applyPagination(query, args);

        final List<Object> results = query.getResultList();
        final Class<?> returnType = method.getReturnType();

        if (Page.class.isAssignableFrom(returnType)) {
            return buildPage(em, metadata.getEntityClass(), results, args, null);
        }
        if (Stream.class.isAssignableFrom(returnType)) {
            return results.stream();
        }
        return results;
    }

    private void handleDelete(final EntityManager em, final Object[] args) {
        if (args == null || args.length != 1) {
            throw new IllegalArgumentException("delete requires exactly one argument");
        }
        final Object entity = args[0];
        if (entity instanceof Iterable<?> iterable) {
            for (final Object e : iterable) {
                removeEntity(em, e);
            }
        } else {
            removeEntity(em, entity);
        }
    }

    private void handleDeleteById(final EntityManager em, final Object[] args) {
        final Object id = args[0];
        final Object entity = em.find(metadata.getEntityClass(), id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    private void handleDeleteAll(final EntityManager em, final Object[] args) {
        if (args != null && args.length == 1) {
            for (final Object entity : asIterable(args[0])) {
                removeEntity(em, entity);
            }
        } else {
            em.createQuery(deleteAllJpql).executeUpdate();
        }
    }

    private Object handleDeleteAnnotation(final EntityManager em, final Method method, final Object[] args) {
        if (args == null || args.length < 1) {
            em.createQuery(deleteAllJpql).executeUpdate();
            return null;
        }

        // Check if parameters have @By annotations (field-based delete)
        final java.lang.reflect.Parameter[] params = method.getParameters();
        boolean hasByAnnotation = false;
        for (final java.lang.reflect.Parameter p : params) {
            if (p.isAnnotationPresent(jakarta.data.repository.By.class)) {
                hasByAnnotation = true;
                break;
            }
        }

        if (hasByAnnotation) {
            final String jpql = jpqlCache.computeIfAbsent(method, m -> {
                final java.lang.reflect.Parameter[] mp = m.getParameters();
                final StringBuilder sb = new StringBuilder("DELETE FROM ")
                    .append(metadata.getEntityClass().getSimpleName()).append(" e WHERE ");
                final List<String> conds = new ArrayList<>();
                for (int j = 0; j < mp.length; j++) {
                    final jakarta.data.repository.By by = mp[j].getAnnotation(jakarta.data.repository.By.class);
                    if (by != null) {
                        conds.add("e." + by.value() + " = :p" + j);
                    }
                }
                sb.append(String.join(" AND ", conds));
                return sb.toString();
            });

            final jakarta.persistence.Query query = em.createQuery(jpql);
            for (int i = 0; i < params.length; i++) {
                if (params[i].isAnnotationPresent(jakarta.data.repository.By.class)) {
                    query.setParameter("p" + i, args[i]);
                }
            }
            query.executeUpdate();
            return null;
        }

        // Entity-based delete
        final Object entity = args[0];
        if (entity instanceof Iterable<?> iterable) {
            for (final Object e : iterable) {
                removeEntity(em, e);
            }
        } else if (entity.getClass().isArray()) {
            for (final Object e : (Object[]) entity) {
                removeEntity(em, e);
            }
        } else {
            removeEntity(em, entity);
        }
        return null;
    }

    private long handleCount(final EntityManager em) {
        return em.createQuery(countAllJpql, Long.class).getSingleResult();
    }

    private boolean handleExistsById(final EntityManager em, final Object[] args) {
        return em.find(metadata.getEntityClass(), args[0]) != null;
    }

    // -- Custom method handling --

    @SuppressWarnings("unchecked")
    private Object handleCustomMethod(final EntityManager em, final Method method, final Object[] args) {
        final MethodMetadata meta = methodCache.computeIfAbsent(method, this::analyzeMethod);

        if (meta.getStrategy() == MethodMetadata.Strategy.METHOD_NAME && meta.getParsedQuery() != null) {
            final MethodNameParser.ParsedQuery parsed = meta.getParsedQuery();

            return switch (parsed.action()) {
                case FIND -> {
                    final String jpql = jpqlCache.computeIfAbsent(method,
                        m -> CriteriaQueryBuilder.buildFindJpql(metadata.getEntityClass(), parsed));
                    yield executeFindQuery(em, method, jpql, parsed, args);
                }
                case DELETE -> {
                    final String jpql = jpqlCache.computeIfAbsent(method,
                        m -> CriteriaQueryBuilder.buildDeleteJpql(metadata.getEntityClass(), parsed));
                    final long deleted = CriteriaQueryBuilder.executeDelete(em, jpql, parsed, args);
                    final Class<?> rt = method.getReturnType();
                    if (rt == int.class || rt == Integer.class) {
                        yield (int) deleted;
                    }
                    yield deleted;
                }
                case COUNT -> {
                    final String jpql = jpqlCache.computeIfAbsent(method,
                        m -> CriteriaQueryBuilder.buildCountJpql(metadata.getEntityClass(), parsed));
                    yield CriteriaQueryBuilder.executeCount(em, jpql, parsed, args).getSingleResult();
                }
                case EXISTS -> {
                    final String jpql = jpqlCache.computeIfAbsent(method,
                        m -> CriteriaQueryBuilder.buildCountJpql(metadata.getEntityClass(), parsed));
                    yield CriteriaQueryBuilder.executeCount(em, jpql, parsed, args).getSingleResult() > 0;
                }
            };
        }

        throw new UnsupportedOperationException("Cannot resolve method: " + method.getName()
            + " on repository " + metadata.getRepositoryInterface().getName());
    }

    @SuppressWarnings("unchecked")
    private Object executeFindQuery(final EntityManager em, final Method method,
                                    final String jpql,
                                    final MethodNameParser.ParsedQuery parsed, final Object[] args) {
        final TypedQuery<?> query = CriteriaQueryBuilder.executeFind(em, metadata.getEntityClass(), jpql, parsed, args);
        final Class<?> returnType = method.getReturnType();

        if (Page.class.isAssignableFrom(returnType)) {
            return buildPage(em, metadata.getEntityClass(), new ArrayList<>(query.getResultList()), args, parsed);
        }

        if (List.class.isAssignableFrom(returnType) || Collection.class.isAssignableFrom(returnType)) {
            return query.getResultList();
        }

        if (Stream.class.isAssignableFrom(returnType)) {
            return query.getResultList().stream();
        }

        if (Optional.class.isAssignableFrom(returnType)) {
            final List<?> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }

        if (returnType.isArray()) {
            return query.getResultList().toArray();
        }

        // Single result
        final List<?> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    private MethodMetadata analyzeMethod(final Method method) {
        // Try method name parsing
        final MethodNameParser.ParsedQuery parsed = MethodNameParser.parse(method.getName());
        if (parsed != null) {
            return new MethodMetadata(MethodMetadata.Strategy.METHOD_NAME, parsed);
        }
        return new MethodMetadata(MethodMetadata.Strategy.BUILTIN, null);
    }

    // -- Helper methods --

    private void removeEntity(final EntityManager em, final Object entity) {
        if (em.contains(entity)) {
            em.remove(entity);
            return;
        }
        // Check if entity exists by looking up its ID
        final Object id = getEntityId(em, entity);
        if (id != null) {
            final Object existing = em.find(entity.getClass(), id);
            if (existing == null) {
                throw new jakarta.data.exceptions.OptimisticLockingFailureException(
                    "Entity not found for deletion: " + entity);
            }
            // Check version if entity has @Version
            checkVersionMatch(em, entity, existing);
            em.remove(existing);
        } else {
            try {
                final Object merged = em.merge(entity);
                em.remove(merged);
            } catch (final Exception e) {
                throw new jakarta.data.exceptions.OptimisticLockingFailureException(
                    "Entity not found for deletion: " + entity, e);
            }
        }
    }

    private static java.lang.reflect.Field findField(final Class<?> clazz, final String name) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (final NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    /**
     * Checks if a deleteById method is a custom one declared on the repository (with @By annotations
     * pointing to real fields) vs the inherited CrudRepository.deleteById (which may have synthetic @By).
     */
    private boolean isCustomDeleteById(final Method method) {
        // If the method is declared on a built-in repository interface (not user-defined), it's not custom
        final Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.equals(CrudRepository.class) || declaringClass.equals(BasicRepository.class)) {
            return false;
        }
        // Check if @By annotations reference real entity fields (not synthetic like "id(this)")
        for (final java.lang.reflect.Parameter p : method.getParameters()) {
            final jakarta.data.repository.By by = p.getAnnotation(jakarta.data.repository.By.class);
            if (by != null && by.value().contains("(")) {
                return false; // Synthetic @By like "id(this)"
            }
        }
        return true;
    }

    private void checkVersionMatch(final EntityManager em, final Object input, final Object existing) {
        try {
            final jakarta.persistence.metamodel.EntityType<?> entityType =
                em.getMetamodel().entity(input.getClass());
            if (entityType.hasVersionAttribute()) {
                for (final jakarta.persistence.metamodel.SingularAttribute<?, ?> attr : entityType.getSingularAttributes()) {
                    if (attr.isVersion()) {
                        final java.lang.reflect.Field field = input.getClass().getDeclaredField(attr.getName());
                        field.setAccessible(true);
                        final Object inputVersion = field.get(input);
                        final Object existingVersion = field.get(existing);
                        if (inputVersion != null && existingVersion != null && !inputVersion.equals(existingVersion)) {
                            throw new jakarta.data.exceptions.OptimisticLockingFailureException(
                                "Version mismatch: expected " + existingVersion + " but got " + inputVersion);
                        }
                        break;
                    }
                }
            }
        } catch (final jakarta.data.exceptions.OptimisticLockingFailureException e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.log(Level.FINE, "Could not check version for deletion", e);
        }
    }

    private Object getEntityId(final EntityManager em, final Object entity) {
        try {
            final jakarta.persistence.metamodel.EntityType<?> entityType =
                em.getMetamodel().entity(entity.getClass());
            for (final jakarta.persistence.metamodel.SingularAttribute<?, ?> attr : entityType.getSingularAttributes()) {
                if (attr.isId()) {
                    final java.lang.reflect.Field field = entity.getClass().getDeclaredField(attr.getName());
                    field.setAccessible(true);
                    return field.get(entity);
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.FINE, "Could not get entity ID", e);
        }
        return null;
    }

    /**
     * Maps JPA and provider-specific exceptions to Jakarta Data exceptions as required by the spec.
     * Provider-specific exceptions (EclipseLink, OpenJPA, etc.) are not embedded as causes
     * to avoid serialization issues in remote containers (e.g., Arquillian).
     */
    private static Exception mapException(final Exception e) {
        // First, check for standard JPA exceptions
        final Throwable jpaCause = findJpaCause(e);
        if (jpaCause != null) {
            final String message = buildExceptionMessage(jpaCause);

            if (jpaCause instanceof jakarta.persistence.EntityExistsException) {
                return new jakarta.data.exceptions.EntityExistsException(message);
            }
            if (jpaCause instanceof jakarta.persistence.OptimisticLockException) {
                return new jakarta.data.exceptions.OptimisticLockingFailureException(message);
            }
            if (jpaCause instanceof jakarta.persistence.EntityNotFoundException) {
                return new jakarta.data.exceptions.EmptyResultException(message);
            }
            if (jpaCause instanceof jakarta.persistence.NonUniqueResultException) {
                return new jakarta.data.exceptions.NonUniqueResultException(message);
            }
            if (jpaCause instanceof jakarta.persistence.NoResultException) {
                return new jakarta.data.exceptions.EmptyResultException(message);
            }
            if (jpaCause instanceof jakarta.persistence.PersistenceException) {
                // SQL constraint violation within a PersistenceException indicates
                // an insert-duplicate / unique-key conflict → EntityExistsException per spec
                if (hasSqlConstraintViolation(jpaCause)) {
                    return new jakarta.data.exceptions.EntityExistsException(message);
                }
                return new jakarta.data.exceptions.DataException(message);
            }
        }

        // Second, check for SQL constraint violations anywhere in the chain
        // (provider-specific exceptions like EclipseLink DatabaseException may not
        //  extend PersistenceException but still wrap SQLIntegrityConstraintViolationException)
        if (hasSqlConstraintViolation(e)) {
            return new jakarta.data.exceptions.EntityExistsException(buildExceptionMessage(e));
        }

        // Third, wrap any remaining provider-specific exceptions to prevent serialization issues
        if (isProviderSpecificException(e)) {
            return new jakarta.data.exceptions.DataException(buildExceptionMessage(e));
        }

        return e;
    }

    /**
     * Checks if the exception cause chain contains a SQL constraint violation
     * (indicating a duplicate key / entity exists scenario).
     */
    private static boolean hasSqlConstraintViolation(final Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof java.sql.SQLIntegrityConstraintViolationException) {
                return true;
            }
            // Also check class name for providers that use custom exception hierarchies
            final String className = current.getClass().getName();
            if (className.contains("ConstraintViolation") || className.contains("IntegrityConstraint")) {
                return true;
            }
            // Check exception message for constraint violation keywords (covers providers
            // like EclipseLink where the internal exception may not be in the getCause() chain)
            final String message = current.getMessage();
            if (message != null) {
                final String lowerMsg = message.toLowerCase(java.util.Locale.ROOT);
                if (lowerMsg.contains("unique constraint") || lowerMsg.contains("integrity constraint")
                    || lowerMsg.contains("duplicate key") || lowerMsg.contains("duplicate entry")
                    || lowerMsg.contains("primary key violation")) {
                    return true;
                }
            }
            // EclipseLink stores internal exceptions in a field not returned by getCause().
            // Use reflection to access getInternalException() if available.
            final Throwable internal = getInternalException(current);
            if (internal != null && internal != current.getCause()) {
                if (internal instanceof java.sql.SQLIntegrityConstraintViolationException) {
                    return true;
                }
                final String internalName = internal.getClass().getName();
                if (internalName.contains("ConstraintViolation") || internalName.contains("IntegrityConstraint")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Attempts to call getInternalException() on the throwable via reflection.
     * This handles EclipseLink's EclipseLinkException which stores internal exceptions
     * separately from the standard getCause() chain.
     */
    private static Throwable getInternalException(final Throwable t) {
        try {
            final java.lang.reflect.Method method = t.getClass().getMethod("getInternalException");
            final Object result = method.invoke(t);
            if (result instanceof Throwable) {
                return (Throwable) result;
            }
        } catch (final Exception ignored) {
            // Method doesn't exist or can't be called - that's fine
        }
        return null;
    }

    /**
     * Checks if the exception originates from a JPA provider's internal package
     * (e.g., EclipseLink, OpenJPA) and might not be serializable on a remote client.
     */
    private static boolean isProviderSpecificException(final Throwable t) {
        Throwable current = t;
        while (current != null) {
            final String className = current.getClass().getName();
            if (className.startsWith("org.eclipse.persistence.")
                || className.startsWith("org.apache.openjpa.")
                || className.startsWith("org.hibernate.")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Builds a descriptive exception message that includes the root cause details
     * without embedding provider-specific exception objects.
     */
    private static String buildExceptionMessage(final Throwable cause) {
        final StringBuilder sb = new StringBuilder();
        if (cause.getMessage() != null) {
            sb.append(cause.getMessage());
        }
        Throwable root = cause.getCause();
        while (root != null) {
            if (root.getMessage() != null && !root.getMessage().isEmpty()) {
                sb.append(" [Caused by: ").append(root.getMessage()).append("]");
                break;
            }
            root = root.getCause();
        }
        return sb.toString();
    }

    private static Throwable findJpaCause(final Throwable t) {
        Throwable current = t;
        while (current != null) {
            if (current instanceof jakarta.persistence.PersistenceException) {
                return current;
            }
            current = current.getCause();
        }
        return null;
    }

    @FunctionalInterface
    private interface EntityOperation {
        Object apply(Object entity);
    }

    @SuppressWarnings("unchecked")
    private static Object applyToEntities(final Object arg, final EntityOperation op) {
        if (arg instanceof Iterable<?>) {
            final List<Object> result = new ArrayList<>();
            for (final Object entity : (Iterable<?>) arg) {
                result.add(op.apply(entity));
            }
            return result;
        }
        if (arg.getClass().isArray()) {
            final Object[] array = (Object[]) arg;
            final Object[] result = java.util.Arrays.copyOf(array, array.length);
            for (int i = 0; i < array.length; i++) {
                result[i] = op.apply(array[i]);
            }
            return result;
        }
        return op.apply(arg);
    }

    @SuppressWarnings("unchecked")
    private static Iterable<Object> asIterable(final Object arg) {
        if (arg instanceof Iterable<?> it) {
            return (Iterable<Object>) it;
        }
        if (arg.getClass().isArray()) {
            return List.of((Object[]) arg);
        }
        return List.of(arg);
    }

    @SuppressWarnings("unchecked")
    private Page<Object> buildPage(final EntityManager em, final Class<?> entityClass,
                                   final List<Object> content, final Object[] args,
                                   final MethodNameParser.ParsedQuery parsed) {
        PageRequest pageRequest = null;
        if (args != null) {
            for (final Object arg : args) {
                if (arg instanceof PageRequest pr) {
                    pageRequest = pr;
                    break;
                }
            }
        }

        if (pageRequest == null) {
            pageRequest = PageRequest.ofPage(1);
        }

        // Count total - use the same conditions as the finder query if available
        final long total;
        if (parsed != null && !parsed.conditions().isEmpty()) {
            final String countJpql = CriteriaQueryBuilder.buildCountJpql(entityClass, parsed);
            total = CriteriaQueryBuilder.executeCount(em, countJpql, parsed, args).getSingleResult();
        } else {
            total = em.createQuery(countAllJpql, Long.class).getSingleResult();
        }

        return new PageRecord<>(pageRequest, content, total);
    }
}
