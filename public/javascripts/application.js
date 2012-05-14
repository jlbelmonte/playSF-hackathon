$(document).ready(function() {
	var twitterHandler;
	var tweetContent;
	var followers = [];
	var topicsByFollowers = {};
	var topicsFound = [];
	var influencers = [];
	var influencersInfo = [];
	var totalPages = 0;
	
	$('form').live('submit', function(e){
		e.preventDefault();
		var twitterName = $('input[name="twitter_name"]').val();
		var error = false;
		
		$('input[name="twitter_name"]').parent().children('.help-inline').remove();
		$('input[name="twitter_name"]').parent().removeClass('error');
		if (twitterName == null || twitterName === '') {
			$('input[name="twitter_name"]').parent().addClass('error');
			$('input[name="twitter_name"]').after($('<span>').addClass('help-inline').text('Required'));
			error = true;
		}
		
		var tweetContent = $('textarea[name="tweet"]').val();
		
		$('textarea[name="tweet"]').parent().children('.help-inline').remove();
		$('textarea[name="tweet"]').parent().removeClass('error');
		if (tweetContent == null || tweetContent === '') {
			$('textarea[name="tweet"]').parent().addClass('error');
			$('textarea[name="tweet"]').after($('<div>').addClass('help-inline').text('Required'));
			error = true;
		}
		
		if (error) {
			return;
		}
		
		if (twitterHandler !== twitterName) {
			followers = [];
			topicsByFollowers = {};
			topicsFound = [];
			influencers = [];
			influencersInfo = [];
			twitterHandler = twitterName;
			getFollowers();
		} else {
			topicsFound = [];
			influencers = [];
			influencersInfo = [];
			findTopics();
			getFollowersInfluentInFoundTopics();
			drawProgressBarAndMessage('Getting Klout\'s info for your followers');
			getKloutUserInformation(0);
			
		}
	});

	function drawProgressBarAndMessage(message) {
		$('.results').empty();
		$('.results').append($('<h3>').addClass('text-centered').text(' ' + message));
		var bar = $('<div>').addClass('progress').addClass('progress-striped').append($('<div>').addClass('bar').width('0%'));
		$('.results').append(bar);
	}
	function getFollowers(retrying) {
		$('.results').empty();
		$('.results').append($('<h3>').addClass('text-centered').append($('<img>').attr('src', IMG_SPINNER)).append($('<span>').text(' Getting your followers from Twitter')));
		if (retrying) {
			$('.results').append($('<div>').addClass('text-centered').text('Error, retrying'));
		}
		$.ajax({
			method: 'GET',
			data: {twitterName: twitterHandler},
			url: URL_GET_FOLLOWERS,
			success: function(data) {
				followers = data;
				drawProgressBarAndMessage('Getting topics from your followers');
				totalPages = Math.ceil(followers.length / 5);
				getTopicsByFollowers(0);
			},
			error: function() {
				getFollowers(true);
			}
		});
	}
	
	function getTopicsByFollowers(page) {
		var followersToRequest = "";
		$('.results').children('.progress').children('.bar').width((page*100/totalPages)+"%");
		for(var i = page * 5; (i < (page + 1) * 5) && (i < followers.length); i++) {
			followersToRequest += followers[i] + ",";
		}
		if (followersToRequest == "") {
			findTopics();
			getFollowersInfluentInFoundTopics();
			drawProgressBarAndMessage('Getting Klout\'s info for your followers');
			getKloutUserInformation(0);
			return;
		}
		$.ajax({
			method: 'GET',
			data: {followers: followersToRequest},
			url: URL_GET_FOLLOWERS_BY_TOPICS,
			success: function(data) {
				for (key in data) {
					var currentValue = topicsByFollowers[key];
					if (currentValue == null) {
						currentValue = [];
					}
					topicsByFollowers[key] = currentValue.concat(data[key]);
				}
				getTopicsByFollowers(page+1);
			},
			error: function() {
				setTimeout(function() {
					getTopicsByFollowers(page)
				}, 1000);
			}
		});
	}
	
	function findTopics() {
		$('.results').empty();
		$('.results').append($('<h3>').addClass('text-centered').text(' Extracting keywords from your tweet'));
		var tweetContent = ' ' + $('textarea[name="tweet"]').val().toLowerCase() + ' ';
		for(key in topicsByFollowers) {
			if (tweetContent.indexOf(' ' + key.toLowerCase() + ' ') != -1) {
				topicsFound.push(key);
			}
		}
	}
	
	function getFollowersInfluentInFoundTopics() {
		for(var i = 0; i < topicsFound.length; i++) {
			influencers = influencers.concat(topicsByFollowers[topicsFound[i]]);
		}
		totalPages = Math.ceil(influencers.length/5);
	}
	
	function getKloutUserInformation(page) {
		$('.results').children('.progress').children('.bar').width((page*100/totalPages)+"%");
		var influencersToRequest = "";
		for(var i = page * 5; (i < (page + 1) * 5) && (i < influencers.length); i++) {
			influencersToRequest += influencers[i] + ",";
		}
		if (influencersToRequest == "") {
			drawResults();
			return;
		}
		$.ajax({
			method: 'GET',
			data: {followers: influencersToRequest},
			url: URL_GET_KLOUT_USERS,
			success: function(data) {
				for(var i = 0; i < data.length; i++) {
					influencersInfo.push(data[i]);
				}
				getKloutUserInformation(page+1);
			},
			error: function() {
				setTimeout(function() {
					getKloutUserInformation(page)
				}, 1000);
			}
		});
	}
	
	function drawResults() {
		$('.results').empty();
		$('.results').hide();
		if (influencersInfo.length == 0) {
			var title = $('<h3>').addClass('mtop20').text('Sorry, no influencers for your tweet...');
			var subtitle = $('<div>').addClass('mbottom10').text('Why don\'t you try again changing or adding some words?');
			var image = $('<div>').append($('<img>').attr('src', IMG_CAT).attr('title', 'Sad cat').width(250).addClass('mtop20'));
			$('.results').append($('<div>').append(title).append(subtitle).append(image));
		} else {
			$('.results').append($('<h3>').addClass('mtop20').addClass('mbottom10').text('Best influencers for your tweet'));
			for(var i = 0; i < influencersInfo.length; i++) {
				if (i % 2 == 0) {
					$('.results').append($('<div>').addClass('row').addClass('mtop20'));
				}
				var kloutScore = $('<div>').addClass('span1').addClass('klout').text(influencersInfo[i].kloutScore);
				var link = $('<a>').attr('href', 'https://twitter.com/!#/'+influencersInfo[i].screenName).addClass('linktitle').attr('target','_blank').text(influencersInfo[i].screenName);
				var amplification = $('<div>').html('Amplification score: <strong>'+influencersInfo[i].amplificationScore+'</strong>');
				
				var user = $('<div>').addClass('row').append(kloutScore).append($('<div>').addClass('span4').append(link).append(amplification));
				$('.results').children('div').last().append($('<div>').addClass('span6').append(user));
			}
		}
		$('.results').fadeIn();
	}
	
});