package models;

import org.json.JSONObject;

import utils.Constants;

public class SFCoApplicant {

	public int id = -1;
	public int userId = -1;
	public String customerName = Constants.NA;
	public String customerMobileNumber = Constants.NA;
	public String customerSecondaryMobileNumber = Constants.NA;
	public String contactId = Constants.NA;
	public String applicantOpportunityId = Constants.NA;
	public String createDatetime = Constants.NA;
	public String updateDatetime = Constants.NA;
	public String imageUrl = Constants.NA;

	
	public SFCoApplicant() {}
	
	public SFCoApplicant(JSONObject json) {
		
		userId = json.optInt("userId", -1);
		customerName = json.optString("customerName", Constants.NA);
		customerMobileNumber = json.optString("customerMobileNumber", Constants.NA);
		customerSecondaryMobileNumber = json.optString("customerSecondaryMobileNumber", Constants.NA);
		contactId = json.optString("contactId", Constants.NA);
		applicantOpportunityId = json.optString("applicantOpportunityId", Constants.NA);
		createDatetime = json.optString("createDatetime", Constants.NA);
		updateDatetime = json.optString("updateDatetime", Constants.NA);

	}
	
}
