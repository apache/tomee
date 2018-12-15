package org.superbiz.mvc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.nio.file.Paths;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@RunWith(Arquillian.class)
public class MVCTest {

    @ArquillianResource
    private URL base;

    @Drone
    private WebDriver webDriver;

    @Deployment(testable = true)
    public static WebArchive createDeployment() {
        final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(new FileAsset(Paths.get("src/main/webapp/WEB-INF/").resolve("views/hello.jsp").toFile()), "views/" + "hello.jsp");
                

        System.out.println(webArchive.toString(true));

        return webArchive;
    }
    
   

    @Test
    @RunAsClient
    public void test() {
        webDriver.get(base + "app/hello?name=TomEE");
        System.out.println(webDriver.getCurrentUrl());
        WebElement h1 = webDriver.findElement(By.tagName("h1"));
        System.out.println(h1.getText());
        assertNotNull(h1);
        assertTrue(h1.getText().contains("Welcome TomEE !"));
    }
}