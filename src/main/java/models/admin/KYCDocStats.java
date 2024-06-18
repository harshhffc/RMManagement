package models.admin;

import org.json.JSONObject;

import utils.Constants;

public class KYCDocStats {

	public String type = Constants.NA;
	public int count = 0;
	
	public KYCDocStats() {}
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("type", type);
		json.put("count", count);
		
		return json;
		
	}
	
}
