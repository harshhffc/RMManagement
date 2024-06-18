package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import databasehelper.AdminDBHelper;
import models.DefaultResponse;
import models.KYCAuth;
import models.PaymentInfo;
import models.SFLead;
import models.admin.ActiveBranchUserInfo;
import models.admin.ActiveUser;
import models.admin.AdminDashboard;
import models.admin.AdminUser;
import utils.AdminUtils;
import utils.BasicUtils;
import utils.Constants;
import utils.DatabaseHelper;
import utils.DateTimeUtils;
import utils.LocalResponse;
import utils.LoggerUtils;
import utils.MailUtils;
import utils.ProptertyUtils;
import v2.managers.SalesForceManager;
import utils.DateTimeUtils.DateTimeFormat;
import utils.DateTimeUtils.DateTimeZone;

public class AdminUserManager {

	private final AdminDBHelper adbHelper;
	private final SalesForceManager sfManger;

	public AdminUserManager() {
		adbHelper = new AdminDBHelper();
		sfManger = new SalesForceManager();
	}

	public enum DashboardFilter {

		LAST_7_DAYS("Last 7 Days", -7), LAST_28_DAYS("Last 28 Days", -28), ALL_TIME("All time", -1);

		public final String value;
		public final int dateCode;

		DashboardFilter(String value, int dateCode) {
			this.value = value;
			this.dateCode = dateCode;
		}

		public static DashboardFilter get(String value) throws Exception {

			for (DashboardFilter item : DashboardFilter.values()) {
				if (item.value.equalsIgnoreCase(value))
					return item;
			}

			throw new Exception("Invalid filter for dashboard.");

		}

	}

	public enum AdminAction {

		DASHBOARD("DASHBOARD"), SCHEDULE_NOTIFICATION("SCHEDULE_NOTIFICATION"), VIEW_PAYMENT("VIEW_PAYMENT"),
		UDPATE_PAYMENT("UDPATE_PAYMENT");

		public final String value;

		AdminAction(String value) {
			this.value = value;
		}

	}

	public enum AdminRole {

		SUPER_ADMIN(711,
				new ArrayList<String>(Arrays.asList(AdminAction.DASHBOARD.value,
						AdminAction.SCHEDULE_NOTIFICATION.value, AdminAction.VIEW_PAYMENT.value,
						AdminAction.UDPATE_PAYMENT.value))),
		ADMIN_L1(712,
				new ArrayList<String>(Arrays.asList(AdminAction.VIEW_PAYMENT.value, AdminAction.UDPATE_PAYMENT.value))),
		ADMIN_L2(715, new ArrayList<String>(
				Arrays.asList(AdminAction.DASHBOARD.value, AdminAction.SCHEDULE_NOTIFICATION.value)));

		public final int role;
		public final ArrayList<String> allowedActions;

		AdminRole(int role, ArrayList<String> allowedActions) {
			this.role = role;
			this.allowedActions = allowedActions;
		}

		public static AdminRole get(int code) {
			for (AdminRole item : AdminRole.values()) {
				if (item.role == code)
					return item;
			}
			return null;
		}

	}

	public DefaultResponse getTokenStatus(String token) {

		DefaultResponse dResponse = new DefaultResponse();

		DatabaseHelper dbHelper = new DatabaseHelper();

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

	public boolean verifySource(String sourceCode) {

		try {

			if (BasicUtils.getTheKey(sourceCode)
					.equals(ProptertyUtils.getValurForKey(ProptertyUtils.Keys.RM_AHAM_BRAHMA)))
				return true;

		} catch (Exception e) {
			LoggerUtils.log("Error while verifying admin source code: " + e.toString());
			e.printStackTrace();
		}

		return false;
	}

	public boolean verifyUser(int userId, String passcode) {

		if (!passcode.equals(Constants.NA)) {

			try {
				AdminUser user = getUserByUserId(userId);
				return user.passcode.equals(passcode);
			} catch (Exception e) {
				LoggerUtils.log("Error while getting admin passcode: " + e.getMessage());
				e.printStackTrace();
				return false;
			}

		}

		return false;
	}

	public AdminUser getUserByUserId(int userId) throws Exception {

		try {
			AdminUser user = adbHelper.getAdminUserByUserId(userId);
			adbHelper.close();
			return user;
		} catch (Exception e) {
			adbHelper.close();
			throw e;
		}

	}

	private AdminUser getUserByEmailId(String emailId) throws Exception {

		try {
			AdminUser user = adbHelper.getAdminUserByEmailId(emailId);
			adbHelper.close();
			return user;
		} catch (Exception e) {
			adbHelper.close();
			throw e;
		}

	}

	private AdminRole getAdminRole(int code) throws Exception {

		AdminRole aRole = AdminRole.get(code);

		if (null == aRole)
			throw new Exception("Admin roles are not specified or invalid.");
		return aRole;
	}

	public JSONObject searchUser(JSONObject jsonRequest) throws IOException {

		try {
			JSONArray userList = adbHelper.getSearchedRMUserList(jsonRequest.getString("name"));
			adbHelper.close();
			JSONObject responseObject = BasicUtils.getSuccessTemplateObject();
			responseObject.put("users", userList);
			return responseObject;
		} catch (Exception e) {
			adbHelper.close();
			System.out.println("Error while searching rm user: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	public JSONObject getLocationInfo(JSONObject jsonRequest) throws IOException {

		try {
			JSONArray locationList = adbHelper.getLocationInfo(jsonRequest);
			adbHelper.close();
			JSONObject responseObject = BasicUtils.getSuccessTemplateObject();
			responseObject.put("locationInfo", locationList);
			return responseObject;
		} catch (Exception e) {
			adbHelper.close();
			System.out.println("Error while rm user location info: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	public JSONObject getDashboard(JSONObject jsonRequest) throws IOException {

		try {
			AdminDashboard aDashboard = new AdminDashboard();

			DashboardFilter filter = DashboardFilter.get(jsonRequest.getString("filter"));

			aDashboard = adbHelper.getLeadCreatedAndConvertedInfo(filter, aDashboard);
			aDashboard = adbHelper.getKYCInfo(filter, aDashboard);
			aDashboard = adbHelper.getUtilityBillInfo(filter, aDashboard);
			aDashboard = adbHelper.getIncomeDocumentInfo(filter, aDashboard);
			aDashboard = adbHelper.getOtherDocumentInfo(filter, aDashboard);
			aDashboard = adbHelper.getPaymentInfo(filter, aDashboard);
			ArrayList<String> sfUserIdArray = adbHelper.getAllRMUsers();
			adbHelper.close();

			String startDatetime = DateTimeUtils.getDateTime(filter.dateCode, DateTimeFormat.yyyy_MM_dd,
					DateTimeZone.IST) + " 00:00:01";

			startDatetime = DateTimeUtils.getStartDate();
			// String endDatetime = DateTimeUtils.getEndDate();

			String sDatetime = DateTimeUtils.getDateTimeFromString(startDatetime, DateTimeFormat.yyyy_MM_dd_HH_mm_ss,
					DateTimeFormat.yyyy_MM_dd_T_HH_mm_ss_SSSZ, DateTimeZone.IST);

			ArrayList<SFLead> sfLeads = sfManger.getLeadStatForDashboard(sDatetime);

			LoggerUtils.log("==> SFLeads received: " + sfLeads.size());

			ArrayList<SFLead> finalSFLeads = new ArrayList<>();
			for (SFLead item : sfLeads) {
				if (sfUserIdArray.contains(item.sfCreatedById))
					finalSFLeads.add(item);
			}

			LoggerUtils.log("==> final relevent leads: " + finalSFLeads.size());

			aDashboard.createdLead.sfCount = finalSFLeads.size();
			aDashboard.convertedLead.sfCount = 0;
			for (SFLead lead : finalSFLeads) {
				if (lead.isConverted)
					aDashboard.convertedLead.sfCount++;
			}

			JSONObject responseObject = BasicUtils.getSuccessTemplateObject();
			responseObject.put("dashboard", aDashboard.toJson());

			System.out.print("======> Dashboard response <======= \n\n" + responseObject.toString()
					+ "\n\n ======> Dashboard response <=======");

			return responseObject;

		} catch (Exception e) {
			adbHelper.close();
			System.out.println("Error while getting dashboard info: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	public JSONObject getActiveUsers(JSONObject jsonRequest) throws IOException {

		try {

			ArrayList<ActiveUser> aUsers = adbHelper.getActiveUsers();
			adbHelper.close();

			JSONObject fResponse = BasicUtils.getSuccessTemplateObject();

			JSONArray activeUsersArray = new JSONArray();
			for (ActiveUser item : aUsers)
				activeUsersArray.put(item.toJson());

			fResponse.put("activeUsers", activeUsersArray);

			return fResponse;

		} catch (Exception e) {
			adbHelper.close();
			System.out.println("Error while getting active users info: " + e.toString());
			e.printStackTrace();
			return null;
		}

	}

	public JSONObject getUserAndBranchInfo(JSONObject jsonRequest) throws IOException {

		try {

			ActiveBranchUserInfo activeBranchUserInfo = adbHelper.getUserAndBranchInfo();
			adbHelper.close();

			JSONObject fResponse = BasicUtils.getSuccessTemplateObject();
			fResponse.put("activeBranchUserInfo", activeBranchUserInfo.toJson());

			return fResponse;

		} catch (Exception e) {
			adbHelper.close();
			System.out.println("Error while getting active users info: " + e.toString());
			e.printStackTrace();
			return null;
		}

	}

	public JSONObject performLogin(JSONObject requestJson, String ipAddress) throws Exception {

		String emailId = requestJson.optString(Constants.EMAIL_ID, Constants.NA);
		String password = requestJson.optString(Constants.PASSWORD, Constants.NA);
		DefaultResponse dResponse = new DefaultResponse();

		if (emailId.equals(Constants.NA)) {
			dResponse.message = "Invalid Email ID!";
			return dResponse.toJson();
		}

		if (password.equals(Constants.NA)) {
			dResponse.message = "Invalid Password!";
			return dResponse.toJson();
		}

		AdminUser aUser = getUserByEmailId(emailId);
		if (null == aUser) {
			dResponse.message = "No user assosicated with Email ID " + emailId + ". Please contact system admin.";
			return dResponse.toJson();
		}

		AdminRole aRole = getAdminRole(aUser.role);

		if (aUser.password.equals(BasicUtils.getTheKey(password))) {

			try {

				String nPasscode = adbHelper.updateAdminPasscode(aUser);
				if (null != nPasscode) {
					aUser.passcode = nPasscode;
					LoggerUtils.log("Updated admin passcode successfully.");
				} else
					LoggerUtils.log("Failed to update admin passcode.");

				if (adbHelper.createSecondaryInfo(aUser.id))
					LoggerUtils.log("Either admin secondary info already exists or created successfully.");
				else
					LoggerUtils.log("Failed to create admin secondary info.");

				if (adbHelper.addAdminLoginInfo(aUser.id, requestJson, ipAddress))
					LoggerUtils.log("Inserted admin login info successfully.");
				else
					LoggerUtils.log("Failed to inserted admin login info.");

				adbHelper.close();

			} catch (Exception e) {
				adbHelper.close();
				LoggerUtils.log("Error while inserting admin secondary or login info: " + e.getMessage());
				e.printStackTrace();
			}

			JSONObject rJson = BasicUtils.getSuccessTemplateObject();
			rJson.put(Constants.ADMIN_USER, aUser.toJsonWithPasscode());
			rJson.put(Constants.ADMIN_ROLE, String.join("|", aRole.allowedActions));

			return rJson;

		} else {

			dResponse.message = "Incorrect password!";
			return dResponse.toJson();

		}

	}

	public JSONObject getPaymentInfo(int adminId, JSONObject requestbody) throws Exception {

		DefaultResponse eResponse = new DefaultResponse();
		AdminUser aUser = getUserByUserId(adminId);


		if (null == aUser) {
			eResponse.message = "No user assosicated with User ID " + adminId + ". Please contact system admin.";
			return eResponse.toJson();
		}
		
		AdminRole aRole = getAdminRole(aUser.role);
		if (!aRole.allowedActions.contains(AdminAction.VIEW_PAYMENT.value)) {
			eResponse.message = "Action not allowed!";
			return eResponse.toJson();
		}

		String transactionId = requestbody.optString(Constants.TRANSACTION_ID, Constants.NA);
		String opportunityNumber = requestbody.optString(Constants.OPPORTUNITY_NUMBER, Constants.NA);
		DefaultResponse dResponse = new DefaultResponse();

		if (transactionId.equals(Constants.NA)) {
			dResponse.message = "Invalid transactionId!";
			return dResponse.toJson();
		}

		if (opportunityNumber.equals(Constants.NA)) {
			dResponse.message = "Invalid opportunityNumber!";
			return dResponse.toJson();
		}

		try {

			PaymentInfo paymentInfo = adbHelper.getPaymentInfoDetails(transactionId, opportunityNumber);
			adbHelper.close();

			if (null != paymentInfo) {

				JSONObject rJson = BasicUtils.getSuccessTemplateObject();
				rJson.put(Constants.PAYMENT_INFO, paymentInfo.toJson());
				return rJson;

			} else {

				eResponse.isSuccess = false;
				eResponse.message = "No payment record found with given Tranaction ID and Opportunity Number.";
				return eResponse.toJson();

			}

		} catch (Exception e) {
			adbHelper.close();
			throw e;
		}

	}

	public JSONObject updatePaymentStatus(int adminId, JSONObject requestObject) throws Exception {

		DefaultResponse dResponse = new DefaultResponse();

		AdminUser aUser = getUserByUserId(adminId);
		if (null == aUser) {
			dResponse.message = "No user assosicated with User ID " + adminId + ". Please contact system admin.";
			return dResponse.toJson();
		}
		
		AdminRole aRole = getAdminRole(aUser.role);
		if (!aRole.allowedActions.contains(AdminAction.UDPATE_PAYMENT.value)) {
			dResponse.message = "Action not allowed!";
			return dResponse.toJson();
		}

		try {

			PaymentInfo updatedPayment = new PaymentInfo(requestObject);

			if (updatedPayment.transactionId.equals(Constants.NA)) {
				dResponse.message = "Invalid transactionId!";
				return dResponse.toJson();
			}

			if (updatedPayment.sfOpportunityNumber.equals(Constants.NA)) {
				dResponse.message = "Invalid opportunityNumber!";
				return dResponse.toJson();
			}

			JSONObject rJson = BasicUtils.getFailureTemplateObject();

			if (!updatedPayment.paymentStatus.equals("success")) {

				String paymentId = updatedPayment.pgPaymentId;

				// get the original payment record
				PaymentInfo existingPayment = adbHelper.getPaymentInfoDetails(updatedPayment.transactionId,
						updatedPayment.sfOpportunityNumber);

				// replace updated payment data with existing one, then update the new payment
				// ID
				updatedPayment = existingPayment;
				updatedPayment.pgPaymentId = paymentId;

				updatedPayment = adbHelper.updatePaymentInfo(updatedPayment);
				adbHelper.close();

				if (null != updatedPayment) {

					AdminUtils adminUtils = new AdminUtils();
					boolean isLogSuccess = adminUtils.addAdminLog(adminId, AdminUtils.RecordType.PAYMENT,
							updatedPayment.transactionId, AdminUtils.AdminAction.UPDATE,
							getPaymentLogString(existingPayment, updatedPayment));

					if (isLogSuccess) {

						new SendPaymentUpdateEmail(aUser, existingPayment, updatedPayment).run();

						rJson = BasicUtils.getSuccessTemplateObject();
						rJson.put(Constants.PAYMENT_INFO, updatedPayment.toJson());

					}

				}

			} else {

				dResponse.isSuccess = false;
				dResponse.message = "Invalid request! You cannot update this payment record.";

				rJson = dResponse.toJson();

			}

			return rJson;

		} catch (Exception e) {
			adbHelper.close();
			throw e;
		}

	}

	private String getPaymentLogString(PaymentInfo existingPayment, PaymentInfo updatedPayment) {

		StringBuilder sb = new StringBuilder();

		sb.append("RM Pro Payment Info updated for Automatic Receipt Creation.");
		sb.append("\nNew Payment ID: " + updatedPayment.pgPaymentId);
		sb.append("\nOld Payment ID: " + existingPayment.pgPaymentId);

		return sb.toString();

	}

	public class SendPaymentUpdateEmail implements Runnable {

		private AdminUser aUser;
		private PaymentInfo updatedPayment;
		private PaymentInfo existingPayment;

		public SendPaymentUpdateEmail(AdminUser aUser, PaymentInfo existingPayment, PaymentInfo updatedPayment) {
			this.aUser = aUser;
			this.existingPayment = existingPayment;
			this.updatedPayment = updatedPayment;
		}

		@Override
		public void run() {
			try {

				if (!Constants.IS_STRICT_PROD_PROCESS_ACTIVE)
					return;

				StringBuilder sb = new StringBuilder();

				sb.append(aUser.name + " has updated RM Pro Payment Info.");

				sb.append("\n\nNew Payment ID: " + updatedPayment.pgPaymentId);
				sb.append("\nOld Payment ID: " + existingPayment.pgPaymentId);
				sb.append("\nTransaction ID: " + updatedPayment.transactionId);
				sb.append("\nOpportunity Number: " + updatedPayment.sfOpportunityNumber);
				sb.append("\nPayment Datetime: " + existingPayment.initialDatetime);
				sb.append("\nUpdate Datetime: "
						+ DateTimeUtils.getDateTime(DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeZone.IST));
				sb.append("\n Opportunity Detail: https://hffc.my.salesforce.com/" + updatedPayment.sfOpportunityId);

				sb.append("\n\n\n============== Admin's Information ================");
				sb.append("\n Name: " + aUser.name);
				sb.append("\n Email: " + aUser.email);

				sb.append("\n\n\nThis is an automatic email generated by HomeFirst RM Pro AI.");
				sb.append("\nPlease do not reply to this email.");

				MailUtils.getInstance().sendDefaultMail("Payment Info Updated | RM Admin App", sb.toString(),
						aUser.email, "sanjay.jaiswar@homefirstindia.com", "epay@homefirstindia.com");

			} catch (Exception e) {
				LoggerUtils.log("Error while sending RM Admin Log email: " + e.getMessage());
				e.printStackTrace();
			}

		}

	}
	
	public JSONObject getUserId(String emailId) throws Exception {
		
		if (!BasicUtils.isNotNullOrNA(emailId)) {
			LocalResponse eResponse = new LocalResponse();
			eResponse.message = "Invalid Email ID!";
			return eResponse.toJson();
		}
				
		AdminUser aUser = getUserByEmailId(emailId);
		
		if (null != aUser) {
			
			JSONObject responseObject = BasicUtils.getSuccessTemplateObject();
			responseObject.put("user", aUser.toJson());
			return responseObject;
			
		}
		
		return BasicUtils.getFailureTemplateObject();
		
	}

}