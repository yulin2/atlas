<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ page trimDirectiveWhitespaces="true" %>
<%@page session="false"%>
<%@page pageEncoding="UTF-8"%>
<%@page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<head>
</head>
<body>
<h1>Add aliases!</h1>
<c:if test="${info != null}">
<c:forEach items="${info}" var="msg">
     <span style="color: blue;"><c:out value="${msg}"/></span><br/>
</c:forEach>
</c:if>
<c:if test="${errors != null}">
<c:forEach items="${errors}" var="error">
    <span style="color: red;"><c:out value="${error}"/></span><br/>
</c:forEach>
</c:if>
<form method="post">
<p>(csv format, e.g. http://graph.facebook.com/12533191290,http://www.bbc.co.uk/programmes/b0070rj8)</p>
<textarea name="csvAliases"cols="100" rows="50"></textarea>
<input type="submit" value="upload"/>
</form>

</body>
</html>