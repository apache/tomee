package org.apache.tomee.microprofile.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.config.BeanAppScoped;

import java.net.URL;

@RunWith(Arquillian.class)
public class MicroprofileConfigTest {

    @Deployment
    public static Archive createDeployment() {
        final WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addClass(BeanAppScoped.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        System.out.println(war.toString(true));
        return war;
    }

    @ArquillianResource
    private URL url;

    @Test
    public void should_get_config_parameter() {
        Assert.fail("Not yet implemented for " + url);
    }
}
