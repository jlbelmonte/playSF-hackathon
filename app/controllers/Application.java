package controllers;

import importers.KloutImporter;
import importers.TwitterImporter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


import models.KloutUser;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.Play;
import play.mvc.Controller;
import play.templates.JavaExtensions;
import siena.Json;
import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import utils.Utils;

public class Application extends Controller {

	public static void index() {
		String tweet = params.get("tweet");
		String twitterName = params.get("twitter_name");
		
		if (tweet == null || tweet.isEmpty() || twitterName == null || twitterName.isEmpty()) {
			Logger.info("Params not found");
			Logger.info(Play.configuration.getProperty("twitter.accessToken"));
			render();
		}
		
		Logger.info("Params received. Twitter %s, tweet %s");
		List<String> followers = TwitterImporter.getFollowersScreenName(twitterName);
		Json followersByTopics = KloutImporter.getFollowersGroupedByTopic(followers);
		List<String> topicsFound = Utils.topicsFound(tweet, followersByTopics);
		Set<String> userScreenNames = Utils.getUserScreenNames(followersByTopics, topicsFound);
		System.out.println(userScreenNames);
		
		List<KloutUser> kloutUsers = KloutImporter.getUsers(userScreenNames);
		System.out.println("Size " + kloutUsers.size());
		renderArgs.put("kloutUsers", kloutUsers);
		render();
	}

}