package org.superbiz.bean;

import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

@RunWith(Arquillian.class)
public class LocalBeanWithLocalTest {
    @EJB
    private LocalBeanWithLocal bean;

    @Deployment
    public static JavaArchive jar() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClass(LocalBeanWithLocal.class);
    }

    @Test
    public void checkItIsDeployed() {
        assertNotNull(bean);
        System.out.println(bean.msg()); // print it to show it is bad
        assertThat(bean.msg(), containsString("@Local"));
    }
}
