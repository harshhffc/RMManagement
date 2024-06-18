package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import dao.DataProvider;
import databasehelper.ColumnsNFields;
import models.KYCAuth;
import models.KYCDocument;
import models.User;
import models.UserLocation;
import models.UtilityBill;
import totalkyc.DocumentKYCHelper;
import totalkyc.DocumentKYCHelper.DocumentType;

public class DatabaseHelper {

	private int userId = -1;

	public DatabaseHelper() {
	}

	public DatabaseHelper(int userId) {
		this.userId = userId;
	}

	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public boolean isAuthTokenValid(String token) throws SQLException {
		return null != getKYCAuthInfo(token);
	}

	private void checkConnection() throws SQLException {
		if (null == connection || !connection.isValid(10))
			connection = DataProvider.getDataSource().getConnection();
	}

	// =============== DOCUMENT KYC TABLE METHODS ======================= //

	public KYCAuth getKYCAuthInfo(String authToken) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.KYC_AUTH_TABLE + " where "
				+ ColumnsNFields.KYCAuthColoumn.AUTH_TOKEN.value + " = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, authToken);

		resultSet = preparedStatement.executeQuery();

		if (null == resultSet)
			return null;
		else {
			if (resultSet.first())
				return parseKYCAuthData(resultSet);
			else
				return null;
		}

	}

	public KYCAuth parseKYCAuthData(ResultSet resultSet) throws SQLException {

		KYCAuth authInfo = new KYCAuth();

		authInfo.id = resultSet.getInt(ColumnsNFields.KYCAuthColoumn.ID.value);
		authInfo.entityName = resultSet.getString(ColumnsNFields.KYCAuthColoumn.ENTITY_NAME.value);
		authInfo.authToken = resultSet.getString(ColumnsNFields.KYCAuthColoumn.AUTH_TOKEN.value);
		authInfo.isValid = resultSet.getBoolean(ColumnsNFields.KYCAuthColoumn.IS_VALID.value);
		authInfo.tokenCount = resultSet.getInt(ColumnsNFields.KYCAuthColoumn.TOKEN_COUNT.value);
		authInfo.creationDatetime = resultSet.getString(ColumnsNFields.KYCAuthColoumn.CREATION_DATE.value);
		authInfo.refreshDatetime = resultSet.getString(ColumnsNFields.KYCAuthColoumn.REFRESH_DATE.value);

		return authInfo;

	}

	public boolean insertOrUpdateVerifiedKYCData(KYCDocument kycDocument, String kycType) throws SQLException {

		KYCDocument existingDocument = getKYCDocumentData(kycDocument.documentType, kycDocument.mobileNumber);

		if (null != existingDocument)
			return updateKYCDocumentData(kycDocument, existingDocument, kycType);
		else
			return insertKYCDocumentData(kycDocument, kycType);

	}

	public boolean insertKYCDocumentData(KYCDocument kycDocument, String kycType) throws SQLException {

		checkConnection();

		String documentUrl = kycDocument.frontImageUrl;
		if (!kycDocument.backImageUrl.equalsIgnoreCase(Constants.NA))
			documentUrl += "|" + kycDocument.backImageUrl;
		if (documentUrl.length() > 264)
			documentUrl = documentUrl.substring(0, 264);

		String query = "INSERT INTO " + ColumnsNFields.DOCUMENT_KYC_TABLE + " ("
				+ ColumnsNFields.DocumentKYCColoumn.MOBILE_NUMBER.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.DOCUMENT_TYPE.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.DOCUMENT_ID.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.IS_VERIFIED.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.USER_NAME.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.USER_GENDER.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.USER_DOB.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.ADDRESS.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.RAW_DATA.value + ","
				+ ColumnsNFields.DocumentKYCColoumn.DOCUMENT_URL.value + "," + ColumnsNFields.COMMON_KEY_USER_ID + ") "
				+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, kycDocument.mobileNumber);
		preparedStatement.setString(2, kycDocument.documentType);
		preparedStatement.setString(3, CryptoUtils.encrypt(kycDocument.documentId));
		preparedStatement.setBoolean(4, kycDocument.isVerified);
		preparedStatement.setString(5, kycDocument.userName);
		preparedStatement.setString(6, kycDocument.userGender);
		preparedStatement.setString(7, kycDocument.userDOB);
		preparedStatement.setString(8, getModifiedAddress(kycDocument.address));

		if (kycDocument.documentType.equals(DocumentType.AADHAR_CARD.value))
			preparedStatement.setString(9, "{}");
		else
			preparedStatement.setString(9, getRawDataObject(kycDocument.rawResponse, null, kycType).toString());

		preparedStatement.setString(10, documentUrl);
		preparedStatement.setInt(11, userId);

		int status = preparedStatement.executeUpdate();

		return status == 1;

	}

	public boolean updateKYCDocumentData(KYCDocument kycDocument, KYCDocument existingDocument, String kycType)
			throws SQLException {

		checkConnection();

		String documentUrl = kycDocument.frontImageUrl;
		if (!kycDocument.backImageUrl.equalsIgnoreCase(Constants.NA))
			documentUrl += "|" + kycDocument.backImageUrl;
		if (documentUrl.length() > 264)
			documentUrl = documentUrl.substring(0, 264);

		String query = "UPDATE " + ColumnsNFields.DOCUMENT_KYC_TABLE + " SET "
				+ ColumnsNFields.DocumentKYCColoumn.DOCUMENT_ID.value + " = ?" + ","
				+ ColumnsNFields.DocumentKYCColoumn.IS_VERIFIED.value + " = ?" + ","
				+ ColumnsNFields.DocumentKYCColoumn.USER_NAME.value + " = ?" + ","
				+ ColumnsNFields.DocumentKYCColoumn.USER_GENDER.value + " = ?" + ","
				+ ColumnsNFields.DocumentKYCColoumn.USER_DOB.value + " = ?" + ","
				+ ColumnsNFields.DocumentKYCColoumn.ADDRESS.value + " = ?" + ","
				+ ColumnsNFields.DocumentKYCColoumn.RAW_DATA.value + " = ?" + ","
				+ ColumnsNFields.DocumentKYCColoumn.DOCUMENT_URL.value + " = ?" + ","
				+ ColumnsNFields.COMMON_KEY_UPDATED_DATETIME + " = ?" + "," + ColumnsNFields.COMMON_KEY_USER_ID + " = ?"
				+ " WHERE " + ColumnsNFields.DocumentKYCColoumn.DOCUMENT_TYPE.value + " = ?" + " AND "
				+ ColumnsNFields.DocumentKYCColoumn.MOBILE_NUMBER.value + " = ?";

		String existingDocId = existingDocument.documentId;
		if (existingDocId.length() > 15)
			existingDocId = CryptoUtils.decrypt(existingDocument.documentId);

		if (kycDocument.documentId.equals(existingDocId)) {

			kycDocument.mobileNumber = restoreDataIfNA(kycDocument.mobileNumber, existingDocument.mobileNumber);
			kycDocument.userName = restoreDataIfNA(kycDocument.userName, existingDocument.userName);
			kycDocument.userGender = restoreDataIfNA(kycDocument.userGender, existingDocument.userGender);
			kycDocument.userDOB = restoreDataIfNA(kycDocument.userDOB, existingDocument.userDOB);
			kycDocument.address = restoreDataIfNA(kycDocument.address, existingDocument.address);
			kycDocument.rawResponse = restoreDataIfNA(kycDocument.rawResponse, existingDocument.rawResponse);

			String exDocumentUrl = existingDocument.documentURL;

			if (documentUrl.equalsIgnoreCase(Constants.NA) && !exDocumentUrl.equalsIgnoreCase(Constants.NA))
				documentUrl = exDocumentUrl;

		}

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, CryptoUtils.encrypt(kycDocument.documentId));
		preparedStatement.setBoolean(2, kycDocument.isVerified);
		preparedStatement.setString(3, kycDocument.userName);
		preparedStatement.setString(4, kycDocument.userGender);
		preparedStatement.setString(5, kycDocument.userDOB);
		preparedStatement.setString(6, getModifiedAddress(kycDocument.address));

		if (kycDocument.documentType.equals(DocumentType.AADHAR_CARD.value))
			preparedStatement.setString(7, "{}");
		else
			preparedStatement.setString(7,
					getRawDataObject(kycDocument.rawResponse, existingDocument.rawResponse, kycType).toString());

		preparedStatement.setString(8, documentUrl);
		preparedStatement.setString(9, DateTimeUtils.getCurrentDateTimeInIST()); // TOOD: added update datetime
		preparedStatement.setInt(10, userId);
		preparedStatement.setString(11, kycDocument.documentType);
		preparedStatement.setString(12, kycDocument.mobileNumber);

		return preparedStatement.executeUpdate() > 0;

	}

	private String restoreDataIfNA(String newString, String oldString) {
		if (null != newString && !newString.equals(Constants.NA)) {
			return newString;
		} else {
			if (null != oldString && !oldString.equals(Constants.NA)) {
				return oldString;
			} else
				return newString;
		}
	}

	private String getModifiedAddress(String addressString) {
		if (null != addressString && (addressString.equals(Constants.NA) || addressString.isEmpty()))
			return null;
		return addressString;
	}

	private JSONArray getRawDataObject(String rawString, String existingRawString, String kycType) {

		JSONArray rawDataArray = new JSONArray();

		if (null != existingRawString && !existingRawString.equals(Constants.NA))
			rawDataArray = new JSONArray(existingRawString);

		boolean shouldAddNewData = true;

		for (int i = 0; i < rawDataArray.length(); i++) {
			JSONObject current = rawDataArray.getJSONObject(i);
			if (current.optString("type", Constants.NA).equals(kycType)) {
				if (null != rawString && !rawString.equals(Constants.NA)) {
					if (kycType.equalsIgnoreCase(DocumentKYCHelper.KEY_KYC_AUTHENTICATION)) {
						current.put("data", new JSONObject(rawString));
					} else {
						current.put("data", new JSONArray(rawString));
					}
					shouldAddNewData = false;
				}
				break;
			}
		}

		if (shouldAddNewData) {

			JSONObject rawDataObject = new JSONObject();
			rawDataObject.put("type", kycType);

			if (null != rawString && !rawString.equals(Constants.NA)) {
				if (kycType.equalsIgnoreCase(DocumentKYCHelper.KEY_KYC_AUTHENTICATION)) {
					rawDataObject.put("data", new JSONObject(rawString));
				} else {
					rawDataObject.put("data", new JSONArray(rawString));
				}
			} else {
				if (kycType.equalsIgnoreCase(DocumentKYCHelper.KEY_KYC_AUTHENTICATION)) {
					rawDataObject.put("data", new JSONObject());
				} else {
					rawDataObject.put("data", new JSONArray());
				}
			}
			rawDataArray.put(rawDataObject);

		}

		return rawDataArray;
	}

	public KYCDocument getKYCDocumentData(String documentType, String mobileNumber) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.DOCUMENT_KYC_TABLE + " where "
				+ ColumnsNFields.DocumentKYCColoumn.DOCUMENT_TYPE.value + " = ? and "
				+ ColumnsNFields.DocumentKYCColoumn.MOBILE_NUMBER.value + " = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, documentType);
		preparedStatement.setString(2, mobileNumber);

		resultSet = preparedStatement.executeQuery();

		if (null == resultSet)
			return null;
		else {
			if (resultSet.first())
				return parseKYCDocumentFromResultSet(resultSet);
			else
				return null;
		}

	}

	private KYCDocument parseKYCDocumentFromResultSet(ResultSet resultSet) throws SQLException {

		KYCDocument kycDocument = new KYCDocument();

		kycDocument.id = resultSet.getInt(ColumnsNFields.DocumentKYCColoumn.ID.value);
		kycDocument.mobileNumber = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.MOBILE_NUMBER.value);
		kycDocument.documentType = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.DOCUMENT_TYPE.value);
		kycDocument.documentId = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.DOCUMENT_ID.value);
		kycDocument.documentURL = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.DOCUMENT_URL.value);
		kycDocument.isVerified = resultSet.getBoolean(ColumnsNFields.DocumentKYCColoumn.IS_VERIFIED.value);
		kycDocument.userName = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.USER_NAME.value);
		kycDocument.userGender = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.USER_GENDER.value);
		kycDocument.userDOB = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.USER_DOB.value);
		kycDocument.userImageURL = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.USER_IMAGE_URL.value);
		kycDocument.address = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.ADDRESS.value);
		kycDocument.rawResponse = resultSet.getString(ColumnsNFields.DocumentKYCColoumn.RAW_DATA.value);

		return kycDocument;

	}

	// ============= GSTIN DETAL TABLE METHODS ========================= //

	public boolean insertOrUpdateGSINDetails(JSONObject requestObject, JSONObject responseObject) throws SQLException {

		checkConnection();

		String query = "";
		int status = 0;

		if (doesGSTINDetailExist(requestObject.getString("gstin"))) {

			query = "update " + ColumnsNFields.GSTIN_DETAIL_TABLE + " set "
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_LEGAL_NAME.value + "=?" + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_CONSTITUTION.value + "=?" + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_ADDRESS.value + "=?" + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_CONTACT_NUMBER.value + "=?" + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_EMAIL.value + "=?" + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_NATURE.value + "=?" + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_REGISTER_DATE.value + "=?" + ","
					+ ColumnsNFields.GstinDetailColumn.RAW_DATA.value + "=?" + "," + ColumnsNFields.COMMON_KEY_USER_ID
					+ "=?" + " where " + ColumnsNFields.GstinDetailColumn.GSTIN.value + "=?";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, responseObject.optString("lgnm", Constants.NA));
			preparedStatement.setString(2, responseObject.optString("ctb", Constants.NA));

			String businessAddress = responseObject.optJSONObject("pradr").optString("adr", Constants.NA);
			if (businessAddress.length() > 256) {
				businessAddress = businessAddress.substring(0, 256);
			}
			preparedStatement.setString(3, businessAddress);

			preparedStatement.setString(4, responseObject.optJSONObject("pradr").optString("mb", Constants.NA));
			preparedStatement.setString(5, responseObject.optJSONObject("pradr").optString("em", Constants.NA));

			String businessNature = responseObject.optJSONObject("pradr").optString("ntr", Constants.NA);
			if (businessNature.length() > 256) {
				businessNature = businessNature.substring(0, 256);
			}
			preparedStatement.setString(6, businessNature);

			preparedStatement.setString(7, responseObject.optString("rgdt", Constants.NA));
			preparedStatement.setString(8, responseObject.toString());
			preparedStatement.setInt(9, userId);
			preparedStatement.setString(10, responseObject.optString("gstin", Constants.NA));

			status = preparedStatement.executeUpdate();

		} else {

			query = "INSERT INTO " + ColumnsNFields.GSTIN_DETAIL_TABLE + " ("
					+ ColumnsNFields.GstinDetailColumn.MOBILE_NUMBER.value + ","
					+ ColumnsNFields.GstinDetailColumn.GSTIN.value + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_LEGAL_NAME.value + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_CONSTITUTION.value + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_ADDRESS.value + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_CONTACT_NUMBER.value + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_EMAIL.value + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_NATURE.value + ","
					+ ColumnsNFields.GstinDetailColumn.BUISNESS_REGISTER_DATE.value + ","
					+ ColumnsNFields.GstinDetailColumn.RAW_DATA.value + "," + ColumnsNFields.COMMON_KEY_USER_ID + ") "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, requestObject.optString("mobile_number", Constants.NA));
			preparedStatement.setString(2, responseObject.optString("gstin", Constants.NA));
			preparedStatement.setString(3, responseObject.optString("lgnm", Constants.NA));
			preparedStatement.setString(4, responseObject.optString("ctb", Constants.NA));

			String businessAddress = responseObject.optJSONObject("pradr").optString("adr", Constants.NA);
			if (businessAddress.length() > 256) {
				businessAddress = businessAddress.substring(0, 256);
			}
			preparedStatement.setString(5, businessAddress);

			preparedStatement.setString(6, responseObject.optJSONObject("pradr").optString("mb", Constants.NA));
			preparedStatement.setString(7, responseObject.optJSONObject("pradr").optString("em", Constants.NA));

			String businessNature = responseObject.optJSONObject("pradr").optString("ntr", Constants.NA);
			if (businessNature.length() > 256) {
				businessNature = businessNature.substring(0, 256);
			}
			preparedStatement.setString(8, businessNature);

			preparedStatement.setString(9, responseObject.optString("rgdt", Constants.NA));
			preparedStatement.setString(10, responseObject.toString());
			preparedStatement.setInt(11, userId);

			status = preparedStatement.executeUpdate();

		}

		return status == 1;

	}

	private boolean doesGSTINDetailExist(String gstin) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.GSTIN_DETAIL_TABLE + " where "
				+ ColumnsNFields.GstinDetailColumn.GSTIN.value + "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, gstin);
		resultSet = preparedStatement.executeQuery();

		return (null != resultSet && resultSet.first());
	}

	// =============== UTILITY BILLS TABLE METHODS ======================= //

	public boolean insertUtilityBill(UtilityBill utilityBill) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.UTILITY_BILL + " where "
				+ ColumnsNFields.UtilityBillColumn.BILL_TYPE.value + " = ?" + " and "
				+ ColumnsNFields.UtilityBillColumn.BILL_ID.value + " = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, utilityBill.billType);
		preparedStatement.setString(2, utilityBill.billId);

		resultSet = preparedStatement.executeQuery();

		int status = 0;

		if (null != resultSet && resultSet.first()) {

			query = "update " + ColumnsNFields.UTILITY_BILL + " set "
					+ ColumnsNFields.UtilityBillColumn.MOBILE_NUMBER.value + "=?" + ","
					+ ColumnsNFields.UtilityBillColumn.BILL_ID_TYPE.value + "=?" + ","
					+ ColumnsNFields.UtilityBillColumn.SERVICE_PROVIDER.value + "=?" + ","
					+ ColumnsNFields.UtilityBillColumn.IS_VERIFIED.value + "=?" + ","
					+ ColumnsNFields.UtilityBillColumn.CUSTOMER_NAME.value + "=?" + ","
					+ ColumnsNFields.UtilityBillColumn.CUSTOMER_ADDRESS.value + "=?" + ","
					+ ColumnsNFields.UtilityBillColumn.RAW_DATA.value + "=?" + "," + ColumnsNFields.COMMON_KEY_USER_ID
					+ "=?" + " where " + ColumnsNFields.UtilityBillColumn.BILL_TYPE.value + "=?" + " and "
					+ ColumnsNFields.UtilityBillColumn.BILL_ID.value + "=?";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, utilityBill.mobileNumber);
			preparedStatement.setString(2, utilityBill.billIdType);
			preparedStatement.setString(3, utilityBill.serviceProvider);
			preparedStatement.setBoolean(4, utilityBill.isVerified);
			preparedStatement.setString(5, utilityBill.customerName);
			preparedStatement.setString(6, utilityBill.customerAddress);
			preparedStatement.setString(7, utilityBill.rawData);
			preparedStatement.setInt(8, userId);
			preparedStatement.setString(9, utilityBill.billType);
			preparedStatement.setString(10, utilityBill.billId);

			status = preparedStatement.executeUpdate();

		} else {

			query = "INSERT INTO " + ColumnsNFields.UTILITY_BILL + " ("
					+ ColumnsNFields.UtilityBillColumn.MOBILE_NUMBER.value + ","
					+ ColumnsNFields.UtilityBillColumn.BILL_TYPE.value + ","
					+ ColumnsNFields.UtilityBillColumn.BILL_ID_TYPE.value + ","
					+ ColumnsNFields.UtilityBillColumn.BILL_ID.value + ","
					+ ColumnsNFields.UtilityBillColumn.SERVICE_PROVIDER.value + ","
					+ ColumnsNFields.UtilityBillColumn.IS_VERIFIED.value + ","
					+ ColumnsNFields.UtilityBillColumn.CUSTOMER_NAME.value + ","
					+ ColumnsNFields.UtilityBillColumn.CUSTOMER_ADDRESS.value + ","
					+ ColumnsNFields.UtilityBillColumn.RAW_DATA.value + "," + ColumnsNFields.COMMON_KEY_USER_ID + ") "
					+ "VALUES (?,?,?,?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, utilityBill.mobileNumber);
			preparedStatement.setString(2, utilityBill.billType);
			preparedStatement.setString(3, utilityBill.billIdType);
			preparedStatement.setString(4, utilityBill.billId);
			preparedStatement.setString(5, utilityBill.serviceProvider);
			preparedStatement.setBoolean(6, utilityBill.isVerified);
			preparedStatement.setString(7, utilityBill.customerName);
			preparedStatement.setString(8, utilityBill.customerAddress);
			preparedStatement.setString(9, utilityBill.rawData);
			preparedStatement.setInt(10, userId);

			status = preparedStatement.executeUpdate();

		}

		return status == 1;

	}

	// =============== VEHICLE RC AUTHENTICATION TABLE METHODS
	// ======================= //

	public boolean insertVehicleRCInfo(JSONObject requestDetail, JSONObject rcDetail) throws Exception {

		checkConnection();

		boolean status = false;

		if (isVehicleInfoPresent(requestDetail.optString(DocumentKYCHelper.KEY_REG_NUMBER, Constants.NA)))
			status = updateVehicleRCData(requestDetail, rcDetail);
		else
			status = insertVehicleRCData(requestDetail, rcDetail);

		return status;

	}

	private boolean isVehicleInfoPresent(String registrationNumber) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.VEHICLE_RC_INFO_TABLE + " where "
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTRATION_NUMBER.value + "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, registrationNumber);

		resultSet = preparedStatement.executeQuery();

		return (null != resultSet && resultSet.first());

	}

	private boolean updateVehicleRCData(JSONObject requestDetail, JSONObject rcDetail) throws SQLException {

		checkConnection();

		String query = "UPDATE " + ColumnsNFields.VEHICLE_RC_INFO_TABLE + " SET "
				+ ColumnsNFields.VehicleRCInfoColumn.MOBILE_NUMBER.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTRATION_NUMBER.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.ENGINE_NUMBER.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.VEHICLE_DESCRIPTION.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.BODY_DESCRIPTION.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTERED_VEHICLE_COLOR.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.PRESENT_ADDRESS.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.PERMANENT_ADDRESS.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.RTO_NAME.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTRATION_DATE.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.INSURANCE_COMPANY.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.INSURANCE_VALID_UPTO.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.INSURANCE_POLICY_NUMBER.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.OWNER_SERIAL_NUMBER.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTERED_OWNER_NAME.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTERED_OWNER_FATHERS_NAME.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.CHASSIS_NUMBER.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.MODEL_MAKER.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.VAHAN_DB_STATUS_MESSAGE.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.FUEL_DESCRIPTION.value + "=?,"
				+ ColumnsNFields.VehicleRCInfoColumn.RAW_DATA.value + "=?," + ColumnsNFields.COMMON_KEY_USER_ID + "=?"
				+ " where " + ColumnsNFields.VehicleRCInfoColumn.REGISTRATION_NUMBER.value + "=?";

		preparedStatement = connection.prepareStatement(query);

		preparedStatement.setString(1, requestDetail.optString(DocumentKYCHelper.KEY_MOBILE_NUMBER, Constants.NA));
		preparedStatement.setString(2, rcDetail.optString("rc_regn_no", Constants.NA));
		preparedStatement.setString(3, rcDetail.optString("rc_eng_no", Constants.NA));
		preparedStatement.setString(4, rcDetail.optString("rc_vh_class_desc", Constants.NA));
		preparedStatement.setString(5, rcDetail.optString("rc_body_type_desc", Constants.NA));
		preparedStatement.setString(6, rcDetail.optString("rc_color", Constants.NA));
		preparedStatement.setString(7, rcDetail.optString("rc_present_address", Constants.NA));
		preparedStatement.setString(8, rcDetail.optString("rc_permanent_address", Constants.NA));
		preparedStatement.setString(9, rcDetail.optString("rc_registered_at", Constants.NA));
		preparedStatement.setString(10, rcDetail.optString("rc_regn_dt", Constants.NA));
		preparedStatement.setString(11, rcDetail.optString("rc_insurance_comp", Constants.NA));
		preparedStatement.setString(12, rcDetail.optString("rc_insurance_upto", Constants.NA));
		preparedStatement.setString(13, rcDetail.optString("rc_insurance_policy_no", Constants.NA));
		preparedStatement.setString(14, rcDetail.optString("rc_owner_sr", Constants.NA));
		preparedStatement.setString(15, rcDetail.optString("rc_owner_name", Constants.NA));
		preparedStatement.setString(16, rcDetail.optString("rc_f_name", Constants.NA));
		preparedStatement.setString(17, rcDetail.optString("rc_chasi_no", Constants.NA));
		preparedStatement.setString(18, rcDetail.optString("rc_maker_model", Constants.NA));
		preparedStatement.setString(19, rcDetail.optString("stautsMessage", Constants.NA));
		preparedStatement.setString(20, rcDetail.optString("rc_fuel_desc", Constants.NA));
		preparedStatement.setString(21, rcDetail.toString());
		preparedStatement.setInt(22, userId);
		preparedStatement.setString(23, requestDetail.optString(DocumentKYCHelper.KEY_REG_NUMBER, Constants.NA));

		int rcStatus = preparedStatement.executeUpdate();

		return rcStatus == 1;

	}

	private boolean insertVehicleRCData(JSONObject requestDetail, JSONObject rcDetail) throws SQLException {

		checkConnection();

		String query = "INSERT INTO " + ColumnsNFields.VEHICLE_RC_INFO_TABLE + " ("
				+ ColumnsNFields.VehicleRCInfoColumn.MOBILE_NUMBER.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTRATION_NUMBER.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.ENGINE_NUMBER.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.VEHICLE_DESCRIPTION.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.BODY_DESCRIPTION.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTERED_VEHICLE_COLOR.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.PRESENT_ADDRESS.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.PERMANENT_ADDRESS.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.RTO_NAME.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTRATION_DATE.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.INSURANCE_COMPANY.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.INSURANCE_VALID_UPTO.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.INSURANCE_POLICY_NUMBER.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.OWNER_SERIAL_NUMBER.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTERED_OWNER_NAME.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.REGISTERED_OWNER_FATHERS_NAME.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.CHASSIS_NUMBER.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.MODEL_MAKER.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.VAHAN_DB_STATUS_MESSAGE.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.FUEL_DESCRIPTION.value + ","
				+ ColumnsNFields.VehicleRCInfoColumn.RAW_DATA.value + "," + ColumnsNFields.COMMON_KEY_USER_ID + ") "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		preparedStatement = connection.prepareStatement(query);

		preparedStatement.setString(1, requestDetail.optString(DocumentKYCHelper.KEY_MOBILE_NUMBER, Constants.NA));
		preparedStatement.setString(2, rcDetail.optString("rc_regn_no", Constants.NA));
		preparedStatement.setString(3, rcDetail.optString("rc_eng_no", Constants.NA));
		preparedStatement.setString(4, rcDetail.optString("rc_vh_class_desc", Constants.NA));
		preparedStatement.setString(5, rcDetail.optString("rc_body_type_desc", Constants.NA));
		preparedStatement.setString(6, rcDetail.optString("rc_color", Constants.NA));
		preparedStatement.setString(7, rcDetail.optString("rc_present_address", Constants.NA));
		preparedStatement.setString(8, rcDetail.optString("rc_permanent_address", Constants.NA));
		preparedStatement.setString(9, rcDetail.optString("rc_registered_at", Constants.NA));
		preparedStatement.setString(10, rcDetail.optString("rc_regn_dt", Constants.NA));
		preparedStatement.setString(11, rcDetail.optString("rc_insurance_comp", Constants.NA));
		preparedStatement.setString(12, rcDetail.optString("rc_insurance_upto", Constants.NA));
		preparedStatement.setString(13, rcDetail.optString("rc_insurance_policy_no", Constants.NA));
		preparedStatement.setString(14, rcDetail.optString("rc_owner_sr", Constants.NA));
		preparedStatement.setString(15, rcDetail.optString("rc_owner_name", Constants.NA));
		preparedStatement.setString(16, rcDetail.optString("rc_f_name", Constants.NA));
		preparedStatement.setString(17, rcDetail.optString("rc_chasi_no", Constants.NA));
		preparedStatement.setString(18, rcDetail.optString("rc_maker_model", Constants.NA));
		preparedStatement.setString(19, rcDetail.optString("stautsMessage", Constants.NA));
		preparedStatement.setString(20, rcDetail.optString("rc_fuel_desc", Constants.NA));
		preparedStatement.setString(21, rcDetail.toString());
		preparedStatement.setInt(22, userId);

		int rcStatus = preparedStatement.executeUpdate();

		return rcStatus == 1;

	}

	// =============== EPF AUTHENTICATION TABLE METHODS ======================= //

	public boolean insertBasicUANDetails(JSONObject requestDetail, JSONObject responseDetail) throws Exception {

		checkConnection();

		int epfStatus = 0;

		JSONArray uanResults = responseDetail.getJSONArray(DocumentKYCHelper.KEY_UAN);
		for (int i = 0; i < uanResults.length(); i++) {
			String uan = uanResults.get(i).toString();
			String mobileNumber = requestDetail.optString(DocumentKYCHelper.KEY_MOBILE, Constants.NA);

			if (!isUANEntryExists(mobileNumber, uan)) {
				String query = "INSERT INTO " + ColumnsNFields.EPF_DETAIL_TABLE + "("
						+ ColumnsNFields.EPFDetailColumn.MOBILE_NUMBER.value + ","
						+ ColumnsNFields.EPFDetailColumn.UAN.value + "," + ColumnsNFields.COMMON_KEY_USER_ID + ")"
						+ "VALUES (?,?,?)";

				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1, mobileNumber);
				preparedStatement.setString(2, uan);
				preparedStatement.setInt(3, userId);

				epfStatus = preparedStatement.executeUpdate();
			}

		}

		return epfStatus == 1;

	}

	public boolean isUANEntryExists(String mobileNumber, String uan) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.EPF_DETAIL_TABLE + " where "
				+ ColumnsNFields.EPFDetailColumn.MOBILE_NUMBER.value + "=?" + " and "
				+ ColumnsNFields.EPFDetailColumn.UAN.value + "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, mobileNumber);
		preparedStatement.setString(2, uan);

		resultSet = preparedStatement.executeQuery();

		return (null != resultSet && resultSet.first());

	}

	public boolean insertUANEmployerDetails(JSONObject requestDetail, JSONArray responseDetail) throws Exception {

		checkConnection();

		int estStatus = 0;

		for (int i = 0; i < responseDetail.length(); i++) {
			JSONObject current = responseDetail.getJSONObject(i);

			String memberId = current.optString("membershipId", Constants.NA);
			if (!doesMemberIdExist(memberId)) {

				String query = "INSERT INTO " + ColumnsNFields.EST_DETAIL_TABLE + " ("
						+ ColumnsNFields.ESTDetailColumn.UAN.value + ","
						+ ColumnsNFields.ESTDetailColumn.MEMBER_ID.value + ","
						+ ColumnsNFields.ESTDetailColumn.EST_NAME.value + ")" + "VALUES (?,?,?)";

				preparedStatement = connection.prepareStatement(query);

				preparedStatement.setString(1, requestDetail.getString(DocumentKYCHelper.KEY_UAN));
				preparedStatement.setString(2, memberId);
				preparedStatement.setString(3, current.optString("estName"));

				estStatus = preparedStatement.executeUpdate();

			}

		}

		return estStatus == 1;

	}

	private boolean doesMemberIdExist(String memberId) throws SQLException {
		if (null == connection)
			connection = DataProvider.getDataSource().getConnection();

		String query = "select * form " + ColumnsNFields.EST_DETAIL_TABLE + " where "
				+ ColumnsNFields.ESTDetailColumn.MEMBER_ID.value + "=?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, memberId);

		resultSet = preparedStatement.executeQuery();

		return (null != resultSet && resultSet.first());
	}

	public boolean insertInitialEpfDetails(JSONObject requestDetail, String requestId) throws Exception {

		checkConnection();

		String query = "INSERT INTO " + ColumnsNFields.EPF_DETAIL_TABLE + "("
				+ ColumnsNFields.EPFDetailColumn.MOBILE_NUMBER.value + "," + ColumnsNFields.EPFDetailColumn.UAN.value
				+ "," + ColumnsNFields.EPFDetailColumn.REQUEST_ID.value + "," + ColumnsNFields.COMMON_KEY_USER_ID + ")"
				+ "VALUES (?,?,?,?)";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, requestDetail.optString(DocumentKYCHelper.KEY_MOBILE_NO, Constants.NA));
		preparedStatement.setString(2, requestDetail.getString(DocumentKYCHelper.KEY_UAN));
		preparedStatement.setString(3, BasicUtils.getTheKey(requestId));
		preparedStatement.setInt(4, userId);

		int epfStatus = preparedStatement.executeUpdate();

		return epfStatus == 1;

	}

	public boolean insertFinalEpfDetails(JSONObject requestDetail, JSONObject epfDetail) throws Exception {

		checkConnection();

		String query = "UPDATE " + ColumnsNFields.EPF_DETAIL_TABLE + " SET "
				+ ColumnsNFields.EPFDetailColumn.EMPLOYEE_NAME.value + "=?" + ","
				+ ColumnsNFields.EPFDetailColumn.EMPLOYEE_DOB.value + "=?" + ","
				+ ColumnsNFields.EPFDetailColumn.EMPLOYEE_FATHER_NAME.value + "=?" + ","
				+ ColumnsNFields.EPFDetailColumn.RAW_DATA.value + "=?" + "," + ColumnsNFields.COMMON_KEY_USER_ID + "=?"
				+ " WHERE " + ColumnsNFields.EPFDetailColumn.REQUEST_ID.value + "=?";

		preparedStatement = connection.prepareStatement(query);

		preparedStatement.setString(1,
				epfDetail.getJSONObject("employee_details").optString("member_name", Constants.NA));
		preparedStatement.setString(2, epfDetail.getJSONObject("employee_details").optString("dob", Constants.NA));
		preparedStatement.setString(3,
				epfDetail.getJSONObject("employee_details").optString("father_name", Constants.NA));
		preparedStatement.setString(4, epfDetail.toString());
		preparedStatement.setInt(5, userId);
		preparedStatement.setString(6,
				BasicUtils.getTheKey(requestDetail.getString(DocumentKYCHelper.KEY_KYC_REQUEST_ID)));

		int epfStatus = preparedStatement.executeUpdate();

		if (epfStatus == 1) {

			LoggerUtils.log("Inserted EPF detail successfully.");

			query = "select " + ColumnsNFields.EPFDetailColumn.UAN.value + " from " + ColumnsNFields.EPF_DETAIL_TABLE
					+ " where " + ColumnsNFields.EPFDetailColumn.REQUEST_ID.value + " = ?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1,
					BasicUtils.getTheKey(requestDetail.getString(DocumentKYCHelper.KEY_KYC_REQUEST_ID)));

			resultSet = preparedStatement.executeQuery();
			if (null != resultSet && resultSet.first()) {
				String uan = resultSet.getString(ColumnsNFields.EPFDetailColumn.UAN.value);
				insertEstDetails(uan, epfDetail.getJSONArray("est_details"));
			}

		} else
			LoggerUtils.log("Failed to insert EPF detail.");

		return epfStatus == 1;

	}

	private void insertEstDetails(String uan, JSONArray estDetails) throws SQLException, ParseException {

		checkConnection();

		for (int i = 0; i < estDetails.length(); i++) {

			JSONObject currentEst = estDetails.getJSONObject(i);

			String query = "INSERT INTO " + ColumnsNFields.EST_DETAIL_TABLE + " ("
					+ ColumnsNFields.ESTDetailColumn.UAN.value + "," + ColumnsNFields.ESTDetailColumn.MEMBER_ID.value
					+ "," + ColumnsNFields.ESTDetailColumn.EST_NAME.value + ","
					+ ColumnsNFields.ESTDetailColumn.DOE_EPF.value + "," + ColumnsNFields.ESTDetailColumn.OFFICE.value
					+ "," + ColumnsNFields.ESTDetailColumn.DOJ_EPF.value + ","
					+ ColumnsNFields.ESTDetailColumn.DOE_EPS.value + ")" + "VALUES (?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);

			preparedStatement.setString(1, uan);
			preparedStatement.setString(2, currentEst.getString(ColumnsNFields.ESTDetailColumn.MEMBER_ID.value));
			preparedStatement.setString(3, currentEst.getString(ColumnsNFields.ESTDetailColumn.EST_NAME.value));
			preparedStatement.setString(4, currentEst.optString(ColumnsNFields.ESTDetailColumn.DOE_EPF.value));
			preparedStatement.setString(5,
					currentEst.optString(ColumnsNFields.ESTDetailColumn.OFFICE.value, Constants.NA));
			preparedStatement.setString(6,
					currentEst.optString(ColumnsNFields.ESTDetailColumn.DOJ_EPF.value, Constants.NA));
			preparedStatement.setString(7, currentEst.optString(ColumnsNFields.ESTDetailColumn.DOE_EPS.value));

			int estStatus = preparedStatement.executeUpdate();

			if (estStatus == 1) {
				LoggerUtils.log("Inserted EST detail successfully.");
				insertEpfPassbookDetail(uan, currentEst.getString(ColumnsNFields.ESTDetailColumn.MEMBER_ID.value),
						currentEst.getJSONArray("passbook"));
			} else
				LoggerUtils.log("Failed to insert EST detail.");

		}

	}

	private void insertEpfPassbookDetail(String uan, String memberId, JSONArray passbookDetails)
			throws SQLException, ParseException {

		checkConnection();

		for (int i = 0; i < passbookDetails.length(); i++) {

			JSONObject passbookObject = passbookDetails.getJSONObject(i);

			String query = "INSERT INTO " + ColumnsNFields.EPF_EST_DETAIL_TABLE + " ("
					+ ColumnsNFields.EpfEstPassbookColumn.UAN.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.MEMBER_ID.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.CR_PEN_BAL.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.APPROVED_ON.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.DB_CR_FLAT.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.TR_APPROVED.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.TR_DATE_MY.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.CR_EE_SHARE.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.R_ORDER.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.CR_ER_SHARE.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.PARTICULAR.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.TRRNO.value + ","
					+ ColumnsNFields.EpfEstPassbookColumn.MONTH_YEAR.value + ")" + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);

			preparedStatement.setString(1, uan);
			preparedStatement.setString(2, memberId);
			preparedStatement.setString(3,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.CR_PEN_BAL.value, Constants.NA));
			preparedStatement.setString(4,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.APPROVED_ON.value, Constants.NA));
			preparedStatement.setString(5,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.DB_CR_FLAT.value, Constants.NA));
			preparedStatement.setString(6,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.TR_APPROVED.value, Constants.NA));
			preparedStatement.setString(7,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.TR_DATE_MY.value, Constants.NA));
			preparedStatement.setString(8,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.CR_EE_SHARE.value, Constants.NA));
			preparedStatement.setString(9,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.R_ORDER.value, Constants.NA));
			preparedStatement.setString(10,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.CR_ER_SHARE.value, Constants.NA));
			preparedStatement.setString(11,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.PARTICULAR.value, Constants.NA));
			preparedStatement.setString(12,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.TRRNO.value, Constants.NA));
			preparedStatement.setString(13,
					passbookObject.optString(ColumnsNFields.EpfEstPassbookColumn.MONTH_YEAR.value, Constants.NA));

			int passbookStatus = preparedStatement.executeUpdate();
			if (passbookStatus == 1)
				LoggerUtils.log("Inserted passbook data successfully.");
			else
				LoggerUtils.log("Failed to insert passbook data.");

		}

	}

	public boolean addITRVDetails(String mobileNumber, JSONObject itrObject) throws SQLException {

		checkConnection();

		String initialQuery = "Select * from " + ColumnsNFields.ITRV_DOCUMENT_TABLE
				+ " where mobile_number=? and acknowledge_number=?";
		preparedStatement = connection.prepareStatement(initialQuery);
		preparedStatement.setString(1, mobileNumber);
		preparedStatement.setString(2, itrObject.optString("Acknowledgement Number", Constants.NA));

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {

			// UPDATE THE VALUES

			String query = "UPDATE " + ColumnsNFields.ITRV_DOCUMENT_TABLE + " SET pan_number=?" + ", aadhar_number=?, "
					+ ", address=?, " + ", assessment_year=?, " + ", date_of_filing=?, " + ", form_number=?, "
					+ ", form_type=?, " + ", customer_name=?, " + ", status=?, " + ", tax_details=? "
					+ " where mobile_number=? and acknowledge_number=?";

			preparedStatement = connection.prepareStatement(query);

			preparedStatement.setString(1, itrObject.optString("PAN", Constants.NA));
			preparedStatement.setString(2, itrObject.optString("Aadhaar Number", Constants.NA));

			JSONObject addressObject = new JSONObject();
			String streetString = itrObject.optString("Flat/Door/Block No", "") + " "
					+ itrObject.optString("Road/Street/Post Office", "") + " "
					+ itrObject.optString("Area/Locality", "");
			String city = itrObject.optString("Town/City/District", "");
			String pincode = itrObject.optString("Pin", "");
			String state = itrObject.optString("State", "");
			String fullAddress = "";
			if (!streetString.isEmpty())
				fullAddress += streetString;
			if (!city.isEmpty())
				fullAddress += ", " + city;
			if (!state.isEmpty())
				fullAddress += ", " + state;
			if (!pincode.isEmpty())
				fullAddress += " " + pincode;

			addressObject.put("street", streetString);
			addressObject.put("city", city.isEmpty() ? Constants.NA : city);
			addressObject.put("pincode", pincode.isEmpty() ? Constants.NA : pincode);
			addressObject.put("state", state.isEmpty() ? Constants.NA : state);
			addressObject.put("country", "IND");
			addressObject.put("full_address", fullAddress);

			preparedStatement.setString(3, addressObject.toString());
			preparedStatement.setString(4, itrObject.optString("Assessment Year", Constants.NA));
			preparedStatement.setString(5, itrObject.optString("Date Of Filing", Constants.NA));
			preparedStatement.setString(6, itrObject.optString("Form No", Constants.NA));
			preparedStatement.setString(7, itrObject.optString("Form Type", Constants.NA));
			preparedStatement.setString(8, itrObject.optString("Name", Constants.NA));
			preparedStatement.setString(9, itrObject.optString("Status", Constants.NA));
			preparedStatement.setString(10, itrObject.optJSONObject("TAX Details").toString());
			preparedStatement.setString(11, mobileNumber);
			preparedStatement.setString(12, itrObject.optString("Acknowledgement Number", Constants.NA));

			int status = preparedStatement.executeUpdate();

			if (status > 0)
				LoggerUtils.log("Inserted ITR-V data successfully.");
			else
				LoggerUtils.log("Failed to insert ITR-V data.");

			return status > 0;

		} else {

			// INSERT NEW VALUES
			String query = "INSERT INTO " + ColumnsNFields.ITRV_DOCUMENT_TABLE + " (mobile_number,"
					+ "acknowledge_number," + "aadhar_number," + "pan_number," + "address," + "assessment_year,"
					+ "date_of_filing," + "form_number," + "form_type," + "customer_name," + "status," + "tax_details,"
					+ "user_id) " + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);

			preparedStatement.setString(1, mobileNumber);
			preparedStatement.setString(2, itrObject.optString("Acknowledgement Number", Constants.NA));
			preparedStatement.setString(3, itrObject.optString("Aadhaar Number", Constants.NA));
			preparedStatement.setString(4, itrObject.optString("PAN", Constants.NA));

			JSONObject addressObject = new JSONObject();
			String streetString = itrObject.optString("Flat/Door/Block No", "") + " "
					+ itrObject.optString("Road/Street/Post Office", "") + " "
					+ itrObject.optString("Area/Locality", "");
			String city = itrObject.optString("Town/City/District", "");
			String pincode = itrObject.optString("Pin", "");
			String state = itrObject.optString("State", "");
			String fullAddress = "";
			if (!streetString.isEmpty())
				fullAddress += streetString;
			if (!city.isEmpty())
				fullAddress += ", " + city;
			if (!state.isEmpty())
				fullAddress += ", " + state;
			if (!pincode.isEmpty())
				fullAddress += " " + pincode;

			addressObject.put("street", streetString);
			addressObject.put("city", city.isEmpty() ? Constants.NA : city);
			addressObject.put("pincode", pincode.isEmpty() ? Constants.NA : pincode);
			addressObject.put("state", state.isEmpty() ? Constants.NA : state);
			addressObject.put("country", "IND");
			addressObject.put("full_address", fullAddress);

			preparedStatement.setString(5, addressObject.toString());

			preparedStatement.setString(6, itrObject.optString("Assessment Year", Constants.NA));
			preparedStatement.setString(7, itrObject.optString("Date Of Filing", Constants.NA));
			preparedStatement.setString(8, itrObject.optString("Form No", Constants.NA));
			preparedStatement.setString(9, itrObject.optString("Form Type", Constants.NA));
			preparedStatement.setString(10, itrObject.optString("Name", Constants.NA));
			preparedStatement.setString(11, itrObject.optString("Status", Constants.NA));
			preparedStatement.setString(12, itrObject.optJSONObject("TAX Details").toString());
			preparedStatement.setInt(13, userId);

			int status = preparedStatement.executeUpdate();

			if (status == 1)
				LoggerUtils.log("Inserted ITR-V data successfully.");
			else
				LoggerUtils.log("Failed to insert ITR-V data.");

			return status == 1;

		}

	}

	public boolean addManualITRVDetails(String panNumber, String mobileNumber, String ackNumber, JSONObject itrObject)
			throws SQLException {

		checkConnection();
		String initialQuery = "Select * from " + ColumnsNFields.ITRV_DOCUMENT_TABLE
				+ " where mobile_number =? and acknowledge_number =?";
		preparedStatement = connection.prepareStatement(initialQuery);
		preparedStatement.setString(1, mobileNumber);
		preparedStatement.setString(2, ackNumber);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {

			String query = "UPDATE " + ColumnsNFields.ITRV_DOCUMENT_TABLE + " SET pan_number =?" + ", status =?"
					+ " where mobile_number = ? and acknowledge_number = ?";

			preparedStatement = connection.prepareStatement(query);

			String itrStatus = itrObject.optString("status", Constants.NA);

			if (!itrStatus.equalsIgnoreCase(Constants.NA) && itrStatus.length() > 264) {

				itrStatus.substring(0, 264);
			}

			preparedStatement.setString(1, panNumber);
			preparedStatement.setString(2, itrStatus);
			preparedStatement.setString(3, mobileNumber);
			preparedStatement.setString(4, ackNumber);

			int status = preparedStatement.executeUpdate();

			if (status == 1)
				LoggerUtils.log("Updated ITR-V data successfully.");
			else
				LoggerUtils.log("Failed to Update ITR-V data.");

			return status == 1;

		} else {

			String query = "INSERT INTO " + ColumnsNFields.ITRV_DOCUMENT_TABLE + " (mobile_number,"
					+ "acknowledge_number," + "pan_number," + "status," + "user_id) " + "VALUES(?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);

			String itrStatus = itrObject.optString("status", Constants.NA);

			preparedStatement.setString(1, mobileNumber);
			preparedStatement.setString(2, ackNumber);
			preparedStatement.setString(3, panNumber);
			preparedStatement.setString(4, itrStatus);
			preparedStatement.setInt(5, userId);

			int status = preparedStatement.executeUpdate();

			if (status == 1)
				LoggerUtils.log("Inserted ITR-V data successfully.");
			else
				LoggerUtils.log("Failed to insert ITR-V data.");

			return status == 1;
		}
	}

	// ----------------------- USER TABLE MEHTODS ------------------------------ //
	// ------------------------------------------------------------------------- //

	public String getUserSessionPasscode(int userId) throws Exception {

		checkConnection();

		String query = "SELECT session_passcode FROM " + ColumnsNFields.USER_TABLE + " WHERE id = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userId);
		resultSet = preparedStatement.executeQuery();

		if (null == resultSet) {
			return Constants.NA;
		} else {
			if (resultSet.first()) {
				return resultSet.getString(ColumnsNFields.UserColumn.SESSION_PASSCODE.value);
			} else
				return Constants.NA;

		}

	}

	public JSONObject getAvailableDocuments(String mobileNumber) throws SQLException {

		checkConnection();

		JSONObject responseJson = new JSONObject();

		return responseJson;

	}

	public boolean insertUserLocationData(UserLocation userLocation) throws SQLException {

		checkConnection();

		String query = "INSERT INTO " + ColumnsNFields.USER_LOCATION_TABLE + " ("
				+ ColumnsNFields.UserLocationColumn.USER_ID.value + ","
				+ ColumnsNFields.UserLocationColumn.SF_USER_ID.value + ","
				+ ColumnsNFields.UserLocationColumn.LATITUDE.value + ","
				+ ColumnsNFields.UserLocationColumn.LONGITUDE.value + ","
				+ ColumnsNFields.UserLocationColumn.DEVICE_ID.value + ","
				+ ColumnsNFields.UserLocationColumn.DEVICE_TYPE.value + ","
				+ ColumnsNFields.UserLocationColumn.UPDATE_DATETIME.value + ","
				+ ColumnsNFields.UserLocationColumn.ADDRESS.value + ") " + "VALUES (?,?,?,?,?,?,?,?)";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userLocation.userId);
		preparedStatement.setString(2, userLocation.sfUserId);
		preparedStatement.setString(3, userLocation.latitude);
		preparedStatement.setString(4, userLocation.longitude);
		preparedStatement.setString(5, userLocation.deviceId);
		preparedStatement.setString(6, userLocation.deviceType);
		preparedStatement.setString(7, DateTimeUtils.getCurrentDateTimeInIST());
		preparedStatement.setString(8, userLocation.address);

		int status = preparedStatement.executeUpdate();

		return status == 1;

	}

	public boolean insertOrUpdateUserInfo(User user) throws Exception {

		checkConnection();

		User existingUser = getUserBySFUserId(user.sfUserId);

		int status = 0;

		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		if (null == existingUser) {

			String query = "INSERT INTO " + ColumnsNFields.USER_TABLE + " ("
					+ ColumnsNFields.UserColumn.SF_USER_ID.value + "," + ColumnsNFields.UserColumn.ORG_ID.value + ","
					+ ColumnsNFields.UserColumn.FIRST_NAME.value + "," + ColumnsNFields.UserColumn.LAST_NAME.value + ","
					+ ColumnsNFields.UserColumn.DISPLAY_NAME.value + "," + ColumnsNFields.UserColumn.EMAIL.value + ","
					+ ColumnsNFields.UserColumn.USERNAME.value + "," + ColumnsNFields.UserColumn.ID_URL.value + ","
					+ ColumnsNFields.UserColumn.DEVICE_ID.value + "," + ColumnsNFields.UserColumn.DEVICE_TYPE.value
					+ "," + ColumnsNFields.UserColumn.REGISTER_DATETIME.value + ","
					+ ColumnsNFields.UserColumn.LAST_LOGIN_DATETIME.value + ","
					+ ColumnsNFields.UserColumn.UPDATE_DATETIME.value + ","
					+ ColumnsNFields.UserColumn.SESSION_PASSCODE.value + ") " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, user.sfUserId);
			preparedStatement.setString(2, user.orgId);
			preparedStatement.setString(3, user.firstName);
			preparedStatement.setString(4, user.lastName);
			preparedStatement.setString(5, user.displayName);
			preparedStatement.setString(6, user.email);
			preparedStatement.setString(7, user.username);
			preparedStatement.setString(8, user.idUrl);
			preparedStatement.setString(9, user.deviceId);
			preparedStatement.setString(10, user.deviceType);
			preparedStatement.setString(11, currentDatetime);
			preparedStatement.setString(12, currentDatetime);
			preparedStatement.setString(13, currentDatetime);
			preparedStatement.setString(14, getPasscodeHash(user));

			status = preparedStatement.executeUpdate();

		} else {
			
			if (BasicUtils.isNotNullOrNA(existingUser.sessionPasscode)) {
				
				String query = "UPDATE " + ColumnsNFields.USER_TABLE + " SET "
						+ ColumnsNFields.UserColumn.LAST_LOGIN_DATETIME.value + " = ?,"
						+ ColumnsNFields.UserColumn.UPDATE_DATETIME.value + " = ?,"
						+ ColumnsNFields.UserColumn.SESSION_PASSCODE.value + " = ?" + " WHERE "
						+ ColumnsNFields.UserColumn.ID.value + " = ?";
				
				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1, currentDatetime);
				preparedStatement.setString(2, currentDatetime);
				preparedStatement.setString(3, getPasscodeHash(user));
				preparedStatement.setInt(4, existingUser.id);

				status = preparedStatement.executeUpdate();
				
			} else {
				
				String query = "UPDATE " + ColumnsNFields.USER_TABLE + " SET "
						+ ColumnsNFields.UserColumn.ORG_ID.value + " = ?,"
						+ ColumnsNFields.UserColumn.USERNAME.value + " = ?,"
						+ ColumnsNFields.UserColumn.ID_URL.value + " = ?,"
						+ ColumnsNFields.UserColumn.DEVICE_ID.value + " = ?,"
						+ ColumnsNFields.UserColumn.DEVICE_TYPE.value + " = ?,"
						+ ColumnsNFields.UserColumn.REGISTER_DATETIME.value + " = ?,"
						+ ColumnsNFields.UserColumn.LAST_LOGIN_DATETIME.value + " = ?,"
						+ ColumnsNFields.UserColumn.UPDATE_DATETIME.value + " = ?,"
						+ ColumnsNFields.UserColumn.SESSION_PASSCODE.value + " = ? WHERE "
						+ ColumnsNFields.UserColumn.ID.value + " = ?";
				
				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1, user.orgId);
				preparedStatement.setString(2, user.username);
				preparedStatement.setString(3, user.idUrl);
				preparedStatement.setString(4, user.deviceId);
				preparedStatement.setString(5, user.deviceType);
				preparedStatement.setString(6, currentDatetime);
				preparedStatement.setString(7, currentDatetime);
				preparedStatement.setString(8, currentDatetime);
				preparedStatement.setString(9, getPasscodeHash(user));
				preparedStatement.setInt(10, existingUser.id);

				status = preparedStatement.executeUpdate();
				
			}

		}

		return status == 1;

	}

	public boolean addUpdateUserSecondaryInfo(User user, String appVersion) throws SQLException {

		checkConnection();

		if (!doesSecondaryInfoExist(user.id)) {
			String query = "INSERT INTO " + ColumnsNFields.USER_SECONDARY_INFO_TABLE + " ("
					+ ColumnsNFields.SecondaryInfoColumn.USER_ID.value + ","
					+ ColumnsNFields.SecondaryInfoColumn.APP_VERSION.value + ","
					+ ColumnsNFields.SecondaryInfoColumn.DEVICE_ID.value + ","
					+ ColumnsNFields.SecondaryInfoColumn.DEVICE_TYPE.value
					+ ") " + "VALUES(?,?,?,?)";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, user.id);
			preparedStatement.setString(2, appVersion);
			preparedStatement.setString(3, user.deviceId);
			preparedStatement.setString(4, user.deviceType);

		} else {

			String query = "UPDATE " + ColumnsNFields.USER_SECONDARY_INFO_TABLE + " SET "
					+ ColumnsNFields.SecondaryInfoColumn.APP_VERSION.value + "=?,"
					+ ColumnsNFields.SecondaryInfoColumn.DEVICE_ID.value + "=?,"
					+ ColumnsNFields.SecondaryInfoColumn.DEVICE_TYPE.value + "=?" + " WHERE "
					+ ColumnsNFields.SecondaryInfoColumn.USER_ID.value + "=?";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, appVersion);
			preparedStatement.setString(2, user.deviceId);
			preparedStatement.setString(3, user.deviceType);
			preparedStatement.setInt(4, user.id);

		}

		return preparedStatement.executeUpdate() > 0;

	}

	public boolean addLoginInfo(int userId, JSONObject requestObject, String ipAddress) throws SQLException {

		checkConnection();

		String query = "INSERT INTO " + ColumnsNFields.LOGIN_INFO_TABLE + " ("
				+ ColumnsNFields.LoginInfoColumn.USER_ID.value + ","
				+ ColumnsNFields.LoginInfoColumn.LOGIN_DATETIME.value + ","
				+ ColumnsNFields.LoginInfoColumn.IP_ADDRESS.value + "," + ColumnsNFields.LoginInfoColumn.DEVICE_ID.value
				+ "," + ColumnsNFields.LoginInfoColumn.DEVICE_TYPE.value + ","
				+ ColumnsNFields.LoginInfoColumn.DEVICE_MODEL.value + ","
				+ ColumnsNFields.LoginInfoColumn.APP_VERSION.value + ","
				+ ColumnsNFields.LoginInfoColumn.OS_VERSION.value + ") VALUES(?,?,?,?,?,?,?,?)";

		preparedStatement = connection.prepareStatement(query);

		preparedStatement.setInt(1, userId);
		preparedStatement.setString(2, DateTimeUtils.getCurrentDateTimeInIST());
		preparedStatement.setString(3, ipAddress);
		preparedStatement.setString(4, requestObject.optString("deviceId", Constants.NA));
		preparedStatement.setString(5, requestObject.optString("deviceType", Constants.NA));

		String modelName = requestObject.optString("deviceModel", Constants.NA);
		if (modelName.length() > 128)
			modelName = modelName.substring(0, 128);

		preparedStatement.setString(6, modelName);
		preparedStatement.setString(7, requestObject.optString("appVersion", Constants.NA));
		preparedStatement.setString(8, requestObject.optString("osVersion", Constants.NA));

		int status = preparedStatement.executeUpdate();

		return status == 1;

	}

	private boolean doesSecondaryInfoExist(int userId) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.USER_SECONDARY_INFO_TABLE + " where "
				+ ColumnsNFields.SecondaryInfoColumn.USER_ID.value + "=?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userId);

		resultSet = preparedStatement.executeQuery();

		return (null != resultSet && resultSet.first());

	}

	private String getPasscodeHash(User user) throws Exception {
		Random random = new Random();
		double randomNumber = random.nextInt(99999);
		return BasicUtils.getTheKey(user.sfUserId + user.deviceId + randomNumber);
	}

	public User getUserBySFUserId(String sfUserId) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.USER_TABLE + " where "
				+ ColumnsNFields.UserColumn.SF_USER_ID.value + " = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, sfUserId);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {
			return parseUserFromResultSet(resultSet);
		} else
			return null;

	}

	public User getUserByUserId(String userId) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.USER_TABLE + " where " + ColumnsNFields.UserColumn.ID.value
				+ " = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, userId);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {
			return parseUserFromResultSet(resultSet);
		} else
			return null;

	}

	public User parseUserFromResultSet(ResultSet resultSet) throws SQLException {

		User user = new User();

		user.id = resultSet.getInt(ColumnsNFields.UserColumn.ID.value);
		user.sfUserId = resultSet.getString(ColumnsNFields.UserColumn.SF_USER_ID.value);
		user.orgId = resultSet.getString(ColumnsNFields.UserColumn.ORG_ID.value);
		user.firstName = resultSet.getString(ColumnsNFields.UserColumn.FIRST_NAME.value);
		user.lastName = resultSet.getString(ColumnsNFields.UserColumn.LAST_NAME.value);
		user.displayName = resultSet.getString(ColumnsNFields.UserColumn.DISPLAY_NAME.value);
		user.email = resultSet.getString(ColumnsNFields.UserColumn.EMAIL.value);
		user.mobileNumber = resultSet.getString(ColumnsNFields.UserColumn.MOBILENUMBER.value);
		user.username = resultSet.getString(ColumnsNFields.UserColumn.USERNAME.value);
		user.idUrl = resultSet.getString(ColumnsNFields.UserColumn.ID_URL.value);
		user.deviceId = resultSet.getString(ColumnsNFields.UserColumn.DEVICE_ID.value);
		user.deviceType = resultSet.getString(ColumnsNFields.UserColumn.DEVICE_TYPE.value);
		user.registerDatetime = resultSet.getString(ColumnsNFields.UserColumn.REGISTER_DATETIME.value);
		user.lastLoginDatetime = resultSet.getString(ColumnsNFields.UserColumn.LAST_LOGIN_DATETIME.value);
		user.sessionPasscode = resultSet.getString(ColumnsNFields.UserColumn.SESSION_PASSCODE.value);

		return user;

	}

	public User getUserByUserId(int userId) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.USER_TABLE + " where " + ColumnsNFields.UserColumn.ID.value
				+ " = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userId);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {
			return parseUserFromResultSet(resultSet);
		} else
			return null;

	}

	public ArrayList<User> getAllUserData() throws SQLException {

		checkConnection();

		ArrayList<User> userData = new ArrayList<User>();

		String query = "select * from " + ColumnsNFields.USER_TABLE;
		// + " where " + ColumnsNFields.UserColumn.ID.value
		// + " = ?";

		preparedStatement = connection.prepareStatement(query);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet) {
			while (resultSet.next()) {
				User u = new User();
				u.email = resultSet.getString(ColumnsNFields.UserColumn.EMAIL.toString());
				u.id = resultSet.getInt(ColumnsNFields.UserColumn.ID.toString());

				userData.add(u);
			}
			return userData;
		} else
			return null;

	}

	// ----------------------- END USER TABLE MEHTODS -------------------------- //
	// ------------------------------------------------------------------------- //

	public void close() {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
			resultSet = null;
		}
		if (preparedStatement != null) {
			try {
				preparedStatement.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
			preparedStatement = null;
		}
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
			connection = null;
		}
	}

}
