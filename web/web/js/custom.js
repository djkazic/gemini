var lastOnline = -3; // 3 attempts before timeout
var timeout = 0;
var peerCountTrack = 0;
var libTracks = [];

$(document).ready(function() {
	// Set initial page as Home
	setHome();

	// Form hooks
	hookAllForms();

	// Disable user zoom
	//disableZoom();

	// Hook play / pause events
	document.onkeypress = function(e) {
		if((e || window.event).keyCode === 32) {
			var player = document.getElementById('player');
			player.paused ? player.play() : player.pause();
		}
	};
	
	// Sidebar listeners
	sidebarOps();

	// Library hook
	hookLibrary();

	// Port status update
	setInterval(pollStatus, 1300);
	
	// Peer number update
	setInterval(peerCount, 1000, true);
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
	$('#page-text').html($('#home-page').html());
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
						event.preventDefault();
						return false;
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

			var dataPack = {};
			dataPack.query = this.id;

			if (connected()) {
				var playIcon = $('#' + this.id);
				playIcon.html("<a>"
								+ "<i class=\"fa fa-cog fa-spin\" aria-hidden=\"true\"></i>"
								+ "</a>");
				var popArr = function(id) {
					if (libTracks.indexOf(id) == 0) {
						libTracks.shift();
					}
				}
				$.ajax({
					url: 'http://localhost:8888/api/play',
					timeout: 40000,
					method: 'POST',
					data: JSON.stringify(dataPack),
					success: function(result) {
						lastOnline = Date.now();
						playIcon.html("<a>"
										+ "<i class=\"fa fa-play-circle-o\" aria-hidden=\"true\"></i>"
										+ "</a>");
						popArr(dataPack.query);
						$('#embed-player').html(JSON.parse(result).value);
						var regxp = new RegExp('#([^\\s]*)','g');
						var rawTitle = JSON.parse(result).title.replace(/<(?:.|\n)*?>/gm, '').replace(regxp, '');
						var title = JSON.parse(result).title;
						if (title.length > 40) {
							title = "<marquee>" + title + "</marquee>";
						}
						$('#song-data').animate({'opacity': 0}, 800, function () {
						    $(this).html(title);
						}).animate({'opacity': 1}, 800);

						document.title = "Gemini | " + rawTitle;

						$.ajax({
							url: 'https://api.spotify.com/v1/search?q=' + rawTitle + '&type=track',
							timeout: 5000,
							method: 'GET',
							success: function(alresult) {
								var albumArt = alresult['tracks']['items'][0]['album']['images'][1]['url'];
								$('#song-cover').html('<img src=\"' + albumArt + "\">");
							},
							error: function(XMLHttpRequest, textStatus, errorThrown) {
								if (XMLHttpRequest.readyState == 0) {
									console.log("Disconnected af");
								}
							}
						});

						// Hook loop
						var player = document.getElementById('player');
						player.onended = function() {
							if (libTracks.length > 0) {
								$('#' + libTracks[0]).click();
							}
						};
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

function hookLibrary() {
	$('#library').click(function(event) {
		if (connected()) {
			var query = 'query';
			var dataPack = {};
			dataPack.query = query;
			$.ajax({
				url: 'http://localhost:8888/api/library',
				timeout: 5000,
				method: 'POST',
				data: JSON.stringify(dataPack),
				success: function(result) {
					lastOnline = Date.now();
					$('#library-results').html(JSON.parse(result).value);
					hookAllPlays();
					libTracks.length = 0;
					$('.res-play').each(function() { 
						libTracks.push($(this).get(0).id);
					});
				},
				error: function(XMLHttpRequest, textStatus, errorThrown) {
					if (XMLHttpRequest.readyState == 0) {
						console.log("Disconnected af");
					}
				}
			});
			event.preventDefault();
			return false;
		}
	});
}

function sidebarOps() {
	$('.sidebar-nav li a').click(function(event) {
		var opt = $(this);
		if (opt.hasClass('nav-option') && !opt.hasClass('active')) {
			$('.active').removeClass('active');
			opt.addClass('active');
			var newText = $('#' + this.id + '-page');
			$('#page-text').html(newText.html());

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
	var res = (lastOnline > 0 && ((lastOnline + 3000) > Date.now()));
	
	if (res == 0) {
		timeout++;
	}

	if (timeout == 4) {
		window.confirm("No connection detected to Gemini client. Click OK to retry.");
		timeout = 0;
		lastOnline = -3;
		onlineTrack = 0;
		setTimeout(function() {
			location.reload(true);
		}, 5000);
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