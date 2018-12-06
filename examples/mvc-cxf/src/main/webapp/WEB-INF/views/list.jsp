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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MVC 1.0 DEMO</title>
</head>
<body>

    <jsp:include page="/templates/menu.jsp"></jsp:include>

    <div class="container">
        <c:if test="${message.messageRedirect != null}">
            <div class="row">
                <div class="col-md-12">
                    <p class="alert alert-success" id="success-alert">${message.messageRedirect}</p>
                </div>
            </div>
        </c:if>
        <div class="row">
            <a class="btn btn-primary" href="new">Add Registres</a>
            <hr />
            <table id="tableData"
                class="table table-bordered table-hover">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Age</th>
                        <th>Country</th>
                        <th>State</th>
                        <th>Application Server</th>
                        <th>Description</th>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <c:forEach items="${list}" var="person">
                            <td>${person.name}</td>
                            <td>${person.age}</td>
                            <td>${person.address.country}</td>
                            <td>${person.address.state}</td>
                            <td>${person.server}</td>
                            <td>${person.description}</td>
                            <td><a href="update/${person.id}" class="btn btn-info">Edit</a></td>
                            <td><a href="remove/${person.id}">
                            <spanclass ="glyphiconglyphicon-trash"></span>Delete </a></td>
                    </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <br />
    <br />
    <script type="text/javascript">
    $(document).ready(
            function () {
                $("#success-alert").hide();
                $().ready(
                    function showAlert() {
                        $("#success-alert").fadeTo(2000, 500)
                            .slideUp(500, function () {
                                $("#success-alert")
                                    .slideUp(500);
                            });
                    });
            });
    </script>
    <jsp:include page="/templates/footer.jsp"></jsp:include>
</body>
</html>