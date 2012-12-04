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
package org.apache.webbeans.util;

import org.apache.webbeans.exception.helper.ViolationMessageBuilder;
import static org.apache.webbeans.exception.helper.ViolationMessageBuilder.newViolation;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.UnproxyableResolutionException;
import java.util.Set;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class InjectionExceptionUtils
{
    private InjectionExceptionUtils()
    {
        // utility class ct
    }

    public static void throwUnproxyableResolutionException(ViolationMessageBuilder violationMessage)
    {
        throw new UnproxyableResolutionException(
                newViolation("WebBeans with api type with normal scope must be proxiable to inject.")
                        .addLine(violationMessage.toString())
                        .toString());
    }

    public static void throwUnsatisfiedResolutionException(Type type, Method producerMethod, Annotation... qualifiers)
    {
        ViolationMessageBuilder violationMessage = newViolation(createProducerMethodMessage(producerMethod));

        violationMessage.append(" in class: ", ClassUtil.getClass(type).getName());
        violationMessage.addLine(createQualifierMessage(qualifiers));

        throw new UnsatisfiedResolutionException(violationMessage.toString());
    }

    private static String createProducerMethodMessage(Method producerMethod)
    {
        return "Producer method component of the disposal method : " + producerMethod.getName() + "is not found";
    }

    public static void throwUnsatisfiedResolutionException(
            Class type, InjectionPoint injectionPoint, Annotation... qualifiers)
    {
        ViolationMessageBuilder violationMessage =
                newViolation("Api type [", type.getName(), "] is not found with the qualifiers ");

        violationMessage.addLine(createQualifierMessage(qualifiers));

        if (injectionPoint != null)
        {
            violationMessage.addLine("for injection into ", injectionPoint.toString());
        }

        throw new UnsatisfiedResolutionException(violationMessage.toString());
    }

    public static void throwAmbiguousResolutionExceptionForBeanName(Set<Bean<?>> beans, String beanName)
    {
        throwAmbiguousResolutionExceptionForBeans(beans,
                newViolation("There are more than one WebBeans with name : ", beanName));
    }

    public static void throwAmbiguousResolutionException(Set<Bean<?>> beans)
    {
        throwAmbiguousResolutionException(beans, null, null);
    }

    public static void throwAmbiguousResolutionException(Set<Bean<?>> beans, Class type, InjectionPoint injectionPoint, Annotation... qualifiers)
    {
        String qualifierMessage = createQualifierMessage(qualifiers);

        ViolationMessageBuilder violationMessage;

        if(type != null)
        {
            violationMessage = newViolation("There is more than one api type with : ",
                    ClassUtil.getClass(type).getName(), " with qualifiers : ", qualifierMessage);
            if (injectionPoint != null)
            {
                violationMessage.addLine("for injection into ", injectionPoint.toString());
            }
        }
        else
        {
            violationMessage = newViolation("Ambigious resolution");
        }

        throwAmbiguousResolutionExceptionForBeans(beans, violationMessage);
    }

    private static void throwAmbiguousResolutionExceptionForBeans(
            Set<Bean<?>> beans, ViolationMessageBuilder violationMessage)
    {
        violationMessage.addLine("found beans: ");

        addBeanInfo(beans, violationMessage);

        throw new AmbiguousResolutionException(violationMessage.toString());
    }

    private static void addBeanInfo(Set<Bean<?>> beans, ViolationMessageBuilder violationMessage)
    {
        for(Bean<?> currentBean : beans)
        {
            violationMessage.addLine(currentBean.toString());
        }
    }

    private static String createQualifierMessage(Annotation... qualifiers)
    {
        if(qualifiers == null || qualifiers.length == 0)
        {
            return null;
        }

        //reused source-code
        StringBuilder qualifierMessage = new StringBuilder("Qualifiers: [");

        int i = 0;
        for(Annotation annot : qualifiers)
        {
            i++;
            qualifierMessage.append(annot);

            if(i != qualifiers.length)
            {
                qualifierMessage.append(",");
            }
        }

        qualifierMessage.append("]");

        return qualifierMessage.toString();
    }
}
