/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.core.MediaType;
import java.io.File;

@RunWith(Arquillian.class)
public class RedeploymentTest {

    public RedeploymentTest() {
    }

    @Deployment(name = "webapp1")
    public static Archive<?> webapp1() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("../WebApp1/target/WebApp1-1.1.0-SNAPSHOT.war"));
    }

    @Deployment(name = "webapp2")
    public static Archive<?> webapp2() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("../WebApp2/target/WebApp2-1.1.0-SNAPSHOT.war"));
    }

    @Test
    public void valid() throws Exception {
        final String port = System.getProperty("server.http.port");
        System.out.println("Running test on port: " + port);
        System.out.println("===========================================");
        System.out.println(WebClient.create("http://localhost:" + port + "/WebApp1/test/")
            .type(MediaType.APPLICATION_JSON_TYPE).post("valid").getStatus());
        System.out.println("-------------------------------------------");
        System.out.println(WebClient.create("http://localhost:" + port + "/WebApp2/test/")
            .type(MediaType.APPLICATION_JSON_TYPE).post("valid").getStatus());
        System.out.println("===========================================");
        System.out.println("");
    }

}
