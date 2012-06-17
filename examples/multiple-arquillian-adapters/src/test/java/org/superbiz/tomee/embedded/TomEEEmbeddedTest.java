package org.superbiz.tomee.embedded;

import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.superbiz.SomeEJB;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(TomEEEmbedded.class)
@RunWith(Arquillian.class)
public class TomEEEmbeddedTest {
    @EJB
    private SomeEJB ejb;

    @Deployment
    public static WebArchive war() { // use test name for the war otherwise arquillian ejb enricher doesn't work
        return ShrinkWrap.create(WebArchive.class, "test.war").addClass(SomeEJB.class);
    }

    @Test
    public void check() {
        assertNotNull(ejb);
        assertEquals("ejb", ejb.ok());
    }
}
