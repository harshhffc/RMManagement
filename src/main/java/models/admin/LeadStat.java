package models.admin;

import org.json.JSONObject;

public class LeadStat {

	public int rmProCount = 0;
	public int sfCount = 0;
	
	public LeadStat() {}

	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("rmProCount", rmProCount);
		json.put("sfCount", sfCount);
		
		return json;
		
	}
	
}
