package org.superbiz.tomee.remote;

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

@Category(TomEERemote.class)
@RunWith(Arquillian.class)
public class TomEERemoteTest {
    @EJB
    private SomeEJB ejb;

    @Deployment
    public static WebArchive war() {
        // use test name for the war otherwise arquillian ejb enricher doesn't work
        // don't forget the category otherwise it will fail since the runner parse annotations
        // in embedded mode it is not so important
        return ShrinkWrap.create(WebArchive.class, "test.war").addClasses(SomeEJB.class, TomEERemote.class);
    }

    @Test
    public void check() {
        assertNotNull(ejb);
        assertEquals("ejb", ejb.ok());
    }
}
