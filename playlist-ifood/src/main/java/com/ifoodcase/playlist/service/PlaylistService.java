package com.ifoodcase.playlist.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class PlaylistService {

	@Autowired
	private Environment enviroment;
	
	/**
	 * connect to the api and get the temperature for the city selected
	 * 
	 * @param city
	 * @return temperature
	 */
	public Double getTemperature(String city) {
		String key = enviroment.getProperty("key.maps-api");
		String temp = new String();
		try {
			URL url = new URL("https://api.openweathermap.org/data/2.5/find?q=" + city + "&units=metric&appid=" + key);
			URLConnection request = url.openConnection();
			request.connect();
			JsonParser parser = new JsonParser();
			JsonArray jsonArray = parser.parse(new InputStreamReader((InputStream) request.getContent()))
					.getAsJsonObject().getAsJsonArray("list");
			JsonObject jsonObj = jsonArray.get(1).getAsJsonObject().get("main").getAsJsonObject();
			temp = jsonObj.get("temp").getAsString();
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return Double.valueOf(temp);
	}

	/**
	 * connect to the api and get the temperature for the city selected
	 * 
	 * @param city
	 * @return temperature
	 */
	public Double getTemperature(String lat, String lon) {
		String key = enviroment.getProperty("key.maps-api");
		String temp = new String();
		try {
			URL url = new URL("https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon
					+ "&units=metric&appid=" + key);
			URLConnection request = url.openConnection();
			request.connect();
			JsonParser parser = new JsonParser();
			JsonObject jsonObj = parser.parse(new InputStreamReader((InputStream) request.getContent()))
					.getAsJsonObject().getAsJsonObject("main");
			temp = jsonObj.get("temp").getAsString();
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return Double.valueOf(temp);
	}

	/**
	 * get the playlist based on the temperature
	 * 
	 * @param temperature
	 * @return
	 */
	public Map<String, List<String>> getPlaylist(Double temperature) {
		String accessToken = getSpotifyAccessToken();
		String style = getTrackStyle(temperature);
		String playlistUrl = getPlaylistUrl(style, accessToken);
		return getTracks(playlistUrl, accessToken);
	}

	/**
	 * connect to the spotify api and gets the access token
	 * 
	 * @return accessToken
	 */
	private String getSpotifyAccessToken() {
		HashMap<String, String> headers = new HashMap<>();
		String accessToken = new String();
		String url = new String("https://accounts.spotify.com/api/token");
		//add the arguments
		List<NameValuePair> arguments = new ArrayList<>();
		arguments.add(new BasicNameValuePair("grant_type", "client_credentials"));
		//add the header
		headers.put("Authorization",
				"Basic "+enviroment.getProperty("key.spotify"));
		try {
			HttpResponse response = sendPostRequest(url, headers, arguments);
			JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
			accessToken = jsonObj.getString("access_token");
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return accessToken;
	}

	/**
	 * get the track style based on the temperature passed
	 * 
	 * @param temperature
	 * @return trackStyle
	 */
	private String getTrackStyle(Double temperature) {
		if (temperature < 10d) {
			return "classical";
		} else if (temperature < 14d) {
			return "rock";
		} else if (temperature < 30d) {
			return "pop";
		} else {
			return "party";
		}
	}

	/**
	 * get the playlist url based on the style
	 * 
	 * @param style
	 * @param accessToken
	 * @return playlistUrl
	 */
	private String getPlaylistUrl(String style, String accessToken) {
		String playlistUrl = new String();
		try {
			String url = "https://api.spotify.com/v1/browse/categories/" + style + "/playlists?country=BR&limit=10";
			HttpResponse response = sendGetRequestWithAccessToken(url, accessToken);
			JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
			JSONArray jsonArray = jsonObj.getJSONObject("playlists").getJSONArray("items");
			playlistUrl = jsonArray.getJSONObject(getRandomNumber(jsonArray.length())).getJSONObject("tracks")
					.getString("href");
		} catch (Exception e) {
			throw new RuntimeException();
		}
		return playlistUrl;
	}

	/**
	 * get list of the tracks based on the style given
	 * 
	 * @param playlistUrl
	 * @param accessToken
	 * @return list of tracks
	 */
	private Map<String, List<String>> getTracks(String playlistUrl, String accessToken) {
		Map<String, List<String>> resultMap = new HashMap<>();
		List<String> resultList = new ArrayList<>();
		try {
			String url = playlistUrl + "?fields=items(track(name))&limit=10";
			HttpResponse response = sendGetRequestWithAccessToken(url, accessToken);
			JSONObject jsonObj = new JSONObject(EntityUtils.toString(response.getEntity()));
			JSONArray array = jsonObj.getJSONArray("items");
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonItem = array.getJSONObject(i).getJSONObject("track");
				resultList.add(jsonItem.getString("name"));
			}
		} catch (Exception e) {
			throw new RuntimeException();
		}
		resultMap.put("tracks", resultList);
		return resultMap;
	}

	/**
	 * send post request
	 * 
	 * @param url
	 * @param headers
	 * @param arguments
	 * @return the response
	 * @throws Exception
	 */
	private HttpResponse sendPostRequest(String url, HashMap<String, String> headers, List<NameValuePair> arguments)
			throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(url);
		headers.forEach((arg, val) -> post.addHeader(arg, val));
		post.setEntity(new UrlEncodedFormEntity(arguments));
		return client.execute(post);
	}

	/**
	 * sends the get requests when access tokens are needed
	 * 
	 * @param url
	 * @param accessToken
	 * @return response
	 * @throws Exception
	 */
	private HttpResponse sendGetRequestWithAccessToken(String url, String accessToken) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet get = new HttpGet(url);
		get.addHeader("Authorization", "Bearer " + accessToken);
		get.addHeader("Accept", "application/json");
		get.addHeader("Content-Type", "application/json");
		return client.execute(get);
	}

	/**
	 * gives a random number on a certain interval
	 * 
	 * @param size
	 * @return random number
	 */
	private int getRandomNumber(int size) {
		Random r = new Random();
		int low = 0;
		int high = size - 1;
		return r.nextInt(high - low) + low;
	}
}