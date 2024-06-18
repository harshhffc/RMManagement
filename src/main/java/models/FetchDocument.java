package models;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import utils.Constants;

public class FetchDocument {

	public String type = Constants.NA;
	public String id = Constants.NA;
	public String customerName = Constants.NA;
	
	public FetchDocument() {}
	
	public FetchDocument(ResultSet resultSet) throws SQLException {
		
		type = resultSet.getString("type");
		id = resultSet.getString("id");
		customerName = resultSet.getString("customerName");

	}

	public JSONObject toJson() {

		JSONObject jsonObject = new JSONObject();

		jsonObject.put("type", type);
		jsonObject.put("id", id);
		jsonObject.put("customerName", customerName);

		return jsonObject;
	}
	
}
