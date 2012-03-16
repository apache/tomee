package org.apache.openejb.assembler.classic;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Configuration;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class ImportSqlScriptTest {
    @EJB
    private Persister persister;

    @Configuration
    public Properties config() {
        final Properties p = new Properties();
        p.put("ImportSqlScriptTest", "new://Resource?type=DataSource");
        p.put("ImportSqlScriptTest.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("ImportSqlScriptTest.JdbcUrl", "jdbc:hsqldb:mem:bval");
        return p;
    }

    @Module
    public SingletonBean app() throws Exception {
        final SingletonBean bean = new SingletonBean(Persister.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Module public Persistence persistence() {
        final PersistenceUnit unit = new PersistenceUnit("ImportSqlScriptTest");
        unit.addClass(Something.class);
        unit.setProperty("openjpa.RuntimeUnenhancedClasses", "supported");
        unit.setProperty("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        unit.setProperty("openjpa.Log", "DefaultLevel=WARN, Runtime=INFO, Tool=INFO, SQL=TRACE");
        unit.setExcludeUnlistedClasses(true);

        final Persistence persistence = new Persistence(unit);
        persistence.setVersion("2.0");
        return persistence;
    }

    @LocalBean
    @Stateless
    public static class Persister {
        @PersistenceContext
        private EntityManager em;

        public int count() {
            return ((Number) em.createQuery("select count(s) from ImportSqlScriptTest$Something s").getSingleResult()).intValue();
        }
    }

    @Entity
    public static class Something {
        @Id
        private long id;
    }

    @Test
    public void checkImportData()  {
        assertEquals(3, persister.count());
    }
}
