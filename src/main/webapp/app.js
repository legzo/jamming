var client = new elasticsearch.Client({
  host: 'data-jte.beta.kermit.orange-labs.fr',
  log: 'info'
});

var drawHistory = function(givenDirection) {
  var now = Date.now() + (1 * 60 * 60 * 1000);

  client.search({
      index: 'trafic',
      type: 'state-partial',
      size: 9999,
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
      drawPeriod(givenDirection, hits);
  }, function (err) {
      console.trace(err.message);
  });
}

var drawPeriod = function(label, hits) {
  var history = '';
  for(var index in hits){
    var hit = hits[index]._source;
    console.log(hit.time)
    history += getAsString(hit.stateAsFloat)
  }

  var historyAsText = '<p><b>' + label + ' : </b>' + history + '</p>';

  console.log(historyAsText);

  $('#history').append(historyAsText);
}

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
}

$(document).ready(function() {
  drawHistory('inner');
  drawHistory('outer');
});