package models.admin;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class ActiveBranchUserInfo {
	
	public int totalUsers = 0;
	public int totalActiveUsers = 0;
	public int totalUsersWithKYC = 0;
	public int totalUsersWithPayment = 0;	
	public ArrayList<ActiveBranchUser> activeBranchUsers = new ArrayList<>();
	
	public ActiveBranchUserInfo() {}
	
	public JSONObject toJson() {
				
		JSONObject json = new JSONObject();
		
		json.put("totalUsers", totalUsers);
		json.put("totalActiveUsers", totalActiveUsers);
		json.put("totalUsersWithKYC", totalUsersWithKYC);
		json.put("totalUsersWithPayment", totalUsersWithPayment);
		
		JSONArray abuArray = new JSONArray();
		
		for (ActiveBranchUser item: activeBranchUsers) 
			abuArray.put(item.toJson());
		
		json.put("activeBranchUsers", abuArray);
		
		return json;
		
	}

}
