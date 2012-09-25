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
package org.apache.webbeans.test.unittests.event.exception;

import java.lang.annotation.Annotation;

import javax.enterprise.context.NormalScope;
import javax.enterprise.util.AnnotationLiteral;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.annotation.binding.Binding1;
import org.apache.webbeans.test.event.LoggedInEvent;
import org.apache.webbeans.test.event.LoggedInObserver;
import org.apache.webbeans.test.event.broke.BrokenEvent;
import org.apache.webbeans.util.ArrayUtil;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class EventExceptionTest extends TestContext
{
    public EventExceptionTest()
    {
        super(EventExceptionTest.class.getName());
    }

    @Test
    public void testAddObserverGenericType()
    {
        Exception exc = null;

        try
        {
            Annotation[] anns = new Annotation[1];
            anns[0] = new AnnotationLiteral<Binding1>()
            {
            };


            getManager().fireEvent(new BrokenEvent(), anns);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNotNull(exc);
    }

    @Test
    public void testFireEventGenericType()
    {
        Exception exc = null;

        try
        {
            Annotation[] anns = new Annotation[1];
            anns[0] = new AnnotationLiteral<Binding1>()
            {
            };

            getManager().fireEvent(new BrokenEvent(), anns);
            Assert.fail();

        }
        catch (Exception e)
        {
            // this is an expected exception!
        }
    }

    @Test
    public void testAddObserverDuplicateBinding()
    {
        Exception exc = null;

        try
        {
            Annotation[] anns = new Annotation[2];
            anns[0] = new AnnotationLiteral<Binding1>()
            {
            };
            anns[1] = new AnnotationLiteral<Binding1>()
            {
            };

            LoggedInObserver observer = new LoggedInObserver(ArrayUtil.asSet(anns));
            WebBeansContext.getInstance().getBeanManagerImpl().getNotificationManager().addObserver(observer, LoggedInEvent.class);

            getManager().fireEvent(new LoggedInEvent(), anns);

            Assert.assertEquals("ok", observer.getResult());

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNotNull(exc);

    }

    @Test
    public void testAddObserverIllegalArgument()
    {
        Exception exc = null;

        try
        {
            Annotation[] anns = new Annotation[2];
            anns[0] = new AnnotationLiteral<NormalScope>()
            {
            };
            
            LoggedInObserver observer = new LoggedInObserver(ArrayUtil.asSet(anns));
            WebBeansContext.getInstance().getBeanManagerImpl().getNotificationManager().addObserver(observer, LoggedInEvent.class);

            getManager().fireEvent(new LoggedInEvent(), anns);

            Assert.assertEquals("ok", observer.getResult());

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            exc = e;
        }

        Assert.assertNotNull(exc);

    }
}
