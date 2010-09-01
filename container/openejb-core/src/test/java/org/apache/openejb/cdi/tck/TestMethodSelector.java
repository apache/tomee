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
package org.apache.openejb.cdi.tck;

import java.util.List;

import org.jboss.testharness.impl.ConfigurationFactory;
import org.jboss.testharness.impl.packaging.IntegrationTest;
import org.testng.IMethodSelector;
import org.testng.IMethodSelectorContext;
import org.testng.ITestNGMethod;

public class TestMethodSelector implements IMethodSelector
{

   private static final long serialVersionUID = 6034298835828495024L;

   public boolean includeMethod(IMethodSelectorContext context, ITestNGMethod method, boolean isTestMethod)
   {
//      if (!ConfigurationFactory.get().isRunIntegrationTests() && method.getMethod().getDeclaringClass().isAnnotationPresent(IntegrationTest.class))
//      {
//         context.setStopped(true);
//         return false;
//      }
//      else
//      {
         return true;
//      }
   }

   public void setTestMethods(List<ITestNGMethod> testMethods)
   {

   }

}
