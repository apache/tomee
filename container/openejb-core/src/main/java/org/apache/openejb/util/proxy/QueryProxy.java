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

package org.apache.openejb.util.proxy;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueryProxy implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, QueryProxy.class);

    // keywords
    public static final String PERSIST_NAME = "save";
    public static final String MERGE_NAME = "update";
    public static final String REMOVE_NAME = "delete";
    public static final String NAMED_QUERY_NAME = "namedQuery";
    public static final String NATIVE_QUERY_NAME = "nativeQuery";
    public static final String QUERY_NAME = "query";

    public static final String FIND_PREFIX = "find";
    public static final String BY = "By";
    public static final String AND = "And";

    // cache for finders of the current instance
    private final Map<String, Class<?>> RETURN_TYPES = new ConcurrentHashMap<String, Class<?>>();
    private final Map<String, List<String>> CONDITIONS = new ConcurrentHashMap<String, List<String>>();

    private EntityManager em;

    private static enum QueryType {
        NAMED, NATIVE, OTHER
    }

    public void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }

        final String methodName = method.getName();
        final Class<?> returnType = method.getReturnType();

        // simple cases
        if (PERSIST_NAME.equals(methodName)) {
            persist(args, returnType);
            return null; // void
        }

        if (MERGE_NAME.equals(methodName)) {
            return merge(args, returnType);
        }

        if (REMOVE_NAME.equals(methodName)) {
            remove(args, returnType);
            return null; // void
        }

        // queries
        if (NAMED_QUERY_NAME.equals(methodName)) {
            return query(method, args, QueryType.NAMED);
        }

        if (NATIVE_QUERY_NAME.equals(methodName)) {
            return query(method, args, QueryType.NATIVE);
        }

        if (QUERY_NAME.equals(methodName)) {
            return query(method, args, QueryType.OTHER);
        }

        // finders
        if (methodName.startsWith(FIND_PREFIX)) {
            return find(method, args);
        }

        throw new IllegalArgumentException("method not yet managed");
    }

    /**
     *
     * @param method the method
     * @param args queryName (String) -> first parameter, parameters (Map<String, ?>) or (Object[]), first and max (int) -> max follows first
     * @param type the query type
     * @return the expected result
     */
    private Object query(Method method, Object[] args, QueryType type) {
        if (args.length < 1) {
            throw new IllegalArgumentException("query() needs at least the query name");
        }

        int matched = 0;
        Query query;
        if (String.class.isAssignableFrom(args[0].getClass())) {
            switch (type) {
                case NAMED:
                    query = em.createNamedQuery((String) args[0]);
                break;

                case NATIVE:
                    query = em.createNativeQuery((String) args[0]);
                break;

                default:
                    query = em.createQuery((String) args[0]);
            }

            matched++;

            for (int i = 1; i < args.length; i++) {
                if (args[i] == null) {
                    continue;
                }

                if (Map.class.isAssignableFrom(args[i].getClass())) {
                    for (Map.Entry<String, ?> entry : ((Map<String, ?>) args[i]).entrySet()) {
                        query = query.setParameter(entry.getKey(), entry.getValue());
                    }
                    matched++;
                } else if (args[i].getClass().isArray()) {
                    Object[] array = (Object[]) args[i];
                    for (int j = 0; j <  array.length; j++) {
                        query = query.setParameter(j, array[j]);
                    }
                    matched++;
                } else if (isInt(args[i].getClass())) {
                    int next = i + 1;
                    if (args.length == next || !isInt(args[next].getClass())) {
                        throw new IllegalArgumentException("if you provide a firstResult (first int parameter)" +
                                "you should provide a maxResult too");
                    }
                    int first = (Integer) args[i];
                    int max = (Integer) args[next];

                    query = query.setFirstResult(first);
                    query = query.setMaxResults(max);

                    matched += 2;
                    i++;
                } else {
                    throw new IllegalArgumentException("not managed parameter " + args[i]
                                + " of type " + args[i].getClass());
                }
            }

            if (matched != args.length) {
                throw new IllegalArgumentException("all argument was not used, please check you signature looks like:" +
                        " <ReturnType> query(String name, Map<String, ?> parameters, int firstResult, int maxResult)");
            }
        } else {
            throw new IllegalArgumentException("query() needs at least the query name of type String");
        }

        return getQueryResult(method, query);
    }

    private Class<?> getReturnedType(Method method) {
        final String methodName = method.getName();
        Class<?> type;
        if (RETURN_TYPES.containsKey(methodName)) {
            type = RETURN_TYPES.get(methodName);
        } else {
            type =  getGenericType(method.getGenericReturnType());
            RETURN_TYPES.put(methodName, type);
        }
        return type;
    }

    private Object getQueryResult(Method method, Query query) {
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
            return query.getResultList();
        }
        return query.getSingleResult();
    }

    private Object find(Method method, Object[] args) {
        final String methodName = method.getName();
        final Class<?> type = getReturnedType(method);
        final Query query = createFinderQuery(em, methodName, type, args);
        return getQueryResult(method, query);
    }

    private void remove(Object[] args, Class<?> returnType) {
        if (args != null && args.length == 1 && returnType.equals(Void.TYPE)) {
            Object entity = args[0];
            if (!em.contains(entity)) { // reattach the entity if possible
                final Class<?> entityClass = entity.getClass();
                final EntityType<? extends Object> et = em.getMetamodel().entity(entityClass);

                if (!et.hasSingleIdAttribute()) {
                    throw new IllegalArgumentException("Dynamic EJB doesn't manage IdClass yet");
                }

                SingularAttribute<?, ?> id = null; // = et.getId(entityClass); doesn't work with openJPA
                for (SingularAttribute<?, ?> sa : et.getSingularAttributes()) {
                    if (sa.isId()) {
                        id = sa;
                        break;
                    }
                }
                if (id == null) {
                    throw new IllegalArgumentException("id field not found");
                }
                final String idName = id.getName();

                Object idValue;
                try {
                    idValue = BeanUtils.getProperty(entity, idName);;
                } catch (InvocationTargetException e) {
                    throw new IllegalArgumentException("can't invoke to get entity id");
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("can't find the method to get entity id");
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("can't access field/method to get entity id");
                }

                entity = em.getReference(et.getJavaType(), idValue);
                if (entity == null) {
                    throw new IllegalArgumentException("entity " + entity + " is not managed and can't be found.");
                }
            }
            em.remove(entity);
        } else {
            throw new IllegalArgumentException(REMOVE_NAME + " should have only one parameter and return void");
        }
    }

    private Object merge(Object[] args, Class<?> returnType) {
        if (args != null && args.length == 1 && returnType.equals(args[0].getClass())) {
            return em.merge(args[0]);
        } else {
            throw new IllegalArgumentException(MERGE_NAME + " should have only one parameter and return the same" +
                    " type than the parameter type");
        }
    }

    private void persist(Object[] args, Class<?> returnType) {
        if (args != null && args.length == 1 && returnType.equals(Void.TYPE)) {
            em.persist(args[0]);
        } else {
            throw new IllegalArgumentException(PERSIST_NAME + " should have only one parameter and return void");
        }
    }

    private Class<?> getGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            if ( pt.getActualTypeArguments().length == 1) {
                return (Class<?>) pt.getActualTypeArguments()[0];
            }
        }
        return Class.class.cast(type);
    }

    private <T> Query createFinderQuery(EntityManager entityManager, String methodName, Class<T> entityType, Object[] args) {
        final List<String> conditions = parseMethodName(methodName);

        final EntityType<T> et = entityManager.getMetamodel().entity(entityType);
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Object> query = cb.createQuery();
        Root<T> from = query.from(entityType);
        query = query.select(from);

        int i = 0;
        Predicate where = null;
        for (String condition : conditions) {
            SingularAttribute<? super T, ?> attribute = et.getSingularAttribute(condition);
            Path<?> path = from.get(attribute);
            Class<?> javaType = attribute.getType().getJavaType();

            Predicate currentClause;
            if (javaType.equals(String.class)) {
                currentClause = cb.like((Expression<String>) path, (String) args[i++]);
            } else if (Number.class.isAssignableFrom(javaType) || javaType.isPrimitive()) {
                currentClause = cb.equal(path, args[i++]);
            } else {
                LOGGER.warning("field " + condition + " not found, ignoring");
                continue;
            }

            if (where == null) {
                where = currentClause;
            } else {
                where = cb.and(where, currentClause);
            }
        }

        if (where != null) {
            query = query.where(where);
        }

        // pagination
        TypedQuery<?> emQuery = entityManager.createQuery(query);
        if (args != null && args.length == conditions.size() + 2
                && isInt(args[args.length - 2].getClass()) && isInt(args[args.length - 1].getClass())) {
            int first = (Integer) args[args.length - 2];
            int max = (Integer) args[args.length - 1];

            emQuery.setFirstResult(first);
            emQuery.setMaxResults(max);
        }

        return emQuery;
    }

    private boolean isInt(Class<?> aClass) {
        return Integer.TYPE.equals(aClass) || Integer.class.equals(aClass);
    }

    private List<String> parseMethodName(final String methodName) {
        List<String> parsed;
        if (CONDITIONS.containsKey(methodName)) {
            parsed = CONDITIONS.get(methodName);
        } else {
            parsed = new ArrayList<String>();

            String toParse = methodName.substring(FIND_PREFIX.length());
            if (toParse.startsWith(BY)) {
                toParse = toParse.substring(2);
                String[] columns = toParse.split(AND);
                for (String column: columns) {
                    parsed.add(StringUtils.uncapitalize(column));
                }
            }

            CONDITIONS.put(methodName, parsed);
        }
        return parsed;
    }

    @Override public String toString() {
        return "OpenEJB :: QueryProxy";
    }

    @Override public InvocationHandler getInvocationHandler() {
        return this;
    }
}
