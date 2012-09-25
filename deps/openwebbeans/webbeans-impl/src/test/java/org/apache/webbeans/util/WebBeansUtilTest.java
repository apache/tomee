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

import static org.junit.Assert.assertEquals;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.IllegalProductException;

import junit.framework.Assert;

import org.junit.Test;


public class WebBeansUtilTest
{

    @Test
    public void testCheckNullInstance()
    {
        String errorMessage = "WebBeans producer method : %s" +
                " return type in the component implementation class : %s" +
                " scope type must be @Dependent to create null instance";
        
        try
        {
            WebBeansUtil.checkNullInstance(null, SessionScoped.class, errorMessage, "aMethodName",
                    String.class);
        } catch (IllegalProductException e)
        {
            String message = e.getMessage();
            assertEquals("WebBeans producer method : aMethodName" +
                " return type in the component implementation class : class java.lang.String" +
                " scope type must be @Dependent to create null instance", message);
            return;
        }
        
        Assert.fail();
    }

}
