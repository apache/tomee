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
package org.apache.webbeans.test.unittests.event;

import java.lang.annotation.Annotation;

import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.TypeLiteral;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.annotation.binding.Binding1;
import org.apache.webbeans.test.event.ITypeArgumentEventInterface;
import org.apache.webbeans.test.event.LoggedInEvent;
import org.apache.webbeans.test.event.LoggedInObserver;
import org.apache.webbeans.test.event.TypeArgumentBaseEvent;
import org.apache.webbeans.test.event.TypeArgumentEvent;
import org.apache.webbeans.test.event.TypeArgumentInterfaceObserver;
import org.apache.webbeans.test.event.TypeArgumentObserver;
import org.apache.webbeans.util.ArrayUtil;
import org.junit.Assert;
import org.junit.Test;

public class EventTest extends TestContext
{
    public EventTest()
    {
        super(EventTest.class.getName());
    }

    @Test
    public void testObserverWithClazz()
    {
        Annotation[] anns = new Annotation[1];
        anns[0] = new AnnotationLiteral<Binding1>()
        {
        };

        LoggedInObserver observer = new LoggedInObserver(ArrayUtil.asSet(anns));
        WebBeansContext.getInstance().getBeanManagerImpl().getNotificationManager().addObserver(observer, LoggedInEvent.class);

        getManager().fireEvent(new LoggedInEvent(), anns);

        Assert.assertEquals("ok", observer.getResult());
    }

    @Test
    public void testObserverWithClazzAndTypeArguments()
    {
        Annotation[] anns = new Annotation[1];
        anns[0] = new AnnotationLiteral<Binding1>()
        {
        };

        TypeArgumentObserver observer = new TypeArgumentObserver(ArrayUtil.asSet(anns));
        TypeLiteral<TypeArgumentBaseEvent> tl = new TypeLiteral<TypeArgumentBaseEvent>()
        {
        };

        WebBeansContext.getInstance().getBeanManagerImpl().getNotificationManager().addObserver(observer, tl);

        getManager().fireEvent(new TypeArgumentEvent(), anns);

        Assert.assertEquals("ok", observer.getResult());
    }

    @Test
    public void testObserverWithInterface()
    {
        Annotation[] anns = new Annotation[1];
        anns[0] = new AnnotationLiteral<Binding1>()
        {
        };

        TypeArgumentInterfaceObserver observer = new TypeArgumentInterfaceObserver(ArrayUtil.asSet(anns));
        WebBeansContext.getInstance().getBeanManagerImpl().getNotificationManager().addObserver(observer, ITypeArgumentEventInterface.class);

        getManager().fireEvent(new TypeArgumentEvent(), anns);
        Assert.assertEquals("ok", observer.getResult());

    }

}
