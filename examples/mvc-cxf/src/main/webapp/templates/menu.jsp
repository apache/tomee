<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"
      name="viewport" content="width=device-width, initial-scale=1">
<title>MVC 1.0 DEMO</title>
<link
    href="${pageContext.request.contextPath}/resources/bootstrap/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
</head>
<body>
    <div class="navbar navbar-inverse">
        <div class="container-fluid">
            <div class="navbar-header">
                <a class="navbar-brand" href="${mvc.basePath}/home">MVC 1.0 DEMO</a>
            </div>
            <div>
                <ul class="nav navbar-nav">
                    <li class="active"><a
                        href="${mvc.basePath}/mvc/show">Peoples</a></li>
                </ul>
            </div>
        </div>
    </div>
    <script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/jquery.min.js">
	</script>

    <script type="text/javascript" src="${pageContext.request.contextPath}/resources/bootstrap/js/bootstrap.min.js">
	</script>
</body>
</html>