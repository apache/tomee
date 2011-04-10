<!--
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
-->
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<html>
<head>
    <title>Calculation Results</title>
</head>
<body>
<f:view>
    <h:form id="mainForm">
        <h2><h:outputText value="Result of adding #{calculator.x} and #{calculator.y} is #{calculator.result }"/></h2>
        <h:commandLink action="back">
            <h:outputText value="Home"/>
        </h:commandLink>
    </h:form>
</f:view>
</body>
</html>