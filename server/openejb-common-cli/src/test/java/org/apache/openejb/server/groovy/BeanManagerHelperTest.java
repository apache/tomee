package org.apache.openejb.server.groovy;

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.apache.openejb.server.cli.OpenEJBScripter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Named;

import static org.junit.Assert.assertEquals;

@RunWith(ApplicationComposer.class)
public class BeanManagerHelperTest {
    private static OpenEJBScripter.BeanManagerHelper helper = new OpenEJBScripter.BeanManagerHelper();

    @Module
    public Beans ejbJar() {
        final Beans beans = new Beans();
        beans.addManagedClass(Pojo.class);
        return beans;
    }

    @Before
    public void resetID() {
        Pojo.ID = 5;
    }

    @Test
    public void getInstanceFromClass() {
        for (int i = 1; i < 5; i++) {
            final Pojo pojo = (Pojo) helper.beanFromClass("BeanManagerHelperTest", Pojo.class.getName());
            assertEquals(5 + i, pojo.id);
        }
    }

    @Test
    public void getInstanceFromName() {
        for (int i = 1; i < 5; i++) {
            final Pojo pojo = (Pojo) helper.beanFromName("BeanManagerHelperTest", "pojo");
            assertEquals(5 + i, pojo.id);
        }
    }

    @Named
    public static class Pojo {
        public static int ID;
        public int id = ++ID;
    }
}
