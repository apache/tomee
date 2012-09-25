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
package org.apache.webbeans.newtests.decorators.tests;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.AnnotationLiteral;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.decorators.multiple.Decorator1;
import org.apache.webbeans.newtests.decorators.multiple.Decorator2;
import org.apache.webbeans.newtests.decorators.multiple.IOutputProvider;
import org.apache.webbeans.newtests.decorators.multiple.OutputProvider;
import org.apache.webbeans.newtests.decorators.multiple.RequestStringBuilder;
import org.junit.Test;

public class MultipleDecoratorStackTests extends AbstractUnitTest
{

    public static final String PACKAGE_NAME = MultipleDecoratorStackTests.class.getPackage().getName();

    @Test
    public void testDecoratorStack()
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Decorator1.class);
        classes.add(Decorator2.class);
        classes.add(IOutputProvider.class);
        classes.add(OutputProvider.class);
        classes.add(RequestStringBuilder.class);

        Collection<String> xmls = new ArrayList<String>();
        xmls.add(getXmlPath(PACKAGE_NAME, "MultipleDecoratorStack"));

        startContainer(classes, xmls);

        Bean<?> bean = getBeanManager().getBeans(OutputProvider.class, new AnnotationLiteral<Default>()
        {
        }).iterator().next();
        Object instance = getBeanManager().getReference(bean, OutputProvider.class, getBeanManager().createCreationalContext(bean));

        OutputProvider outputProvider = (OutputProvider) instance;

        Assert.assertTrue(outputProvider != null);

        String result = outputProvider.getOutput();
        System.out.println(result);
        // Verify that the Decorators were called in order, and in a stack.
        Assert.assertTrue(result.equalsIgnoreCase("Decorator1\nDecorator2\nOutputProvider\n"));
        
        String hijackedStack = outputProvider.trace();
        // Verify that the a method change in Decorator2 from trace->otherMethod results in the right stack
        Assert.assertEquals("Decorator1/trace,Decorator2/trace,delegate/otherMethod", hijackedStack);
        
        
    }
}
