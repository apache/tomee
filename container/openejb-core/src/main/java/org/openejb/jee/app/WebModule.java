/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.jee.app;

/**
 * @version $Revision$ $Date$
 */
public class WebModule extends Module {
    private Web web;

    public WebModule() {
    }

    public WebModule(Web web) {
        this.web = web;
    }

    public WebModule(String webUri, String contextRoot) {
        this.web = new Web(webUri, contextRoot);
    }

    public Web getWeb() {
        return web;
    }

    public void setWeb(Web web) {
        this.web = web;
    }

    public String getWebUri() {
        return web.getWebUri();
    }

    public void setWebUri(String webUri) {
        web.setWebUri(webUri);
    }

    public String getContextRoot() {
        return web.getContextRoot();
    }

    public void setContextRoot(String contextRoot) {
        web.setContextRoot(contextRoot);
    }
}
