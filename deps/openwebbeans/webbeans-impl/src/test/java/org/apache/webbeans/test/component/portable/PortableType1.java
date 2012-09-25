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
package org.apache.webbeans.test.component.portable;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.Interceptor;

import org.apache.webbeans.test.annotation.binding.Binding1;
import org.apache.webbeans.test.annotation.binding.Binding2;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.inject.parametrized.Dao;
import org.apache.webbeans.test.component.library.Book;
import org.apache.webbeans.test.event.LoggedInEvent;
import org.apache.webbeans.test.xml.annot.BindingType1;
import org.apache.webbeans.test.xml.annot.BindingType2;

@Default
@Binding1
@Binding2
@Interceptor
@Named
public class PortableType1
{
    private @Default IPayment payment;
    
    private @Binding2 @Default Book book;
    
    private @Produces @BindingType2 CheckWithCheckPayment check;
    
    @Inject
    public PortableType1()
    {
        
    }
    
    @Produces
    public Dao<?> getDao(@BindingType1 String hio)
    {
        return null;
    }
    
    public void notify(@Observes @Binding2 LoggedInEvent event)
    {
        
    }

    public IPayment getPayment()
    {
        return payment;
    }

    public void setPayment(IPayment payment)
    {
        this.payment = payment;
    }

    public Book getBook()
    {
        return book;
    }

    public void setBook(Book book)
    {
        this.book = book;
    }

    public CheckWithCheckPayment getCheck()
    {
        return check;
    }

    public void setCheck(CheckWithCheckPayment check)
    {
        this.check = check;
    }

    
}
