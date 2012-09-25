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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;

public final class DecoratorResolverRules
{
    private DecoratorResolverRules()
    {
        throw new UnsupportedOperationException();
    }
    
    @SuppressWarnings("unchecked")
    public static boolean compareType(Type delegateType, Type beanType)
    {
        Asserts.assertNotNull(delegateType, "delegateType parameter is null");
        Asserts.assertNotNull(beanType, "beanType parameter is null");
        
        if(delegateType instanceof ParameterizedType && beanType instanceof Class)
        {
            return beanIsRawAndDelegateIsParametrized(delegateType, beanType);
        }
        else if (delegateType instanceof ParameterizedType && beanType instanceof ParameterizedType)
        {
            return beanIsParametrizedAndDelegateIsParametrized((ParameterizedType)delegateType, (ParameterizedType)beanType);
        }
        //Both type is class type
        else if (beanType instanceof Class && delegateType instanceof Class)
        {
            Class<?> clzBeanType = (Class<?>)beanType;
            Class<?> clzDelType = (Class<?>)delegateType;
            
            
            return clzDelType == clzBeanType;
        }


        return false;
    }
    
    @SuppressWarnings("unchecked")
    private static boolean recursiveCheckOnParametrizedTypeActualTypeArguments(Type[] delegateTypeArgs, Type[] beanTypeArgs)
    {
        Type delegateTypeArg = null;
        Type beanTypeArg = null;
        for(int i = 0; i< delegateTypeArgs.length;i++)
        {
            delegateTypeArg = delegateTypeArgs[i];
            beanTypeArg = beanTypeArgs[i];
            
            //Item -1 in 8.3.1
            if(ClassUtil.isParametrizedType(delegateTypeArg) && ClassUtil.isParametrizedType(beanTypeArg))
            {
                return beanIsParametrizedAndDelegateIsParametrized((ParameterizedType)delegateTypeArg, (ParameterizedType)beanTypeArg);
            }
            
          //Item -2 and Item-3 in 8.3.1
            else if(ClassUtil.isWildCardType(delegateTypeArg))
            {
                return  ClassUtil.checkRequiredTypeIsWildCard(beanTypeArg, delegateTypeArg);
            }

            //Item-4 in 8.3.1
            else if(ClassUtil.isTypeVariable(delegateTypeArg) && ClassUtil.isTypeVariable(beanTypeArg))
            {
                return ClassUtil.checkBeanTypeAndRequiredIsTypeVariable(delegateTypeArg, beanTypeArg);
            }      
            
            //Item-5 in 8.3.1
            else if((beanTypeArg instanceof Class) && (ClassUtil.isTypeVariable(delegateTypeArg)))
            {
                return ClassUtil.checkRequiredTypeIsTypeVariableAndBeanTypeIsClass(beanTypeArg, delegateTypeArg);
            }
            
            //Both type argument is a class
            else if((beanTypeArg instanceof Class) && (delegateTypeArg instanceof Class))
            {
                if(beanTypeArg == delegateTypeArg)
                {
                    return true;
                }
            }
            //Delegate type is actual type and bean type is type variable
            else if((delegateTypeArg instanceof Class) && (ClassUtil.isTypeVariable(beanTypeArg)))
            {
                return ClassUtil.checkRequiredTypeIsTypeVariableAndBeanTypeIsClass(delegateTypeArg, beanTypeArg);
            }
            
            
        }
        
        return false;
        
    }
    
    
    //A parameterized bean type is considered assignable to a parameterized 
    //delegate type if they have identical raw type and
    //for each parameter:see recursiveCheckOnParametrizedType(...,...);
    private static boolean beanIsParametrizedAndDelegateIsParametrized(ParameterizedType delegateType, ParameterizedType beanType)
    {
        Class<?> delegateRawType = (Class<?>) delegateType.getRawType();
        Class<?> beanRawType = (Class<?>) beanType.getRawType();

        if (delegateRawType == beanRawType)
        {
            //Delegate api type actual type arguments
            Type[] delegateTypeArgs = delegateType.getActualTypeArguments();
            
            //Bean type actual arguments
            Type[] beanTypeArgs = beanType.getActualTypeArguments();
            
            if(beanTypeArgs.length != delegateTypeArgs.length)
            {                
                return false;
            }
            else
            {
                return recursiveCheckOnParametrizedTypeActualTypeArguments(delegateTypeArgs, beanTypeArgs);
            }
        }

        return false;
        
    }
    
    @SuppressWarnings("unchecked")
    //A raw bean type is considered assignable to a parameterized delegate 
    //type if the raw types are identical and all type para-
    //meters of the delegate type are either unbounded type variables or java.lang.Object.
    private static boolean beanIsRawAndDelegateIsParametrized(Type delegateType, Type beanType)
    {
        boolean ok = true;
        ParameterizedType ptDelegate = (ParameterizedType)delegateType;
        Class<?> clazzDelegateType = (Class<?>)ptDelegate.getRawType();
        Class<?> clazzReqType = (Class<?>)beanType;
        if(clazzDelegateType == clazzReqType)
        {
            Type[]  parametrizedTypeArgs = ptDelegate.getActualTypeArguments();               
            for(Type actual : parametrizedTypeArgs)
            {
                if(!ClassUtil.isUnboundedTypeVariable(actual))
                {
                    if(actual instanceof Class)
                    {
                        Class<?> clazz = (Class<?>)actual;
                        if(!clazz.equals(Object.class))
                        {
                            ok = false;
                            break;
                        }
                    }
                    else
                    {
                        ok = false;
                        break;
                    }
                }
            }                
        }
        else
        {
            ok = false;
        }
                
        return ok;        
    }
}
