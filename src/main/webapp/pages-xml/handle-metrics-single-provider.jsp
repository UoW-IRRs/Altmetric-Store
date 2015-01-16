<?xml version="1.0" encoding="UTF-8"?>

<%@ page contentType="text/xml;charset=UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<altmetrics>
<c:if test="${not empty result}">
    <handle>${result.handle}</handle>
    <provider>${result.provider}</provider>
    <last-updated>${result.lastUpdated}</last-updated>
    <metrics>
        <c:forEach items="${result.metrics}" var="metric">
            <name>${metric.key}</name>
            <value>${metric.value}</value>
        </c:forEach>
    </metrics>
</c:if>
</altmetrics>