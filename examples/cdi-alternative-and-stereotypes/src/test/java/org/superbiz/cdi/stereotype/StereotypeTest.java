package org.superbiz.cdi.stereotype;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

public class StereotypeTest {
    private static EJBContainer container;
    private static Journey journey;

    @BeforeClass
    public static void start() throws NamingException {
        container = EJBContainer.createEJBContainer();
        journey = (Journey) container.getContext().lookup("java:global/cdi-alternative-and-stereotypes/Journey");
    }

    @AfterClass
    public static void shutdown() {
        if (container != null) {
            container.close();
        }
    }

    @Test
    public void assertVehicleInjected() {
        assertEquals("the fatest", journey.vehicle());
    }

    @Test
    public void assertMockOverrideWorks() {
        assertEquals("simply the best", journey.category());
    }
}
