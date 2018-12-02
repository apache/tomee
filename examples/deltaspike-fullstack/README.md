index-group=Unrevised
type=page
status=published
title=Apache DeltaSpike Demo
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

    http://localhost:8080/

This example shows how to improve JSF2/CDI/BV/JPA applications with features provided by Apache DeltaSpike and MyFaces ExtVal.

<h2>Intro of Apache DeltaSpike and MyFaces ExtVal</h2>

The Apache DeltaSpike project hosts portable extensions for Contexts and Dependency Injection (CDI - JSR 299). DeltaSpike is a toolbox for your CDI application. Like CDI itself DeltaSpike is focused on type-safety. It is a modularized and extensible framework. So it's easy to choose the needed parts to facilitate the daily work in your project.

MyFaces Extensions Validator (aka ExtVal) is a JSF centric validation framework which is compatible with JSF 1.x and JSF 2.x.
This example shows how it improves the default integration of Bean-Validation (JSR-303) with JSF2 as well as meta-data based cross-field validation.


<h2>Illustrated Features</h2>

<h3>Apache DeltaSpike</h3>

<ul>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/config/Pages.java" target="_blank">Type-safe view-config</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/InfoPage.java" target="_blank">Type-safe (custom) view-meta-data</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/MenuBean.java" target="_blank">Type-safe navigation</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/CustomProjectStage.java" target="_blank">Type-safe custom project-stage</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/UserHolder.java" target="_blank">@WindowScoped</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/MenuBean.java" target="_blank">Controlling DeltaSpike grouped-conversations with GroupedConversationManager</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/FeedbackPage.java" target="_blank">@GroupedConversationScoped</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/FeedbackPage.java" target="_blank">Manual conversation handling</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/security/LoginAccessDecisionVoter.java" target="_blank">Secured pages (AccessDecisionVoter)</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/repository/Repository.java" target="_blank">@Transactional</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/view/RegistrationPage.java" target="_blank">I18n (type-safe messages)</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/domain/validation/UniqueUserNameValidator.java" target="_blank">Dependency-Injection for JSR303 (BV) constraint-validators</a></li>
    <li><a href="./src/main/java/org/superbiz/deltaspike/DebugPhaseListener.java" target="_blank">Dependency-Injection for JSF phase-listeners</a></li>
</ul>

<h3>Apache MyFaces ExtVal</h3>

<ul>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/RegistrationPage.java" target="_blank">Cross-Field validation (@Equals)</a></li>
    <li><a href="./src/main/java/org/superbiz/myfaces/view/RegistrationPage.java" target="_blank">Type-safe group-validation (@BeanValidation) for JSF action-methods</a></li>
</ul>
