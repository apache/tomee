package org.apache.openejb.util.proxy;

import org.apache.commons.lang.StringUtils;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rmannibucau
 */
public class QueryProxy implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, QueryProxy.class);

    // keywords
    public static final String FIND_PREFIX = "find";
    public static final String BY = "By";
    public static final String AND = "And";

    // cache for current instance
    private final Map<String, Class<?>> RETURN_TYPES = new ConcurrentHashMap<String, Class<?>>();
    private final Map<String, List<String>> CONDITIONS = new ConcurrentHashMap<String, List<String>>();

    private EntityManager em;

    public QueryProxy(EntityManager entityManager) {
        em = entityManager;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }

        if (!method.getName().startsWith(FIND_PREFIX)) {
            throw new IllegalArgumentException("finder should start with find");
        }

        final String methodName = method.getName();
        Class<?> type;
        if (RETURN_TYPES.containsKey(methodName)) {
            type = RETURN_TYPES.get(methodName);
        } else {
            type =  getGenericType(method.getGenericReturnType());
        }
        Query query = getQuery(em, methodName, type, args);
        if (Collection.class.isAssignableFrom(method.getReturnType())) {
            return query.getResultList();
        }
        return query.getSingleResult();
    }

    private Class<?> getGenericType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            if ( pt.getActualTypeArguments().length == 1) {
                return (Class<?>) pt.getActualTypeArguments()[0];
            }
        }
        return (Class<?>) type;
    }

    private <T> Query getQuery(EntityManager entityManager, String methodName, Class<T> entityType, Object[] args) {
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

        return entityManager.createQuery(query);
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
