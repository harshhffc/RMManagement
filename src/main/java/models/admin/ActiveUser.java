package models.admin;

import org.json.JSONObject;

import models.User;
import models.UserLocation;

public class ActiveUser {
	
	public User user = new User();
	public UserLocation location = new UserLocation();
	
	public ActiveUser() {}
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("user", user.toJson());
		json.put("location", location.toJson());
		
		return json;
		
	}
	
}
