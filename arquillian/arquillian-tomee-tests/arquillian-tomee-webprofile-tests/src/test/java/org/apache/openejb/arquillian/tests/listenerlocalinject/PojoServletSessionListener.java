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

package org.apache.openejb.arquillian.tests.listenerlocalinject;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.StringUtils;

public class PojoServletSessionListener implements HttpSessionListener {

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


    public void sessionCreated(HttpSessionEvent event) {
        final String name = "OpenEJB";
        final HttpSession context = event.getSession();

        if (car != null) {
            context.setAttribute(ContextAttributeName.KEY_Car.name(), car.drive(name));
        }
        if (localCompany != null) {
            context.setAttribute(ContextAttributeName.KEY_LocalEjb.name(), "Local: " + localCompany.employ(name));
        }
        if (market != null) {
            context.setAttribute(ContextAttributeName.KEY_Market.name(), market.shop(name));
        }
        if (connectionPool != null) {
            context.setAttribute(ContextAttributeName.KEY_ConnPool.name(), "Connection Pool: " + connectionPool);
        }
        if (startCount != null) {
            context.setAttribute(ContextAttributeName.KEY_StartCount.name(), "Start Expressions.Count: " + startCount);
        }
        if (initSize != null) {
            context.setAttribute(ContextAttributeName.KEY_InitSize.name(), "Init Size: " + initSize);
        }
        if (totalQuantity != null) {
            context.setAttribute(ContextAttributeName.KEY_TotalQuantity.name(), "Total Quantity: " + totalQuantity);
        }
        if (enableEmail != null) {
            context.setAttribute(ContextAttributeName.KEY_EnableEmail.name(), "Enable Email: " + enableEmail);
        }
        if (optionDefault != null) {
            context.setAttribute(ContextAttributeName.KEY_DefaultOption.name(), "Option Default: " + optionDefault);
        }
        if (StringUtils.isNotEmpty(returnEmail) && returnEmail.equals("tomee@apache.org")) {
            context.setAttribute(ContextAttributeName.KEY_ReturnEmail.name(), returnEmail);
        }
        if (auditWriter != null) {
            context.setAttribute(ContextAttributeName.KEY_AuditWriter.name(), auditWriter.getClass().getName());
        }
        if (defaultCode != null) {
            context.setAttribute(ContextAttributeName.KEY_DefaultCode.name(), "DefaultCode: " + defaultCode);
        }
    }

    public void sessionDestroyed(HttpSessionEvent event) {
    }

}