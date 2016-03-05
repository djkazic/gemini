var lastOnline = -3; // 3 attempts before timeout
var audioPlayer;

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
	setInterval(pollStatus, 1500);
	
	// Peer number update
	setInterval(peerCount, 1500);

	audioPlayer = startAudio();
	audioPlayer.load("file:///C:/Users/kevin/Documents/GitHub/Radiator/web/New Navy - Zimbabwe (Flume Remix).mp3");
});

function pad(n, width, z) {
	z = z || '0';
	n = n + '';
	return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

function startAudio() {
	$('.progress').on("mousedown", scrubberMouseDownHandler);

	var audio5js = new Audio5js({
		swf_path: '/statics/swf/audio5js.swf',
		throw_errors: true,
		format_time: false,
		ready: audioPlayerBindings
	});

	return audio5js;
}

function audioPlayerBindings() {
	this.firstPlay = 1;

	$('#play').on("click", playPause.bind(this));
	$('#play-prev').on("click", moveToStart.bind(this));
	$('#progress-col').on("mousedown", moveToSeek.bind(this));

	this.on('timeupdate', function (position, duration) {
		console.log(position);
		var nicePosition = pad(Math.floor(position / 60), 2);
		var positionSecLeft = pad(Math.floor(position - (nicePosition * 60)), 2);

		var niceDuration = pad(Math.floor(duration / 60), 2);
		var secondsLeft = pad(Math.floor(duration - (niceDuration * 60)), 2);
		if(window.innerWidth < 960) {
			$('#play-time').html(nicePosition + ":" + positionSecLeft + " " + niceDuration + ":" + secondsLeft);
		} else {
			$('#play-time').html(nicePosition + ":" + positionSecLeft + " | " + niceDuration + ":" + secondsLeft);
		}
		$('#music-progress').css('width', ((position / duration) * 100) + "%");
	});
}

var moveToStart = function () {
	this.seek(0);
}

var moveToSeek = function() {
	var percentSeek = (seekpoint) * this.duration;
	this.seek(percentSeek);
	if(!this.playing) {
		$('#play').click();
	}
}

function playPause() {
	if(this.playing) {
		this.pause();
		$('#play').css('background-image', 'url(img/play.svg)');
	} else {
		this.play();
		$('#play').css('background-image', 'url(img/pause.svg)');
	}
}

function scrubberMouseDownHandler(e) {
	var $this = $(this);
	var x = e.pageX - $this.offset().left;
	var percent = x / $this.width();
	$('#music-progress').width((percent * 100) + "%");
	seekpoint = percent;
}

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
						var dataPack = {};
						dataPack.query = query;

						$('#search-query').val('');
						$('#search-results').removeClass('hidden');
						var loadGif = "<img class=\"loader\" src=\"img/loading.gif\">";
						$('#search-results').html(loadGif);
						
						// TODO: actually populating table
						//

						if(connected()) {
							$('#search-results').addClass('text-center');
							$.ajax({
								url: 'http://localhost:8888/api/search',
								method: 'POST',
								data: JSON.stringify(dataPack)
							}).done(function(result) {
								lastOnline = Date.now();
								$('#search-results').html(JSON.parse(result).value);
								$('#search-results').removeClass('text-center');
							});
						} else {
							$('#search-results').html("No connection was detected :(");
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
			url: 'http://localhost:8888/api/portcheck',
			timeout: 1000,
			method: 'GET'
		}).done(function(result) {
			lastOnline = Date.now();
			if (result.value) {
				$('#status').html("<span class=\"label label-success\">OPEN</span>");
			} else {
				$('#status').html("<span class=\"label label-danger\">CLOSED</span>");
			}
		});
	} else {
		$('#status').html("<span class=\"label label-default\">- - - - - -</span>");
	}
}

function peerCount() {
	if(connected()) {
		$.ajax({
			url: 'http://localhost:8888/api/peers/count',
			method: 'GET'
		}).done(function(result) {
			lastOnline = Date.now();
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
	// First 3 attempts at connection is free
	if(lastOnline < 0) {
		lastOnline++;
		return true;
	}
	var res = (lastOnline > 0 && ((lastOnline + 2000) > Date.now()))
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