$(document).ready(function() {
	// Set initial page as Home
	setHome();

	// Disable user zoom
	disableZoom();
	
	// Sidebar listeners
	sidebarOps();

	var formNames = [];
	processForms(formNames);

	// Port status update
	pollStatus();
	
	// Peer number update
	peerCount();
	setInterval(peerCount, 1500);
});

function disableZoom() {
	$(window).keydown(function(event) {
	    if (event.ctrlKey==true && (event.which == '187'  || event.which == '189')) {
			event.preventDefault();
		}

	    $(window).bind('mousewheel DOMMouseScroll', function(event) {
	        if (event.ctrlKey == true) {
	            event.preventDefault(); 
	        }
	    });
	});
}

function setHome() {
	$('#page-text').html($('#home-page').html());
}

function sidebarOps() {
	$('.sidebar-nav li a').click(function(event) {
		var opt = $(this);
		if (opt.hasClass('nav-option') && !opt.hasClass('active')) {
			$('.active').removeClass('active');
			opt.addClass('active');
			var newText = $('#' + this.id + '-page');
			$('#page-text').html(newText.html());
			event.preventDefault();
		}
	});
}

function processForms(formNames) {
	getAllForms(formNames);

	//Iterate through forms, attaching submit listeners
	for (i = 0; i < formNames.length; i++) {
		var thisForm = formNames[i];
		var switcher = '#' + thisForm.id;

		//Switcher
		switch (switcher) {
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
		if (result.value) {
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
		} else if (peerCount > 0 && peerCount <= 2) {
			imageSrc = 'img/connect1.png';
		} else if (peerCount > 2 && peerCount <= 4) {
			imageSrc = 'img/connect2.png';
		} else if (peerCount > 4 && peerCount <= 6) {
			imageSrc = 'img/connect3.png';
		} else if (peerCount > 6) {
			imageSrc = 'img/connect4.png';
		}
		$('#peerCount').html("<img src=\"" + imageSrc + "\" width=20 height=20>");
	});
}

// Debug jQuery plugin for listing classes
;!(function ($) {
    $.fn.classes = function (callback) {
        var classes = [];
        $.each(this, function (i, v) {
            var splitClassName = v.className.split(/\s+/);
            for (var j in splitClassName) {
                var className = splitClassName[j];
                if (-1 === classes.indexOf(className)) {
                    classes.push(className);
                }
            }
        });
        if ('function' === typeof callback) {
            for (var i in classes) {
                callback(classes[i]);
            }
        }
        return classes;
    };
})(jQuery);