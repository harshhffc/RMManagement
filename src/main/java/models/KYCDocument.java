package models;

import org.json.JSONArray;
import org.json.JSONObject;

import utils.Constants;

public class KYCDocument {
	
	public int id = -1;
	public String mobileNumber = Constants.NA;
	public String documentType = Constants.NA;
	public String documentId = Constants.NA;
	public String documentURL = Constants.NA;
	public boolean isVerified = false;
	public String userName = Constants.NA;
	public String userGender = Constants.NA;
	public String userDOB = Constants.NA;
	public String userImageURL = Constants.NA;
	public String address = Constants.NA;
	public String rawResponse = Constants.NA;
	public String frontImageUrl = Constants.NA;
	public String backImageUrl = Constants.NA;
	public String maskedFrontImageUrl = Constants.NA;
	public String maskedBackImageUrl = Constants.NA;
	
	public KYCDocument() {}
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("id", id);
		json.put("mobileNumber", mobileNumber);
		json.put("documentType", documentType);
		json.put("documentId", documentId);
		json.put("documentURL", documentURL);
		json.put("isVerified", isVerified);
		json.put("userName", userName);
		json.put("userGender", userGender);
		json.put("userDOB", userDOB);
		json.put("userImageURL", userImageURL);
		
		if (null != address && !address.equals(Constants.NA))
			json.put("address", new JSONObject(address));
		else 
			json.put("address", new JSONObject());		
		
		if (null != rawResponse && !rawResponse.equals(Constants.NA))
			json.put("rawResponse", new JSONObject(rawResponse));
		else 
			json.put("rawResponse", new JSONObject());
		
		return json;
	}
	
	public JSONObject toJsonWithArrayRawResponse() {
		
		JSONObject json = new JSONObject();
		
		json.put("id", id);
		json.put("mobileNumber", mobileNumber);
		json.put("documentType", documentType);
		json.put("documentId", documentId);
		json.put("documentURL", documentURL);
		json.put("isVerified", isVerified);
		json.put("userName", userName);
		json.put("userGender", userGender);
		json.put("userDOB", userDOB);
		json.put("userImageURL", userImageURL);
		
		if (null != address && !address.equals(Constants.NA))
			json.put("address", new JSONObject(address));
		else 
			json.put("address", new JSONObject());		
		
		if (null != rawResponse && !rawResponse.equals(Constants.NA))
			json.put("rawResponse", new JSONArray(rawResponse));
		else 
			json.put("rawResponse", new JSONArray());
		
		json.put("maskedFrontImageUrl", maskedFrontImageUrl);
		json.put("maskedBackImageUrl", maskedBackImageUrl);
		
		return json;
	}

}
