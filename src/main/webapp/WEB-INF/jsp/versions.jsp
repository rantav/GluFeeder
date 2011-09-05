<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="content-type" content="text/html; charset=utf-8" />
<title>Live Versions Variation</title>
<link rel="stylesheet" type="text/css" href="../terminal.css" media="screen" />
<script type="text/javascript" src="http://www.google.com/jsapi"></script>
 <script type="text/javascript">
   google.load('visualization', '1', {packages: ['corechart', 'table']});
 </script>
<script type="text/javascript">
function drawVisualization() {
  // Populate the data table.
  var data = google.visualization.arrayToDataTable([
<c:forEach items="${data}" var="entry">
     ['${entry.module}',${entry.min}, ${entry.max}, '${entry.minAgentsHtml}', '${entry.maxAgentsHtml}'],
</c:forEach>
   ], true);
  data.setColumnLabel(0, 'Module');
  data.setColumnLabel(1, 'Min');
  data.setColumnLabel(2, 'Max');
  data.setColumnLabel(3, 'Min Agents');
  data.setColumnLabel(4, 'Max Agents');

  // Draw the chart.
  var chart = new google.visualization.CandlestickChart(document.getElementById('chart'));
  var chartView = new google.visualization.DataView(data);
  chartView.setColumns([0, 1, 1, 2, 2]);
  chart.draw(chartView, {legend:'none', hAxis: {showTextEvery: 1, slantedText: true, slantedTextAngle: 90},
    title: 'Versions according to the LIVE model'});

  var table = new google.visualization.Table(document.getElementById('table'));
  var tableView = new google.visualization.DataView(data);
  tableView.setColumns([0, 1, 3, 2, 4]);
  table.draw(tableView, {showRowNumber: false, allowHtml: true});
}
google.setOnLoadCallback(drawVisualization);
 </script>
</head>
<body>
<h2>~ $ Live Versions Variation</h2>
 <c:import url="links.jsp" />
 <div id='chart' style='width: 1200px; height: 600px;'></div>
 <div id='table' style='margin-top:20px'></div>
<c:import url="links.jsp" />
</body>
</html>