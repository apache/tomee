package org.superbiz.reloadable.pu;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.varia.NullAppender;
import org.apache.openjpa.persistence.StoreCacheImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.embeddable.EJBContainer;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * @author rmannibucau
 */
public class CacheActivationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheActivationTest.class);

    private static EJBContainer container;

    @EJB
    private PersonManager mgr;

    @PersistenceUnit
    private EntityManagerFactory emf;

    @BeforeClass
    public static void start() {
        // customizing logs to print mainly sql queries
        Properties p = new Properties();
        p.put("openejb.nobanner", "false");
        p.put("log4j.rootLogger", "error, N");
        p.put("log4j.category.OpenEJB.options", "error");
        p.put("log4j.category.OpenEJB.options", "error");
        p.put("log4j.category.OpenEJB.server", "error");
        p.put("log4j.category.OpenEJB.startup", "error");
        p.put("log4j.category.OpenEJB.startup.service", "error");
        p.put("log4j.category.OpenEJB.startup.config", "error");
        p.put("log4j.category.openjpa.jdbc.SQL", "debug, C"); // we want sql queries
        p.put("log4j.category.openjpa.Enhance", "error");
        p.put("log4j.category.org.superbiz", "info, C"); // we want our own logs
        p.put("log4j.appender.N", NullAppender.class.getName());
        p.put("log4j.appender.C", ConsoleAppender.class.getName());
        p.put("log4j.appender.C.layout", SimpleLayout.class.getName());

        // finally starting the container
        container = EJBContainer.createEJBContainer(p);
    }

    @Before
    public void inject() throws NamingException {
        container.getContext().bind("inject", this);
    }

    @AfterClass
    public static void shutdown() {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void activeCacheAtRuntime() throws Exception {
        LOGGER.info("TEST, data initialization");
        final Person person = mgr.createUser("user #1");
        final long personId = person.getId();
        LOGGER.info("TEST, end of data initialization\n\n");

        assertNull(((StoreCacheImpl) emf.getCache()).getDelegate());
        LOGGER.info("TEST, doing some queries without cache");
        query(personId);
        LOGGER.info("TEST, queries without cache done\n\n");

        activateCache();

        assertNotNull(((StoreCacheImpl) emf.getCache()).getDelegate());
        LOGGER.info("TEST, doing some queries with cache");
        query(personId);
        LOGGER.info("TEST, queries with cache done\n\n");
    }

    private void activateCache() throws Exception {
        ObjectName on = new ObjectName("openejb.management:ObjectType=persistence-unit,PersistenceUnit=reloadable");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        server.invoke(on, "setProperty", new Object[] { "openjpa.DataCache", "true" }, null);
        server.invoke(on, "setProperty", new Object[] { "openjpa.RemoteCommitProvider", "sjvm" }, null);
        server.invoke(on, "setSharedCacheMode", new Object[] { "ALL" }, null);
        server.invoke(on, "reload", new Object[0], null);
    }

    private void query(long personId) {
        for (int i = 0; i < 3; i++) { // some multiple time to get if cache works or not
            Person found = mgr.search(personId);
            assertNotNull(found);
            assertEquals(personId,  found.getId());
        }
    }
}
