var lastOnline = -1;

$(document).ready(function() {
	// Set initial page as Home
	setHome();

	// Form hooks
	hookAllForms();

	// Disable user zoom
	disableZoom();
	
	// Sidebar listeners
	sidebarOps();

	// Port status update
	setInterval(pollStatus, 1500);
	
	// Peer number update
	setInterval(peerCount, 1500);
});

function setHome() {
	$('#page-text').html($('#home-page').html()).append($('#footer-page').html());
}

function hookAllForms() {
	var seen = {};
	$('form').each(function() {
		var id = this.id;
		if (seen[id]) {
			$(this).remove();
		} else {
			seen[id] = true;
			var idObj = '#' + id;
			switch (id) {
				case 'search':
					$(idObj).on('submit', function() {
						var query = $('#search-query').val();
						var dataPack = {
							"rpc": "search",
							"query": query
						};

						$('#search-query').val('');
						$('#search-results').removeClass('hidden');
						var loadGif = "<img class=\"loader\" src=\"img/loading.gif\">";
						$('#search-results').html(loadGif);
						
						// TODO: actually populating table
						//

						if(connected()) {
							$.ajax({
								url: 'http://localhost:8888/api',
								method: 'POST',
								data: JSON.stringify(dataPack)
							}).done(function(result) {
								$('#search-results').html(JSON.parse(result).value);
								$('#search-results').removeClass('text-center');
								console.log(result);
							});
						} else {
							$('#search-results').html("No connection :(");
						}
						return false;
					});
					break;

				default:
					alert('default');
					alert(this.id);
			}
		}
	});
}

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

function sidebarOps() {
	$('.sidebar-nav li a').click(function(event) {
		var opt = $(this);
		if (opt.hasClass('nav-option') && !opt.hasClass('active')) {
			$('.active').removeClass('active');
			opt.addClass('active');
			var newText = $('#' + this.id + '-page');
			$('#page-text').html(newText.html()).append($('#footer-page').html());
			event.preventDefault();
		}
	});
}

function pollStatus() {
	if(connected()) {
		$.ajax({
			url: 'http://localhost:8888/api',
			timeout: 1000,
			method: 'POST',
			data: '{ "rpc": "port_check" }'
		}).done(function(result) {
			lastOnline = Date.now();
			if (result.value) {
				$('#status').html("<span class=\"label label-success\">OPEN</span>");
			} else {
				$('#status').html("<span class=\"label label-danger\">CLOSED</span>");
			}
		});
	} else {
		$('#status').html("<span class=\"label label-default\">- - - - - - -</span>");
	}
}

function peerCount() {
	if(connected()) {
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
	} else {
		$('#peerCount').html("<img src=\"" + 'img/connect0.png' + "\" width=20 height=20>");
	}
}

function connected() {
	var res = (lastOnline < 0 || ((lastOnline + 1000) > Date.now()))
	if(lastOnline = -1) {
		lastOnline = 0;
	}
	return res;
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