package org.superbiz.persistence;

import java.util.Optional;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;
import org.superbiz.model.Person;

@Repository
public interface PersonRepository extends EntityRepository<Person, Long> {

    Optional<Person> findById(Long id);
}
