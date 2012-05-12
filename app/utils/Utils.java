package utils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import siena.Json;

public class Utils {

	public static Set<String> getUserScreenNames(Json followersByTopics, List<String> topics) {
		Set<String> result = new HashSet<String>();
		for (String topic : topics) {
			for(Json name : followersByTopics.get(topic)) {
				result.add(name.str());
			}
		}
		return result;
	}

	public static Json fetchJson(String url) throws Exception {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();

		if (conn.getResponseCode() == 200) {
			InputStream in = conn.getInputStream();
			byte[] bytes = IOUtils.toByteArray(in);
			return Json.loads(new String(bytes, "UTF-8"));
		} else if (conn.getResponseCode() != 404){
			throw new Exception("Error code : "+conn.getResponseCode()+" while fetching URL: "+url);
		}
		return Json.map();
	}
}
