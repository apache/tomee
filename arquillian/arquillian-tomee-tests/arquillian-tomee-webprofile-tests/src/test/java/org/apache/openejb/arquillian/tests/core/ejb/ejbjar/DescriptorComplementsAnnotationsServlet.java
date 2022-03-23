/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.openejb.arquillian.tests.core.ejb.ejbjar;


import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DescriptorComplementsAnnotationsServlet extends HttpServlet {
   /*
     * Check that the deployment descriptor will
     * complement the information from the
     * EJB annotation. For instance the bean interface
     * is resolved from the EjbJar descriptor
     */

    @EJB(name = "ejb/first", beanName = "FirstBean")
    Object first;

    @EJB(name = "ejb/second", beanName = "SecondBean")
    Object second;

    //Check that the descriptor takes precedence over annotation -> override the bean name
    @EJB(name = "ejb/override", beanName = "FirstBean")
    Bean override;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final PrintWriter writer = resp.getWriter();
        final String testToExecute = req.getParameter("test");

        try {
            final Method method = this.getClass().getDeclaredMethod(testToExecute);
            method.invoke(this);
            writer.println(testToExecute + "=true");
        } catch (Exception ex) {
            final Throwable rootCause = ex instanceof InvocationTargetException ? ex.getCause() : ex;
            writer.println(testToExecute + "=false");
            rootCause.printStackTrace(writer);
        }
    }

    public void verifyFirstBeanInjection() {
        verifyBean(FirstBean.class, first);
    }

    public void verifySecondBeanInjection() {
        verifyBean(SecondBean.class, second);
    }

    public void verifyOverridenBeanInjection() {
        verifyBean(SecondBean.class, override);
    }

    private void verifyBean(Class<?> beanClass, Object beanInstance) {
        if (null == beanInstance) {
            throw new IllegalStateException("Expecting [" + beanClass + "] but the injected instance is null");
        }

        if (!(beanInstance instanceof Bean)) {
            throw new IllegalStateException("Expecting instance of [" + beanClass + "] " +
                    "Instead received [" + beanInstance.getClass() + "]"
            );
        }

        final String beanName = ((Bean) beanInstance).getName();
        if (!beanClass.getName().equals(beanName)) {
            throw new IllegalStateException("Expecting [" + beanClass + "] " +
                    "Instead received [" + beanName + "]"
            );
        }
    }
}
