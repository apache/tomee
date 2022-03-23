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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.arquillian.tests.filterenventry;

import java.io.IOException;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.apache.commons.lang3.StringUtils;

public class PojoServletFilter implements Filter {

    @Inject
    private Car car;

    @EJB
    private CompanyLocal localCompany;

    @EJB
    private SuperMarket market;

    @Resource(name = "returnEmail")
    private String returnEmail;

    @Resource(name = "connectionPool")
    private Integer connectionPool;

    @Resource(name = "startCount")
    private Long startCount;

    @Resource(name = "initSize")
    private Short initSize;

    @Resource(name = "totalQuantity")
    private Byte totalQuantity;

    @Resource(name = "enableEmail")
    private Boolean enableEmail;

    @Resource(name = "optionDefault")
    private Character optionDefault;

    /* TODO: Enable this resource after functionality is fixed
    @Resource
    */
    private Code defaultCode;

    /* TODO: Enable this resource after functionality is fixed
            @Resource
            @SuppressWarnings("unchecked")
    */
    private Class auditWriter;


    private FilterConfig config;

    public void init(FilterConfig config) {
        this.config = config;
    }

    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        String name = req.getParameter("name");
        if (StringUtils.isEmpty(name)) {
            name = "OpenEJB";
        }

        if (car != null) {
            resp.getOutputStream().println(car.drive(name));
        }
        if (localCompany != null) {
            resp.getOutputStream().println("Local: " + localCompany.employ(name));
        }
        if (market != null) {
            resp.getOutputStream().println(market.shop(name));
        }
        if (connectionPool != null) {
            resp.getOutputStream().println("Connection Pool: " + connectionPool);
        }
        if (startCount != null) {
            resp.getOutputStream().println("Start Count: " + startCount);
        }
        if (initSize != null) {
            resp.getOutputStream().println("Init Size: " + initSize);
        }
        if (totalQuantity != null) {
            resp.getOutputStream().println("Total Quantity: " + totalQuantity);
        }
        if (enableEmail != null) {
            resp.getOutputStream().println("Enable Email: " + enableEmail);
        }
        if (optionDefault != null) {
            resp.getOutputStream().println("Option Default: " + optionDefault);
        }
        if (StringUtils.isNotEmpty(returnEmail) && returnEmail.equals("tomee@apache.org")) {
            resp.getOutputStream().println(returnEmail);
        }
        if (auditWriter != null) {
            resp.getOutputStream().println(auditWriter.getClass().getName());
        }
        if (defaultCode != null) {
            resp.getOutputStream().println("DefaultCode: " + defaultCode);
        }
    }


}