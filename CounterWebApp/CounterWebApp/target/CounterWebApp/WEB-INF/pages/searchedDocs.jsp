<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<h1 class="space"; style="color:BLACK;font-size:30px">KHOJ <sub style="color:BLACK;font-size:15px"> Document Searcher </sub></h1><i></i> 

		<b>Total Results:</b>
		<cout>${fn:length(foundResults)}</cout>
	
		</br>
		<c:forEach var="data" items="${foundResults}">
			<c:forEach var="highlight" items="${data.highlightedText}">
			<p>
				<b style="font-size:20px;">File Path:</b>
				<cout style="color:Blue;font-size:20px">${data.docPath}</cout>
				<sub><b>Relevance:</b>
				<cout style="color:Blue;">${data.score}</cout></sub>
				<br>
				<b>Documnet Snippets:</b><br>
				
				<i><cout style="color:Green;">${highlight}</cout></i>
				<br></br>
				</c:forEach>
		</c:forEach>
		</p>
	
</body>
</html>