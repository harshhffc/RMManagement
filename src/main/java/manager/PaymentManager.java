package manager;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import databasehelper.RMDatabaseHelper;
import models.DefaultResponse;
import models.PaymentInfo;
import utils.BasicUtils;
import utils.DateTimeUtils;

public class PaymentManager {
	
	public static final String CHECKOUT = "checkout";
	public static final String PAYMENT_UPDATE_TYPE = "paymentUpdateType";
	public static final String PAYMENT_UPDATE_STATUS = "paymentUpdateType";
	public static final String PARTIAL_PRE_PAYMENT = "Partial Pre-Payment";
	public static final String RAZOR_PAY_PAID_STATUS = "Paid";

	public enum PaymentStatus {
		NONE("none"),
		INITIATED("initiated"),
		PENDING("pending"),
		SUCCESS("success"),
		CANCELED("canceled"),
		FAILED("failed"),
		CREATED("created");		
		
		public final String value;
		PaymentStatus(String value) {
			this.value = value;
		}
		
	}
	
	public enum PaymentUpdateType {
	    PAYMENT("payment"),
	    RECEIPT("receipt");
	    
	    public final String value;
	    PaymentUpdateType(String value) {
			this.value = value;
		}
	    
	    public static PaymentUpdateType get(String value) {
	    	for (PaymentUpdateType item: PaymentUpdateType.values()) {
	    		if (item.value.equalsIgnoreCase(value))
	    			return item;
	    	}
	    	return null;
	    }
	}
	
	public PaymentManager() {}
	
	public JSONObject initalizePayment(String body) throws Exception {
		
		JSONObject requestObject = new JSONObject(body);
		JSONObject checkoutJson = requestObject.getJSONObject(CHECKOUT);
		PaymentInfo rmCheckout;
		if (checkoutJson.has("nameValuePairs")) 
			rmCheckout = new PaymentInfo(checkoutJson.getJSONObject("nameValuePairs"));
		else
			rmCheckout = new PaymentInfo(checkoutJson);
		
		if (rmCheckout.userId < 1) {
			DefaultResponse errorResponse = new DefaultResponse();
			errorResponse.message = "Invalid User ID.";
			return errorResponse.toJson();
		}
		
		if (rmCheckout.amount < 1) {
			DefaultResponse errorResponse = new DefaultResponse();
			errorResponse.message = "Invalid Amount.";
			return errorResponse.toJson();
		}
		
		RMDatabaseHelper dbHelper = new RMDatabaseHelper();
		
		try {
			
			rmCheckout = dbHelper.initalizePayment(rmCheckout);
			dbHelper.close();
			
			if (null != rmCheckout) {
				
				JSONObject resonseJson = BasicUtils.getSuccessTemplateObject();
				resonseJson.put("checkout", rmCheckout.toJson());
				return resonseJson;
				
			} else return BasicUtils.getFailureTemplateObject();
			
		} catch (Exception e) {						
			dbHelper.close();
			throw e;
		}
		
	}
	
	public JSONObject updatePayment(String body) throws Exception {
		
		JSONObject requestObject = new JSONObject(body);
		JSONObject checkoutJson = requestObject.getJSONObject(CHECKOUT);
		PaymentInfo rmCheckout;
		if (checkoutJson.has("nameValuePairs")) 
			rmCheckout = new PaymentInfo(checkoutJson.getJSONObject("nameValuePairs"));
		else
			rmCheckout = new PaymentInfo(checkoutJson);
		
		if (rmCheckout.userId < 1) {
			DefaultResponse errorResponse = new DefaultResponse();
			errorResponse.message = "Invalid User ID.";
			return errorResponse.toJson();
		}
		
		if (rmCheckout.amount < 1) {
			DefaultResponse errorResponse = new DefaultResponse();
			errorResponse.message = "Invalid Amount.";
			return errorResponse.toJson();
		}
		
		PaymentUpdateType updateType = PaymentUpdateType.get(requestObject.getString(PAYMENT_UPDATE_TYPE));
		
		RMDatabaseHelper dbHelper = new RMDatabaseHelper();
		
		PaymentInfo ePaymentInfo = dbHelper.getPaymentInfoById(rmCheckout.transactionId);
		
		try {
			
			if (updateType == PaymentUpdateType.PAYMENT) {
				
				rmCheckout = dbHelper.updatePaymentInfo(updateType, rmCheckout);
				
			} else {
				
				if(!BasicUtils.isNotNullOrNA(ePaymentInfo.completionDatetime)) 
					rmCheckout.completionDatetime = DateTimeUtils.getCurrentDateTimeInIST();
				else
					rmCheckout.completionDatetime = ePaymentInfo.completionDatetime;
		
				rmCheckout = dbHelper.updateReceiptInfo(updateType, rmCheckout);
				
			}
			
			dbHelper.close();
			
			if (null != rmCheckout) {
				
				JSONObject resonseJson = BasicUtils.getSuccessTemplateObject();
				resonseJson.put(CHECKOUT, rmCheckout.toJson());
				return resonseJson;
				
			} else return BasicUtils.getFailureTemplateObject();
						
		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}
		
	}
	
	public JSONObject getPendingPayments(int userId) throws Exception {
		
		RMDatabaseHelper dbHelper = new RMDatabaseHelper();
		
		try {
			
			ArrayList<PaymentInfo> payments = dbHelper.getPendingPayments(userId);
			dbHelper.close();
			
			JSONObject responseJson = BasicUtils.getSuccessTemplateObject();
			JSONArray paymentArray = new JSONArray();
			
			for (PaymentInfo payment: payments) {
				paymentArray.put(payment.toJson());
			}
			
			responseJson.put("payments", paymentArray);
			return responseJson;
						
		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}
		
	}
	
}