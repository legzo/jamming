$(document).ready(function() {
	getFiles();

	$('#displayCurrent').click(function() {
		xhrFile('/rest/traffic/history');
	});
});

var dateInputFormat = d3.time.format("bison-%m-%d-%H_%M_%S.json");
var dateOutputFormat = d3.time.format("%a %d %B ");

var getFilesCallback = function(files) {
	for (var key in files) {
		var file = files[key];

		var date = dateInputFormat.parse(file);
		date.setFullYear(2012);
		
		var partOfDay = "AM";
		
		if(date.getHours() > 12) {
			partOfDay = "PM";
		}
		
		var $fileLi = $("<li><a href='#'>" + dateOutputFormat(date) + partOfDay + "</a></li>");
		
		$fileLi.attr(
				{
					id: file,
					rel: file
				}
			)
		
		$("#files").append($fileLi);
	}
	
	$("#files").delegate("li", "click", function() {
		var url = '/rest/traffic/files/' + $(this).attr("rel");
		xhrFile(url);
	});
}

var getFiles = function() {

	$.ajax({
		url : '/rest/traffic/files',
		dataType : 'json',
		success : getFilesCallback,
		error : function() {
			alert('error getting files');
		}
	});
}

var xhrError = function() {
	$('#queryStatus').text("no file found");
	$("#evolGraph").empty();
	$('#summary').text('');
}

var treatData = function(result) {
	$('#history').show();
	$('#queryStatus').text("got file!");
	$('#summary').text(result.numberOfSamples + ' samples found');

	var data = [];
	var dates = [];
	
	for ( var time in result.states) {
		var state = result.states[time];
		data.push({
				x: state.stateAsFloat,
				y: new Date(state.time)
			});
		
		dates.push(new Date(state.time));
	}
	
	var extremaDates = [];
	var startDate = d3.min(dates);
	extremaDates.push(startDate);
	extremaDates.push(new Date(startDate.getTime() + (3*60*60*1000)));
	
	graph(data, extremaDates);
};

var xhrFile = function(url) {
	$('#history').hide();
	$('#queryStatus').text("getting file...");

	$.ajax({
		url : url,
		dataType : 'json',
		success : treatData,
		error : xhrError
	});
}

var graph = function(data, dates) {
	$("#evolGraph").empty();
	
	var format = d3.time.format("%H:%M");
	var barHeight = 4;

	var chart = d3
		.select("#evolGraph").append("svg")
			.attr("class", "chart")
			.attr("width", 500)
			.attr("height", 500)
			.append("g")
			.attr("transform", "translate(45,15)");

	var x = d3.scale
		.linear()
		.domain([ 0, 1 ])
		.range([ 0, 420 ]);
	
	var y = d3.time.scale()
		.domain(dates)
		.range([0, 500]);
	
	var c = d3.scale.linear()
    	.domain([0, 1])
    	.range(["green", "red"])
    	.interpolate(d3.interpolateHsl);

	chart.selectAll("line")
		.data(x.ticks(10))
		.enter()
		.append("line")
		.attr("x1",	x)
		.attr("x2", x)
		.attr("y1", 0)
		.attr("y2", barHeight * data.length)
		.style("stroke", "#ccc");
	
	chart.selectAll(".rule")
		.data(x.ticks(10))
		.enter()
		.append("text")
		.attr("class", "rule")
		.attr("x", x)
		.attr("y", -5)
		.attr("text-anchor", "middle").text(String);
	
	chart.selectAll(".yrule")
		.data(y.ticks(12))
		.enter()
		.append("text")
		.attr("class", "rule")
		.attr("x", -20)
		.attr("y", y)
		.attr("text-anchor", "middle")
		.text(function(d, i) 
				{ return format(d); }
		);
	
	chart.selectAll("rect").data(data)
		.enter()
		.append("rect")
		.attr("y", function(d) { return y(d.y) })
		.attr("height", barHeight)
		.attr("fill", function(d) { return c(d.x) })
		.transition()
		.duration(300)
		.attr("width", function(d) { return x(d.x) });

}
