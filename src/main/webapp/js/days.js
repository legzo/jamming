var calendarFormatAsString = "yyyy-mm-dd";
var calendarFormat = d3.time.format("%Y-%m-%d");
var truncateFormat = d3.time.format("%H:%M:%S");
var height = 100;
var width = 250;
var r = 4.5;
var nbOfDays = 5;
var cache = {};

$(document).ready(function() {
	
	for(var i=2; i<= 1 + nbOfDays; i++) {
		getDataForDay(i);
	}
	
});

var getDataForDay = function(i) {
	d3.select("#evolGraph"+i).append("svg")
		.attr("class", "chart")
		.attr("width", width + 20)
		.attr("height", height + 20)
	.append("g")
		.attr("transform", "translate(45,15)");
		
	setTimeout(function() {
		xhrFile('/rest/traffic/statesForDay/' + i + '/AM', {
			noCache : true
		});
	}, 5000 * (i-2));		
}

var xhrError = function() {
	$('#queryStatus').text("no data found");
	$("#evolGraph").empty();
	$('#summary').text('');
}

var getTreatAndCacheDataCallback = function(url) {
	return function(result) {
		$('#history').show();
		
		cache[url] = result;
		
		$('#queryStatus').text("got data!");
		$('#summary').text(result.length + ' samples found');

		var data = [];
		var dates = [];
		
		var dayIndex = result[0].dayIndex;
		
		for (var index in result) {
			var state = result[index];
			var timeAsString = truncateFormat(new Date(state.time));
			var time = truncateFormat.parse(timeAsString);
			
			data.push({
					x: state.summary,
					y: time
				});
			
			dates.push(time);
		}
		
		var extremaDates = [];
		var startDate = d3.min(dates);
		extremaDates.push(startDate);
		extremaDates.push(new Date(startDate.getTime() + (3*60*60*1000)));
		
		graph(dayIndex, data, extremaDates);
	}; 
} 

var xhrFile = function(url, options) {
	$('#history').hide();
	$('#queryStatus').text("getting data...");
	
	var cachedResult = cache[url];
	var callback = getTreatAndCacheDataCallback(url);
	
	var useCache = !(options && options.noCache);
	
	if(cachedResult && useCache) {
		callback(cachedResult);
	} else {
		$.ajax({
			url : url,
			dataType : 'json',
			success : callback,
			error : xhrError
		});
	} 
}

var graph = function(dayIndex, data, dates) {
	
	var format = d3.time.format("%H:%M");

	var chart = d3
		.select("#evolGraph" + dayIndex + " svg g");

	var x = d3.scale
		.linear()
		.domain([ 0, 1 ])
		.range([ 0, width ]);
	
	var y = d3.time.scale()
		.domain(dates)
		.range([0, height]);
	
	var c = d3.scale.linear()
    	.domain([0, 1])
    	.range(["green", "red"])
    	.interpolate(d3.interpolateHsl);

	chart.selectAll("line")
			.data(x.ticks(1))
		.enter()
			.append("line")
			.attr("x1",	x)
			.attr("x2", x)
			.attr("y1", 0)
			.attr("y2", height)
			.style("stroke", "#ccc");
	
	var yTicks = chart.selectAll("text.yrule").data(y.ticks(6));
		
	yTicks.enter()
		.append("text")
		.attr("class", "rule yrule")
		.attr("x", -20)
		.attr("text-anchor", "middle")
		.text(function(d, i) 
				{ return format(d); }
		)
		.attr("y", function(d, i) 
				{ return y(d) + 5; })
		;
	
	var selection = chart.selectAll("rect").data(data);

	selection.enter()
		.append("circle")
		.attr("cx", function(d) { return x(d.x) })
		.attr("cy", function(d) { return y(d.y) })
		.attr("fill", function(d) { return c(d.x) })
		.attr("r", r)
		;

}
