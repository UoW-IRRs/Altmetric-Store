<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <title>Altmetrics Store</title>
    <c:set var="url">${pageContext.request.requestURL}</c:set>
    <base href="${fn:substring(url, 0, fn:length(url) - fn:length(pageContext.request.requestURI))}${pageContext.request.contextPath}/" />
    <!-- Latest compiled and minified CSS -->
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
    <!-- Optional theme -->
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap-theme.min.css">
</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top navbar-fixed-top">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="">Altmetrics Store</a>
        </div>
    </div>
</div>

<div class="container" style="margin-top: 70px">
    <c:if test="${not empty error}">
    <div id="error">
        <p class="alert alert-danger">${error}</p>
    </div>
    </c:if>
    <c:if test="${not empty notice}">
    <div id="notice">
        <p class="alert alert-warning">${notice}</p>
    </div>
    </c:if>
    <c:if test="${not empty success}">
    <div id="notice">
        <p class="alert alert-success">${success}</p>
    </div>
    </c:if>

    <c:if test="${not empty page}">
        <c:import url="${page}.jsp"/>
    </c:if>

    <footer>
        <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="//netdna.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
    </footer>
</div>
</body>
</html>