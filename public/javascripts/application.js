$(document).ready(function() {
	var topicsByFollowers = {};
	var topicsFound = [];
	var influencers = [];
	var influencersInfo = [];
	
	$('form').live('submit', function(e){
		e.preventDefault();
		//TODO validate fields
		getFollowers($('input[name="twitter_name"]').val());
	});

	function getFollowers(twitterName) {
		$.ajax({
			method: 'GET',
			data: {twitterName: twitterName},
			url: URL_GET_FOLLOWERS,
			success: function(data) {
				getTopicsByFollowers(data, 0);
			},
			error: function() {
				getFollowers(twitterName);
			}
		});
	}
	
	function getTopicsByFollowers(followers, page) {
		var followersToRequest = "";
		
		for(var i = page * 5; (i < (page + 1) * 5) && (i < followers.length); i++) {
			followersToRequest += followers[i] + ",";
		}
		if (followersToRequest == "") {
			findTopics();
			getFollowersInfluentInFoundTopics();
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
				getTopicsByFollowers(followers, page+1);
			},
			error: function() {
				setTimeout(function() {
					getTopicsByFollowers(followers, page)
				}, 1000);
			}
		});
	}
	
	function findTopics() {
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
	}
	
	function getKloutUserInformation(page) {
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