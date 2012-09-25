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
package org.apache.webbeans.test.unittests.library;

import java.util.List;

import junit.framework.Assert;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.ContextFactory;
import org.apache.webbeans.test.TestContext;
import org.apache.webbeans.test.component.library.Book;
import org.apache.webbeans.test.component.library.BookShop;
import org.apache.webbeans.test.component.library.Shop;
import org.junit.Before;
import org.junit.Test;

public class LibraryComponentTest extends TestContext
{
    public LibraryComponentTest()
    {
        super(LibraryComponentTest.class.getSimpleName());
    }

    @Before
    public void init()
    {
        super.init();
    }

    @Test
    public void testTypedComponent() throws Throwable
    {
        clear();

        defineManagedBean(BookShop.class);
        List<AbstractOwbBean<?>> comps = getComponents();

        ContextFactory contextFactory = WebBeansContext.getInstance().getContextFactory();
        contextFactory.initRequestContext(null);

        Assert.assertEquals(1, comps.size());

        AbstractOwbBean<?> obj = comps.get(0);

        Object instance = getManager().getInstance(obj);
        Assert.assertTrue(instance instanceof Shop);

        @SuppressWarnings("unchecked")
        Shop<Book> shop = (Shop<Book>) instance;
        shop.shop();

        contextFactory.destroyRequestContext(null);
    }

}
