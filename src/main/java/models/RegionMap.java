//package models;
//
//public class RegionMap {
//
//
//	
//		
//		public int id = -1;
//	    public String email = Constants.NA;
//	    public String username = Constants.NA;
//	    public String profileImageUrl = Constants.NA;
//	    public String idUrl = Constants.NA;
//	    public String deviceId = Constants.NA;
//	    public String deviceType = Constants.NA;
//	    public String registerDatetime = Constants.NA;
//	    public String lastLoginDatetime = Constants.NA;
//	    public String sessionPasscode = Constants.NA;
//		
//		public User() {}
//		
//		public User(JSONObject json) {
//			id = json.optInt("id", -1);
//		    sfUserId = json.optString("sfUserId", Constants.NA);
//		    orgId = json.optString("orgId", Constants.NA);
//		    firstName = json.optString("firstName", Constants.NA);
//		    lastName = json.optString("lastName", Constants.NA);
//		    displayName = json.optString("displayName", Constants.NA);
//		    email = json.optString("email", Constants.NA);
//		    username = json.optString("username", Constants.NA);
//		    profileImageUrl = json.optString("profileImageUrl", Constants.NA);
//		    idUrl = json.optString("idUrl", Constants.NA);
//		    deviceId = json.optString("deviceId", Constants.NA);
//		    deviceType = json.optString("deviceType", Constants.NA);
//		    registerDatetime = json.optString("registerDatetime", Constants.NA);
//		    lastLoginDatetime = json.optString("lastLoginDatetime", Constants.NA);
//		    sessionPasscode = json.optString("sessionPasscode", Constants.NA);
//		}
//		
//		public JSONObject toJson() {
//			JSONObject json = new JSONObject();
//			json.put("id", id);
//		    json.put("sfUserId", sfUserId);
//		    json.put("orgId", orgId);
//		    json.put("firstName", firstName);
//		    json.put("lastName", lastName);
//		    json.put("displayName", displayName);
//		    json.put("email", email);
//		    json.put("username", username);
//		    json.put("profileImageUrl", profileImageUrl);
//		    json.put("idUrl", idUrl);
//		    json.put("deviceId", deviceId);
//		    json.put("deviceType", deviceType);
//		    json.put("registerDatetime", registerDatetime);
//		    json.put("lastLoginDatetime", lastLoginDatetime);
//		    json.put("sessionPasscode", sessionPasscode);	    
//			return json;
//		}
//		
//		public JSONObject jsonForSearch() {
//			JSONObject json = new JSONObject();
//			json.put("id", id);
//		    json.put("sfUserId", sfUserId);	    
//		    json.put("displayName", displayName);
//		    json.put("email", email);
//		    json.put("username", username);
//		    json.put("idUrl", idUrl);
//		    json.put("registerDatetime", registerDatetime);
//		    json.put("lastLoginDatetime", lastLoginDatetime);	    
//			return json;
//		}
//
//	
//
//}
