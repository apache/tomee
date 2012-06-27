package org.superbiz.groovy

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.apache.ziplock.JarLocation
import javax.inject.Inject
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import org.jboss.shrinkwrap.api.asset.EmptyAsset
import org.jboss.shrinkwrap.api.ArchivePaths

@RunWith(Arquillian.class)
class HelloTest {
    @Inject
    private Hello hello

    @Deployment
    static WebArchive war() {
        ShrinkWrap.create(WebArchive.class)
            .addAsLibraries(JarLocation.jarLocation(GroovyObject.class))
            .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
            .addClasses(Hello.class)
    }

    @Test
    void hello() {
        assertNotNull hello
        assertEquals "hi", hello.hi()
    }
}
