<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
<head>
    <title>Home Page</title>
</head>
<body>
Hello World!
<div>
    <c:set var="salary" value="3540.2301"/>
    <c:set var="total" value="56225.2301"/>
    percent:<fmt:formatNumber value="${salary/total}" type="percent" maxFractionDigits="4"/>
</div>
</body>
</html>
