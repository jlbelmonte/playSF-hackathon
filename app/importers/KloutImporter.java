package importers;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import models.KloutUser;

import org.apache.commons.io.IOUtils;

import play.Play;

import siena.Json;
import utils.Utils;

public class KloutImporter {

	private static String kloutKey = Play.configuration.getProperty("kloutKey");

	public static List<KloutUser> getUsers(Set<String> screenNames) {
		List<String> screenNamesList = new ArrayList<String>(screenNames);
		List<KloutUser> result = new ArrayList<KloutUser>();
		for(int i = 0; i < screenNames.size(); i+=5) {
			int start = i;
			int end = i+5;

			String usersByCommas = Utils.getStringByCommasFromSublist(screenNamesList, start, end);

			String url = "http://api.klout.com/1/users/show.json?key="+kloutKey+"&users="+usersByCommas;

			try{
				URL u = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) u.openConnection();

				if (conn.getResponseCode() == 200) {
					InputStream in = conn.getInputStream();
					byte[] bytes = IOUtils.toByteArray(in);
					Json response =  Json.loads(new String(bytes, "UTF-8"));
					if (response.containsKey("users")) {
						for (Json user : response.get("users")) {
							KloutUser kloutUser = new KloutUser();
							kloutUser.screenName = user.get("twitter_screen_name").str();
							kloutUser.amplificationScore = user.get("score").get("amplification_score").asInt();
							kloutUser.kloutScore = (int) user.get("score").get("kscore").asFloat();
							result.add(kloutUser);
						}
					}
					Thread.sleep(200);
				}
				if (conn.getResponseCode() != 200) {
					throw new Exception("Error code : "+conn.getResponseCode()+" while fetching URL: "+url);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		return result;
	}
	
	public static Json getFollowersGroupedByTopic(List<String> followers) {
		Json results = Json.map();
		for(int i = 0; i < followers.size(); i+=5) {
			int start = i;
			int end = i+5;

			String usersByCommas = Utils.getStringByCommasFromSublist(followers, start, end);

			String url = "http://api.klout.com/1/users/topics.json?key="+kloutKey+"&users="+usersByCommas;

			try{
				URL u = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) u.openConnection();

				if (conn.getResponseCode() == 200) {
					InputStream in = conn.getInputStream();
					byte[] bytes = IOUtils.toByteArray(in);
					Json response =  Json.loads(new String(bytes, "UTF-8"));
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
					Thread.sleep(200);
				}
				if (conn.getResponseCode() != 200) {
					throw new Exception("Error code : "+conn.getResponseCode()+" while fetching URL: "+url);
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		return results;
	}
}
