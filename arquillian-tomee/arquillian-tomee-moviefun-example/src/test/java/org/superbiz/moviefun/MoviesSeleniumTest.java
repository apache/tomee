/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.moviefun;

import java.io.File;
import java.net.URL;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.base.asset.ClassLoaderAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.superbiz.moviefun.setup.ExampleDataProducer;
import org.superbiz.moviefun.setup.Examples;
import org.superbiz.moviefun.setup.Setup;
import org.superbiz.moviefun.util.JsfUtil;
import org.superbiz.moviefun.util.PaginationHelper;

import com.thoughtworks.selenium.DefaultSelenium;

@RunWith(Arquillian.class)
public class MoviesSeleniumTest {
	
	@ArquillianResource
	private URL deploymentUrl;
	
	@Drone
	private DefaultSelenium driver;

	@Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "moviefun.war")
        		.addClasses(ActionServlet.class, SetupServlet.class, Movie.class, MovieController.class, Movies.class, MoviesImpl.class, MoviesRemote.class, JsfUtil.class, PaginationHelper.class, ExampleDataProducer.class, Examples.class, Setup.class)
        		.addAsResource(new ClassLoaderAsset("META-INF/ejb-jar.xml") , "META-INF/ejb-jar.xml")
        		.addAsResource(new ClassLoaderAsset("META-INF/persistence.xml") , "META-INF/persistence.xml")
        		.addAsLibraries(new File("target/test-libs/commons-beanutils.jar"),
        				new File("target/test-libs/commons-codec.jar"),
        				new File("target/test-libs/commons-collections.jar"),
        				new File("target/test-libs/commons-digester.jar"),
        				new File("target/test-libs/commons-logging.jar"),
        				new File("target/test-libs/jstl.jar"),
        				new File("target/test-libs/log4j.jar"),
        				new File("target/test-libs/standard.jar"));
        
        addResources("src/main/webapp", "", archive);
        System.out.println(archive.toString(true));
		return archive;
    }
	
    private static void addResources(String source, String target, WebArchive archive) {
		File sourceFile = new File(source);
		if (! sourceFile.exists()) return;
		if (sourceFile.isFile()) {
			archive.add(new FileAsset(sourceFile), target);
		}
		
		if (sourceFile.isDirectory()) {
			for (File file : sourceFile.listFiles()) {
				if (file.getName().startsWith(".")) continue;
				addResources(source + File.separator + file.getName(), target + File.separator + file.getName(), archive);
			}
		}
	}

	@Test
    public void testShouldMakeSureWebappIsWorking() throws Exception {
		driver.open(deploymentUrl.toString());
		driver.click("link=Setup");
		driver.waitForPageToLoad("30000");
		Assert.assertTrue(driver.isTextPresent("Seeded Database with the Following movies"));
		Assert.assertTrue(driver.isTextPresent("Wedding Crashers"));
		Assert.assertTrue(driver.isTextPresent("David Dobkin"));
		Assert.assertTrue(driver.isTextPresent("Comedy"));
		Assert.assertTrue(driver.isTextPresent("Starsky & Hutch"));
		Assert.assertTrue(driver.isTextPresent("Shanghai Knights"));
		Assert.assertTrue(driver.isTextPresent("I-Spy"));
		Assert.assertTrue(driver.isTextPresent("The Royal Tenenbaums"));
		Assert.assertTrue(driver.isTextPresent("Zoolander"));
		Assert.assertTrue(driver.isTextPresent("Shanghai Noon"));
		driver.click("link=Go to main app");
		driver.waitForPageToLoad("30000");
		driver.type("name=title", "Bad Boys");
		driver.type("name=director", "Michael Bay");
		driver.type("name=genre", "Action");
		driver.type("name=rating", "9");
		driver.type("name=year", "1995");
		driver.click("//input[@name='action' and @value='Add']");
		driver.waitForPageToLoad("30000");
		driver.click("css=input[name=\"action\"]");
		driver.waitForPageToLoad("30000");
		Assert.assertTrue(driver.isTextPresent("Bad Boys"));
		driver.select("name=action", "label=Genre");
		driver.type("name=key", "Comedy");
		driver.click("css=input[type=\"submit\"]");
		driver.waitForPageToLoad("30000");
		Assert.assertTrue(driver.isTextPresent("Wedding Crashers"));
		Assert.assertTrue(driver.isTextPresent("The Royal Tenenbaums"));
		Assert.assertTrue(driver.isTextPresent("Zoolander"));
		Assert.assertTrue(driver.isTextPresent("Shanghai Noon"));
		Assert.assertTrue(driver.isTextPresent("1 - 4 of 4"));
		driver.close();
    }

}
