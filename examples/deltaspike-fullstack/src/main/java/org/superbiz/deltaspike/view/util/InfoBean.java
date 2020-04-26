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
package org.superbiz.deltaspike.view.util;

import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigDescriptor;
import org.apache.deltaspike.core.api.config.view.metadata.ViewConfigResolver;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.apache.deltaspike.jsf.api.message.JsfMessage;
import org.apache.myfaces.extensions.validator.ExtValInformation;
import org.apache.myfaces.extensions.validator.util.ClassUtils;
import org.superbiz.deltaspike.WebappMessageBundle;
import org.superbiz.deltaspike.view.InfoPage;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Persistence;
import jakarta.validation.Validation;
import java.io.Serializable;

@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
@Named
@SessionScoped
public class InfoBean implements Serializable {
    private static final long serialVersionUID = -1748909261695527800L;

    @Inject
    private WindowContext windowContext;

    @Inject
    private JsfMessage<WebappMessageBundle> webappMessages;

    @Inject
    private ProjectStage projectStage;

    @Inject
    private ViewConfigResolver viewConfigResolver;

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
            this.webappMessages.addInfo().msgWelcome();
        }
    }

    public boolean isInfoPage() {
        ViewConfigDescriptor viewConfigDescriptor =
                this.viewConfigResolver.getViewConfigDescriptor(FacesContext.getCurrentInstance().getViewRoot().getViewId());

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

    public String getDeltaSpikeVersion() {
        return ClassUtils.getJarVersion(BeanManagerProvider.class);
    }

    public String getCdiVersion() {
        try {
            return ClassUtils.getJarVersion(BeanManagerProvider.getInstance().getBeanManager().getClass());
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to get CDI Version: " + e.getMessage();
        }
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

    public String getWindowId() {
        return this.windowContext.getCurrentWindowId();
    }
}
