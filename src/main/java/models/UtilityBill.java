package models;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

import databasehelper.ColumnsNFields;
import utils.Constants;

public class UtilityBill {
	
	public int id = -1;
	public String mobileNumber = Constants.NA;
	public String billType = Constants.NA;
	public String billIdType = Constants.NA;
	public String billId = Constants.NA;
	public String serviceProvider = Constants.NA;
	public boolean isVerified = false;
	public String customerName = Constants.NA;
	public String customerAddress = Constants.NA;
	public String rawData = Constants.NA;
	
	public UtilityBill() {}
	
	public UtilityBill(ResultSet resultSet) throws SQLException
	{
		UtilityBill utilityBill = new UtilityBill();
		
		utilityBill.id = resultSet.getInt(ColumnsNFields.UtilityBillColumn.ID.value);
		utilityBill.mobileNumber = resultSet.getString(ColumnsNFields.UtilityBillColumn.MOBILE_NUMBER.value);
		utilityBill.billType = resultSet.getString(ColumnsNFields.UtilityBillColumn.BILL_TYPE.value);
		utilityBill.billIdType = resultSet.getString(ColumnsNFields.UtilityBillColumn.BILL_ID_TYPE.value);
		utilityBill.serviceProvider = resultSet.getString(ColumnsNFields.UtilityBillColumn.SERVICE_PROVIDER.value);
		utilityBill.customerName = resultSet.getString(ColumnsNFields.UtilityBillColumn.CUSTOMER_NAME.value);
		utilityBill.customerAddress = resultSet.getString(ColumnsNFields.UtilityBillColumn.CUSTOMER_ADDRESS.value);
		utilityBill.rawData = resultSet.getString(ColumnsNFields.UtilityBillColumn.RAW_DATA.value);
		
	}
	
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("id", id);
		json.put("mobileNumber", mobileNumber);
		json.put("billType", billType);
		json.put("billIdType", billIdType);
		json.put("billId", billId);
		json.put("serviceProvider", serviceProvider);
		json.put("isVerified", isVerified);
		json.put("customerName", customerName);
		json.put("customerAddress", customerAddress);
		
		if (null != rawData && !rawData.equals(Constants.NA))
			json.put("rawResponse", new JSONObject(rawData));
		else 
			json.put("rawResponse", new JSONObject());
		
		return json;
	}

	

}
