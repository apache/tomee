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
package org.apache.webbeans.web.failover;

import java.io.Serializable;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.FailOverService;

/**
 * {@link HttpSessionActivationListener} which informs the {@link FailOverService}
 * about session activation and passivation.
 * 
 * It must NOT be manually registered because the {@link FailOverService}
 * store it as session attribute and therefore it will be executed automatically.
 */
public class FailOverSessionActivationListener implements HttpSessionActivationListener, Serializable
{
    public static final String SESSION_ATTRIBUTE_NAME = "o.a.owb.SESSION_ACTIVATION_LISTENER";

    private static final long serialVersionUID = -5690043082210295824L;

    public void sessionWillPassivate(HttpSessionEvent event)
    {
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        FailOverService failOverService = webBeansContext.getService(FailOverService.class);

        if (failOverService != null && failOverService.isSupportPassivation())
        {
            HttpSession session = event.getSession();
            failOverService.sessionWillPassivate(session);
        }
    }

    public void sessionDidActivate(HttpSessionEvent event)
    {
        WebBeansContext webBeansContext = WebBeansContext.currentInstance();
        FailOverService failOverService = webBeansContext.getService(FailOverService.class);

        if (failOverService != null && (failOverService.isSupportFailOver() || failOverService.isSupportPassivation()))
        {
            HttpSession session = event.getSession();
            failOverService.sessionDidActivate(session);
        }
    }
}