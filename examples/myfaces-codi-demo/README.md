index-group=Unrevised
type=page
status=published
title=MyFaces CODI Demo
~~~~~~
Notice:    Licensed to the Apache Software Foundation (ASF) under one
           or more contributor license agreements.  See the NOTICE file
           distributed with this work for additional information
           regarding copyright ownership.  The ASF licenses this file
           to you under the Apache License, Version 2.0 (the
           "License"); you may not use this file except in compliance
           with the License.  You may obtain a copy of the License at
           .
             http://www.apache.org/licenses/LICENSE-2.0
           .
           Unless required by applicable law or agreed to in writing,
           software distributed under the License is distributed on an
           "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
           KIND, either express or implied.  See the License for the
           specific language governing permissions and limitations
           under the License.

<h2>Steps to run the example</h2>

Build and start the demo:

    mvn clean package tomee:run

Open:

    http://localhost:8080/myfaces-codi-1.0-SNAPSHOT/

This example shows how to improve JSF2/CDI/BV/JPA applications with features provided by Apache MyFaces CODI and ExtVal.

<h2>Intro of MyFaces CODI and ExtVal</h2>

The Apache MyFaces Extensions CDI project (aka CODI) hosts portable extensions for Contexts and Dependency Injection (CDI - JSR 299). CODI is a toolbox for your CDI application. Like CDI itself CODI is focused on type-safety. It is a modularized and extensible framework. So it's easy to choose the needed parts to facilitate the daily work in your project.

MyFaces Extensions Validator (aka ExtVal) is a JSF centric validation framework which is compatible with JSF 1.x and JSF 2.x.
This example shows how it improves the default integration of Bean-Validation (JSR-303) with JSF2 as well as meta-data based cross-field validation.


<h2>Illustrated Features</h2>

<h3>Apache MyFaces CODI</h3>

<ul>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/config/Pages.java" target="_blank">Type-safe view-config</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/InfoPage.java" target="_blank">Type-safe (custom) view-meta-data</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/MenuBean.java" target="_blank">Type-safe navigation</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/CustomJsfModuleConfig.java" target="_blank">Type-safe (specialized) config</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/CustomProjectStage.java" target="_blank">Type-safe custom project-stage</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/UserHolder.java" target="_blank">@WindowScoped</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/MenuBean.java" target="_blank">Controlling CODI scopes with WindowContext</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/FeedbackPage.java" target="_blank">@ViewAccessScoped</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/FeedbackPage.java" target="_blank">Manual conversation handling</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/security/LoginAccessDecisionVoter.java" target="_blank">Secured pages (AccessDecisionVoter)</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/repository/Repository.java" target="_blank">@Transactional</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/RegistrationPage.java" target="_blank">I18n (fluent API)</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/domain/validation/UniqueUserNameValidator.java" target="_blank">Dependency-Injection for JSR303 (BV) constraint-validators</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/DebugPhaseListener.java" target="_blank">Dependency-Injection for JSF phase-listeners</a></li>
</ul>

<h3>Apache MyFaces ExtVal</h3>

<ul>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/RegistrationPage.java" target="_blank">Cross-Field validation (@Equals)</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/RegistrationPage.java" target="_blank">Type-safe group-validation (@BeanValidation) for JSF action-methods</a></li>
</ul>
