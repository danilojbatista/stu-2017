<%@ page import="java.io.*,java.util.*" %>
<html>
<head>
</head>
<body>
<%
   // New location to be redirected
   String site = new String("feed");
   response.setHeader("Location", site); 
%>
</body>
</html>