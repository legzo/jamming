$(document).ready(function() {
	getFiles();

	$('#displayCurrent').click(function() {
		xhrFile('/rest/traffic/history');
	});
});

var getFilesCallback = function(files) {
	for (var key in files) {
		var file = files[key];
		var month = file.substring(6, 8) - 1; 
		var date = new Date(2012, month, file.substring(9, 11));
		
		var partOfDay = "AM";
		
		if(file.substring(12, 14) == '19') {
			partOfDay = "PM";
		}
		
		var $fileLi = $("<li><a href='#'>" + date.toString("dddd d MMMM ") + partOfDay + "</a></li>");
		
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
	graph(data, d3.extent(dates));
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
	
	chart.selectAll("rect").data(data)
		.enter()
		.append("rect")
		.attr("y", function(d) { return y(d.y) })
		.attr("width", function(d) { return x(d.x) })
		.attr("height", barHeight)
		.attr("fill", function(d) { return c(d.x) });

	chart.selectAll(".rule")
		.data(x.ticks(10))
		.enter()
		.append("text")
		.attr("class", "rule")
		.attr("x", x)
		.attr("y", 0)
		.attr("dy", -3)
		.attr("text-anchor", "middle").text(String);
	
	chart.selectAll(".yrule")
		.data(y.ticks(10))
		.enter()
		.append("text")
		.attr("class", "rule")
		.attr("x", 0)
		.attr("y", y)
		.attr("dx", -30)
		.attr("text-anchor", "middle")
		.text(function(d, i) 
				{ return format(d); }
		);
}
