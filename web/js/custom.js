$(document).ready(function() {
	var formNames = [];
	processForms(formNames);

	//Port status update
	pollStatus();
	
	//Peer number update
	peerCount();
	setInterval(peerCount, 1500);
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
	$.ajax({
		url: 'http://localhost:8888/api',
		method: 'POST',
		data: '{ "rpc": "port_check" }'
	}).done(function(result) {
		if(result.value) {
			$('#status').html("<span class=\"label label-success\">OPEN</span>");
		} else {
			$('#status').html("<span class=\"label label-danger\">CLOSED</span>");
		}
	});
}

function peerCount() {
	$.ajax({
		url: 'http://localhost:8888/api',
		method: 'POST',
		data: '{ "rpc": "peer_count" }'
	}).done(function(result) {
		result = JSON.parse(result);
		var peerCount = Number(result.value);
		var imageSrc;
		if(peerCount == 0) {
			imageSrc = 'img/connect0.png';
		} else if(peerCount > 0 && peerCount <= 2) {
			imageSrc = 'img/connect1.png';
		} else if(peerCount > 2 && peerCount <= 4) {
			imageSrc = 'img/connect2.png';
		} else if(peerCount > 4 && peerCount <= 6) {
			imageSrc = 'img/connect3.png';
		} else if(peerCount > 6) {
			imageSrc = 'img/connect4.png';
		}
		$('#peerCount').html("<img src=\"" + imageSrc + "\" width=20 height=20>");
	});
}