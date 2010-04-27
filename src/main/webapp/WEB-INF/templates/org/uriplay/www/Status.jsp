<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Server Status</title>
</head>

<body>
<p>Server is alive</p>
<c:choose>
<c:when test="${not empty next_hi}">
<p>Sequence retrieved from Database is ${next_hi}</p>
</c:when>
<c:otherwise>
<p>No connection to the Database</p>
</c:otherwise>
</c:choose>
</body>

</html>