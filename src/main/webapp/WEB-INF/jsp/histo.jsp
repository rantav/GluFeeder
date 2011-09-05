<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Glu Feeder Deployments Histogram</title>
<link rel="stylesheet" type="text/css" href="../terminal.css" media="screen" />
<fmt:setTimeZone value="Israel" scope="session"/>
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script type="text/javascript">
      google.load("visualization", "1", {packages:["corechart"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Date');
        data.addColumn('number', 'Failed');
        data.addColumn('number', 'Success');
<c:forEach items="${histo}" var="entry" varStatus="status">
        data.addRows(1);
        data.setValue(${status.count - 1}, 0, '<fmt:formatDate value="${entry.key}" pattern="dd-MM"/>');
        data.setValue(${status.count - 1}, 1, ${entry.value.failed});
        data.setValue(${status.count - 1}, 2, ${entry.value.success});
</c:forEach>
        data.sort([{column: 0, desc: false}]);
        var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));
        chart.draw(data, {width: 800, height: 600, title: 'Deployments by date',
                          hAxis: {title: 'Date', titleTextStyle: {color: 'white'}},
                          backgroundColor: '#aaa', legend: 'none', isStacked: true,
                          colors:['red','green']
                         });
      }
    </script>
</head>
<body>
<h2>~ $ Glu Feeder Deployments Histogram</h2>


<div id="chart_div" style="text-align: center;vertical-align: center"></div>

<c:import url="links.jsp" />
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.0/jquery.min.js"></script>
</body>
</html>