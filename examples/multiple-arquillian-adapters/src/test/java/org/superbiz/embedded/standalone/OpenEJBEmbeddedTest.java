package org.superbiz.embedded.standalone;

import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.superbiz.SomeEJB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(Embedded.class)
@RunWith(Arquillian.class)
public class OpenEJBEmbeddedTest {
    @EJB
    private SomeEJB ejb;

    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class).addClass(SomeEJB.class);
    }

    @Test
    public void check() {
        assertNotNull(ejb);
        assertEquals("ejb", ejb.ok());
    }
}
