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
package org.apache.webbeans.samples.tomcat;

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InjectorServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private @Inject CurrentDateProvider dateProvider;
    
    private @Inject BeanManager beanManager;
         
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        PrintWriter writer = resp.getWriter();
        writer.write("==================Injection of Bean Instance into Servlet==================" + "\n\n");
        writer.write("Caller Principal name injection into DateProvider instance : " + dateProvider.getPrincipal().getName() + "\n\n");
        writer.write("Current Date : " + dateProvider.toString());
        writer.write("\n");        
        
        writer.write("==================Injection of BeanManager into Servlet==================" + "\n\n");
        if(beanManager != null)
        {
            writer.write("Injection of @Inject BeanManager into servlet is successfull");
        }
        else
        {
            writer.write("Injection of @Inject BeanManager into servlet has failed");
        }
        
    }
    
    

}
