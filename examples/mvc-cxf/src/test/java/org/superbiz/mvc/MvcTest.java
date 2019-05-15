/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.superbiz.mvc;

import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.arquillian.ArquillianCucumber;
import cucumber.runtime.arquillian.api.Features;
import org.apache.ziplock.maven.Mvn;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ArquillianCucumber.class)
@Features("org/superbiz/mvc/mvc.feature")
@CucumberOptions(strict = true)
@RunAsClient
public class MvcTest {

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

    @Given("^I navigate to the new user screen$")
    public void navigateToNewUserScreen() {
        webDriver.get(this.base.toExternalForm() + "app/home");
        webDriver.findElement(By.linkText("Peoples")).click();
        webDriver.findElement(By.linkText("Add Registres")).click();
    }

    @When("^I submit a new user with name: (.*?) age: (\\d+) country: (.*?) state: (.*?) server: (.*?) description: (.*?)$")
    public void submitNewUser(final String name, final Integer age, final String country, final String state, final String server, final String description) {
        webDriver.findElement(By.id("name")).click();
        webDriver.findElement(By.id("name")).clear();
        webDriver.findElement(By.id("name")).sendKeys(name);
        webDriver.findElement(By.id("age")).clear();
        webDriver.findElement(By.id("age")).sendKeys(age.toString());
        webDriver.findElement(By.id("state")).clear();
        webDriver.findElement(By.id("state")).sendKeys(state);
        webDriver.findElement(By.xpath("//input[@name='server'][@value='" + server + "']")).click();
        webDriver.findElement(By.id("country")).click();
        new Select(webDriver.findElement(By.id("country"))).selectByVisibleText(country);
        webDriver.findElement(By.id("description")).click();
        webDriver.findElement(By.id("description")).clear();
        webDriver.findElement(By.id("description")).sendKeys(description);
        webDriver.findElement(By.xpath("(.//*[normalize-space(text()) and normalize-space(.)='Description:'])[1]/following::button[1]")).click();
    }

    @Then("^The user should be added: (.*?) age: (\\d+) country: (.*?) state: (.*?) server: (.*?) description: (.*?)$")
    public void userShouldBeAdded(final String name, final Integer age, final String country, final String state, final String server, final String description) {
        final WebElement element = webDriver.findElement(By.id("success-alert"));
        assertNotNull(element);
        assertTrue(element.getText().contains("Joe Bloggs was successfully registered"));

        assertEquals(name, webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[1]")).getText());
        assertEquals(age.toString(), webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[2]")).getText());
        assertEquals(country, webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[3]")).getText());
        assertEquals(state, webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[4]")).getText());
        assertEquals(server, webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[5]")).getText());
        assertEquals(description, webDriver.findElement(By.xpath("//*[@id=\"tableData\"]/tbody/tr[1]/td[6]")).getText());
    }
}