package models;

import org.json.JSONObject;

import utils.BasicUtils;
import utils.Constants;

public class UserAddress {

	public String street = Constants.NA;
	public String city = Constants.NA;
	public String state = Constants.NA;
	public String postalCode = Constants.NA;
	public String country = "India";

	public UserAddress() {
	}

	public UserAddress(JSONObject json) {

		if (null == json)
			return;
		
		json = BasicUtils.fetchJson(json);

		street = json.optString("street", Constants.NA);
		city = json.optString("city", Constants.NA);
		state = json.optString("state", Constants.NA);
		postalCode = json.optString("postalCode", Constants.NA);
		country = json.optString("country", country);

	}

	public JSONObject toJson() {

		JSONObject json = new JSONObject();

		json.put("street", street);
		json.put("city", city);
		json.put("state", state);
		json.put("postalCode", postalCode);
		json.put("country", country);

		return json;

	}

}
