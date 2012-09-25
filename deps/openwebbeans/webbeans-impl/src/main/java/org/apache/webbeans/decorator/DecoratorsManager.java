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
package org.apache.webbeans.decorator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.decorator.Decorator;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.BeanManagerImpl;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.util.Asserts;

public class DecoratorsManager
{
    private List<Class<?>> enabledDecorators = new CopyOnWriteArrayList<Class<?>>();
    private final BeanManagerImpl manager;

    public DecoratorsManager(WebBeansContext webBeansContext)
    {
        manager = webBeansContext.getBeanManagerImpl();
    }

    public void addNewDecorator(Class<?> decoratorClazz)
    {
        Asserts.assertNotNull(decoratorClazz, "decoratorClazz parameter can not be emtpy");
        if (!enabledDecorators.contains(decoratorClazz))
        {
            enabledDecorators.add(decoratorClazz);
        }                
    }

    public int compare(Class<?> src, Class<?> target)
    {
        Asserts.assertNotNull(src, "src parameter can not be  null");
        Asserts.assertNotNull(target, "target parameter can not be null");

        int srcIndex = enabledDecorators.indexOf(src);
        int targetIndex = enabledDecorators.indexOf(target);

        if (srcIndex == -1 || targetIndex == -1)
        {
            throw new IllegalArgumentException("One of the compare class of the list : [" + src.getName() + "," + target.getName() + "]"
                                               + " is not contained in the enabled decorators list!");
        }

        if (srcIndex == targetIndex)
        {
            return 0;
        }
        else if (srcIndex < targetIndex)
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }

    public boolean isDecoratorEnabled(Class<?> decoratorClazz)
    {
        Asserts.nullCheckForClass(decoratorClazz, "decoratorClazz can not be null");

        return enabledDecorators.contains(decoratorClazz);
    }
    
    public void validateDecoratorClasses()
    {
        for(Class<?> decoratorClazz : enabledDecorators)
        {
            //Validate decorator classes
            if(!decoratorClazz.isAnnotationPresent(Decorator.class) && !manager.containsCustomDecoratorClass(decoratorClazz))
            {
                throw new WebBeansConfigurationException("Given class : " + decoratorClazz + " is not a decorator class");
            }   
        }                
    }

}
