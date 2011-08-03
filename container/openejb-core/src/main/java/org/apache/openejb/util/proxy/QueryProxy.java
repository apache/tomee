package org.apache.openejb.util.proxy;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
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
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author rmannibucau
 */
public class QueryProxy implements InvocationHandler {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, QueryProxy.class);

    // keywords
    public static final String PERSIST_NAME = "save";
    public static final String MERGE_NAME = "update";
    public static final String REMOVE_NAME = "delete";
    public static final String FIND_PREFIX = "find";
    public static final String BY = "By";
    public static final String AND = "And";

    // cache for finders of the current instance
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

        // other cases (finders)
        if (methodName.startsWith(FIND_PREFIX)) {
            return find(method, args);
        }

        throw new IllegalArgumentException("method not yet managed");
    }

    private Object find(Method method, Object[] args) {
        final String methodName = method.getName();

        Class<?> type;
        if (RETURN_TYPES.containsKey(methodName)) {
            type = RETURN_TYPES.get(methodName);
        } else {
            type =  getGenericType(method.getGenericReturnType());
            RETURN_TYPES.put(methodName, type);
        }

        Query query = createFinderQuery(em, methodName, type, args);

        if (Collection.class.isAssignableFrom(method.getReturnType())) {
            return query.getResultList();
        }
        return query.getSingleResult();
    }

    private void remove(Object[] args, Class<?> returnType) {
        if (args != null && args.length == 1 && returnType.equals(Void.TYPE)) {
            Object entity = args[0];
            if (!em.contains(entity)) { // should not be done (that's why it is not cached) but can be easier...
                final Class<?> entityClass = entity.getClass();
                final EntityType<? extends Object> et = em.getMetamodel().entity(entity.getClass());

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

                entity = em.find(et.getJavaType(), idValue);
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
