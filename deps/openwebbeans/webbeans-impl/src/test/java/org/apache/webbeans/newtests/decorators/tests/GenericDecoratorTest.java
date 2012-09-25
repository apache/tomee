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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.util.TypeLiteral;

import junit.framework.Assert;

import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.decorators.common.Cow;
import org.apache.webbeans.newtests.decorators.common.Garphly;
import org.apache.webbeans.newtests.decorators.common.GarphlyDecorator;
import org.apache.webbeans.newtests.decorators.generic.DecoratedBean;
import org.apache.webbeans.newtests.decorators.generic.GenericInterface;
import org.apache.webbeans.newtests.decorators.generic.SampleDecorator;
import org.junit.Test;

public class GenericDecoratorTest extends AbstractUnitTest
{
    public static final String PACKAGE_NAME = GenericDecoratorTest.class.getPackage().getName();
    
    
    @Test
    public void testGenericDecorator()
    {
        
        TypeLiteral<Garphly<Cow>> GARPHLY_LITERAL = new TypeLiteral<Garphly<Cow>>()
        {
        };

        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(Garphly.class);
        classes.add(GarphlyDecorator.class);
        
        Collection<String> xmls = new ArrayList<String>();
        xmls.add(getXmlPath(PACKAGE_NAME, "GenericDecoratorTest"));
        
        startContainer(classes,xmls);
        
        Set<Type> types = new HashSet<Type>();
        types.add(GARPHLY_LITERAL.getType());
        List<Decorator<?>> decorators = getBeanManager().resolveDecorators(types, new Annotation[]{});
        
        Assert.assertTrue(decorators.size() > 0);
        
        shutDownContainer();
    }

    //X TODO currently broken @Test
    public void injection() throws Exception {
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(DecoratedBean.class);
        classes.add(GenericInterface.class);
        classes.add(SampleDecorator.class);

        Collection<String> xmls = new ArrayList<String>();
        xmls.add(getXmlPath(PACKAGE_NAME, "GenericDecoratorTest"));

        startContainer(classes, xmls);

        DecoratedBean decoratedBean = (DecoratedBean) getInstance(DecoratedBean.class);
        Assert.assertTrue(decoratedBean.isDecoratorCalled());
    }

}
