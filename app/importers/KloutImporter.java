package importers;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;

import models.KloutUser;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Play;

import siena.Json;
import utils.Utils;

public class KloutImporter {

	private static String kloutKey = Play.configuration.getProperty("kloutKey");

	public static Json getUsers(String screenNames) throws Exception {
		Json result = Json.list();
		String url = "http://api.klout.com/1/users/show.json?key="+kloutKey+"&users="+screenNames;

		Json response = Utils.fetchJson(url);
		if (response.containsKey("users")) {
			for (Json user : response.get("users")) {
				KloutUser kloutUser = new KloutUser();
				kloutUser.screenName = user.get("twitter_screen_name").str();
				kloutUser.amplificationScore = user.get("score").get("amplification_score").asInt();
				kloutUser.kloutScore = (int) user.get("score").get("kscore").asFloat();
				result.add(kloutUser.toJson());
			}
		}
		return result;
	}

	public static Json getFollowersGroupedByTopic(String users) throws Exception {
		String url = "http://api.klout.com/1/users/topics.json?key="+kloutKey+"&users="+users;
		Json results = Json.map();
		Json response =  Utils.fetchJson(url);
		if (response.containsKey("users")) {
			for (Json user : response.get("users")) {
				for (Json topic : user.get("topics")) {
					Json usersByTopic = Json.list();
					if (results.containsKey(topic.str())) {
						usersByTopic = results.get(topic.str());
					}
					usersByTopic.add(user.get("twitter_screen_name"));
					results.put(topic.str(), usersByTopic);
				}
			}
		}
		return results;
	}
}
