/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.ManagedBean;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.jee.SessionType;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatefulBean;
import org.apache.openejb.jee.StatelessBean;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.Files;
import org.apache.openejb.loader.SystemInstance;
import org.junit.Assert;
import org.junit.Test;

public class OutputGeneratedDescriptorsTest {

	@Test
	public void testOutputDescriptor() throws Exception {
		
		final OutputGeneratedDescriptors dynamicDeployer = new OutputGeneratedDescriptors();
		
		final EjbJar ejbJar = new EjbJar();
		
		final StatelessBean redBean = new StatelessBean();
		redBean.setEjbClass("com.foo.Red");
		redBean.setEjbName("Red");
		redBean.setRemote("com.foo.Color");
		
		final ManagedBean orangeBean = new ManagedBean("Orange", "com.foo.Orange");
		
		final StatefulBean yellowBean = new StatefulBean();
		yellowBean.setEjbClass("com.foo.Yellow");
		yellowBean.setEjbName("Yellow");
		yellowBean.setRemote("com.foo.Color");

		final SingletonBean greenBean = new SingletonBean();
		greenBean.setEjbClass("com.foo.Green");
		greenBean.setEjbName("Green");
		greenBean.setRemote("com.foo.Color");
		
		ejbJar.addEnterpriseBean(redBean);
		ejbJar.addEnterpriseBean(orangeBean);
		ejbJar.addEnterpriseBean(yellowBean);
		ejbJar.addEnterpriseBean(greenBean);
		
		final OpenejbJar openejbJar = new OpenejbJar();
		final EjbModule ejbModule = new EjbModule(ejbJar, openejbJar);
		final AppModule appModule = new AppModule(ejbModule);
		
		File tempFolder = File.createTempFile("tmp", "ogd");
		tempFolder.delete();
		Assert.assertTrue("unable to create temp folder", tempFolder.mkdirs());
		
		try {
			Properties properties = ejbModule.getOpenejbJar().getProperties();
			properties.setProperty(OutputGeneratedDescriptors.OUTPUT_DESCRIPTORS, "true");
			properties.setProperty(OutputGeneratedDescriptors.OUTPUT_DESCRIPTORS_FOLDER, tempFolder.getAbsolutePath());
			
			SystemInstance.get().setProperty(OutputGeneratedDescriptors.OUTPUT_DESCRIPTORS_FOLDER, tempFolder.getAbsolutePath());
			
			dynamicDeployer.deploy(appModule);
			
			boolean seenEjbJarXml = false;
			boolean seenOpenejbJarXml = false;
			
			File[] listFiles = tempFolder.listFiles();
			for (File file : listFiles) {
				if (file.getName().startsWith("ejb-jar-")) {
					seenEjbJarXml = true;
					assertEjbFileCorrect(file);
				}
				
				if (file.getName().startsWith("openejb-jar-")) {
					seenOpenejbJarXml = true;
				}
			}
			
			Assert.assertTrue("No ejb-jar.xml file produced", seenEjbJarXml);
			Assert.assertTrue("No openejb-jar.xml file produced", seenOpenejbJarXml);
		
		} finally {
			// clean up temporary folder
			Files.delete(tempFolder);
		}
	}

	private void assertEjbFileCorrect(File file) throws Exception {

        try (FileInputStream in = new FileInputStream(file)) {
            EjbJar ejbJar = (EjbJar) JaxbJavaee.unmarshalJavaee(EjbJar.class, in);

            Assert.assertEquals(4, ejbJar.getEnterpriseBeans().length);
            Assert.assertEquals("Red", ejbJar.getEnterpriseBeans()[0].getEjbName());
            Assert.assertEquals("com.foo.Red", ejbJar.getEnterpriseBeans()[0].getEjbClass());
            Assert.assertEquals("com.foo.Color", ((SessionBean) ejbJar.getEnterpriseBeans()[0]).getRemote());
            Assert.assertEquals(SessionType.STATELESS, ((SessionBean) ejbJar.getEnterpriseBeans()[0]).getSessionType());
            Assert.assertEquals("Orange", ejbJar.getEnterpriseBeans()[1].getEjbName());
            Assert.assertEquals("com.foo.Orange", ejbJar.getEnterpriseBeans()[1].getEjbClass());
            Assert.assertEquals(SessionType.MANAGED, ((SessionBean) ejbJar.getEnterpriseBeans()[1]).getSessionType());
            Assert.assertEquals("Yellow", ejbJar.getEnterpriseBeans()[2].getEjbName());
            Assert.assertEquals("com.foo.Yellow", ejbJar.getEnterpriseBeans()[2].getEjbClass());
            Assert.assertEquals("com.foo.Color", ((SessionBean) ejbJar.getEnterpriseBeans()[2]).getRemote());
            Assert.assertEquals(SessionType.STATEFUL, ((SessionBean) ejbJar.getEnterpriseBeans()[2]).getSessionType());
            Assert.assertEquals("Green", ejbJar.getEnterpriseBeans()[3].getEjbName());
            Assert.assertEquals("com.foo.Green", ejbJar.getEnterpriseBeans()[3].getEjbClass());
            Assert.assertEquals("com.foo.Color", ((SessionBean) ejbJar.getEnterpriseBeans()[3]).getRemote());
            Assert.assertEquals(SessionType.SINGLETON, ((SessionBean) ejbJar.getEnterpriseBeans()[3]).getSessionType());

        }
		
		
	}
	
}
