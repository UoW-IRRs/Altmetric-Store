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

    <h2>Look up stored altmetrics</h2>
    <form method="get" action="from-store" class="form-inline well" role="form">
        <form class="form-inline">
            <div class="form-group">
                <label class="sr-only" for="from-store_handle">Handle</label>
                <input type="text" required="required" class="form-control" id="from-store_handle" name="handle" placeholder="Handle*" pattern="\S+/\S+" title="The item's handle, eg &quot;123456789/1&quot;">
            </div>
            <div class="form-group">
                <label class="sr-only" for="from-store_provider">Provider</label>
                <select required="required" id="from-store_provider" name="provider" class="form-control">
                    <option value="" selected="selected">Choose provider*</option>
                    <option value="scopus">Scopus</option>
                    <option value="wos">Web of Science</option>
                </select>
            </div>
            <button type="submit" class="btn btn-default">Retrieve from altmetrics store</button>
        </form>
    </form>

    <hr/>

    <h2>Look up altmetrics</h2>

    <h3>From Scopus</h3>
    <p>This can only be done from localhost.</p>
    <form method="get" action="from-provider" class="form-inline well" role="form">
        <form class="form-inline">
            <div class="form-group">
                <label class="sr-only" for="from-scopus_handle">Handle</label>
                <input type="text" required="required" class="form-control" id="from-scopus_handle" name="handle" placeholder="Handle*" pattern="\S+/\S+" title="The item's handle, eg &quot;123456789/1&quot;">
            </div>
            <div class="form-group">
                <label class="sr-only" for="from-scopus_doi">DOI</label>
                <input type="text" required="required" class="form-control" id="from-scopus_doi" name="param_doi" placeholder="DOI*" title="The item's DOI, eg &quot;10.1002/jqs.1016&quot;">
            </div>
            <input type="hidden" name="provider" value="scopus"/>
            <button type="submit" class="btn btn-default">Retrieve from Scopus</button>
        </form>
    </form>

    <h3>From Web of Science</h3>
    <p>This can only be done from localhost.</p>
    <form method="get" action="from-provider" class="form-inline well" role="form">
        <form class="form-inline">
            <div class="form-group">
                <label class="sr-only" for="from-scopus_handle">Handle</label>
                <input type="text" required="required" class="form-control" id="from-wos_handle" name="handle" placeholder="Handle*" pattern="\S+/\S+" title="The item's handle, eg &quot;123456789/1&quot;">
            </div>
            <div class="form-group">
                <label class="sr-only" for="from-wos_doi">DOI</label>
                <input type="text" required="required" class="form-control" id="from-wos_doi" name="param_doi" placeholder="DOI*" title="The item's DOI, eg &quot;10.1002/jqs.1016&quot;">
            </div>
            <input type="hidden" name="provider" value="wos"/>
            <button type="submit" class="btn btn-default" disabled="disabled">Retrieve from Web of Science</button>
        </form>
    </form>

    <hr/>

    <h2>Delete stored altmetrics</h2>
    <p>This can only be done from localhost.</p>
    <p>If neither handle nor provider are given, this will delete all stored altmetrics that were last updated more than 2 weeks ago.</p>
    <form method="post" action="from-store" class="form-inline well" role="form">
    <form class="form-inline">
        <div class="form-group">
            <label class="sr-only" for="delete_handle">Handle</label>
            <input type="text" class="form-control" id="delete_handle" name="handle" placeholder="Handle" pattern="\S+/\S+" title="The item's handle, eg &quot;123456789/1&quot;">
        </div>
        <div class="form-group">
            <label class="sr-only" for="delete_provider">Provider</label>
            <select id="delete_provider" name="provider" class="form-control">
                <option value="" selected="selected">Choose provider</option>
                <option value="scopus">Scopus</option>
                <option value="wos">Web of Science</option>
            </select>
        </div>
        <input type="hidden" name="command" value="delete"/>
        <button type="submit" class="btn btn-default">Delete from store</button>
    </form>
</form>

    <footer>
        <script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <!-- Latest compiled and minified JavaScript -->
        <script src="//netdna.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
    </footer>
</div>
</body>
</html>