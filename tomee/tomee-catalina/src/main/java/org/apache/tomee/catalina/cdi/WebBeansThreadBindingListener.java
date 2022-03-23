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
package org.apache.tomee.catalina.cdi;

import jakarta.enterprise.context.RequestScoped;

import org.apache.catalina.ThreadBindingListener;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.spi.ContextsService;

/**
 * For Tomcat we need to implement a ThreadBindingListener to
 * release the ServletRequest in case of Async requests.
 * Tomcat only sends the requestDestroyed event only when the 'final'
 * detached response gets rendered. But this happens on a totally
 * different Thread.
 * Thus in order to release e.g. locks on Conversations and prevent mem leaks
 * we need to end the request on unbind() as well.
 * Note that the ContextsService will do nothing if the Request was already
 * properly destroyed in standard synchronous Servlet Requests.
 */
public class WebBeansThreadBindingListener implements ThreadBindingListener {

    private final ContextsService contextsService;
    private final ThreadBindingListener delegate;

    public WebBeansThreadBindingListener(WebBeansContext webBeansContext, ThreadBindingListener delegate) {
        this.contextsService = webBeansContext.getContextsService();
        this.delegate = delegate;
    }

    @Override
    public void bind() {
        delegate.bind();
    }

    @Override
    public void unbind() {
        contextsService.endContext(RequestScoped.class, null);
        delegate.unbind();
    }
}
