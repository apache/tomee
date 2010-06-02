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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.core.asynch;

import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.StatelessBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing of the @Asynchronous annotation on beans.
 * 
 * @author Matthew B. Jones
 *
 */
public class AsynchTest{
	private Context context = null;
	
	@Before
	public void beforeTest() throws Exception{
		System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());
        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));
        AppModule app = new AppModule(this.getClass().getClassLoader(), "testasynch");
        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(new StatelessBean(AsynchBeanImpl.class));
        ejbJar.addEnterpriseBean(new SingletonBean(AsynchSingletonBeanImpl.class));
        app.getEjbModules().add(new EjbModule(ejbJar));

        AppInfo appInfo = config.configureApplication(app);
        assembler.createApplication(appInfo);
	}

	@After
	public void afterTest() throws Exception{
		if(this.context != null) this.context.close();
	}
	
	@Test
	public void testAsynch() throws Exception{
		InitialContext context = new InitialContext();
		String[] beans = new String[]{"AsynchBeanImplLocal", "AsynchSingletonBeanImplLocal"};
		for(String beanName : beans){
			AsynchBean bean = (AsynchBean)context.lookup(beanName);
			
			long before = System.currentTimeMillis();
			bean.executeAsynch();
			long delta = System.currentTimeMillis() - before;
			Thread.sleep(1500); // Wait for asynch execution
			Assert.assertTrue(beanName + " was never excuted", bean.wasFired());
			Assert.assertTrue(beanName + " was executed in a blocking fashion", delta < 1000);
			
			bean.reset();
			before = System.currentTimeMillis();
			Future<Boolean> future = bean.executeAsynchWithFuture();
			delta = System.currentTimeMillis() - before;
			Assert.assertTrue("The Future for " + beanName + " should not be done yet", !future.isDone());
			Thread.sleep(1500); // Wait for asynch execution
			Assert.assertTrue(beanName + " was never excuted", bean.wasFired());
			Assert.assertTrue("The Future for " + beanName + " should be done now", future.isDone());
			Assert.assertTrue(beanName + " was executed in a blocking fashion", delta < 1000);
			Assert.assertTrue(beanName + " was expected to return a value of true", future.get());
		}
	}
	
	public interface AsynchBean{
		public void reset();
		public boolean wasFired();
		public void executeAsynch();
		public Future<Boolean> executeAsynchWithFuture();
	}

	@Stateless
	public static class AsynchBeanImpl implements AsynchBean{
		private static boolean FIRED = false;
		public void reset(){ FIRED = false; }
		public boolean wasFired(){ return FIRED; }
		@Asynchronous
		public void executeAsynch(){
			FIRED = true;
			try{Thread.sleep(1000);}catch(Exception e){}
		}
		@Asynchronous
		public Future<Boolean> executeAsynchWithFuture(){
			executeAsynch();
			return new AsyncResult<Boolean>(true);
		}
	}
	
	@Singleton
	public static class AsynchSingletonBeanImpl implements AsynchBean{
		private static boolean FIRED = false;
		public void reset(){ FIRED = false; }
		public boolean wasFired(){ return FIRED; }
		@Asynchronous
		public void executeAsynch(){
			FIRED = true;
			try{Thread.sleep(1000);}catch(Exception e){}
		}
		@Asynchronous
		public Future<Boolean> executeAsynchWithFuture(){
			executeAsynch();
			return new AsyncResult<Boolean>(true);
		}
	}
}
