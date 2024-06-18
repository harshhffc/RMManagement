package models;

import org.json.JSONObject;

import utils.Constants;

public class DefaultResponse {

	public boolean isSuccess = false;
	public String message = Constants.DEFAULT_ERROR_MESSAGE;
	public String error = Constants.NA;
	
	public DefaultResponse() {}
	
	public DefaultResponse setStatus(boolean status) {
		this.isSuccess = status;
		return this;
	}
	
	public DefaultResponse setMessage(String message) {
		this.message = message;
		return this;
	}
	
	public DefaultResponse setError(String error) {
		this.error = error;
		return this;
	}
	
	public JSONObject toJson() {
		
		JSONObject jsonObject = new JSONObject();
		
		try {
			
			jsonObject.put(Constants.STATUS, isSuccess ? Constants.SUCCESS : Constants.FAILURE);
			jsonObject.put(Constants.MESSAGE, message);
			jsonObject.put(Constants.ERROR, error);
			
		} catch (Exception e) {
			System.out.println("error while generating json object from LocalResponse: " + e.toString());
		}
		
		return jsonObject;
		
	}
	
}
