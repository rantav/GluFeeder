<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="my" tagdir="/WEB-INF/tags" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Glu Feeder Modules</title>
<link rel="stylesheet" type="text/css" href="../terminal.css" media="screen" />
</head>
<body>
<h2>~ $ Glu Feeder Modules ${today}</h2>

<c:import url="links.jsp" />
<c:forEach items="${modules}" var="module">
	<h3>All deployments for module <a href="modules?modules=${module.name}">${module.name}</a>:</h3>
	<my:deploymentsList deployments="${module.deployments}" allowModification="true"/>
</c:forEach>
<c:import url="links.jsp" />
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
</body>
</html>