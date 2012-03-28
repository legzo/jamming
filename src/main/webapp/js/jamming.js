var treatData = function(data) {
	$('#summary').text(data.historySummaryAsString);
};

var dateChosenCallback = function(ev) {
	$('#datepicker').datepicker('hide');
	$('#datepicker').blur();
	
	var url = 'http://localhost:8080/rest/traffic/file/bison-' + $('#datepicker').val().substring(5) +  '-9_59_59.json';
	
	$.ajax({
		url: url,
		dataType: 'json'
	}).done(treatData);
};

$('#datepicker').datepicker({
    format: 'yyyy-mm-dd'
}).on('changeDate', dateChosenCallback);
