package org.superbiz.dao;

import java.util.List;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.superbiz.domain.Person;

@Singleton
@Lock(LockType.READ)
public class PersonDAO {
    @PersistenceContext
    private EntityManager em;

    public Person save(final String name) {
        final Person person = new Person();
        person.setName(name);
        em.persist(person);
        return person;
    }

    public List<Person> findAll() {
        return em.createNamedQuery("Person.findAll", Person.class).getResultList();
    }
}
