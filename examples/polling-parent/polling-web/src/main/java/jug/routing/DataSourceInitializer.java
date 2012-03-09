package jug.routing;

import jug.domain.Subject;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

// hack for OpenJPA
// it initializes lazily datasource (buildSchema) so simply call it here
// for hibernate it works without this hack
@ApplicationScoped
public class DataSourceInitializer {
    @PersistenceContext(unitName = "client1")
    private EntityManager client1;

    @PersistenceContext(unitName = "client2")
    private EntityManager client2;

    private boolean invoked = false;

    public void init() {
        if (invoked) {
            return;
        }

        client1.find(Subject.class, 0);
        client2.find(Subject.class, 0);
        invoked = true;
    }
}
