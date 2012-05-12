package models;

import siena.Json;

public class KloutUser {

	public String screenName;
	public int amplificationScore;
	public int kloutScore;
	
	public Json toJson() {
		return Json.map().put("screenName", screenName).put("amplificationScore", amplificationScore).put("kloutScore", kloutScore);
	}
}
