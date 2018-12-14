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
package org.apache.openejb.assembler.classic;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.ModuleContext;
import org.apache.openejb.SystemException;
import org.apache.openejb.core.ivm.naming.IvmContext;
import org.apache.openejb.loader.SystemInstance;

import junit.framework.TestCase;

public class AppNamingReadOnlyTest extends TestCase {
	
	public void testReadOnlyAppNamingContext() throws SystemException, URISyntaxException {
    	
        String originalValue = System.getProperty(Assembler.FORCE_READ_ONLY_APP_NAMING);
        System.setProperty(Assembler.FORCE_READ_ONLY_APP_NAMING, Boolean.TRUE.toString());
        try {
        	List<BeanContext> mockBeanContextsList = getMockBeanContextsList();
        	
        	Assembler assembler = new Assembler();
        	assembler.setAppNamingContextReadOnly(mockBeanContextsList);
        	
        	Context beanNamingContext = mockBeanContextsList.get(0).getJndiContext();
        	//may return null or throw exception depending on openejb.jndiExceptionOnFailedWrite value;
        	//this test is not intended to test read-only behavior (null/exception); it should check whether naming context is marked as read only 
        	try {
				Context subContext = beanNamingContext.createSubcontext("sub");
				assertNull(subContext);
			} catch (OperationNotSupportedException e) {
				//ok
			} catch (NamingException e) {
				throw new AssertionError();
			}
        } finally {
            if(originalValue == null) {
                System.clearProperty(Assembler.FORCE_READ_ONLY_APP_NAMING);
            } else {
                System.setProperty(Assembler.FORCE_READ_ONLY_APP_NAMING, originalValue);
            }
            SystemInstance.reset();
        }
    }
    
    //check TOMEE behavior is backward compatible
    public void testAppNamingContextWritableByDefault() throws SystemException, URISyntaxException, NamingException {

    	List<BeanContext> mockBeanContextsList = getMockBeanContextsList();
    	
    	Assembler assembler = new Assembler();
    	assembler.setAppNamingContextReadOnly(mockBeanContextsList);
    	
    	Context beanNamingContext = mockBeanContextsList.get(0).getJndiContext();
		Context subContext = beanNamingContext.createSubcontext("sub");
		
		assertNotNull(subContext);
    }
    
    private List<BeanContext> getMockBeanContextsList() throws SystemException, URISyntaxException {
    	IvmContext context = new IvmContext();
    	
    	AppContext mockAppContext = new AppContext("appId", SystemInstance.get(),  this.getClass().getClassLoader(), context, context, false);
    	ModuleContext mockModuleContext =  new ModuleContext("moduleId", new URI(""), "uniqueId", mockAppContext, context, this.getClass().getClassLoader());
    	BeanContext mockBeanContext = new BeanContext("test", context, mockModuleContext, this.getClass(), this.getClass(), new HashMap<>());
    	
    	List<BeanContext> beanContextsList = new ArrayList<>();
    	beanContextsList.add(mockBeanContext);
    	
    	return beanContextsList;
    }
}
