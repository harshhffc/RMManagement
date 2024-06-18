package models;

import org.json.JSONObject;

import utils.Constants;

public class UserLocation {

	public int id = -1;
	public int userId = -1;
    public String sfUserId = Constants.NA;
    public String latitude = Constants.NA;
    public String longitude = Constants.NA;
    public String deviceId = Constants.NA;
    public String deviceType = Constants.NA;
    public String updateDatetime = Constants.NA;
    public String address = Constants.NA;
    
    public UserLocation() {}
    
    public UserLocation(JSONObject json) {
    	userId = json.optInt("userId", -1);
    	sfUserId = json.optString("sfUserId", Constants.NA);
    	latitude = json.optString("latitude", Constants.NA);
    	longitude = json.optString("longitude", Constants.NA);    
    	deviceId = json.optString("deviceId", Constants.NA);
    	deviceType = json.optString("deviceType", Constants.NA);
    	updateDatetime = json.optString("updateDatetime", Constants.NA);
    	address = json.optString("address", Constants.NA);
    }
    
    public JSONObject toJson() {
    	JSONObject json = new JSONObject();
    	json.put("id", id);
    	json.put("userId", userId);
    	json.put("sfUserId", sfUserId);
    	json.put("latitude", latitude);
    	json.put("longitude", longitude);    
    	json.put("deviceId", deviceId);
    	json.put("deviceType", deviceType);
    	json.put("updateDatetime", updateDatetime);
    	json.put("address", address);
    	return json;
    }
	
}
