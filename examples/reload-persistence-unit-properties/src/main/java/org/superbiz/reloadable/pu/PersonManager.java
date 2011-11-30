package org.superbiz.reloadable.pu;

import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author rmannibucau
 */
@Singleton
public class PersonManager {
    private static int ID = 0;

    @PersistenceContext
    private EntityManager em;

    public Person createUser(String name) {
        Person user = new Person(ID++, name);
        em.persist(user);
        return user;
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public Person search(long id) {
        return em.find(Person.class, id);
    }
}
