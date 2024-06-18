package models.admin;

import org.json.JSONObject;

import utils.Constants;

public class ActiveBranchUser {

	public String branchName = Constants.NA;
	public int totalRMCount = 0;
	public int totalActiveUser = 0;
	public int appAdoption = 0;
	public int rmWithKYC = 0;
	public int kycAdoption = 0;
	public int rmWithPayment = 0;
	public int paymentAdoption = 0;
	
	public ActiveBranchUser() {}
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("branchName", branchName);
		json.put("totalRMCount", totalRMCount);
		json.put("totalActiveUser", totalActiveUser);
		json.put("appAdoption", appAdoption);
		json.put("rmWithKYC", rmWithKYC);
		json.put("kycAdoption", kycAdoption);
		json.put("rmWithPayment", rmWithPayment);
		json.put("paymentAdoption", paymentAdoption);
		
		return json;
		
	}
	
}
