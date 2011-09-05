<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Deployment</title>
   <link rel="stylesheet" type="text/css" href="../terminal.css" media="screen" />
  <link rel="stylesheet" href="../css/autocomplete.css" type="text/css" media="all" />
  <link rel="stylesheet" href="../css/ui-lightness/jquery-ui-1.8.6.custom.css" type="text/css" media="all" />
  <script type="text/javascript" src="../js/jquery-1.4.2.min.js"></script>
  <script type="text/javascript" src="../js/jquery-ui-1.8.13.custom.js"></script>
  <script type="text/javascript" src="../js/jsonpath.js"></script>
</head>
<body>

<script language="text/javaScript" type="text/javascript">
 var gluJson = ${gluJsonStr};
 var  allAppsDuplicates =  jsonPath(gluJson, "$..app");
 </script>

 <script type="text/javascript" src="../js/servicesAutoComplete.js"></script>




<h2>~ $ Build at TeamCity and then deploy</h1>

<form name="input" method="get">
<table>
  <tr>
    <td>$ Services:</td>
    <td><input type="text" name="serviceName" size="100" id="services"/></td>
  </tr>
  <tr>
    <td>$ Tags:</td>
    <td><input type="text" name="tags" size="100" id="tags"/></td>
  </tr>
  <tr>
    <td>$ Version:</td>
    <td><input type="text" disabled="disabled" style="background-color: #eee" value="latest" /></td>
  </tr>
  <tr>
    <td>$ Who are you?</td>
    <td><input type="text" name="committer" /></td>
  <tr />
  <tr>
    <td>$ Message:
      <div class="comment">Will be used for yammer</div>
    </td>
    <td><input type="text" name="commitMessage" size="100" /></td>
  <tr />
  <tr>
    <td># Prepare Only:</td>
    <td><input type="checkbox" name="prepareOnly" /></td>
  <tr />
</table>
<input type="submit" value="Submit To TeamCity" onclick="removeServicesLastComma()" />
</form>

<c:import url="links.jsp" />
</body>
</html>