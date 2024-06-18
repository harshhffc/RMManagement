package models;

import org.json.JSONObject;

import utils.Constants;

public class LeaderboardFilter {
	
	public static final String ALL = "all";

	public String region = Constants.NA;
	public String time = Constants.NA;
	public String contestName = Constants.NA;
	public String year = Constants.NA;
	
	public LeaderboardFilter() {}
	
	public LeaderboardFilter(JSONObject json) {
		region = json.optString("region", Constants.NA);
		time = json.optString("time", Constants.NA);
		contestName = json.optString("contestName", Constants.NA);
		year = json.optString("year", Constants.NA);

	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("region", region);
		json.put("time", time);
		json.put("contestName", contestName);
		json.put("year", year);
		return json;
	}
	
}