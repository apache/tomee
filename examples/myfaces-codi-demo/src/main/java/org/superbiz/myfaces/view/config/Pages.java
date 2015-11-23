/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.superbiz.myfaces.view.config;

import org.apache.myfaces.extensions.cdi.core.api.config.view.DefaultErrorView;
import org.apache.myfaces.extensions.cdi.core.api.config.view.ViewConfig;
import org.apache.myfaces.extensions.cdi.core.api.security.Secured;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.Page;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.PageBean;
import org.superbiz.myfaces.view.FeedbackPage;
import org.superbiz.myfaces.view.InfoPage;
import org.superbiz.myfaces.view.security.LoginAccessDecisionVoter;

import static org.apache.myfaces.extensions.cdi.jsf.api.config.view.Page.NavigationMode.REDIRECT;

@Page(navigation = REDIRECT)
public interface Pages extends ViewConfig {

    @Page
    class Index implements Pages {

    }

    @InfoPage
    @Page
    class About implements Pages {

    }

    @Page
    class Registration implements Pages {

    }

    @Page
    class Login extends DefaultErrorView implements Pages /*just to benefit from the config*/ {

    }

    @Secured(LoginAccessDecisionVoter.class)
            //@Secured(value = LoginAccessDecisionVoter.class, errorView = Login.class)
    interface Secure extends Pages {

        @PageBean(FeedbackPage.class)
        @Page
        class FeedbackList implements Secure {

        }
    }
}
