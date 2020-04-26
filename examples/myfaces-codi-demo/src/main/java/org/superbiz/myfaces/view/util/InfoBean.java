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
package org.superbiz.myfaces.view.util;

import org.apache.myfaces.extensions.cdi.core.api.CodiInformation;
import org.apache.myfaces.extensions.cdi.core.api.projectstage.ProjectStage;
import org.apache.myfaces.extensions.cdi.core.api.provider.BeanManagerProvider;
import org.apache.myfaces.extensions.cdi.core.api.util.ClassUtils;
import org.apache.myfaces.extensions.cdi.jsf.api.Jsf;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.ViewConfigDescriptor;
import org.apache.myfaces.extensions.cdi.jsf.api.config.view.ViewConfigResolver;
import org.apache.myfaces.extensions.cdi.message.api.MessageContext;
import org.apache.myfaces.extensions.validator.ExtValInformation;
import org.superbiz.myfaces.view.InfoPage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Persistence;
import jakarta.validation.Validation;
import java.io.Serializable;

@Named
@SessionScoped
public class InfoBean implements Serializable {

    private static final long serialVersionUID = -1748909261695527800L;

    @Inject
    private
    @Jsf
    MessageContext messageContext;

    @Inject
    private ProjectStage projectStage;

    @Inject
    private ViewConfigResolver viewConfigResolver;

    @Inject
    private FacesContext facesContext;

    private String applicationMessageVersionInfo;

    private String beanValidationVersion;

    private String jpaVersion;

    @PostConstruct
    protected void showWelcomeMessage() {
        String versionString = ClassUtils.getJarVersion(InfoBean.class);

        if (versionString != null) {
            this.applicationMessageVersionInfo = " (v" + versionString + ")";
        }

        this.beanValidationVersion =
                ClassUtils.getJarVersion(Validation.buildDefaultValidatorFactory().getValidator().getClass());

        this.jpaVersion =
                ClassUtils.getJarVersion(Persistence.createEntityManagerFactory("demoApplicationPU").getClass());

        if (!ProjectStage.IntegrationTest.equals(this.projectStage)) {
            this.messageContext.message().text("{msgWelcome}").add();
        }
    }

    public boolean isInfoPage() {
        ViewConfigDescriptor viewConfigDescriptor =
                this.viewConfigResolver.getViewConfigDescriptor(this.facesContext.getViewRoot().getViewId());

        if (viewConfigDescriptor == null) {
            return false;
        }

        return !viewConfigDescriptor.getMetaData(InfoPage.class).isEmpty();
    }

    public String getProjectStage() {
        return this.projectStage.toString();
    }

    public String getApplicationVersion() {
        return this.applicationMessageVersionInfo;
    }

    public String getCodiVersion() {
        return CodiInformation.VERSION;
    }

    public String getCdiVersion() {
        return ClassUtils.getJarVersion(BeanManagerProvider.getInstance().getBeanManager().getClass());
    }

    public String getExtValVersion() {
        return ExtValInformation.VERSION;
    }

    public String getJsfVersion() {
        return ClassUtils.getJarVersion(FacesContext.class);
    }

    public String getBeanValidationVersion() {
        return this.beanValidationVersion;
    }

    public String getJpaVersion() {
        return this.jpaVersion;
    }
}
