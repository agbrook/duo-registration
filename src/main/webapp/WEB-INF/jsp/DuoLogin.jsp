<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!doctype html>
	<head>
		<meta charset="iso-8859-1">
		<title>2Factor Authentication Login</title>
	</head>
	<body onload='document.f.j_username.focus();'>
			<h1 class="page-title">2Factor Authentication Login</h1>
			<h2>Login with BSDAD Username and Password</h2>
			<form name='loginForm' action="<spring:url value='/j_spring_security_check' />" method='POST'>
				<fieldset>
					<c:if test="${not empty error}">
						<div class="alert alert-error">
							Invalid username and password.
						</div>
					</c:if>
					<c:if test="${not empty msg}">
						<div th:if="${param.logout}" class="alert alert-success">
							You have been logged out.
						</div>
					</c:if>
					<label for="username">Username</label>
					<input type="text" id="username" name="username"/>
					<label for="password">Password</label>
					<input type="password" id="password" name="password"  autocomplete="off"/>    
					<button type="submit" class="btn">Log in</button>
				</fieldset>
			</form>
	</body>
</html>	
