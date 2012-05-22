package org.apache.openejb.arquillian.openejb;

import javassist.util.proxy.ProxyObject;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.webbeans.proxy.JavassistProxyFactory;
import org.apache.webbeans.web.intercept.RequestScopedBeanInterceptorHandler;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class RequestScopeArquillianStandaloneTest {
    @Inject
    private ARequestBean bean;

    @Deployment
    public static JavaArchive archive() {
        return ShrinkWrap.create(JavaArchive.class, RequestScopeArquillianStandaloneTest.class.getSimpleName().concat(".jar"))
                .addClass(ARequestBean.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));
    }

    @Test
    public void checkInjections() {
        assertNotNull(bean);
        assertTrue(JavassistProxyFactory.isProxyInstance(bean));
    }

    @RequestScoped
    public static class ARequestBean {}
}
