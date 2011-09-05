<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Submitted to TeamCity</title>
	<link rel="stylesheet" type="text/css" href="../terminal.css" media="screen" /> 
</head>
<body>
  <h2>~ Submitted to TeamCity</h2>
  <div>Module: ${form.serviceName}</div>
  <div>Tags: ${form.tags}</div>
  <div>By: ${form.committer}</div>
  <div>Message: ${form.commitMessage}</div>
  <div><a href="${teamcUrl}">See it here</a></div>
  <c:import url="links.jsp" />
</body>
</html>