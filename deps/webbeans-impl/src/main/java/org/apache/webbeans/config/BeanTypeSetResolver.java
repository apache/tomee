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
package org.apache.webbeans.config;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;

import org.apache.webbeans.util.ClassUtil;

public class BeanTypeSetResolver
{
    /**Starting type*/
    private final Type beanType;
    
    /**Hierarchy of the types*/
    private Set<Type> hierarchy = new HashSet<Type>();
    
    public BeanTypeSetResolver(Type beanType)
    {
        this.beanType = beanType;
    }
    
    /**
     * Starts the type hierarchy configuration.
     */
    public void startConfiguration()
    {
        if(beanType == Object.class || (beanType instanceof Class && ((Class)beanType).isSynthetic()))
        {
            return;
        }
        else if(ClassUtil.isParametrizedType(beanType))
        {
            parametrizedClassConfiguration((ParameterizedType) beanType);
        }
        else 
        {
            if(beanType instanceof Class)
            {
                normalClassConfiguration((Class<?>) beanType);
   
            }            
        }
    }
    
    /**
     * Normal class configuration.
     */
    private void normalClassConfiguration(Class<?> beanClass)
    {
        //Bean class contains TypeVariables
        if(ClassUtil.isDefinitionContainsTypeVariables(beanClass))
        {
            //TypeVariables
            OwbParametrizedTypeImpl pt = new OwbParametrizedTypeImpl(beanClass.getDeclaringClass(),beanClass);
            TypeVariable<?>[] tvs = beanClass.getTypeParameters();
            for(TypeVariable<?> tv : tvs)
            {
                pt.addTypeArgument(tv);
            }

            hierarchy.add(pt);
        }
        //Normal Bean class
        else
        {
            //Add this normal class
            hierarchy.add(beanClass);
        }
        
        
        //Look for super class
        Type superClass = beanClass.getGenericSuperclass();
        
        if(superClass != null)
        {
            if(superClass != Object.class)
            {
                BeanTypeSetResolver superResolver = new BeanTypeSetResolver(superClass);
                superResolver.startConfiguration();
                hierarchy.addAll(superResolver.getHierarchy());
            }            
        }
        
        //Look for interfaces
        Type[] interfaces = beanClass.getGenericInterfaces();
        for(Type interfaceType : interfaces)
        {
            BeanTypeSetResolver superResolver = new BeanTypeSetResolver(interfaceType);
            superResolver.startConfiguration();
            hierarchy.addAll(superResolver.getHierarchy());
        }
    }
    
    /**
     * Bean class is a parametrized.
     * @param parametrizedClass parametrized class
     */
    private void parametrizedClassConfiguration(ParameterizedType parametrizedClass)
    {
        //Add this parametrized type
        hierarchy.add(parametrizedClass);
        
        //Get raw type
        Class<?> rawType = (Class<?>)parametrizedClass.getRawType();
        
        //Look for super class
        Type superClassGeneric = rawType.getGenericSuperclass();
        
        if(superClassGeneric != null)
        {
            boolean configured = false;

            //Super class is a parametrized
            if(ClassUtil.isParametrizedType(superClassGeneric))
            {
                //Resolve Type Arguments
                hierarchy.add(resolveTypeArguments(parametrizedClass, (ParameterizedType) superClassGeneric));
                configured = true;
            }
            //Super class is a normal
            else
            {
                hierarchy.add(superClassGeneric);
            }
            
            //Get super class hiearchy
            BeanTypeSetResolver superResolver = new BeanTypeSetResolver(superClassGeneric);
            superResolver.startConfiguration();
            hierarchy.addAll(superResolver.getHierarchy());
            if(configured)
            {
                hierarchy.remove(superClassGeneric);
            }
        }
                
        //Interfaces here
        Type[] superInterfacesGeneric = rawType.getGenericInterfaces();
        
        //Iterate over interfaces
        for(Type superInterfaceGeneric : superInterfacesGeneric)
        {
            boolean configured = false;
            
            if(ClassUtil.isParametrizedType(superInterfaceGeneric))
            {
                //Resolve Type Arguments
                hierarchy.add(resolveTypeArguments(parametrizedClass, (ParameterizedType) superInterfaceGeneric));
                configured = true;
            }
            else
            {
                hierarchy.add(superInterfaceGeneric);
            }   
            
            //Interface hierachy
            BeanTypeSetResolver superResolver = new BeanTypeSetResolver(superInterfaceGeneric);
            superResolver.startConfiguration();
            hierarchy.addAll(superResolver.getHierarchy());
            if(configured)
            {
                hierarchy.remove(superInterfaceGeneric);
            }            
        }        
    }
    
    /**
     * Configures type variables with actual parameters.
     * @param beanClass parametrized bean class
     * @param superClass parametrized super class
     * @return parametrized type
     */
    private OwbParametrizedTypeImpl resolveTypeArguments(ParameterizedType beanClass, ParameterizedType superClass)
    {
        OwbParametrizedTypeImpl ptImpl = new OwbParametrizedTypeImpl(superClass.getOwnerType(),superClass.getRawType());
        
        //Bean class type parameters
        TypeVariable<?>[] beanClassTypeVariables = ClassUtil.getClass(beanClass).getTypeParameters();
        //Bean class actual type arguments
        Type[] beanClassArgs = beanClass.getActualTypeArguments();

                                                                           
            //Super class type arguments
            Type[] superClassArgs = superClass.getActualTypeArguments();
            for(Type superClassArg : superClassArgs)
            {
                boolean found = false;
                for(int i = 0 ; i< beanClassTypeVariables.length ; i++)
                {
                    TypeVariable<?> beanClassTypeVariable  = beanClassTypeVariables[i];
                    Type beanClassArg = beanClassArgs[i];
                
                    //If TypeVariable replace with actual type
                    if(ClassUtil.isTypeVariable(superClassArg) && superClassArg.equals(beanClassTypeVariable))
                    {
                        ptImpl.addTypeArgument(beanClassArg);
                        found = true;
                        break;
                    }
                }

                if(!found)
                {
                    if(ClassUtil.isParametrizedType(superClassArg))
                    {
                        ptImpl.addTypeArgument(resolveTypeArguments(beanClass, (ParameterizedType)superClassArg));
                    }
                    else
                    {
                        ptImpl.addTypeArgument(superClassArg);
                    }                    
                }
            }
        
        return ptImpl;
    }
    
    /**
     * Gets hierarchy.
     * @return hierarchy
     */
    public Set<Type> getHierarchy()
    {
        return hierarchy;
    }
}
