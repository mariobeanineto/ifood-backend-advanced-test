package com.ifoodcase.playlist.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ifoodcase.playlist.service.PlaylistService;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {

	@Autowired
	private PlaylistService service;

	@GetMapping("/weather")
	public Map<String, List<String>> getPlaylistByCity(@RequestParam Map<String, String> requestParams) {
		Double temperature = 0d;
		String city = requestParams.get("city");
		String lat = requestParams.get("lat");
		String lon = requestParams.get("lon");
		if (city != null)
			temperature = service.getTemperature(requestParams.get("city"));
		else if (lat != null && lon != null)
			temperature = service.getTemperature(requestParams.get("lat"), requestParams.get("lon"));
		return service.getPlaylist(temperature);
	}
}
