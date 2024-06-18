package v2.managers;

import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import totalkyc.DocumentKYCHelper.DocumentType;
import utils.BasicUtils;
import utils.Constants;
import utils.LoggerUtils;
import utils.ProptertyUtils;
import utils.ProptertyUtils.Keys;

public class ContactManager {

	private static String authKey = null;
	private final String templateId = "5ec37ba7d6fc05331c64e308";
	private final String extraParam = "{\"COMPANY_NAME\":\"HomeFirst\"}";
	
	public ContactManager() throws Exception {
		if(null == authKey)
			authKey = ProptertyUtils.getValurForKey(Keys.AUTH_KEY);
	}
	
	public enum OTPVerifyResponseMessage{
		
		INVALID_OTP("invalid_otp"),
		OTP_NOT_VERIFIED("otp_not_verified"),
		OTP_EXPIRED("otp_expired"),
		MOBILE_NO_NOT_FOUND("Mobile no. not found"),
		OTP_REQUEST_INVALID("OTP request invalid"),
		OTP_NOT_MATCH("OTP not match"),
		MOBILE_NO_ALREADY_VERIFIED("Mobile no. already verified");
		
		
		public final String value;

		OTPVerifyResponseMessage(String value) {
			this.value = value;
		}
		
	}
	
	public enum UtilityBillType {
		ELECTRICITY_BILL("electricity_bill"), 
		TELEPHONE_BILL("telephone_bill"), 
		MOBILE("mobile"), 
		LPG_ID("lpg_id");

		public final String value;

		UtilityBillType(String value) {
			this.value = value;
		}

		static DocumentType get(String name) {
			for (DocumentType item : DocumentType.values()) {
				if (item.value.equals(name))
					return item;
			}
			return null;
		}
	}

	public JSONObject sendOTP(String mobileNumber, String countryCode) throws Exception {

		JSONObject responseObject = new JSONObject();

		String uri = "https://api.msg91.com/api/v5/otp?" + "authkey=" + authKey + 
				"&template_id=" + templateId + 
				"&extra_param=" + URLEncoder.encode(extraParam, "UTF-8") +
				"&mobile="
				+ (countryCode + mobileNumber);

		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPost = new HttpPost(uri.replace(" ", "+"));

		HttpResponse response = httpClient.execute(httpPost);
		String response_string = EntityUtils.toString(response.getEntity());
		JSONObject json = new JSONObject(response_string);
		
		LoggerUtils.log("Send OTP response: " + json);

		if (!json.getString("type").equalsIgnoreCase(Constants.SUCCESS)) {

			LoggerUtils.log("Error while sending OTP: " + json.getString("request_id"));

			responseObject.put(Constants.STATUS, Constants.FAILURE);
			responseObject.put(Constants.MESSAGE, Constants.DEFAULT_ERROR_MESSAGE);

			return responseObject;
		} else {
			responseObject = BasicUtils.getSuccessTemplateObject();
			
			responseObject.put("otpReferenceId", json.getString("request_id"));
			return responseObject;
		}

	}

	public JSONObject verifyOTP(String mobileNumber, String countryCode, String OTP) throws Exception {

		JSONObject responseObject = new JSONObject();

		String uri = "https://api.msg91.com/api/v5/otp/verify?" + "authkey=" + authKey + "&mobile="
				+ (countryCode + mobileNumber) + "&otp=" + OTP;

		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPost = new HttpPost(uri.replace(" ", "+"));
		httpPost.addHeader("content-type", "application/x-www-form-urlencoded");

		HttpResponse response = httpClient.execute(httpPost);
		String response_string = EntityUtils.toString(response.getEntity());

		JSONObject json = new JSONObject(response_string);

		LoggerUtils.log("Verify OTP response: " + json);

		if (!json.getString("type").equalsIgnoreCase(Constants.SUCCESS)) {
			
			String message = json.getString("message");

			LoggerUtils.log("Error while sending OTP: " + message);
			

			if (message.equalsIgnoreCase(OTPVerifyResponseMessage.INVALID_OTP.value) 
					|| message.equalsIgnoreCase(OTPVerifyResponseMessage.OTP_NOT_VERIFIED.value)
					|| message.equalsIgnoreCase(OTPVerifyResponseMessage.OTP_EXPIRED.value)
					|| message.equalsIgnoreCase(OTPVerifyResponseMessage.MOBILE_NO_NOT_FOUND.value)
					|| message.equalsIgnoreCase(OTPVerifyResponseMessage.OTP_REQUEST_INVALID.value)
					|| message.equalsIgnoreCase(OTPVerifyResponseMessage.OTP_NOT_MATCH.value) 
			) {
				responseObject.put(Constants.STATUS, Constants.FAILURE);
				responseObject.put(Constants.MESSAGE, "Invalid OTP");
			}else if (message.equals(OTPVerifyResponseMessage.MOBILE_NO_ALREADY_VERIFIED.value)){
				responseObject.put(Constants.STATUS, Constants.SUCCESS);
            	responseObject.put(Constants.MESSAGE, Constants.NA);
			}
			else {
				responseObject.put(Constants.STATUS, Constants.FAILURE);
				responseObject.put(Constants.MESSAGE, Constants.DEFAULT_ERROR_MESSAGE);
			}
			return responseObject;
		} else {
			responseObject.put(Constants.STATUS, Constants.SUCCESS);
			responseObject.put(Constants.MESSAGE, Constants.NA);
			return responseObject;
		}

	}

	public JSONObject resendOTP(String mobileNumber, String countryCode) throws Exception {

		JSONObject responseObject = new JSONObject();

		String uri = "https://api.msg91.com/api/v5/otp/retry?" + "authkey=" + authKey + "&mobile="
				+ (countryCode + mobileNumber);

		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPost = new HttpPost(uri.replace(" ", "+"));
		httpPost.addHeader("content-type", "application/x-www-form-urlencoded");

		HttpResponse response = httpClient.execute(httpPost);
		String response_string = EntityUtils.toString(response.getEntity());
		JSONObject json = new JSONObject(response_string);

		LoggerUtils.log("Resend OTP response: " + json);

		if (!json.getString("type").equalsIgnoreCase(Constants.SUCCESS)) {
			if (json.getString("message").equalsIgnoreCase("invalid_mobile_number")
					|| json.getString("message").equalsIgnoreCase("No OTP request found to retryotp")
					|| json.getString("message").equalsIgnoreCase("last_otp_request_on_this_number_is_invalid")
					) {
				responseObject.put(Constants.STATUS, Constants.FAILURE);
				responseObject.put(Constants.MESSAGE, "Invalid mobile number");
			} else {
				responseObject.put(Constants.STATUS, Constants.FAILURE);
				responseObject.put(Constants.MESSAGE, Constants.DEFAULT_ERROR_MESSAGE);
			}
			return responseObject;

		} else {
			responseObject.put(Constants.STATUS, Constants.SUCCESS);
			responseObject.put(Constants.MESSAGE, Constants.NA);
			return responseObject;
		}

	}

}
