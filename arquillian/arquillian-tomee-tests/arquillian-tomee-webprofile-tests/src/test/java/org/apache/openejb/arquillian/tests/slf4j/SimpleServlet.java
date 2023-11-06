/**
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
package org.apache.openejb.arquillian.tests.slf4j;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/logtest")
public class SimpleServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServlet.class);

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        final ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        final String loggerFactoryName = iLoggerFactory.getClass().getName();

        LOGGER.info("Simple servlet called");
        LOGGER.info("Logger Factory: " + loggerFactoryName);

        final PrintWriter writer = resp.getWriter();
        writer.println("It works!\n" +
                "Logger Factory: " + loggerFactoryName + "\n" +
                "Protection Domain: " + iLoggerFactory.getClass().getProtectionDomain().toString());
        writer.flush();
    }
}
