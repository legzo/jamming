var myformat = d3.time.format("%H:%M");
var fullFormat = d3.time.format(
  "%Y-%m-%d");

var client = new elasticsearch.Client({
  host: 'jamming-data.kermit.orange-labs.fr',
  log: 'error'
});

var hitz = { outer: [], inner: [] };

var remainingRequests = 0;
var totalRequests = 0;
var totalDocs = 0;

var drawHistory = function(givenDirection, givenDate, givenDayOfWeek) {

  var query = {
      index: 'trafic',
      type: 'state-partial',
      size: 4 * 24 * 60,
      sort: 'time:asc',
      body: {
        filter: {
          and: [
            {
              term: { direction: givenDirection }
            }
          ]
      }
    }
  };

  if(givenDate) {
    query.body.filter.and.push({term: { date: givenDate}});
  }

   if(givenDayOfWeek) {
    query.body.filter.and.push({term: { dayOfWeek: givenDayOfWeek}});
  }

  updateCounter(+1);

  client.search(query).then(function (resp) {
      updateCounter(-1);
      var hits = resp.hits.hits;
      console.log(hits.length + ' docs fetched');
      totalDocs += hits.length;

      if(hits.length > 0) {
        hitz[givenDirection].push(hits);
        drawGraph(givenDirection, hitz[givenDirection]);
      }

  }, function (err) {
      updateCounter(-1);
      console.trace(err.message);
  });
};

var updateCounter = function(toBeAdded) {
  remainingRequests += toBeAdded;
   

   if(remainingRequests > 0) {
     $('#counter').text(remainingRequests + '/' + 2 * totalRequests);
     $('#loader').show();
   } else {
     $('#counter').text(totalRequests + ' done');
     $('#loader').hide();
   }

   $('#counter-docs').text(abbrNum(totalDocs, 2) + ' docs' );
};

var dateFn = function(d) { 
  var zou = myformat.parse(d._source.instant);

  if(zou === null || zou === undefined || isNaN(zou)) {
    console.log('date fail ', d._source);
  }

  return zou; 
};

var amountFn = function(d) {
   var value = d._source.stateAsFloat; 

   if(isNaN(value)) {
    return 0;
   }

   return value;
 };

var drawGraph = function(label, hits) {
  
  $('#graph-' + label).empty();

  var x = d3.time.scale()
            .range([0, getWidth()])
            .domain(d3.extent(hits[0], dateFn));

  // colorbrewer RdYlGr11 http://bl.ocks.org/mbostock/5577023
  var colsRdYlGr11 = ["#a50026","#d73027","#f46d43","#fdae61","#fee08b","#ffffbf","#d9ef8b","#a6d96a","#66bd63","#1a9850","#006837"];

  var color = d3.scale.linear()
                .range(colsRdYlGr11.reverse())
                .domain(d3.range(0,1,0.1));

  var y = d3.scale.linear()
                .range([getHeight(), 0])
                .domain([0,1]);

  var svgContainer = 
    d3.select('#graph-' + label)
      .append('svg')
      .attr('id', 'svg-' + label)
      .attr('width', getWidth())
      .attr('height', getHeight());

  var line = d3.svg.line()
    .interpolate("basis")
    .x(function(d) { 
        return x(dateFn(d)); 
    })
    .y(function(d) {
        return y(amountFn(d)); 
    });

  var bars = 
    svgContainer.selectAll('path')
                .data(hits)
                .enter()
                .append('path')
                .attr("class", "line")
                .attr("d", function(d) { 
                  return line(d.filter(function(item) {
                                                  return !isNaN(amountFn(item));
                                                })
                );})
                ;
};

var getWidth = function() {
  return $('#graphs').width();
};

var getHeight = function() {
  return 180;
};

var getBarWidth = function(hits) {
  var barWidth = getWidth() / hits.length + 1;
  return barWidth;
};

var drawGraphsForDate = function(givenDate, givenDayOfWeek) {
  console.log('requesting data for ' + givenDate);
  drawHistory('inner', givenDate, givenDayOfWeek);
  drawHistory('outer', givenDate, givenDayOfWeek);
};

var updateNav = function(elt) {
  $('.nav li').removeClass();
  $('#' + elt.id).addClass('active');

};

var leftPad = function(i) {
  return ("00" + i).slice(-2);
};

var drawDay = function(wantedDayOfWeek) {
  var daysOfWeek = ['dimanche', 'lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi'];

  for(var month = 2; month <= 12; month++) {
    for (var i = 1; i <= 30; i++) {
      var day = '2014-' + leftPad(month) + '-'+ leftPad(i);
      

      var date = fullFormat.parse(day);
      var dayOfWeek = date.getDay();
      if(dayOfWeek+1 === wantedDayOfWeek) {
        totalRequests++;
        window.setInterval(drawGraphsForDate(day, wantedDayOfWeek), 500);
      }
    }
  }

  $('#title').text(daysOfWeek[wantedDayOfWeek-1]);
};


$(document).ready(function() {

  var wantedDayOfWeek = 2; // 1 = dimanche
                           // 2 = lundi
                           // 3 = mardi
                           // 4 = mercredi
                           // 5 = jeudi
                           // 6 = vendredi
                           // 7 = samedi

  //drawDay(wantedDayOfWeek);

  $('#show-monday').click(function() {
    drawDay(2);
    updateNav(this);
  });

  $('#show-tuesday').click(function() {
    drawDay(3);
    updateNav(this);
  });

  $('#show-wednesday').click(function() {
    drawDay(4);
    updateNav(this);
  });

  $('#show-thursday').click(function() {
    drawDay(5);
    updateNav(this);
  });

  $('#show-friday').click(function() {
    drawDay(6);
    updateNav(this);
  });

  $('#show-saturday').click(function() {
    drawDay(7);
    updateNav(this);
  });

  $('#show-sunday').click(function() {
    drawDay(1);
    updateNav(this);
  });
  
  console.log('done');
});