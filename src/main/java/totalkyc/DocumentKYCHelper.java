package totalkyc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import models.DefaultResponse;
import models.KYCAuth;
import models.KYCDocument;
import models.UtilityBill;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import services.ImageService;
import utils.BasicUtils;
import utils.Constants;
import utils.DatabaseHelper;
import utils.LoggerUtils;
import utils.ProptertyUtils;
import utils.ProptertyUtils.Keys;
import v2.managers.AmazonClient;
import v2.managers.AmazonClient.S3BucketPath;

public class DocumentKYCHelper {

	public static final String KEY_CONSENT = "consent";
	public static final String KEY_PAN = "pan";
	public static final String KEY_NAME = "name";
	public static final String KEY_LAST_NAME = "last_name";
	public static final String KEY_DOI = "doi";
	public static final String KEY_DOB = "dob";
	public static final String KEY_DL_NUMBER = "dl_no";
	public static final String KEY_EPIC_NUMBER = "epic_no";
	public static final String KEY_GENDER = "gender";
	public static final String KEY_PASSPORT_NUMBER = "passportNo";
	public static final String KEY_AADHAR_NUMBER = "aadhar_number";
	public static final String KEY_TYPE = "type";
	public static final String KEY_COUNTRY = "country";
	public static final String KEY_MOBILE_NUMBER = "mobile_number";
	public static final String KEY_MOBILE = "mobile";
	public static final String KEY_DOUCMENT_TYPE = "document_type";
	public static final String KEY_BILL_TYPE = "bill_type";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_OTP = "otp";
	public static final String KEY_SHOULD_VERIFY = "should_verify";
	public static final String KEY_KYC_REQUEST_ID = "request_id";
	public static final String KEY_KYC_STATUS_CODE = "status-code";
	public static final String KEY_KYC_RESULT = "result";
	public static final String KEY_RESPONSE = "response";
	public static final String KEY_CONSUMER_ID = "consumer_id";
	public static final String KEY_SERVICE_PROVIDER = "service_provider";
	public static final String KEY_TELEPHONE_NUMBER = "tel_no";
	public static final String KEY_LPG_ID = "lpg_id";
	public static final String KEY_ACK = "ack";
	public static final String KEY_REG_NUMBER = "reg_no";
	public static final String KEY_UAN = "uan";
	public static final String KEY_MOBILE_NO = "mobile_no";
	public static final String KEY_KYC_OCR = "OCR";
	public static final String KEY_KYC_AUTHENTICATION = "authentication";
	public static final String KEY_FILE_NAME = "file_name";
	public static final String KEY_FILE_DATA = "file_data";
	public static final String KEY_FRONT_IMAGE_FILE = "front_image_file";
	public static final String KEY_FRONT_IMAGE_NAME = "front_image_name";
	public static final String KEY_BACK_IMAGE_FILE = "back_image_file";
	public static final String KEY_BACK_IMAGE_NAME = "back_image_name";
	public static final String KEY_GST_NUMBER = "gstin";
	public static final String NAME_VALUE_PAIRS = "nameValuePairs";

	public static final String GENERATE_STRUCTURED_DATA = "generate_structured_data";
	private final static int KYC_TIMEOUT = 120;

	public static final String KARZA_BASE_URL_TEST = "https://testapi.karza.in";
	public static final String KARZA_BASE_URL_PRODUCTION = "https://api.karza.in";

	public static final String KARZA_GST_BASE_URL_TEST = "https://gst.karza.in/uat";
	public static final String KARZA_GST_BASE_URL_PRODUCTION = "https://gst.karza.in/prod";

	public static final String KARZA_API_VERSION = "/v2";

	public static final String UPLOAD_FILE_SERVER = "/var/www/images/document_picture/";
	public static final String UPLOAD_FILE_LOCAL_SERVER = "/Users/sanjay/var/www/images/document_picture/";

	public static final String S3_BUCKET_DOUCMENT_URL = "https://homefirstindia-s3bucket.s3.ap-south-1.amazonaws.com/RMManagement/Documents/";

	private static String karzaApiKey = null;

	private enum KarzaApiVersion {
		V1("/v1"), V2("/v2"), V3("/v3");

		public final String value;

		KarzaApiVersion(String value) {
			this.value = value;
		}

	}

	public enum DocumentType {
		PAN_CARD("pan_card"), AADHAR_CARD("aadhar_card"), DRIVER_LICENSE("drivers_license"), VOTER_ID("voter_id"),
		PASSPORT("passport");

		public final String value;

		DocumentType(String value) {
			this.value = value;
		}

		public static DocumentType get(String name) {
			for (DocumentType item : DocumentType.values()) {
				if (item.value.equals(name))
					return item;
			}
			return null;
		}
	}

	public enum UtilityBillType {
		ELECTRICITY_BILL("electricity_bill"), TELEPHONE_BILL("telephone_bill"), MOBILE("mobile"), LPG_ID("lpg_id");

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

	private int userId = -1;

	public DocumentKYCHelper() {
	}

	public DocumentKYCHelper(int userId) {
		this.userId = userId;
	}

	// MARK: - Common implementation

	private String getKarzaGstApiUrl(KarzaApiVersion version) {

		String karzaApiUrl = Constants.NA;
		if (Constants.IS_API_IN_PRODUCTION)
			karzaApiUrl = KARZA_GST_BASE_URL_PRODUCTION;
		else
			karzaApiUrl = KARZA_GST_BASE_URL_TEST;

		if (null != version) {
			return karzaApiUrl + version.value;
		} else {
			return karzaApiUrl + KarzaApiVersion.V2.value;
		}
	}

	private String getKarzaApiUrl(KarzaApiVersion version) {

		String karzaApiUrl = Constants.NA;
		if (Constants.IS_API_IN_PRODUCTION)
			karzaApiUrl = KARZA_BASE_URL_PRODUCTION;
		else
			karzaApiUrl = KARZA_BASE_URL_TEST;

		if (null != version) {
			return karzaApiUrl + version.value;
		} else {
			return karzaApiUrl + KarzaApiVersion.V2.value;
		}
	}

	private String getKarzaAPIKey() throws Exception {

		if (null == karzaApiKey) {

			if (Constants.IS_API_IN_PRODUCTION)
				karzaApiKey = ProptertyUtils.getValurForKey(Keys.KARZA_PRODUCTION_API_KEY);

			else
				karzaApiKey = ProptertyUtils.getValurForKey(Keys.KARZA_TEST_API_KEY);

		}

		return karzaApiKey;

	}

	public DefaultResponse getTokenStatus(String token) {

		DefaultResponse dResponse = new DefaultResponse();

		DatabaseHelper dbHelper = new DatabaseHelper(userId);

		try {

			KYCAuth authInfo = dbHelper.getKYCAuthInfo(BasicUtils.getTheKey(token));
			dbHelper.close();

			if (null != authInfo) {
				if (authInfo.isValid) {
					dResponse.isSuccess = true;
					dResponse.message = "Auth token is valid";
				} else {
					dResponse.isSuccess = false;
					if (authInfo.tokenCount == -1 || authInfo.tokenCount > 0)
						dResponse.message = "Token is valid. But your access has been denied.";
					else
						dResponse.message = "Token usage has been exhausted.";
				}
			} else
				dResponse.message = "Invalid auth token.";

		} catch (Exception e) {
			dbHelper.close();
			dResponse.message = "Invalid auth token.";
			LoggerUtils.log("Error while getting kyc auth token status: " + e.toString());
			e.printStackTrace();
		}

		return dResponse;

	}

	public boolean isTotalKYCSuccess(JSONObject response) {
		return response.optInt(KEY_KYC_STATUS_CODE, -1) == 101;
	}

	public boolean isTotalKYCSuccessV3(JSONObject response) {
		return response.optInt("statusCode", -1) == 101;
	}

	private boolean shouldGetStructuredData(JSONObject request) {
		return request.optBoolean(GENERATE_STRUCTURED_DATA, false);
	}

	public JSONObject totalKYCDefaultPostCall(JSONObject jsonRequest, String endPoint) throws Exception {

		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

		MediaType mediaType = MediaType.parse("application/json");
		RequestBody requestBody = RequestBody.create(mediaType, jsonRequest.toString());
		Request request = new Request.Builder().url(endPoint).post(requestBody)
				.addHeader("content-type", "application/json").addHeader("x-karza-key", getKarzaAPIKey()).build();

		Response response = client.newCall(request).execute();
		JSONObject responseObject = new JSONObject(response.body().string());

		return responseObject;

	}

	public JSONObject totalKYCDefaultPostCall(JSONObject jsonRequest, String endPoint, int timeout) throws Exception {

		// OkHttpClient client = new OkHttpClient();

		OkHttpClient client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS)
				.writeTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout, TimeUnit.SECONDS).build();

		MediaType mediaType = MediaType.parse("application/json");
		RequestBody requestBody = RequestBody.create(mediaType, jsonRequest.toString());
		Request request = new Request.Builder().url(endPoint).post(requestBody)
				.addHeader("content-type", "application/json").addHeader("x-karza-key", getKarzaAPIKey()).build();

		Response response = client.newCall(request).execute();
		JSONObject responseObject = new JSONObject(response.body().string());

		LoggerUtils.log("===>  KYCDefaultPostCall Respones: " + responseObject.toString());

		return responseObject;

	}

	// MARK: - Document verification and authentication

	public JSONObject documentKYC(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		JSONObject responseObject = new JSONObject();

		String documentType = bodyObject.optString(KEY_DOUCMENT_TYPE, Constants.NA);
		if (!documentType.equals(Constants.NA)) {

			DocumentType dType = DocumentType.get(documentType);

			switch (dType) {

			case PAN_CARD:
				if (bodyObject.optInt(KEY_SHOULD_VERIFY, 0) == 1)
					responseObject = verifyPAN(bodyObject);
				else
					responseObject = authenticatePAN(bodyObject);
				break;

			case DRIVER_LICENSE:
				responseObject = authenticateDL(bodyObject);
				break;

			case AADHAR_CARD:
				responseObject = authenticateAadhar(bodyObject);
				break;

			case VOTER_ID:
				responseObject = authenticateVotedID(bodyObject);
				break;

			case PASSPORT:
				responseObject = authenticatePassport(bodyObject);
				break;

			default:
				DefaultResponse errorResponse = new DefaultResponse();
				errorResponse.isSuccess = false;
				errorResponse.message = "Invalid document type.";
				responseObject = errorResponse.toJson();
				break;

			}

		} else {

			DefaultResponse errorResponse = new DefaultResponse();
			errorResponse.isSuccess = false;
			errorResponse.message = "No value for key: " + KEY_DOUCMENT_TYPE;
			return errorResponse.toJson();

		}

		return responseObject;

	}

	public JSONObject authenticatePAN(JSONObject jsonRequest) throws Exception {

		DefaultResponse requestStatus = KYCRequestValidator.validatePANRequest(jsonRequest);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(jsonRequest, getKarzaApiUrl(null) + "/pan", KYC_TIMEOUT);

		LoggerUtils.log("PAN authentication reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.getJSONObject(KEY_KYC_RESULT);

			KYCDocument kycDocument = new KYCDocument();
			kycDocument.mobileNumber = jsonRequest.optString(KEY_MOBILE_NUMBER, Constants.NA);
			kycDocument.documentType = jsonRequest.optString(KEY_DOUCMENT_TYPE, Constants.NA);
			kycDocument.documentId = jsonRequest.optString(KEY_PAN, Constants.NA);
			kycDocument.isVerified = true;
			kycDocument.userName = kycResult.optString(KEY_NAME, Constants.NA);
			kycDocument.rawResponse = kycResult.toString();

			kycDocument.userDOB = jsonRequest.optString(KEY_DOB, Constants.NA);
			kycDocument.userGender = jsonRequest.optString(KEY_GENDER, Constants.NA);

			JSONObject addressObject = jsonRequest.optJSONObject(KEY_ADDRESS);
			if (null != addressObject) {
				if (addressObject.has(NAME_VALUE_PAIRS))
					kycDocument.address = addressObject.getJSONObject(NAME_VALUE_PAIRS).toString();
				else
					kycDocument.address = addressObject.toString();
			}

			DatabaseHelper dbHelper = new DatabaseHelper(userId);
			try {
				boolean status = dbHelper.insertOrUpdateVerifiedKYCData(kycDocument, KEY_KYC_AUTHENTICATION);
				dbHelper.close();
				if (status)
					LoggerUtils.log("KYC document saved successfully!");
				else
					LoggerUtils.log("Failed to save KYC document");
			} catch (Exception e) {
				dbHelper.close();
				e.printStackTrace();
			}

			if (shouldGetStructuredData(jsonRequest)) {
				JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();

				structureResponse.put(KEY_RESPONSE, kycDocument.toJson());
				return structureResponse;
			} else {
				JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
				rawResponse.put(KEY_RESPONSE, responseObject);
				return rawResponse;
			}

		} else {
			LoggerUtils.log("PAN Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject verifyPAN(JSONObject jsonRequest) throws Exception {

		DefaultResponse requestStatus = KYCRequestValidator.validatePANVerifcationRequest(jsonRequest);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(jsonRequest, getKarzaApiUrl(null) + "/pan-authentication",
				KYC_TIMEOUT);

		LoggerUtils.log("PAN verification reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				if (kycResult.optBoolean("dobMatch", false) && kycResult.optBoolean("nameMatch", false)) {

					KYCDocument kycDocument = new KYCDocument();
					kycDocument.mobileNumber = jsonRequest.optString(KEY_MOBILE_NUMBER, Constants.NA);
					kycDocument.documentType = jsonRequest.optString(KEY_DOUCMENT_TYPE, Constants.NA);
					kycDocument.documentId = jsonRequest.optString(KEY_PAN, Constants.NA);
					kycDocument.isVerified = true;
					kycDocument.userName = jsonRequest.optString(KEY_NAME, Constants.NA);
					kycDocument.userDOB = jsonRequest.optString(KEY_DOB, Constants.NA);
					kycDocument.rawResponse = kycResult.toString();
					kycDocument.userGender = jsonRequest.optString(KEY_GENDER, Constants.NA);

					JSONObject addressObject = jsonRequest.optJSONObject(KEY_ADDRESS);
					if (null != addressObject) {
						if (addressObject.has(NAME_VALUE_PAIRS))
							kycDocument.address = addressObject.getJSONObject(NAME_VALUE_PAIRS).toString();
						else
							kycDocument.address = addressObject.toString();
					}

					DatabaseHelper dbHelper = new DatabaseHelper(userId);
					try {
						boolean status = dbHelper.insertOrUpdateVerifiedKYCData(kycDocument, KEY_KYC_AUTHENTICATION);
						dbHelper.close();
						if (status)
							LoggerUtils.log("KYC document saved successfully!");
						else
							LoggerUtils.log("Failed to save KYC document");
					} catch (Exception e) {
						dbHelper.close();
						e.printStackTrace();
					}

					if (shouldGetStructuredData(jsonRequest)) {
						JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
						structureResponse.put(KEY_RESPONSE, kycResult);
						return structureResponse;
					} else {
						JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
						rawResponse.put(KEY_RESPONSE, responseObject);
						return rawResponse;
					}

				} else {
					LoggerUtils.log("PAN verification failed!");
					JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
					errorResponse.put(KEY_RESPONSE, responseObject);
					return errorResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("PAN verification failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject authenticateAadhar(JSONObject jsonRequest) throws Exception {

		KYCDocument kycDocument = new KYCDocument();
		kycDocument.mobileNumber = jsonRequest.optString(KEY_MOBILE_NUMBER, Constants.NA);
		kycDocument.documentType = jsonRequest.optString(KEY_DOUCMENT_TYPE, Constants.NA);
		kycDocument.documentId = jsonRequest.optString(KEY_AADHAR_NUMBER, Constants.NA);
		kycDocument.isVerified = true;
		kycDocument.rawResponse = jsonRequest.toString();
		kycDocument.userName = jsonRequest.optString(KEY_NAME, Constants.NA);
		kycDocument.userDOB = jsonRequest.optString(KEY_DOB, Constants.NA);
		kycDocument.userGender = jsonRequest.optString(KEY_GENDER, Constants.NA);

		JSONObject addressObject = jsonRequest.optJSONObject(KEY_ADDRESS);
		if (null != addressObject) {
			if (addressObject.has(NAME_VALUE_PAIRS))
				kycDocument.address = addressObject.getJSONObject(NAME_VALUE_PAIRS).toString();
			else
				kycDocument.address = addressObject.toString();
		}

		DatabaseHelper dbHelper = new DatabaseHelper(userId);
		try {
			boolean status = dbHelper.insertOrUpdateVerifiedKYCData(kycDocument, KEY_KYC_AUTHENTICATION);
			dbHelper.close();
			if (status)
				LoggerUtils.log("KYC document saved successfully!");
			else
				LoggerUtils.log("Failed to save KYC document");
		} catch (Exception e) {
			dbHelper.close();
			e.printStackTrace();
		}

		JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
		structureResponse.put(KEY_RESPONSE, kycDocument.toJson());
		return structureResponse;

	}

	public JSONObject authenticateDL(JSONObject jsonRequest) throws Exception, SQLException {

		DefaultResponse requestStatus = KYCRequestValidator.validateDLRequest(jsonRequest);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(jsonRequest, getKarzaApiUrl(null) + "/dl", KYC_TIMEOUT);

		LoggerUtils.log("Driver's license authentication reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.getJSONObject(KEY_KYC_RESULT);

			KYCDocument kycDocument = new KYCDocument();
			kycDocument.mobileNumber = jsonRequest.optString(KEY_MOBILE_NUMBER, Constants.NA);
			kycDocument.documentType = jsonRequest.optString(KEY_DOUCMENT_TYPE, Constants.NA);
			kycDocument.documentId = jsonRequest.optString(KEY_DL_NUMBER, Constants.NA);
			kycDocument.isVerified = true;
			kycDocument.userName = kycResult.optString(KEY_NAME, Constants.NA);
			kycDocument.userDOB = kycResult.optString(KEY_DOB, Constants.NA);
			kycDocument.address = getAddressObject(kycResult.optString(KEY_ADDRESS, Constants.NA)).toString();
			kycDocument.rawResponse = kycResult.toString();
			kycDocument.userGender = jsonRequest.optString(KEY_GENDER, Constants.NA);

			JSONObject addressObject = jsonRequest.optJSONObject(KEY_ADDRESS);
			if (null != addressObject) {
				if (addressObject.has(NAME_VALUE_PAIRS))
					addressObject = addressObject.getJSONObject(NAME_VALUE_PAIRS);

				JSONObject existingAddress = getAddressObject(kycResult.optString(KEY_ADDRESS, Constants.NA));
				addressObject.put("full_address", existingAddress.optString("full_address", Constants.NA));
				kycDocument.address = addressObject.toString();
			}

			DatabaseHelper dbHelper = new DatabaseHelper(userId);
			try {
				boolean status = dbHelper.insertOrUpdateVerifiedKYCData(kycDocument, KEY_KYC_AUTHENTICATION);
				dbHelper.close();
				if (status)
					LoggerUtils.log("KYC document saved successfully!");
				else
					LoggerUtils.log("Failed to save KYC document");
			} catch (Exception e) {
				dbHelper.close();
				e.printStackTrace();
			}

			if (shouldGetStructuredData(jsonRequest)) {
				JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
				structureResponse.put(KEY_RESPONSE, kycDocument.toJson());
				return structureResponse;
			} else {
				JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
				rawResponse.put(KEY_RESPONSE, responseObject);
				return rawResponse;
			}

		} else {
			LoggerUtils.log("DL Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject authenticateVotedID(JSONObject jsonRequest) throws Exception {

		DefaultResponse requestStatus = KYCRequestValidator.validateVoterIDRequest(jsonRequest);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(jsonRequest, getKarzaApiUrl(null) + "/voter", KYC_TIMEOUT);

		LoggerUtils.log("Voter ID authentication reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.getJSONObject(KEY_KYC_RESULT);

			KYCDocument kycDocument = new KYCDocument();
			kycDocument.mobileNumber = jsonRequest.optString(KEY_MOBILE_NUMBER, Constants.NA);
			kycDocument.documentType = jsonRequest.optString(KEY_DOUCMENT_TYPE, Constants.NA);
			kycDocument.documentId = jsonRequest.optString(KEY_EPIC_NUMBER, Constants.NA);
			kycDocument.isVerified = true;
			kycDocument.userName = kycResult.optString(KEY_NAME, Constants.NA);
			kycDocument.userGender = kycResult.optString(KEY_GENDER, Constants.NA);
			kycDocument.userDOB = kycResult.optString(KEY_DOB, Constants.NA);

			String addressString = kycResult.optString("house_no", Constants.NA)
					+ kycResult.optString("district", Constants.NA) + kycResult.optString("state", Constants.NA);

			kycDocument.address = getAddressObject(addressString).toString();
			kycDocument.rawResponse = kycResult.toString();

			JSONObject addressObject = jsonRequest.optJSONObject(KEY_ADDRESS);
			if (null != addressObject) {
				if (addressObject.has(NAME_VALUE_PAIRS))
					addressObject = addressObject.getJSONObject(NAME_VALUE_PAIRS);

				JSONObject existingAddress = getAddressObject(kycResult.optString(KEY_ADDRESS, Constants.NA));
				addressObject.put("full_address", existingAddress.optString("full_address", Constants.NA));
				kycDocument.address = addressObject.toString();
			}

			DatabaseHelper dbHelper = new DatabaseHelper(userId);
			try {
				boolean status = dbHelper.insertOrUpdateVerifiedKYCData(kycDocument, KEY_KYC_AUTHENTICATION);
				dbHelper.close();
				if (status)
					LoggerUtils.log("KYC document saved successfully!");
				else
					LoggerUtils.log("Failed to save KYC document");
			} catch (Exception e) {
				dbHelper.close();
				e.printStackTrace();
			}

			if (shouldGetStructuredData(jsonRequest)) {
				JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();

				structureResponse.put(KEY_RESPONSE, kycDocument.toJson());
				return structureResponse;
			} else {
				JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
				rawResponse.put(KEY_RESPONSE, responseObject);
				return rawResponse;
			}

		} else {
			LoggerUtils.log("Voter ID Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject authenticatePassport(JSONObject jsonRequest) throws Exception {
		LoggerUtils.log("Passport authentication request: " + jsonRequest);

		DefaultResponse requestStatus = KYCRequestValidator.validatePassportRequest(jsonRequest);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		// changed passport to passport-verification and null to KarzaApiVersion.V3
		JSONObject responseObject = totalKYCDefaultPostCall(jsonRequest,
				getKarzaApiUrl(KarzaApiVersion.V3) + "/passport-verification", KYC_TIMEOUT);

		LoggerUtils.log("Passport authentication reponse: " + responseObject);
		// changed isTotalKYCSuccess to isTotalKYCSuccessV3
		if (isTotalKYCSuccessV3(responseObject)) {

			JSONObject kycResult = responseObject.getJSONObject(KEY_KYC_RESULT);

			KYCDocument kycDocument = new KYCDocument();
			kycDocument.mobileNumber = jsonRequest.optString(KEY_MOBILE_NUMBER, Constants.NA);
			kycDocument.documentType = jsonRequest.optString(KEY_DOUCMENT_TYPE, Constants.NA);
			kycDocument.documentId = jsonRequest.optString(KEY_PASSPORT_NUMBER, Constants.NA);
			kycDocument.isVerified = true;
			kycDocument.rawResponse = kycResult.toString();
			kycDocument.userName = jsonRequest.optString(KEY_NAME, Constants.NA);
			kycDocument.userDOB = jsonRequest.optString(KEY_DOB, Constants.NA);
			kycDocument.userGender = jsonRequest.optString(KEY_GENDER, Constants.NA);

			JSONObject addressObject = jsonRequest.optJSONObject(KEY_ADDRESS);
			if (null != addressObject) {
				if (addressObject.has(NAME_VALUE_PAIRS))
					kycDocument.address = addressObject.getJSONObject(NAME_VALUE_PAIRS).toString();
				else
					kycDocument.address = addressObject.toString();
			}

			DatabaseHelper dbHelper = new DatabaseHelper(userId);
			try {
				boolean status = dbHelper.insertOrUpdateVerifiedKYCData(kycDocument, KEY_KYC_AUTHENTICATION);
				dbHelper.close();
				if (status)
					LoggerUtils.log("KYC document saved successfully!");
				else
					LoggerUtils.log("Failed to save KYC document");
			} catch (Exception e) {
				dbHelper.close();
				e.printStackTrace();
			}

			if (shouldGetStructuredData(jsonRequest)) {
				JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();

				structureResponse.put(KEY_RESPONSE, kycDocument.toJson());
				return structureResponse;
			} else {
				JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
				rawResponse.put(KEY_RESPONSE, responseObject);
				return rawResponse;
			}

		} else {
			LoggerUtils.log("Passport Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject authenticateGst(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateGstAuthRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject,
				getKarzaGstApiUrl(KarzaApiVersion.V1) + "/gstdetailed", KYC_TIMEOUT);

		LoggerUtils.log("Gst authentication reponse: " + responseObject);

		if (isTotalKYCSuccessV3(responseObject)) {

			JSONObject gstResult = responseObject.getJSONObject(KEY_KYC_RESULT);

			DatabaseHelper dbHelper = new DatabaseHelper(userId);
			try {
				boolean status = dbHelper.insertOrUpdateGSINDetails(bodyObject, gstResult);
				dbHelper.close();
				if (status)
					LoggerUtils.log("GSTIN details saved successfully!");
				else
					LoggerUtils.log("Failed to save GSTIN details");
			} catch (Exception e) {
				dbHelper.close();
				e.printStackTrace();
			}

			if (shouldGetStructuredData(bodyObject)) {
				JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
				structureResponse.put(KEY_RESPONSE, gstResult);
				return structureResponse;
			} else {
				JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
				rawResponse.put(KEY_RESPONSE, gstResult);
				return rawResponse;
			}

		} else {
			LoggerUtils.log("GST Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject getAddressObject(String addressString) {
		JSONObject address = new JSONObject();
		address.put("street", Constants.NA);
		address.put("city", Constants.NA);
		address.put("pincode", Constants.NA);
		address.put("state", Constants.NA);
		address.put("country", "IND");
		address.put("full_address", addressString);
		return address;
	}

	// MARK: - Mobile number authentication implementation

	public JSONObject mobileOTPGenerate(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateMobileOTPGenerateRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/mobile/otp",
				KYC_TIMEOUT);

		LoggerUtils.log("Mobile OTP generate reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {
			JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
			rawResponse.put(KEY_RESPONSE, responseObject);
			return rawResponse;
		} else {
			LoggerUtils.log("Mobile OTP generate failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject mobileOTPAuth(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateMobileOTPAuthRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject mStatusObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/mobile/status",
				KYC_TIMEOUT);

		LoggerUtils.log("Mobile OTP auth reponse: " + mStatusObject);

		if (isTotalKYCSuccess(mStatusObject)) {

			JSONObject kycResult = mStatusObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				boolean optValidated = kycResult.getJSONObject("sim_details").optBoolean("otp_validated", false);

				if (!optValidated) {

					LoggerUtils.log("Mobile number Authentication failed!");
					DefaultResponse errorResponse = new DefaultResponse();
					errorResponse.message = "Entered OTP is invalid";
					return errorResponse.toJson();

				}
			}

		} else {
			LoggerUtils.log("Mobile number Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, mStatusObject);
			return errorResponse;
		}

		JSONObject detailRequestObject = new JSONObject();
		detailRequestObject.put("request_id", bodyObject.getString(KEY_KYC_REQUEST_ID));

		JSONObject mDetailObject = totalKYCDefaultPostCall(detailRequestObject,
				getKarzaApiUrl(null) + "/mobile/details", KYC_TIMEOUT);

		LoggerUtils.log("Mobile Detail response: " + mDetailObject);

		if (isTotalKYCSuccess(mDetailObject)) {

			// we have all the data now
			// store in db and move along

			JSONObject kycResult = mDetailObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				UtilityBill bill = new UtilityBill();
				bill.mobileNumber = bodyObject.optString(KEY_MOBILE, Constants.NA);
				bill.billType = UtilityBillType.MOBILE.value;
				bill.billIdType = KEY_MOBILE;
				bill.billId = bodyObject.optString(KEY_MOBILE, Constants.NA);
				bill.isVerified = true;
				bill.serviceProvider = kycResult.getJSONObject("sim_details").optString("provider", Constants.NA);
				bill.customerName = kycResult.getJSONObject("identity").optString("name", Constants.NA);
				bill.customerAddress = kycResult.getJSONObject("contact").optString("address", Constants.NA);
				bill.rawData = kycResult.toString();

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean status = dbHelper.insertUtilityBill(bill);
					dbHelper.close();
					if (status)
						LoggerUtils.log("Mobile number saved successfully!");
					else
						LoggerUtils.log("Failed to save Mobile number data.");
				} catch (Exception e) {
					dbHelper.close();
					e.printStackTrace();
				}

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, bill.toJson());
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, mDetailObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, mDetailObject);
				return errorResponse;
			}

		} else {

			// failed to get detail of the mobile number
			// store whatever data we have in hand and move along
			if (isTotalKYCSuccess(mStatusObject)) {

				JSONObject result = mStatusObject.optJSONObject(KEY_KYC_RESULT);

				if (null != result) {

					UtilityBill bill = new UtilityBill();
					bill.mobileNumber = bodyObject.optString(KEY_MOBILE, Constants.NA);
					bill.billType = UtilityBillType.MOBILE.value;
					bill.billIdType = KEY_MOBILE;
					bill.billId = bodyObject.optString(KEY_MOBILE, Constants.NA);
					bill.isVerified = true;
					bill.serviceProvider = mStatusObject.getJSONObject("sim_details").optString("provider",
							Constants.NA);
					// bill.customerName = kycResult.getJSONObject("identity").optString("name",
					// Constants.NA);
					// bill.customerAddress =
					// kycResult.getJSONObject("contact").optString("address", Constants.NA);
					bill.rawData = mStatusObject.toString();

					DatabaseHelper dbHelper = new DatabaseHelper(userId);
					try {
						boolean status = dbHelper.insertUtilityBill(bill);
						dbHelper.close();
						if (status)
							LoggerUtils.log("Mobile number saved successfully!");
						else
							LoggerUtils.log("Failed to save Mobile number data.");
					} catch (Exception e) {
						dbHelper.close();
						e.printStackTrace();
					}

					if (shouldGetStructuredData(bodyObject)) {
						JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
						structureResponse.put(KEY_RESPONSE, bill.toJson());
						return structureResponse;
					} else {
						JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
						rawResponse.put(KEY_RESPONSE, mStatusObject);
						return rawResponse;
					}

				} else {
					LoggerUtils.log("KYC Result is null");
					JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
					errorResponse.put(KEY_RESPONSE, mStatusObject);
					return errorResponse;
				}

			} else {
				LoggerUtils.log("Mobile number Authentication failed!");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, mStatusObject);
				return errorResponse;
			}

		}

	}

	// MARK: - Electricity Bill authentication

	public JSONObject electricityBillAuth(String body) throws Exception {

		JSONObject jsonRequest = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateElectricityBillAuthRequest(jsonRequest);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(jsonRequest, getKarzaApiUrl(null) + "/elec", KYC_TIMEOUT);

		LoggerUtils.log("Electricity bill auth reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				UtilityBill bill = new UtilityBill();
				bill.mobileNumber = jsonRequest.optString(KEY_MOBILE_NUMBER, Constants.NA);
				bill.billType = UtilityBillType.ELECTRICITY_BILL.value;
				bill.billIdType = KEY_CONSUMER_ID;
				bill.billId = jsonRequest.optString(KEY_CONSUMER_ID, Constants.NA);
				bill.serviceProvider = KEY_SERVICE_PROVIDER;
				bill.isVerified = true;
				bill.customerName = kycResult.optString("consumer_name", Constants.NA);
				bill.customerAddress = kycResult.optString("address", Constants.NA);
				bill.rawData = kycResult.toString();

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean status = dbHelper.insertUtilityBill(bill);
					dbHelper.close();
					if (status)
						LoggerUtils.log("Electricity bill saved successfully!");
					else
						LoggerUtils.log("Failed to save Electricity bill");
				} catch (Exception e) {
					dbHelper.close();
					e.printStackTrace();
				}

				if (shouldGetStructuredData(jsonRequest)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, bill.toJson());
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, responseObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("Electricity Bill Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	// MARK: - Telephone bill authentication

	public JSONObject telephoneBillAuth(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateTelephoneBillAuthRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/tele", KYC_TIMEOUT);

		LoggerUtils.log("Telephone bill auth reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				UtilityBill bill = new UtilityBill();
				bill.mobileNumber = bodyObject.optString(KEY_MOBILE_NUMBER, Constants.NA);
				bill.billType = UtilityBillType.TELEPHONE_BILL.value;
				bill.billIdType = KEY_TELEPHONE_NUMBER;
				bill.billId = bodyObject.optString(KEY_TELEPHONE_NUMBER, Constants.NA);
				bill.isVerified = true;
				bill.customerName = kycResult.optString("name", Constants.NA);
				bill.customerAddress = kycResult.optString("address", Constants.NA);
				bill.rawData = kycResult.toString();

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean status = dbHelper.insertUtilityBill(bill);
					dbHelper.close();
					if (status)
						LoggerUtils.log("Telephone bill saved successfully!");
					else
						LoggerUtils.log("Failed to save Telephone bill data.");
				} catch (Exception e) {
					dbHelper.close();
					e.printStackTrace();
				}

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, bill.toJson());
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, responseObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("Telephone bill Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	// MARK: - LPG ID authentication

	public JSONObject lpgIdAuth(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateLPGIdAuthRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/lpg", KYC_TIMEOUT);

		LoggerUtils.log("LPD ID auth reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				UtilityBill bill = new UtilityBill();
				bill.mobileNumber = bodyObject.optString(KEY_MOBILE_NUMBER, Constants.NA);
				bill.billType = UtilityBillType.LPG_ID.value;
				bill.billIdType = KEY_LPG_ID;
				bill.billId = bodyObject.optString(KEY_LPG_ID, Constants.NA);
				bill.isVerified = true;
				bill.serviceProvider = kycResult.optString("DistributorName", Constants.NA);
				bill.customerName = kycResult.optString("ConsumerName", Constants.NA);
				bill.customerAddress = kycResult.optString("ConsumerAddress", Constants.NA);
				bill.rawData = kycResult.toString();

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean status = dbHelper.insertUtilityBill(bill);
					dbHelper.close();
					if (status)
						LoggerUtils.log("LPG ID saved successfully!");
					else
						LoggerUtils.log("Failed to save LPG ID data.");
				} catch (Exception e) {
					dbHelper.close();
					e.printStackTrace();
				}

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, bill.toJson());
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, responseObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("LPG ID Authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	// MARK: - ITR Authentication

	public JSONObject itrAuthentication(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateITRAuthenticationRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/itr", KYC_TIMEOUT);

		LoggerUtils.log("ITR Authentication reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				String mobileNumber = bodyObject.optString(KEY_MOBILE_NUMBER, Constants.NA);
				String ackNumber = bodyObject.optString(KEY_ACK, Constants.NA);
				String panNumber = bodyObject.optString(KEY_PAN, Constants.NA);

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				boolean dbStatus = dbHelper.addManualITRVDetails(panNumber, mobileNumber, ackNumber, kycResult);
				dbHelper.close();
				if (dbStatus)
					LoggerUtils.log("Manual ITR saved successfully!");
				else
					LoggerUtils.log("Failed to save manual ITR");

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, kycResult);
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, responseObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("ITR authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	// MARK: - Vehicle RC Authentication

	public JSONObject vehicleRCAuthentication(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateVehicleRCAuthenticationRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/rc", 180);

		LoggerUtils.log("Vehicle RC Authentication reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean status = dbHelper.insertVehicleRCInfo(bodyObject, kycResult);
					dbHelper.close();
					if (status)
						LoggerUtils.log("Vehicle RC data saved successfully!");
					else
						LoggerUtils.log("Failed to save Vehicle RC");
				} catch (Exception e) {
					dbHelper.close();
					e.printStackTrace();
				}

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, kycResult);
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, responseObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("Vehicle RC authentication failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	// MARK: - EPF Authentication

	public JSONObject epfUANLookup(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateEFPUANLookupRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/uan-lookup",
				KYC_TIMEOUT);

		LoggerUtils.log("EPF UAN lookup reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean isSuccess = dbHelper.insertBasicUANDetails(bodyObject, kycResult);
					dbHelper.close();
					if (isSuccess)
						LoggerUtils.log("Basic UAN data inserted in database successfully");
					else
						LoggerUtils.log("Failed to insterting Basic UAN data in database");
				} catch (Exception e) {
					dbHelper.close();
					LoggerUtils.log("Error while insterting basic UAN data in database: " + e.getMessage());
					e.printStackTrace();
				}

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, kycResult);
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, responseObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("EPF UAN lookup failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject epfUANEmployerLookup(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateEFPUANEmployerLookupRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/membership-lookup",
				KYC_TIMEOUT);

		LoggerUtils.log("EPF UAN Employer lookup reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONArray kycResult = responseObject.optJSONArray(KEY_KYC_RESULT);

			if (null != kycResult) {

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean isSuccess = dbHelper.insertUANEmployerDetails(bodyObject, kycResult);
					dbHelper.close();
					if (isSuccess)
						LoggerUtils.log("UAN Employer data inserted in database successfully");
					else
						LoggerUtils.log("Failed to insterting UAN Employer data in database");
				} catch (Exception e) {
					dbHelper.close();
					LoggerUtils.log("Error while insterting UAN Empolyer data in database: " + e.getMessage());
					e.printStackTrace();
				}

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, kycResult);
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, responseObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("EPF UAN Emoplyer lookup failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject epfGetOTP(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateEPFGetOTPRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/epf-get-otp",
				KYC_TIMEOUT);

		LoggerUtils.log("EPF get OTP reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean status = dbHelper.insertInitialEpfDetails(bodyObject,
							responseObject.getString(DocumentKYCHelper.KEY_KYC_REQUEST_ID));
					dbHelper.close();
					if (status)
						LoggerUtils.log("EPF get OTP saved successfully!");
					else
						LoggerUtils.log("Failed to save EPF get OTP");
				} catch (Exception e) {
					dbHelper.close();
					e.printStackTrace();
				}

				JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
				rawResponse.put(KEY_RESPONSE, responseObject);
				return rawResponse;

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("EPF get OTP failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	public JSONObject epfGetPassbook(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateEPFGetPassbookRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		JSONObject responseObject = totalKYCDefaultPostCall(bodyObject, getKarzaApiUrl(null) + "/epf-get-passbook",
				KYC_TIMEOUT);

		LoggerUtils.log("EPF get Passbook reponse: " + responseObject);

		if (isTotalKYCSuccess(responseObject)) {

			JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);

			if (null != kycResult) {

				DatabaseHelper dbHelper = new DatabaseHelper(userId);
				try {
					boolean status = dbHelper.insertFinalEpfDetails(bodyObject, kycResult);
					dbHelper.close();
					if (status)
						LoggerUtils.log("EPF passbook data saved successfully!");
					else
						LoggerUtils.log("Failed to save EPF passbook data.");
				} catch (Exception e) {
					dbHelper.close();
					e.printStackTrace();
				}

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, kycResult);
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					rawResponse.put(KEY_RESPONSE, responseObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("KYC Result is null");
				JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
				errorResponse.put(KEY_RESPONSE, responseObject);
				return errorResponse;
			}

		} else {
			LoggerUtils.log("EPF get passbook failed!");
			JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
			errorResponse.put(KEY_RESPONSE, responseObject);
			return errorResponse;
		}

	}

	// MARK: - Document OCR implementation

	public JSONObject performITRvOCR(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateITRvOCRRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		String mobileNumber = bodyObject.optString(KEY_MOBILE_NUMBER, Constants.NA);
		String fileName = bodyObject.optString(KEY_FILE_NAME, Constants.NA).replace(" ", "_");
		String base64String = bodyObject.optString(KEY_FILE_DATA, Constants.NA);

		String qualifiedUploadFileName = mobileNumber + "_" + fileName;

		AmazonClient amazonClient = new AmazonClient();
		boolean status = amazonClient.uploadImage(qualifiedUploadFileName, base64String, S3BucketPath.DOCUMENT_IMAGES);

		if (status) {

			InputStream inputStream = null;
			OutputStream outputStream = null;

			String qualifiedUploadFilePath = (Constants.IS_DB_IN_PRODUCTION ? UPLOAD_FILE_SERVER
					: UPLOAD_FILE_LOCAL_SERVER) + qualifiedUploadFileName;

			File frontFile = new File(qualifiedUploadFilePath);

			try {
				inputStream = new ByteArrayInputStream(Base64.decodeBase64(base64String.getBytes()));
				outputStream = new FileOutputStream(frontFile);
				int read = 0;
				byte[] bytes = new byte[1024];
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				outputStream.flush();

				status = true;

				LoggerUtils.log("=========== ITR-V OCR Dcoument saved successfully at: " + qualifiedUploadFilePath);

			} catch (IOException ioe) {
				LoggerUtils.log("Error while saving ITR-V OCR Document file: " + ioe.toString());
				ioe.printStackTrace();
			} finally {
				if (null != outputStream)
					outputStream.close();
			}

			if (status) {

				OkHttpClient client = new OkHttpClient.Builder().connectTimeout(KYC_TIMEOUT, TimeUnit.SECONDS)
						.writeTimeout(KYC_TIMEOUT, TimeUnit.SECONDS).readTimeout(KYC_TIMEOUT, TimeUnit.SECONDS).build();

				RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
						.addFormDataPart("file", qualifiedUploadFileName,
								RequestBody.create(MediaType.get("image/jpeg"), frontFile))
						.build();

				Request request = new Request.Builder().url(getKarzaApiUrl(null) + "/ocr/itrv").post(requestBody)
						.addHeader("x-karza-key", getKarzaAPIKey()).build();

				Response response = client.newCall(request).execute();
				String responseMessage = response.body().string();
				JSONObject responseObject = new JSONObject(responseMessage);
				LoggerUtils.log("ITR-V OCR reponse: " + responseObject);

				// delete the stored ITR image after the OCR has been completed
				frontFile.delete();

				// finally save all data in the database
				if (isTotalKYCSuccess(responseObject)) {

					JSONObject kycResult = responseObject.optJSONObject(KEY_KYC_RESULT);
					if (null != kycResult) {
						DatabaseHelper dbHelper = new DatabaseHelper(userId);
						try {
							boolean dbStatus = dbHelper.addITRVDetails(mobileNumber, kycResult);
							dbHelper.close();
							if (dbStatus)
								LoggerUtils.log("OCR document saved successfully!");
							else
								LoggerUtils.log("Failed to save OCR document");
						} catch (Exception e) {
							dbHelper.close();
							e.printStackTrace();
						}

						if (shouldGetStructuredData(bodyObject)) {
							JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
							structureResponse.put(KEY_RESPONSE, kycResult);
							return structureResponse;
						} else {
							JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
							rawResponse.put(KEY_RESPONSE, responseObject);
							return rawResponse;
						}

					} else {
						LoggerUtils.log("ITR-V OCR failed!");
						JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
						errorResponse.put(KEY_RESPONSE, responseObject);
						return errorResponse;
					}

				} else {
					LoggerUtils.log("ITR-V OCR failed!");
					JSONObject errorResponse = BasicUtils.getFailureTemplateObject();
					errorResponse.put(KEY_RESPONSE, responseObject);
					return errorResponse;
				}

			} else {

				// delete the stored ITR image after the OCR has been completed
				frontFile.delete();

				LoggerUtils.log("Failed to store ITR-V OCR file");
				return (new DefaultResponse()).toJson();
			}

		} else {
			LoggerUtils.log("Failed to store ITR-V OCR file");
			return (new DefaultResponse()).toJson();
		}

	}

	public JSONObject performDocumentOCR(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		DefaultResponse requestStatus = KYCRequestValidator.validateDocumentOCRRequest(bodyObject);
		if (!requestStatus.isSuccess) {
			return requestStatus.toJson();
		}

		String mobileNumber = bodyObject.optString(KEY_MOBILE_NUMBER, Constants.NA);
		String documentType = bodyObject.optString(KEY_DOUCMENT_TYPE, Constants.NA);
		DocumentType dType = DocumentType.get(documentType);

		String frontFileName = bodyObject.optString(KEY_FRONT_IMAGE_NAME, Constants.NA);
		String frontBase64String = bodyObject.optString(KEY_FRONT_IMAGE_FILE, Constants.NA);

		String backFileName = bodyObject.optString(KEY_BACK_IMAGE_NAME, Constants.NA);
		String backBase64String = bodyObject.optString(KEY_BACK_IMAGE_FILE, Constants.NA);

		String frontQualifiedUploadFileName = mobileNumber + "_" + frontFileName;
		String backQualifiedUploadFileName = mobileNumber + "_" + backFileName;

		AmazonClient amazonClient = new AmazonClient();

		boolean status = amazonClient.uploadImage(frontQualifiedUploadFileName, frontBase64String,
				S3BucketPath.DOCUMENT_IMAGES);

		String frontPublicUrl = amazonClient.getPublicURL(frontQualifiedUploadFileName, S3BucketPath.DOCUMENT_IMAGES,
				10);

		String backPublicUrl = Constants.NA;

		if (dType != DocumentType.PAN_CARD) {

			status = amazonClient.uploadImage(backQualifiedUploadFileName, backBase64String,
					S3BucketPath.DOCUMENT_IMAGES);

			backPublicUrl = amazonClient.getPublicURL(backQualifiedUploadFileName, S3BucketPath.DOCUMENT_IMAGES, 10);

		}

		if (status) {

			KYCDocument kycDocument = new KYCDocument();
			kycDocument.mobileNumber = mobileNumber;
			kycDocument.documentType = documentType;
			kycDocument.isVerified = true;

			JSONObject frontResponseObject = documentOCRRequest(frontPublicUrl);
			JSONObject backResponseObject = null;

			boolean dStatus = setKycDocumentData(dType, frontResponseObject, kycDocument);

			if (dStatus && dType != DocumentType.PAN_CARD) {
				backResponseObject = documentOCRRequest(backPublicUrl);
				dStatus = setKycDocumentData(dType, backResponseObject, kycDocument);
			}

			if (dStatus) {

				// finally save all data in the database
				DatabaseHelper dbHelper = new DatabaseHelper(userId);

				try {

					kycDocument.frontImageUrl = frontQualifiedUploadFileName;

					if (dType != DocumentType.PAN_CARD)
						kycDocument.backImageUrl = backQualifiedUploadFileName;

					boolean dbStatus = dbHelper.insertOrUpdateVerifiedKYCData(kycDocument, KEY_KYC_OCR);
					dbHelper.close();
					if (dbStatus)
						LoggerUtils.log("OCR document saved successfully!");
					else
						LoggerUtils.log("Failed to save OCR document");
				} catch (Exception e) {
					dbHelper.close();
					e.printStackTrace();
				}

				if (dType == DocumentType.AADHAR_CARD)
					new ImageService().uploadMaskedAadhaarImageToS3(kycDocument);

				if (shouldGetStructuredData(bodyObject)) {
					JSONObject structureResponse = BasicUtils.getSuccessTemplateObject();
					structureResponse.put(KEY_RESPONSE, kycDocument.toJsonWithArrayRawResponse());
					return structureResponse;
				} else {
					JSONObject rawResponse = BasicUtils.getSuccessTemplateObject();
					JSONObject finalObject = frontResponseObject;
					if (dType != DocumentType.PAN_CARD) {
						JSONArray backResult = backResponseObject.optJSONArray(KEY_KYC_RESULT);
						finalObject.getJSONArray(KEY_KYC_RESULT).put(backResult);
					}
					rawResponse.put(KEY_RESPONSE, finalObject);
					return rawResponse;
				}

			} else {
				LoggerUtils.log("Failed to store OCR Document file");
				return (new DefaultResponse()).toJson();
			}

		} else {
			LoggerUtils.log("Failed to store OCR Document file");
			return (new DefaultResponse()).toJson();
		}

	}

	private String getModifiedDOB(String dob) {
		if (null == dob)
			return Constants.NA;
		else if (dob.isEmpty())
			return Constants.NA;
		else
			return dob;
	}

	private JSONObject documentOCRRequest(String fileName) throws Exception {

		JSONObject requestBody = new JSONObject();
		requestBody.put("url", fileName);
		requestBody.put("maskAadhaar", true);
		requestBody.put("hideAadhaar", false);
		requestBody.put("conf", true);
		requestBody.put("docType", "");

		JSONObject responseObject = totalKYCDefaultPostCall(requestBody,
				getKarzaApiUrl(KarzaApiVersion.V3) + "/ocr/kyc", KYC_TIMEOUT);

		return responseObject;

	}

	private boolean setKycDocumentData(DocumentType dType, JSONObject responseObject, KYCDocument kycDocument) {

		if (isTotalKYCSuccessV3(responseObject)) {

			JSONArray kycResult = responseObject.optJSONArray(KEY_KYC_RESULT);

			if (null != kycResult && kycResult.length() > 0) {

				switch (dType) {

				case VOTER_ID:

					for (int i = 0; i < kycResult.length(); i++) {
						JSONObject current = kycResult.getJSONObject(i);
						if (current.optString("type", Constants.NA).equalsIgnoreCase("Voterid Front")) {

							JSONObject voterIdDetails = current.getJSONObject("details");

							kycDocument.documentId = voterIdDetails.getJSONObject("voterid").getString("value");
							kycDocument.userName = voterIdDetails.getJSONObject("name").getString("value");

							String gender = voterIdDetails.getJSONObject("gender").getString("value");
							kycDocument.userGender = (gender.equalsIgnoreCase("MALE") ? "M" : "F");

							kycDocument.userDOB = getModifiedDOB(
									voterIdDetails.getJSONObject("dob").getString("value"));

							break;
						}
					}

					for (int i = 0; i < kycResult.length(); i++) {
						JSONObject current = kycResult.getJSONObject(i);
						if (current.optString("type", Constants.NA).equalsIgnoreCase("Voterid Back")) {

							JSONObject voterIdDetails = current.getJSONObject("details");

							// kycDocument.documentId =
							// voterIdDetails.getJSONObject("voterid").getString("value");

							JSONObject addressObject = new JSONObject();
							addressObject.put("street", voterIdDetails.getJSONObject("addressSplit").getString("line1")
									+ voterIdDetails.getJSONObject("addressSplit").getString("line2"));
							addressObject.put("city", voterIdDetails.getJSONObject("addressSplit").getString("city"));
							addressObject.put("pincode", voterIdDetails.getJSONObject("addressSplit").getString("pin"));
							addressObject.put("state", voterIdDetails.getJSONObject("addressSplit").getString("state"));
							addressObject.put("country", "IND");
							addressObject.put("full_address",
									voterIdDetails.getJSONObject("address").getString("value"));

							kycDocument.address = addressObject.toString();

							break;
						}
					}

					break;

				case PASSPORT:

					for (int i = 0; i < kycResult.length(); i++) {
						JSONObject current = kycResult.getJSONObject(i);
						if (current.optString("type", Constants.NA).equalsIgnoreCase("Passport Front")) {

							JSONObject passportDetails = current.getJSONObject("details");

							kycDocument.documentId = passportDetails.getJSONObject("passportNum").getString("value");
							kycDocument.userName = passportDetails.getJSONObject("givenName").getString("value");

							String gender = passportDetails.getJSONObject("gender").getString("value");
							kycDocument.userGender = (gender.equalsIgnoreCase("FEMALE") ? "F" : "M");

							kycDocument.userDOB = getModifiedDOB(
									passportDetails.getJSONObject("dob").getString("value"));

							break;
						}
					}

					for (int i = 0; i < kycResult.length(); i++) {
						JSONObject current = kycResult.getJSONObject(i);
						if (current.optString("type", Constants.NA).equalsIgnoreCase("Passport Back")) {

							JSONObject passportDetails = current.getJSONObject("details");

							JSONObject addressObject = new JSONObject();
							addressObject.put("street", passportDetails.getJSONObject("addressSplit").getString("line1")
									+ passportDetails.getJSONObject("addressSplit").getString("line2"));
							addressObject.put("city", passportDetails.getJSONObject("addressSplit").getString("city"));
							addressObject.put("pincode",
									passportDetails.getJSONObject("addressSplit").getString("pin"));
							addressObject.put("state",
									passportDetails.getJSONObject("addressSplit").getString("state"));
							addressObject.put("country", "IND");
							addressObject.put("full_address",
									passportDetails.getJSONObject("address").getString("value"));

							kycDocument.address = addressObject.toString();

							break;
						}
					}

					break;

				case AADHAR_CARD:

					for (int i = 0; i < kycResult.length(); i++) {
						JSONObject current = kycResult.getJSONObject(i);
						if (current.optString("type", Constants.NA).equalsIgnoreCase("Aadhaar Front Top")) {

							JSONObject aadharDetails = current.getJSONObject("details");

							kycDocument.documentId = aadharDetails.getJSONObject("aadhaar").getString("value");
							kycDocument.userName = aadharDetails.getJSONObject("name").getString("value");
							kycDocument.maskedFrontImageUrl = aadharDetails.getJSONObject("imageUrl").optString("value",
									Constants.NA);

							JSONObject addressObject = new JSONObject();
							addressObject.put("street", aadharDetails.getJSONObject("addressSplit").getString("line1")
									+ aadharDetails.getJSONObject("addressSplit").getString("line2"));
							addressObject.put("city", aadharDetails.getJSONObject("addressSplit").getString("city"));
							addressObject.put("pincode", aadharDetails.getJSONObject("addressSplit").getString("pin"));
							addressObject.put("state", aadharDetails.getJSONObject("addressSplit").getString("state"));
							addressObject.put("country", "IND");
							addressObject.put("full_address",
									aadharDetails.getJSONObject("address").getString("value"));

							kycDocument.address = addressObject.toString();

							break;
						}
					}

					for (int i = 0; i < kycResult.length(); i++) {
						JSONObject current = kycResult.getJSONObject(i);
						if (current.optString("type", Constants.NA).equalsIgnoreCase("Aadhaar Front Bottom")) {

							JSONObject aadharDetails = current.getJSONObject("details");

							kycDocument.documentId = aadharDetails.getJSONObject("aadhaar").getString("value");
							kycDocument.userName = aadharDetails.getJSONObject("name").getString("value");
							
							if (!BasicUtils.isNotNullOrNA(kycDocument.maskedFrontImageUrl)) {
								kycDocument.maskedFrontImageUrl = aadharDetails.getJSONObject("imageUrl").optString("value",
										Constants.NA);
							} else {
								kycDocument.maskedBackImageUrl = aadharDetails.getJSONObject("imageUrl").optString("value",
										Constants.NA);
							}
							
							String gender = aadharDetails.getJSONObject("gender").getString("value");
							kycDocument.userGender = (gender.equalsIgnoreCase("MALE") ? "M" : "F");
							kycDocument.userDOB = getModifiedDOB(aadharDetails.getJSONObject("dob").getString("value"));

							break;
						}
					}

					for (int i = 0; i < kycResult.length(); i++) {
						JSONObject current = kycResult.getJSONObject(i);
						if (current.optString("type", Constants.NA).equalsIgnoreCase("Aadhaar Back")) {

							JSONObject aadharDetails = current.getJSONObject("details");

							JSONObject addressObject = new JSONObject();
							addressObject.put("street", aadharDetails.getJSONObject("addressSplit").getString("line1")
									+ aadharDetails.getJSONObject("addressSplit").getString("line2"));
							addressObject.put("city", aadharDetails.getJSONObject("addressSplit").getString("city"));
							addressObject.put("pincode", aadharDetails.getJSONObject("addressSplit").getString("pin"));
							addressObject.put("state", aadharDetails.getJSONObject("addressSplit").getString("state"));
							addressObject.put("country", "IND");
							addressObject.put("full_address",
									aadharDetails.getJSONObject("address").getString("value"));

							kycDocument.address = addressObject.toString();

							kycDocument.maskedBackImageUrl = aadharDetails.getJSONObject("imageUrl").optString("value",
									Constants.NA);

							break;
						}
					}

					break;

				case PAN_CARD:

					JSONObject panDetails = kycResult.getJSONObject(0).getJSONObject("details");

					kycDocument.documentId = panDetails.getJSONObject("panNo").getString("value");
					kycDocument.userName = panDetails.getJSONObject("name").getString("value");
					kycDocument.userGender = Constants.NA;
					kycDocument.userDOB = getModifiedDOB(panDetails.getJSONObject("date").getString("value"));
					kycDocument.address = Constants.NA;

					break;

				default:
					LoggerUtils.log("Invalid document type. Can't add data in database");
					return false;

				}

				kycDocument.userImageURL = Constants.NA;
				if (null != kycDocument.rawResponse && BasicUtils.isNotNullOrNA(kycDocument.rawResponse)) {
					JSONArray rawArray = new JSONArray(kycDocument.rawResponse);
					rawArray.put(new JSONArray(kycResult.toString()));
					kycDocument.rawResponse = rawArray.toString();
				} else {
					kycDocument.rawResponse = kycResult.toString();
				}

				return true;

			} else {
				LoggerUtils.log("OCR Result is null");
				return false;
			}

		} else {
			LoggerUtils.log("Document OCR failed!");
			return false;
		}

	}

}
