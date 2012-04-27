var dateInputFormat = d3.time.format("bison-%m-%d-%H_%M_%S.json");
var calendarFormatAsString = "yyyy-mm-dd";
var calendarFormat = d3.time.format("%Y-%m-%d");
var maxTimeFormat = d3.time.format("%A %d %B - max @ %H:%M");
var barHeight = 4;
var height = 500;
var width = 500;
var cache = {};

$(document).ready(function() {
	
	var today = calendarFormat(new Date());
	
	var chart = d3
			.select("#evolGraph").append("svg")
			.attr("class", "chart")
			.attr("width", width + 20)
			.attr("height", height + 20)
		.append("g")
			.attr("transform", "translate(45,15)");
	
	$('#datepicker-am').val(today);
	$('#datepicker-pm').val(today);
	
	$('#datepicker-am').datepicker({
		format : calendarFormatAsString
	}).on('changeDate', dateChosenCallbackAM);

	$('#datepicker-pm').datepicker({
		format : calendarFormatAsString
	}).on('changeDate', dateChosenCallbackPM);
	
	
	$('#displayCurrent').click(function() {
		xhrFile('/rest/traffic/history');
	});
});

var dateChosenCallbackAM = function(ev) {
	dateChosenCallback('am', '-09_59_59.json');
};
var dateChosenCallbackPM = function(ev) {
	dateChosenCallback('pm', '-19_59_59.json');
};

var dateChosenCallback = function(id, suffix) {
	var datePicker = $('#datepicker-' + id);
	
	datePicker.datepicker('hide');
	datePicker.blur();

	var url = '/rest/traffic/files/bison-'
			+ datePicker.val().substring(5) + suffix;

	xhrFile(url);
};

var xhrError = function() {
	$('#queryStatus').text("no file found");
	$("#evolGraph").empty();
	$('#summary').text('');
}

var getTreatAndCacheDataCallback = function(url) {
	return function(result) {
		$('#history').show();
		
		cache[url] = result;
		
		var maxTime = maxTimeFormat(new Date(result.max.time));
		
		$('#queryStatus').text("got file! " + maxTime);
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
} 

var xhrFile = function(url) {
	$('#history').hide();
	$('#queryStatus').text("getting file...");

	
	var cachedResult = cache[url];
	var callback = getTreatAndCacheDataCallback(url);
	
	if(cachedResult) {
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

var graph = function(data, dates) {
	
	var format = d3.time.format("%H:%M");

	var chart = d3
		.select("#evolGraph svg g");

	var x = d3.scale
		.linear()
		.domain([ 0, 1 ])
		.range([ 0, 420 ]);
	
	var y = d3.time.scale()
		.domain(dates)
		.range([0, height]);
	
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
		.text(function(d, i) 
				{ return format(d); }
		)
		;
	
	var selection = chart.selectAll("rect").data(data);

	selection.enter()
		.append("rect")
		.attr("y", function(d) { return y(d.y) })
		.attr("height", barHeight)
		.attr("width", 0)
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
