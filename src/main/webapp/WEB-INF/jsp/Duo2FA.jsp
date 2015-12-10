<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!doctype html>
	<head>
		<meta charset="iso-8859-1">
		<title>2Factor Authentication | The University of Chicago</title>
		<script src="<spring:url value='/resources/js/libs/modernizr.js'/>"></script>
		<script src="<spring:url value='/resources/js/Duo-Web-v2.min.js'/>"></script>
	</head>
	<body>
			<script>
				Duo.init({
					'host': '${DuoHost}',
					'sig_request': '${DuoReq}',
					'post_argument': 'signedDuoResponse'
				});
			</script>
			<form:form method="post" commandName="duo_form">
			</form:form>
			<h1>2Factor Authentication</h1>
			<iframe id="duo_iframe" width="100%" height="360" frameborder="0"></iframe>
	</body>
</html>
