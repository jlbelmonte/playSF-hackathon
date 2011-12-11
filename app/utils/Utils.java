package utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import siena.Json;

public class Utils {

	public static String getStringByCommasFromSublist(List<String> list, int start, int end) {
		String result = "";
		if (end > list.size()) {
			end = list.size();
		}
		List<String> sublist = list.subList(start, end);
		for (String string : sublist) {
			result += string +",";
		}
		return result;
	}
	
	public static List<String> topicsFound(String tweet, Json followersByTopics) {
		List<String> result = new ArrayList<String>();
		for(String topic : followersByTopics.keys()) {
			if (tweet.contains(topic)) {
				result.add(topic);
			}
		}
		return result;
	}
	
	public static Set<String> getUserScreenNames(Json followersByTopics, List<String> topics) {
		Set<String> result = new HashSet<String>();
		for (String topic : topics) {
			for(Json name : followersByTopics.get(topic)) {
				result.add(name.str());
			}
		}
		return result;
	}
}
