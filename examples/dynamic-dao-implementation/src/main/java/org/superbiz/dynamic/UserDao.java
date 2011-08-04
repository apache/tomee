package org.superbiz.dynamic;

/**
 * @author rmannibucau
 */

import javax.ejb.Stateless;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Map;

@Stateless
@PersistenceContext(name = "dynamic")
public interface UserDao {
    User findById(long id);

    Collection<User> findByName(String name);

    Collection<User> findByNameAndAge(String name, int age);

    Collection<User> findAll();

    Collection<User> findAll(int first, int max);

    Collection<User> namedQuery(String name, Map<String, ?> params, int first, int max);

    Collection<User> namedQuery(String name, int first, int max, Map<String, ?> params);

    Collection<User> namedQuery(String name, Map<String, ?> params);

    Collection<User> namedQuery(String name);

    Collection<User> query(String value, Map<String, ?> params);

    void save(User u);

    void delete(User u);

    User update(User u);
}
