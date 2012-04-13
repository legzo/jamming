var barHeight = 4;
var height = 500;
var width = 500;

var format = d3.time.format("%Y-%m-%d_%H_%M");

var now = new Date();
var threeHoursAgo = new Date(now - 3 * 60 * 60 * 1000);
var from = format(threeHoursAgo);
var to = format(now);

var dates = [];
dates.push(threeHoursAgo);
dates.push(now);



$(document).ready(function() {
	
	var chart = d3
		.select("#evolGraph").append("svg")
		.attr("class", "chart")
		.attr("width", width + 20)
		.attr("height", height + 20)
		.append("g")
		.attr("transform", "translate(45,15)");
	
	$('#displayCurrent').click(function() {
		var now = new Date();
		var threeHoursAgo = new Date(now - 3 * 60 * 60 * 1000);
		var from = format(threeHoursAgo);
		var to = format(now);
		
		xhrFile('/rest/traffic/states/' + from + '/' + to);
	});
});

var xhrError = function() {
	$('#queryStatus').text("no file found");
	$("#evolGraph").empty();
	$('#summary').text('');
}

var treatData = function(result) {
	$('#queryStatus').text("got file!");
	$('#summary').text(result.numberOfSamples + ' samples found');

	var data = [];
	
	for ( var time in result.states) {
		var state = result.states[time];
		data.push({
				x: state.stateAsFloat,
				y: new Date(state.time)
			});
	}
	
	var now = new Date();
	dates = [];
	dates.push(new Date(now.getTime() - (3*60*60*1000)));
	dates.push(now);
	
	graph(data, dates);
};

var xhrFile = function(url) {
	$('#queryStatus').text("getting file...");

	$.ajax({
		url : url,
		dataType : 'json',
		success : treatData,
		error : xhrError
	});
}


var graph = function(data, dates) {
	var format = d3.time.format("%H:%M");
	
	var chart = d3.select("#evolGraph svg g");
	
	var chart = d3
	.select("#evolGraph svg g");

	var x = d3.scale
		.linear()
		.domain([ 0, 1 ])
		.rangeRound([ 0, 420 ]);
	
	var y = d3.time.scale()
		.domain(dates)
		.rangeRound([0, height]);
	
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
		.attr("y2", height)
		.style("stroke", "#ccc");
	
	chart.selectAll(".rule")
		.data(x.ticks(10))
		.enter()
		.append("text")
		.attr("class", "rule")
		.attr("x", x)
		.attr("y", -5)
		.attr("text-anchor", "middle").text(String);
	
	
	var yTicks = chart.selectAll("text.yrule").data(y.ticks(12));
	
	yTicks.enter()
		.append("text")
		.attr("class", "rule yrule")
		.attr("x", -20)
		.attr("text-anchor", "middle")
		.text(function(d, i) 
				{ return format(d); }
		)
		.attr("y", y)
		;
	
	yTicks
		.transition()
		.duration(300)
		.delay(300)
		.attr("y", y)
		;
	
	var selection = chart.selectAll("rect").data(data);

	selection.enter()
		.append("rect")
		.attr("y", function(d) { return y(d.y) })
		.attr("height", barHeight)
		.attr("fill", "red")
		.attr("width", 400)
		;
	
	selection
		.transition()
		.duration(300)
		.delay(300)
		.attr("y", function(d) { return y(d.y) })
		.attr("height", barHeight)
		.attr("fill", function(d) { return c(d.x) })
		.attr("width", function(d) { return x(d.x) })
		;	

}
