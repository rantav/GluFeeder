<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Glu Feeder Deployments Histogram</title>
<link rel="stylesheet" type="text/css" href="../terminal.css" media="screen" />
<script type="text/javascript" src="http://www.google.com/jsapi"></script>
<script type="text/javascript">
 google.load('visualization', '1', {'packages':['annotatedtimeline']});
 function drawChart() {
   var data = new google.visualization.DataTable();
   data.addColumn('date', 'Date');
<c:forEach items="${data.modules}" var="module">
data.addColumn('number', '${module}');
data.addColumn('string', 'title${module}');
data.addColumn('string', 'text${module}');
</c:forEach>

data.addRows([
<c:forEach items="${data.rows}" var="row">

 [new Date(${row.date.time}),
  <c:forEach items="${row.modulesData}" var="moduleData">
  ${moduleData.revision}, ${moduleData.title}, ${moduleData.text}, // ${moduleData.name}
  </c:forEach>
  ],
</c:forEach>
]);
   var chart = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div'));
   chart.draw(data, {
     displayAnnotations: true,
     displayExactValues: true,
     displayZoomButtons: false,
     legendPosition: 'newRow',
     scaleType: 'allmaximized',
     thickness: 2,
     highlightDot: 'last',
     displayAnnotationsFilter: true,
     allowHtml: true
   });
 }

 google.setOnLoadCallback(drawChart);
 </script>
</head>
<body>
<h2>~ $ Glu Feeder Deployments</h2>


 <div id='chart_div' style='width: 1200px; height: 600px;'></div>

<c:import url="links.jsp" />
</body>
</html>