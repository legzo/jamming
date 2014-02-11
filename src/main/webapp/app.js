var format = d3.time.format.iso;

var client = new elasticsearch.Client({
  host: 'data-jte.beta.kermit.orange-labs.fr',
  log: 'info'
});

var drawHistory = function(givenDirection, hours) {
  var now = Date.now() + (1 * 60 * 60 * 1000);

  client.search({
      index: 'trafic',
      type: 'state-partial',
      size: hours * 60 * 60,
      sort: 'time:asc',
      body: {
        filter: {
          and: [
            {
              term: { direction: givenDirection }
            }, {
              range: {
                time : {
                  gte : now - (hours * 60 * 60 * 1000),
                  lte : now
                }
              }
            }
          ]
      }
    }
  }).then(function (resp) {
      var hits = resp.hits.hits;
      console.log(hits.length + ' docs fetched');
      drawGraph(givenDirection, hits);
  }, function (err) {
      console.trace(err.message);
  });
};

var dateFn = function(d) { return format.parse(d._source.time); };

var amountFn = function(d) { return d._source.stateAsFloat };

var drawGraph = function(label, hits) {
  
  $('#graph-' + label).empty();

  var x = d3.time.scale()
            .range([0, getWidth()])
            .domain(d3.extent(hits, dateFn));

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

  var bars = 
    svgContainer.selectAll('rect')
                .data(hits)
                .enter()
                .append('rect')
                .attr('fill', function(d) { return color(amountFn(d)) })
                .attr('x', function(d) { return x(dateFn(d)) })
                .attr('y', function(d) { return y(amountFn(d)) })
                .attr('height', 120)
                .attr('width', getBarWidth(hits))
                .text(function(d) { return amountFn(d) });

}

var getWidth = function() {
  return $('#graphs').width();
}

var getHeight = function() {
  return 80;
}

var getBarWidth = function(hits) {
  var barWidth = getWidth() / hits.length + 1;
  return barWidth;
}

var drawGraphsForTimespan = function(hours) {
  drawHistory('inner', hours);
  drawHistory('outer', hours);
}

var updateNav = function(elt) {
  $('.nav li').removeClass();
  $('#' + elt.id).addClass('active');

}

$(document).ready(function() {
  drawGraphsForTimespan(3);
  
  $('#show-now').click(function() {
    drawGraphsForTimespan(3);
    updateNav(this);
  });

  $('#show-day').click(function() {
    drawGraphsForTimespan(24);
    updateNav(this);
  });

});