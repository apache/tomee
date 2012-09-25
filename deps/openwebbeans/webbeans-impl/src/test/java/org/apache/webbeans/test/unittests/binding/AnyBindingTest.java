/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.unittests.binding;


import java.lang.annotation.Annotation;
import java.util.Set;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.binding.AnyBindingComponent;
import org.apache.webbeans.test.component.binding.DefaultAnyBinding;
import org.apache.webbeans.test.component.binding.NonAnyBindingComponent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AnyBindingTest extends TestContext{

	public AnyBindingTest()
	{
		super(AnyBindingTest.class.getName());
	}
	
	@Before
	public void init()
	{
	}
	
	@Test
	public void testAny()
	{
		AbstractOwbBean<AnyBindingComponent> comp1 = defineManagedBean(AnyBindingComponent.class);
		Set<Annotation> qualifiers = comp1.getQualifiers();
		
		Assert.assertEquals(2, qualifiers.size());
		
		AbstractOwbBean<NonAnyBindingComponent> comp2 = defineManagedBean(NonAnyBindingComponent.class);
		qualifiers = comp2.getQualifiers();
		
		Assert.assertEquals(4, qualifiers.size());
		

		AbstractOwbBean<DefaultAnyBinding> comp3 = defineManagedBean(DefaultAnyBinding.class);
		qualifiers = comp3.getQualifiers();
		
		Assert.assertEquals(2, qualifiers.size());

		
	}
}
