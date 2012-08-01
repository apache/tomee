package org.apache.openejb.core.stateless;

import org.apache.openejb.jee.Empty;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.junit.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import static org.junit.Assert.assertNotNull;

@RunWith(ApplicationComposer.class)
public class ThisInConstructorTest {
    @EJB
    private AStatelessWithAConstructor stateless;

    @Test
    public void validThis() {
        assertNotNull(stateless);
        assertNotNull(stateless.getThat());
    }

    @Module
    public StatelessBean bean() {
        final StatelessBean bean = new StatelessBean(AStatelessWithAConstructor.class);
        bean.setLocalBean(new Empty());
        return bean;
    }

    @Stateless
    public static class AStatelessWithAConstructor {
        private AStatelessWithAConstructor that;

        public AStatelessWithAConstructor() {
            that = this;
            System.out.println(this);
        }

        public AStatelessWithAConstructor getThat() {
            return that;
        }
    }
}
