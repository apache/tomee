<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>MVC 1.0 DEMO</title>
</head>
<body>
    <jsp:include page="/templates/menu.jsp"></jsp:include>

    <h1 align="center">Be welcome!</h1>

    <div align="center">
        <img src="${pageContext.request.contextPath}/resources/images/tomee.png" class="img-responsive" />
    </div>
    <br/>
    <br/>
    <jsp:include page="/templates/footer.jsp"></jsp:include>
</body>
</html>