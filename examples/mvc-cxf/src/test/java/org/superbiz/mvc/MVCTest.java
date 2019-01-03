/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.mvc;

import org.apache.ziplock.maven.Mvn;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class MVCTest {

    @ArquillianResource
    private URL base;

    @Drone
    private WebDriver webDriver;

    @Deployment
    public static WebArchive createDeployment() {

        final Archive<?> war = Mvn.war();
        System.out.println(war.toString(true));
        return (WebArchive) war;
    }

    @Test
    @RunAsClient
    public void testAssertUserCanBeAdded() throws Exception {
        webDriver.get(this.base.toExternalForm() + "app/home");
        webDriver.findElement(By.linkText("Peoples")).click();
        webDriver.findElement(By.linkText("Add Registres")).click();
        webDriver.findElement(By.id("name")).click();
        webDriver.findElement(By.id("name")).clear();
        webDriver.findElement(By.id("name")).sendKeys("Joe Bloggs");
        webDriver.findElement(By.id("age")).clear();
        webDriver.findElement(By.id("age")).sendKeys("42");
        webDriver.findElement(By.id("state")).clear();
        webDriver.findElement(By.id("state")).sendKeys("California");
        webDriver.findElement(By.name("server")).click();
        webDriver.findElement(By.id("country")).click();
        new Select(webDriver.findElement(By.id("country"))).selectByVisibleText("United States");
        webDriver.findElement(By.id("description")).click();
        webDriver.findElement(By.id("description")).clear();
        webDriver.findElement(By.id("description")).sendKeys("This is a test");
        webDriver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Description:'])[1]/following::button[1]")).click();

        final WebElement element = webDriver.findElement(By.id("success-alert"));
        assertNotNull(element);
        assertTrue(element.getText().contains("Joe Bloggs was successfully registered"));

        assertEquals("Joe Bloggs", webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[1]")).getText());
        assertEquals("42", webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[2]")).getText());
        assertEquals("United States", webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[3]")).getText());
        assertEquals("California", webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[4]")).getText());
        assertEquals("TomEE", webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[5]")).getText());
        assertEquals("This is a test", webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[6]")).getText());
    }
}