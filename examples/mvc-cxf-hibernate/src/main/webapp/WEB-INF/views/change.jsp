<!--

    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>MVC 1.0 DEMO</title>
</head>
<body>

    <div class="container">
        <c:if test="${error.errors.length() != 0}">
            <div class="row">
                <div class="col-md-12">
                    <p class="alert alert-danger">${error.errors}</p>
                </div>
            </div>
        </c:if>
        <div class="row">
            <div class="col-md-4 col-md-offset-4">
                <form action="${mvc.basePath}/mvc/update" method="post">
                    <h2>Change Registration</h2>
                    <div class="form-group">

                        <input type="text" name="id" value="${person.id}" hidden="true">

                        <label for="name">Name:</label> 
                        <input id="name" name="name" class="form-control" autofocus value="${person.name}">
                        <c:if test="${mvc.encoders.html(error.getMessage('name').length() != 0)}">
                            <div class="row">
                                <div class="col-md-12">
                                    <p class="alert alert-danger">${mvc.encoders.html(error.getMessage("name"))}</p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <div class="form-group">
                        <label for="age">Age:</label> 
                        <input type="number" id="age" name="age" class="form-control" value="${person.age}">
                        <c:if test="${mvc.encoders.html(error.getMessage('age').length() != 0)}">
                            <div class="row">
                                <div class="col-md-12">
                                    <p class="alert alert-danger">${mvc.encoders.html(error.getMessage("age"))}</p>
                                </div>
                            </div>
                        </c:if>
                    </div>

                    <div class="form-group">
                        <label for="state">State:</label> 
                        <input type="text" id="state" name="state" class="form-control" value="${person.address.state}">
                        <c:if test="${mvc.encoders.html(error.getMessage('state').length() != 0)}">
                            <div class="row">
                                <div class="col-md-12">
                                    <p class="alert alert-danger">${mvc.encoders.html(error.getMessage("state"))}</p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <div class="form-group">
                        <label for="state">Application Server:</label> <br />
                        <label class="radio-inline">
                        <input type="radio" name="server" value="TomEE" class="form-check-input"/>TomEE</label> 
                        <label class="radio-inline">
                        <input type="radio" name="server" value="Wildfly" class="form-check-input"/>Wildfly</label> 
                        <label class="radio-inline">
                        <input type="radio" name="server" value="Payara" class="form-check-input"/>Payara</label>

                        <c:if test="${mvc.encoders.html(error.getMessage('server').length() != 0)}">
                            <div class="row">
                                <div class="col-md-12">
                                    <p class="alert alert-danger">${mvc.encoders.html(error.getMessage("server"))}</p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <div class="form-group">
                        <label for="country">Country:</label> 
                        <select id="country" name="country" class="form-control">
                            <option value="${person.address.country}">${person.address.country}</option>
                            <c:forEach var="countries" items="${countries}">
                                <option>${countries}</option>
                            </c:forEach>
                        </select>
                        <c:if test="${mvc.encoders.html(error.getMessage('country').length() != 0)}">
                            <div class="row">
                                <div class="col-md-12">
                                    <p class="alert alert-danger">${mvc.encoders.html(error.getMessage("country"))}</p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <div class="form-group">
                        <label for="description">Description:</label>
                        <textarea type="" id="description"
                            name="description" class="form-control">${person.description}</textarea>
                        <c:if
                            test="${mvc.encoders.html(error.getMessage('description').length() != 0)}">
                            <div class="row">
                                <div class="col-md-12">
                                    <p class="alert alert-danger">${mvc.encoders.html(error.getMessage("description"))}</p>
                                </div>
                            </div>
                        </c:if>
                    </div>
                    <button class="btn btn-primary btn-block" type="submit">Register</button>
                </form>
            </div>
        </div>
    </div>
    <br />
    <br />
    <jsp:include page="/templates/footer.jsp"></jsp:include>
</body>
</html>