<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h2>Altmetrics from ${provider} for handle ${handle}</h2>

<div class="altmetrics-data">
  <c:if test="${not empty result}">
  <table id="metrics" class="table table-striped">
    <thead>
    <tr>
      <th>Metric name</th>
      <th>Metric value</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${result.metrics}" var="metric">
      <tr>
        <td>${metric.key}</td>
        <td>
          <c:choose>
          <c:when test="${fn:endsWith(metric.key, '-link') and (fn:startsWith(metric.value, 'http://') or fn:startsWith(metric.value, 'https://'))}">
            <a href="${metric.value}">${metric.value}</a>
          </c:when>
            <c:otherwise>${metric.value}</c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:forEach>
    </tbody>
  </table>
  </c:if>
  <p>Last updated: ${result.lastUpdated}</p>
</div>