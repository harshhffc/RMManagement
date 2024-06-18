package models;

import org.json.JSONObject;

import utils.BasicUtils;
import utils.Constants;

public class SFCustomer {

	public String fullName = Constants.NA;
	public String gender = Constants.NA;
	public String mobileNumber = Constants.NA;
	public String emailId = Constants.NA;
	public String mailingCity = Constants.NA;
	public String accountId = Constants.NA;
	public String contactId = Constants.NA;
	public String opportunityId = Constants.NA;
	public String crmAccountNumber = Constants.NA;
	public String opportunityNumber = Constants.NA;
	public String loanAccountNumber = Constants.NA;
	public String clContractNumber = Constants.NA;
	public String stage = Constants.NA;
	public String branchName = Constants.NA;
	public String ownerId = Constants.NA;
	public String xSellProductId = Constants.NA;
	public String loanProductType = Constants.NA;
	public String subProductType = Constants.NA;

	public SFCustomer() {
	}

	public SFCustomer(JSONObject json, boolean isHomeLoan) {

		if (null == json)
			return;

		json = BasicUtils.fetchJson(json);

		if (!isHomeLoan) {

			JSONObject oppJson = json.optJSONObject("Opportunity__r");
			fullName = oppJson.optString("Name", fullName);
			JSONObject primayContact = oppJson.optJSONObject("Primary_Contact__r");
			if (null != primayContact) {
				gender = primayContact.optString("Gender__c", gender);
				mobileNumber = primayContact.optString("MobilePhone", mobileNumber);
				emailId = primayContact.optString("Email", emailId);
				mailingCity = primayContact.optString("MailingCity", mailingCity);
			}
			accountId = oppJson.optString("AccountId", accountId);
			contactId = oppJson.optString("Primary_Contact__c", contactId);
			opportunityId = oppJson.optString("Id", opportunityId);
			crmAccountNumber = oppJson.optString("CRM_Account_No__c", crmAccountNumber);
			opportunityNumber = oppJson.optString("Opportunity_No__c", opportunityNumber);
			stage = oppJson.optString("StageName", stage);
			branchName = oppJson.optJSONObject("Opportunity_Branch_New__r").optString("Name", branchName);
			ownerId = oppJson.optString("OwnerId", ownerId);

			loanAccountNumber = json.optString("Name", loanAccountNumber);
			clContractNumber = json.optString("Name", clContractNumber);
			xSellProductId = json.optString("X_Sell_Products__c", xSellProductId);
			loanProductType = json.optString("loan__Loan_Product_Name__c", loanProductType);
			subProductType = json.optString("Sub_Product_Type__c", subProductType);
		} else {

			JSONObject oppJson = json;
			fullName = oppJson.optString("Name", fullName);
			JSONObject primayContact = oppJson.optJSONObject("Primary_Contact__r");

			if (null != primayContact) {

				gender = primayContact.optString("Gender__c", gender);
				mobileNumber = primayContact.optString("MobilePhone", mobileNumber);
				emailId = primayContact.optString("Email", emailId);
				mailingCity = primayContact.optString("MailingCity", mailingCity);

			}
			accountId = oppJson.optString("AccountId", accountId);
			contactId = oppJson.optString("Primary_Contact__c", contactId);
			opportunityId = oppJson.optString("Id", opportunityId);
			crmAccountNumber = oppJson.optString("CRM_Account_No__c", crmAccountNumber);
			opportunityNumber = oppJson.optString("Opportunity_No__c", opportunityNumber);
			stage = oppJson.optString("StageName", stage);

			JSONObject branchJson = oppJson.optJSONObject("Opportunity_Branch_New__r");
			if (null != branchJson) {
				branchName = branchJson.optString("Name", branchName);
			}
			ownerId = oppJson.optString("OwnerId", ownerId);

			loanAccountNumber = oppJson.optString("CL_Contract_No_LAI__c", loanAccountNumber);
			clContractNumber = oppJson.optString("CL_Contract_No_LAI__c", clContractNumber);

			xSellProductId = json.optString("X_Sell_Products__c", xSellProductId);
			loanProductType = json.optString("loan__Loan_Product_Name__c", loanProductType);
			subProductType = json.optString("Sub_Product_Type__c", subProductType);
		}

	}

	public JSONObject toJson() {

		JSONObject json = new JSONObject();

		json.put("fullName", fullName);
		json.put("gender", gender);
		json.put("mobileNumber", mobileNumber);
		json.put("emailId", emailId);
		json.put("mailingCity", mailingCity);
		json.put("accountId", accountId);
		json.put("contactId", contactId);
		json.put("opportunityId", opportunityId);
		json.put("crmAccountNumber", crmAccountNumber);
		json.put("opportunityNumber", opportunityNumber);
		json.put("loanAccountNumber", loanAccountNumber);
		json.put("clContractNumber", clContractNumber);
		json.put("stage", stage);
		json.put("branchName", branchName);
		json.put("ownerId", ownerId);
		json.put("xSellProductId", xSellProductId);
		json.put("loanProductType", loanProductType);
		json.put("subProductType", subProductType);

		return json;

	}

}
