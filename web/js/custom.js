var lastOnline = -3; // 3 attempts before timeout
var peerCountTrack = 0;

$(document).ready(function() {
	// Set initial page as Home
	setHome();

	// Form hooks
	hookAllForms();

	// Disable user zoom
	//disableZoom();
	
	// Sidebar listeners
	sidebarOps();

	// Port status update
	setInterval(pollStatus, 1200);
	
	// Peer number update
	setInterval(peerCount, 1200, true);
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

function pad(n, width, z) {
	z = z || '0';
	n = n + '';
	return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

function setHome() {
	$('#page-text').html($('#home-page').html()).append($('#footer-page').html());
}

function hookAllForms() {
	var seen = {};
	$('form').each(function() {
		var id = this.id;
		if (!seen[id]) {
			seen[id] = id;
			var idObj = '#' + id;
			switch (id) {
				case 'search-form':
					$(idObj).on('submit', function(event) {
						var query = $('#search-query').val();
						var dataPack = {};
						dataPack.query = query;

						$('#search-query').val('');
						$('#search-results').removeClass('hidden');
						var loadGif = "<img class=\"loader\" src=\"img/loading.gif\">";
						$('#search-results').html(loadGif);
						
						// TODO: actually populating table

						if (connected() && peerCountTrack > 0) {
							$('#search-results').addClass('text-center');
							$.ajax({
								url: 'http://localhost:8888/api/search',
								timeout: 5000,
								method: 'POST',
								data: JSON.stringify(dataPack),
								success: function(result) {
									lastOnline = Date.now();
									$('#search-results').html(JSON.parse(result).value);
									$('#search-results').removeClass('text-center');
									hookAllPlays();
								},
								error: function(XMLHttpRequest, textStatus, errorThrown) {
									if (XMLHttpRequest.readyState == 0) {
										console.log("Disconnected af");
									}
								}
							});
							event.preventDefault();
							return false;
						} else if (peerCountTrack == 0) {
							$('#search-results').html("No peers are connected :(");
						} else {
							$('#search-results').html("No connection was detected :(");
						}
					});
					break;

				default:
					console.log('default: ' + this.id);
			}
		}
	});
}

function hookAllPlays() {
	$('.res-play').each(function() {
		$(this).on('click', function(event) {
			//TODO: ajax
			var dataPack = {};
			dataPack.query = this.id;

			if (connected()) {
				var playIcon = $('#' + this.id);
				playIcon.html("<a href=\"#\">"
								+ "<i class=\"fa fa-cog fa-spin\" aria-hidden=\"true\"></i>"
								+ "</a>");
				$.ajax({
					url: 'http://localhost:8888/api/play',
					timeout: 15000,
					method: 'POST',
					data: JSON.stringify(dataPack),
					success: function(result) {
						lastOnline = Date.now();
						playIcon.html("<a href=\"#\">"
								+ "<i class=\"fa fa-play-circle-o\" aria-hidden=\"true\"></i>"
								+ "</a>");
						window.open(JSON.parse(result).value);
					},
					error: function(XMLHttpRequest, textStatus, errorThrown) {
						if (XMLHttpRequest.readyState == 0) {
							console.log("Disconnected af");
						}
					}
				});
			}
			event.preventDefault();
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
			if (this.id == "home") {
				hookAllForms();
			}
			event.preventDefault();
		}
	});
}

function pollStatus() {
	if(connected()) {
		$.ajax({
			url: 'http://localhost:8888/api/portcheck',
			timeout: 5000,
			method: 'GET',
			success: function(result) {
				lastOnline = Date.now();
				if (result.value) {
					$('#status').html("<span class=\"label label-success\">OPEN</span>");
				} else {
					$('#status').html("<span class=\"label label-danger\">CLOSED</span>");
				}
			},
			error: function(XMLHttpRequest, textStatus, errorThrown) {
				if (XMLHttpRequest.readyState == 0) {
					console.log("Disconnected af");
				}
			}
		});
	} else {
		$('#status').html("<span class=\"label label-default\">- - - - - -</span>");
	}
}

function peerCount(setLabel) {
	var peerCount = 0;
	if (setLabel) {
		if(connected()) {
			$.ajax({
				url: 'http://localhost:8888/api/peers/count',
				timeout: 5000,
				method: 'GET',
				success: function(result) {
					lastOnline = Date.now();
					result = JSON.parse(result);
					peerCount = Number(result.value);
					peerCountTrack = peerCount;
					var imageSrc;
					if (peerCount == 0) {
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
				},
				error: function(XMLHttpRequest, textStatus, errorThrown) {
					if (XMLHttpRequest.readyState == 0) {
						console.log("Disconnected af");
					}
				}
			});
		} else {
			$('#peerCount').html("<img src=\"" + 'img/connect0.png' + "\" width=20 height=20>");
		}
	}
}

function connected() {
	// First 3 attempts at connection is free
	if(lastOnline < 0) {
		lastOnline++;
		return true;
	}
	var res = (lastOnline > 0 && ((lastOnline + 3000) > Date.now()))
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