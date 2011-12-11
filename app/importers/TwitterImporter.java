package importers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.Play;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class TwitterImporter {

	public static List<String> getFollowersScreenName(String twitterName) {
		Twitter twitter = new TwitterFactory().getInstance();
		String twitterAccessToken = Play.configuration.getProperty("twitter.accessToken");
		String twitterTokenSecret = Play.configuration.getProperty("twitter.tokenSecret");
		String consumerKey = Play.configuration.getProperty("twitter.consumerKey");
		String consumerSecret = Play.configuration.getProperty("twitter.consumerSecret");
		AccessToken accessToken = new AccessToken(twitterAccessToken, twitterTokenSecret);
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(accessToken);

		long cursor = -1;
		IDs ids = null;
		do {
			try {
				ids = twitter.getFollowersIDs(twitterName, cursor);
			} catch (TwitterException e) {
			}
		} while (ids != null && (cursor = ids.getNextCursor()) != 0);
		if (ids == null) {
			//TODO 
		}
		long[] longs = ids.getIDs();

		List<User> users = new ArrayList<User>();
		int counter = 0;
		do {
			try {
				int init = counter;
				counter += 100;

				if (counter >= longs.length) {
					counter = longs.length;
				}
				long[] longRange = Arrays.copyOfRange(longs, init, counter);
				ResponseList<User> response = twitter.lookupUsers(longRange);
				users.addAll(response);
			} catch (TwitterException e) {
			}
		} while (counter <= longs.length -1);
		List<String> userScreenNames = new ArrayList<String>();
		for (User user : users) {
			userScreenNames.add(user.getScreenName());
		}
		return userScreenNames;
	}
}
