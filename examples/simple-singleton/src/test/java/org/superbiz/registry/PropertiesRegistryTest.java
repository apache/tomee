package org.superbiz.registry;

import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.Test;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

public class PropertiesRegistryTest {

    private final static EJBContainer ejbContainer = EJBContainer.createEJBContainer();

    @Test
    public void oneInstancePerMultipleReferences() throws Exception {

        Context context = ejbContainer.getContext();

        PropertyRegistry one = (PropertyRegistry) context.lookup("java:global/simple-singleton/PropertyRegistry");
        PropertyRegistry two = (PropertyRegistry) context.lookup("java:global/simple-singleton/PropertyRegistry");

        one.setProperty("url", "http://superbiz.org");
        String url = two.getProperty("url");
        Assert.assertSame("http://superbiz.org", url);

        two.removeProperty("url");
        url = one.getProperty("url");
        Assert.assertNull(url);

        two.setProperty("version", "1.0.5");
        String version = one.getProperty("version");
        Assert.assertSame("1.0.5", version);

        one.removeProperty("version");
        version = two.getProperty("version");
        Assert.assertNull(version);
    }

    @AfterClass
    public static void closeEjbContainer() {
        ejbContainer.close();
    }
}