package models;

import org.json.JSONObject;

import utils.Constants;

public class SFApplicant {

	public int id = -1;
	public int userId = -1;
	public String customerName = Constants.NA;
	public String customerMobileNumber = Constants.NA;
	public String customerSecondaryMobileNumber = Constants.NA;
	public String leadStage = Constants.NA;
	public String leadId = Constants.NA;
	public String accountId = Constants.NA;
	public String contactId = Constants.NA;
	public String opportunityId = Constants.NA;
	public String createDatetime = Constants.NA;
	public String updateDatetime = Constants.NA;
	public String imageUrl = Constants.NA;
	public String source = Constants.NA;

	public SFApplicant() {}
	
	public SFApplicant(JSONObject json) {
	
		userId = json.optInt("userId", -1);
		customerName = json.optString("customerName", Constants.NA);
		customerMobileNumber = json.optString("customerMobileNumber", Constants.NA);
		customerSecondaryMobileNumber = json.optString("customerSecondaryMobileNumber", Constants.NA);
		leadStage = json.optString("leadStage", Constants.NA);
		leadId = json.optString("leadId", Constants.NA);
		accountId = json.optString("accountId", Constants.NA);
		contactId = json.optString("contactId", Constants.NA);
		opportunityId = json.optString("opportunityId", Constants.NA);

	}
	
}
