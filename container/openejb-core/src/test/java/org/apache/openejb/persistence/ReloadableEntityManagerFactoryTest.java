package org.apache.openejb.persistence;

import org.apache.openejb.assembler.classic.ReloadableEntityManagerFactory;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

/**
 * @author rmannibucau
 */
@RunWith(ApplicationComposer.class)
public class ReloadableEntityManagerFactoryTest {
    @javax.persistence.PersistenceUnit
    private EntityManagerFactory emf;

    @Module
    public Persistence persistence() throws Exception {
        PersistenceUnit unit = new PersistenceUnit("foo-unit");
        unit.addClass(MyEntity.class);
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.getProperties().setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.getProperties().setProperty("openjpa.DatCache", "false");
        unit.setExcludeUnlistedClasses(true);

        Persistence persistence = new org.apache.openejb.jee.jpa.unit.Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("ReloadableEntityManagerFactoryTest", "new://Resource?type=DataSource");
        p.put("ReloadableEntityManagerFactoryTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("ReloadableEntityManagerFactoryTest.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Test public void reload() {
        final ReloadableEntityManagerFactory remft = (ReloadableEntityManagerFactory) emf;
        final EntityManagerFactory originalEmf = remft.getDelegate();
        assertEquals("false", emf.getProperties().get("openjpa.DataCache"));
        select();

        remft.setProperty("openjpa.DataCache", "true(Types=" + MyEntity.class.getName() + ")");
        remft.reload();
        select();
        assertEquals("true(Types=" + MyEntity.class.getName() + ")", emf.getProperties().get("openjpa.DataCache"));

        final EntityManagerFactory reloadedEmf = remft.getDelegate();
        assertNotSame(originalEmf, reloadedEmf);
    }

    private void select() {
        emf.createEntityManager()
            .createQuery("select m from ReloadableEntityManagerFactoryTest$MyEntity m")
            .getResultList();
    }

    @Entity
    public static class MyEntity {
        @Id @GeneratedValue
        private long id;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }
}
