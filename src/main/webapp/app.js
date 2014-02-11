var format = d3.time.format.iso;

var client = new elasticsearch.Client({
  host: 'data-jte.beta.kermit.orange-labs.fr',
  log: 'info'
});

var drawHistory = function(givenDirection) {
  var now = Date.now() + (1 * 60 * 60 * 1000);

  client.search({
      index: 'trafic',
      type: 'state-partial',
      size: 200,
      sort: 'time:asc',
      body: {
        filter: {
          and: [
            {
              term: { direction: givenDirection }
            }, {
              range: {
                time : {
                  gte : now - (2 * 60 * 60 * 1000),
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
      /*drawPeriod(givenDirection, hits);*/
      drawGraph(givenDirection, hits);
  }, function (err) {
      console.trace(err.message);
  });
};

var dateFn = function(d) { return format.parse(d._source.time); };

var amountFn = function(d) { return d._source.stateAsFloat };

var drawGraph = function(label, hits) {
  
  var x = d3.time.scale()
            .range([0, getWidth()])
            .domain(d3.extent(hits, dateFn));

  var color = d3.scale.linear()
                .range(["green", "red"])
                .domain(d3.extent(hits, amountFn));

  var y = d3.scale.linear()
                .range([getHeight(), 0])
                .domain(d3.extent(hits, amountFn));

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
                .attr('width', 2);

}

var drawPeriod = function(label, hits) {
  var history = '';
  for(var index in hits){
    var hit = hits[index]._source;
    history += getAsString(hit.stateAsFloat);
  }
  console.log(history);
  $('#history-' + label).append(history);
};

var getAsString = function(stateAsFloat) {
  var result = "@";

  if (stateAsFloat < 0.6) {
    result = "O";
  }
  if (stateAsFloat < 0.4) {
    result = "o";
  }
  if (stateAsFloat < 0.2) {
    result = "=";
  }
  if (stateAsFloat < 0.1) {
    result = "-";
  }
  return result;
};

var getWidth = function() {
  return $('#graphs').width();
}

var getHeight = function() {
  return 40;
}

$(document).ready(function() {
  drawHistory('inner');
  drawHistory('outer');
});