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
		render();
	}

	public static void getTwitterFollowers(String twitterName) {
		if (twitterName == null || twitterName.isEmpty()) {
			renderJSON(Json.map().put("error", "twitter name required").toString());
		}

		Json followers = TwitterImporter.getFollowersScreenName(twitterName);
		renderJSON(followers.toString());	
	}

	public static void getFollowersByTopics(String followers) throws Exception {
		renderJSON(KloutImporter.getFollowersGroupedByTopic(followers).toString());
	}
	
	public static void getKloutUsers(String followers) throws Exception {
		renderJSON(KloutImporter.getUsers(followers).toString());
	}

}