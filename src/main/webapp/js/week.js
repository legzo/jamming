
var format = d3.time.format("%Y-%m-%d_%H_%M");
var dayFormat = d3.time.format("%Y-%m-%d");

var height = 800;
var width = 720;

var rulesDone = {
	'morning' : false,
	'evening' : false
};

$(document).ready(function() {
	
	var chart = d3
		.select("#evolGraph").append("svg")
		.attr("class", "chart")
		.attr("width", width + 20)
		.attr("height", height + 20)
		.append("g")
		.attr("transform", "translate(15,45)");
	
	$('#displayCurrent').click(function() {
		var startAsString = '2012-03-26_00_00';
		var endAsString = '2012-03-30_20_00';
		
		xhrFile('/rest/traffic/states/' + startAsString + '/' + endAsString, 0);

		startAsString = '2012-04-02_00_00';
		endAsString = '2012-04-06_20_00';
		
		xhrFile('/rest/traffic/states/' + startAsString + '/' + endAsString, 1);

		startAsString = '2012-04-09_00_00';
		endAsString = '2012-04-13_20_00';
		
		xhrFile('/rest/traffic/states/' + startAsString + '/' + endAsString, 2);

		startAsString = '2012-04-16_00_00';
		endAsString = '2012-04-20_20_00';
		
		xhrFile('/rest/traffic/states/' + startAsString + '/' + endAsString, 3);
	});
});

var xhrError = function() {
	$('#queryStatus').text("no file found");
	$("#evolGraph").empty();
	$('#summary').text('');
}

var treatData = function(weekIndex) {
	var resultFunction = function(result) {
		$('#queryStatus').text("got file!");
	
		$('#summary').text(result.numberOfSamples + ' samples found');
	
		var weeks = {};
		
		for (var time in result.states) {
			
			var date = new Date(time);
			var day = date.getDay();
			
			var state = result.states[time];
			if(!weeks[day]) {
				weeks[day] = {
					'morning' : [],
					'evening' : []
				};
			}
			
			stateObj = {
				value: state.stateAsFloat,
				dayIndex: day - 1,
				x: new Date(state.time)
			}
			
			if(date.getHours() < 12) {
				weeks[day]['morning'].push(stateObj);
			} else {
				weeks[day]['evening'].push(stateObj);
			}
		}
		
		for(var dayIndex in weeks) {
			console.log('graphing day ' + dayIndex);
			graphDay(weeks, weekIndex, dayIndex, 'morning');
			graphDay(weeks, weekIndex, dayIndex, 'evening');
		}
	}
	return resultFunction;
};

var graphDay = function(week, weekIndex, dayIndex, timeOfDay) {
	var dates = [];
	var data = week[dayIndex][timeOfDay];
	
	if(data.length > 0) {
	
		for(var index in data) {
			var datum = data[index];
			
			dates.push(datum.x);
		}
		var extremaDates = [];
		var startDate = d3.min(dates);
		extremaDates.push(startDate);
		extremaDates.push(new Date(startDate.getTime() + (3*60*60*1000)));
		
		graph(data, extremaDates, weekIndex, dayIndex, timeOfDay);
	}
}

var xhrFile = function(url, weekIndex) {
	$('#queryStatus').text("getting file...");

	$.ajax({
		url : url,
		dataType : 'json',
		success : treatData(weekIndex),
		error : xhrError
	});
}

var graph = function(data, dates, weekIndex, dayIndex, timeOfDay) {
	var barHeight = 20;
	var vPadding = 5;

	var chart = d3.select("#evolGraph svg g");
	
	var chart = d3
		.select("#evolGraph svg g");
	
	var x = d3.time.scale()
		.domain(dates)
		.rangeRound([0, 180]);

	var c = d3.scale.linear()
		.domain([0, 1])
		.range(["green", "red"])
		.interpolate(d3.interpolateHsl);
	
	var selection = chart.selectAll("rect.day_" + dayIndex + '_' + timeOfDay).data(data);

	var xOffset = 0;
	if(timeOfDay == 'evening'){
		xOffset = 210;
	}
	
	var y = function(dayIndex) {
		 return dayIndex * (barHeight + vPadding) + 150 * weekIndex;
	}
	
	var xForTicks = function(d) {
		return x(d) + xOffset;
	}  
	
	if(!rulesDone[timeOfDay]){
		chart.selectAll(".yrule_" + timeOfDay)
			.data(x.ticks(7))
			.enter()
			.append("line")
			.attr("class", "yrule_" + timeOfDay)
			.attr("x1", function(d) {return xForTicks(d) })
			.attr("y1", 0)
			.attr("x2", function(d) {return xForTicks(d) })
			.attr("y2", 600)
			.style("stroke", "#ccc")
			;
		
		chart.selectAll(".yruleText_" + timeOfDay)
			.data(x.ticks(3))
			.enter()
			.append("text")
			.attr("class", "rule yruleText_" + timeOfDay)
			.attr("x", function(d) {return xForTicks(d) })
			.attr("y", -10)
			.attr("text-anchor", "middle")
			.text(function(d, i) 
				{ return d3.time.format("%H:%M")(d); }
			);
		
		rulesDone[timeOfDay] = true;
	}
	
	selection.enter()
		.append("rect")
		.attr("x", function(d) { return x(d.x) + xOffset })
		.attr("y", function(d) { return y(d.dayIndex) })
		.attr("height", barHeight)
		.attr("fill", function(d) { return c(d.value) })
		.attr("width", 1)
		;	

	
}
