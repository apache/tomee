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
package org.superbiz.deltaspike.view.config;

import org.apache.deltaspike.core.api.config.view.DefaultErrorView;
import org.apache.deltaspike.core.api.config.view.ViewConfig;
import org.apache.deltaspike.core.api.config.view.controller.ViewControllerRef;
import org.apache.deltaspike.jsf.api.config.view.View;
import org.apache.deltaspike.security.api.authorization.Secured;
import org.superbiz.deltaspike.view.FeedbackPage;
import org.superbiz.deltaspike.view.InfoPage;
import org.superbiz.deltaspike.view.security.LoginAccessDecisionVoter;

import static org.apache.deltaspike.jsf.api.config.view.View.NavigationMode.REDIRECT;

@View(navigation = REDIRECT)
public interface Pages extends ViewConfig {
    class Index implements Pages {
    }

    @InfoPage
    class About implements Pages {
    }

    class Registration implements Pages {
    }

    class Login extends DefaultErrorView implements Pages /*just to benefit from the config*/ {
    }

    @Secured(LoginAccessDecisionVoter.class)
            //@Secured(value = LoginAccessDecisionVoter.class, errorView = Login.class)
    interface Secure extends Pages {
        @ViewControllerRef(FeedbackPage.class)
                //optional: @View
        class FeedbackList implements Secure {
        }
    }
}
