$(document).ready(function() {
	var formNames = [];

	processForms(formNames);
	setInterval(pollStatus, 1000);
});

function processForms(formNames) {
	getAllForms(formNames);

	//Iterate through forms, attaching submit listeners
	for(i = 0; i < formNames.length; i++) {
		var thisForm = formNames[i];
		var switcher = '#' + thisForm.id;

		//Switcher
		switch(switcher) {
			case '#searchForm':
				$(switcher).submit(function(event) {
					console.log("Handler attch");
					event.preventDefault();
				});
				break;

			default:
				//alert('default');
		}
	}
}

function getAllForms(forms) {
	$('form').each(function() {
		forms.push(this);
	});
}

function pollStatus() {
	$('#statusIndicator').html("Status: [<span class=\"label label-success\"><span id=\"status\">ONLINE</span></span>]");
}