package org.superbiz.rest.dao;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.superbiz.rest.model.User;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import static junit.framework.Assert.assertNotNull;

/**
 * @author rmannibucau
 */
public class UserDaoTest {
    private static EJBContainer container;

    @BeforeClass public static void start() {
        container = EJBContainer.createEJBContainer();
    }

    @AfterClass public static void stop() {
        if (container != null) {
            container.close();
        }
    }

    @Test public void create() throws NamingException {
        UserDAO dao = (UserDAO) container.getContext().lookup("java:global/rest-example/UserDAO");
        User user = dao.create("foo", "dummy", "foo@bar.org");
        assertNotNull(dao.find(user.getId()));
    }
}
