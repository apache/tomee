/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.newtests.concepts.apiTypes.tests;

import java.lang.reflect.Type;
import java.util.Set;

import junit.framework.Assert;

import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.config.DefinitionUtil;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.newtests.AbstractUnitTest;
import org.apache.webbeans.newtests.concepts.apiTypes.common.ApiTypeBean;
import org.junit.Test;

public class ApiTypeTest extends AbstractUnitTest
{
    public ApiTypeTest()
    {
        
    }
    
    @Test
    public void testApiType()
    {
        ManagedBean<ApiTypeBean> bean = new ManagedBean<ApiTypeBean>(ApiTypeBean.class, WebBeansContext.getInstance());
        DefinitionUtil.defineApiTypes(bean, ApiTypeBean.class);
        
        Set<Type> type = bean.getTypes();
        Assert.assertEquals(5, type.size());
    }

}
