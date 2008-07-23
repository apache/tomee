/**
 *
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
package org.apache.openejb.core.stateless;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.ProxyFactoryInfo;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.core.ivm.naming.InitContextFactory;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.StatelessBean;

/**
 * @version $Revision: 581127 $ $Date: 2007-10-02 07:43:16 +0530 (Tue, 02 Oct 2007) $
 */
public class StatelessInstanceManagerPoolingTest extends TestCase {

    public void testStatelessBeanPooling() throws Exception {

        InitialContext ctx = new InitialContext();            
        Object object = ctx.lookup("CounterBeanLocal");
        assertTrue("instanceof counter", object instanceof Counter);

        final Counter counter = (Counter) object;
        // Do a business method...
        Runnable r = new Runnable(){
        	public void run(){
        		counter.waitFor(10);        	
        	}
        };
        
        // How much ever the no of client invocations the count should be 10 as only 10 instances will be created.
        for(int i=0;i<=30;i++){
        	Thread t = new Thread(r);
        	t.start();
        	if(i==30) t.join();
        }

        assertEquals(10,CounterBean.counter);

    }
    
    public void testStatelessBeanTimeout() throws Exception {
        InitialContext ctx = new InitialContext();            
        Object object = ctx.lookup("CounterBeanLocal");
        assertTrue("instanceof counter", object instanceof Counter);

        final Counter counter = (Counter) object;
        // Do a business method...
        Runnable r = new Runnable(){
        	public void run(){
        		try{
        		    counter.waitFor(30);
        		    assertFalse(true);
        		}catch (Exception ex){
        			ex.printStackTrace();
        			assertEquals("An invocation of the Stateless Session Bean CounterBean has timed-out",ex.getMessage());
        		}
        	}
        };
        
        // How much ever the no of client invocations the count should be 10 as only 10 instances will be created.
        for(int i=0;i<=30;i++){
        	Thread t = new Thread(r);
        	t.start();        	
        }

        
    	
    }

    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, InitContextFactory.class.getName());

        ConfigurationFactory config = new ConfigurationFactory();
        Assembler assembler = new Assembler();

        assembler.createProxyFactory(config.configureService(ProxyFactoryInfo.class));
        assembler.createTransactionManager(config.configureService(TransactionServiceInfo.class));
        assembler.createSecurityService(config.configureService(SecurityServiceInfo.class));

        // containers
        StatelessSessionContainerInfo statelessContainerInfo = config.configureService(StatelessSessionContainerInfo.class);
        statelessContainerInfo.properties.setProperty("TimeOut", "100");
        statelessContainerInfo.properties.setProperty("PoolSize", "10");
        statelessContainerInfo.properties.setProperty("StrictPooling", "true");
        assembler.createContainer(statelessContainerInfo);

        // Setup the descriptor information

        StatelessBean bean = new StatelessBean(CounterBean.class);
        bean.addBusinessLocal(Counter.class.getName());
        bean.addBusinessRemote(RemoteCounter.class.getName());
        bean.addPostConstruct("init");
        bean.addPreDestroy("destroy");

        EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        assembler.createApplication(config.configureApplication(ejbJar));

    }

 
    public static interface Counter {
        int count();
        void waitFor(int i);
    }
    
    @Remote
    public static interface RemoteCounter extends Counter {

    }

    public static enum Lifecycle {
        CONSTRUCTOR, INJECTION, POST_CONSTRUCT, BUSINESS_METHOD, PRE_DESTROY
    }

    @Stateless
    public static class CounterBean implements Counter, RemoteCounter {

        public static int counter = 0;

        public CounterBean() {
        	counter++;
        }
        
        public int count(){
        	return counter;
        }

        public void waitFor(int i){
        	try {
				Thread.sleep(i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        public void init(){
        	
        }
        
        public void destroy(){
        	
        }
    }
}
