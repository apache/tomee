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
package org.apache.webbeans.newtests.decorators.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.junit.Assert;
import org.junit.Test;

public class SimpleDecoratorTest extends AbstractUnitTest
{
    public static final String PACKAGE_NAME = SimpleDecoratorTest.class.getPackage().getName();

    @Test
    public void testSimpleDecorator()
    {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(LogDecorator.class);
        classes.add(MyLog.class);
        

        Collection<String> xmls = new ArrayList<String>();
        xmls.add(getXmlPath(PACKAGE_NAME, "SimpleDecoratorTest"));

        startContainer(classes, xmls);

        Set<Type> types = new LinkedHashSet<Type>();
        types.add(ILog.class);
        
        Decorator<?> decorator = getBeanManager().resolveDecorators(types, new Annotation[0]).iterator().next();
        InjectionPoint injectionPoint = decorator.getInjectionPoints().iterator().next();
        
        //Check that injection point is delegate
        Assert.assertTrue(injectionPoint.isDelegate());
        
        Bean<?> bean = getBeanManager().getBeans("org.apache.webbeans.newtests.decorators.simple.MyLog").iterator().next();
        
        ILog ilog = (ILog) getBeanManager().getReference(bean, ILog.class, getBeanManager().createCreationalContext(bean));
        ilog.log("DELEGATE TEST");
        
        //Check that decorator is called succesfully
        Assert.assertEquals("DELEGATE TEST", LogDecorator.MESSAGE);
        
        shutDownContainer();
    }
    
    /**
     * Ensure that if we have multiple beans that can resolve to the @Delegate injection point we don't report an error
     * even if one of those beans has a passivating scope.
     */
    @Test
    public void testDecorateTwoBeans(){
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(LogDecorator.class);
        classes.add(MyLog.class);
        classes.add(OtherLog.class);
        

        Collection<String> xmls = new ArrayList<String>();
        xmls.add(getXmlPath(PACKAGE_NAME, "SimpleDecoratorTest"));

        startContainer(classes, xmls);
        
        shutDownContainer();
    }
}
