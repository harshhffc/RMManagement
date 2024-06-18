package models;

import org.json.JSONObject;

import utils.Constants;

public class ScoreWeightage {
	
	public int id = -1;
	public String contestName = Constants.NA;
	public String startDatetime = Constants.NA;
	public String endDatetime = Constants.NA;
	public int target = -1;
	public int leadCreated = -1;
	public int leadConverted = -1;
	public int kycDocument = -1;
	public int utilityBill = -1;
	public int vehicleRC = -1;
	public int gstin = -1;
	public int epf = -1;
	public int itr = -1;
	public int payment = -1;
	public int bankStatement = -1;
	public int taskCompleted = -1;
	public int visitCompleted = -1;
	public int taskMaxPointsAllowed = -1;
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("contestName", contestName);
		json.put("startDatetime", startDatetime);
		json.put("endDatetime", endDatetime);
		json.put("target", target);
		json.put("leadCreated", leadCreated);
		json.put("leadConverted", leadConverted);
		json.put("kycDocument", kycDocument);
		json.put("utilityBill", utilityBill);
		json.put("vehicleRC", vehicleRC);
		json.put("gstin", gstin);
		json.put("epf", epf);
		json.put("itr", itr);
		json.put("payment", payment);
		json.put("bankStatement", bankStatement);
		json.put("taskCompleted", taskCompleted);
		json.put("visitCompleted", visitCompleted);
		json.put("taskMaxPointsAllowed", taskMaxPointsAllowed);
		return json;
	}

}
