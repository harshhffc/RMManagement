package databasehelper;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

import dao.DataProvider;
import manager.AdminUserManager.DashboardFilter;
import models.AdminLog;
import models.PaymentInfo;
import models.User;
import models.UserLocation;
import models.admin.ActiveBranchUser;
import models.admin.ActiveBranchUserInfo;
import models.admin.ActiveUser;
import models.admin.AdminDashboard;
import models.admin.AdminUser;
import models.admin.KYCDocStats;
import models.admin.KYCStats;
import models.admin.LeadStat;
import utils.BasicUtils;
import utils.Constants;
import utils.DateTimeUtils;
import utils.LoggerUtils;
import utils.DateTimeUtils.DateTimeFormat;

public class AdminDBHelper {

	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

	public AdminDBHelper() {
	}

	private void checkConnection() throws SQLException {
		if (null == connection || !connection.isValid(10))
			connection = DataProvider.getDataSource().getConnection();
	}

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

	public AdminUser getAdminUserByUserId(int userId) throws Exception {

		checkConnection();

		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(ColumnsNFields.ADMIN_USER_TABLE);
		sb.append(" where ");
		sb.append(ColumnsNFields.COMMON_KEY_ID);
		sb.append("=?");

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setInt(1, userId);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return getAdminUserFromRS(resultSet);

		return null;

	}

	public JSONArray getSearchedRMUserList(String nameKey) throws SQLException {

		checkConnection();

		String query = "SELECT " + ColumnsNFields.UserColumn.ID.value + "," + ColumnsNFields.UserColumn.SF_USER_ID.value
				+ "," + ColumnsNFields.UserColumn.DISPLAY_NAME.value + "," + ColumnsNFields.UserColumn.EMAIL.value + ","
				+ ColumnsNFields.UserColumn.USERNAME.value + "," + ColumnsNFields.UserColumn.ID_URL.value + ","
				+ ColumnsNFields.UserColumn.REGISTER_DATETIME.value + ","
				+ ColumnsNFields.UserColumn.LAST_LOGIN_DATETIME.value + " FROM " + ColumnsNFields.USER_TABLE + " WHERE "
				+ ColumnsNFields.UserColumn.DISPLAY_NAME.value + " LIKE ?" + " order by "
				+ ColumnsNFields.UserColumn.LAST_LOGIN_DATETIME.value + " desc";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, "%" + nameKey + "%");

		resultSet = preparedStatement.executeQuery();

		JSONArray userArray = new JSONArray();

		if (null != resultSet && resultSet.first()) {
			do {
				userArray.put(getParsedUser(resultSet).jsonForSearch());
			} while (resultSet.next());
		}

		return userArray;

	}

	private User getParsedUser(ResultSet resultSet) throws SQLException {

		User user = new User();

		user.id = resultSet.getInt(ColumnsNFields.UserColumn.ID.value);
		user.sfUserId = resultSet.getString(ColumnsNFields.UserColumn.SF_USER_ID.value);
		user.displayName = resultSet.getString(ColumnsNFields.UserColumn.DISPLAY_NAME.value);
		user.email = resultSet.getString(ColumnsNFields.UserColumn.EMAIL.value);
		user.username = resultSet.getString(ColumnsNFields.UserColumn.USERNAME.value);
		user.idUrl = resultSet.getString(ColumnsNFields.UserColumn.ID_URL.value);
		user.registerDatetime = resultSet.getString(ColumnsNFields.UserColumn.REGISTER_DATETIME.value);
		user.lastLoginDatetime = resultSet.getString(ColumnsNFields.UserColumn.LAST_LOGIN_DATETIME.value);

		return user;

	}

	public JSONArray getLocationInfo(JSONObject request) throws SQLException {

		checkConnection();

		String query = "SELECT * FROM " + ColumnsNFields.USER_LOCATION_TABLE + " WHERE "
				+ ColumnsNFields.UserLocationColumn.USER_ID.value + "=?";

		String fromDatetime = request.optString("fromDatetime", Constants.NA);
		String toDatetime = request.optString("toDatetime", Constants.NA);

		if (!fromDatetime.equalsIgnoreCase(Constants.NA) && !toDatetime.equalsIgnoreCase(Constants.NA)) {
			query += " and " + ColumnsNFields.UserLocationColumn.UPDATE_DATETIME.value + " between " + "? and ?";
		}

		String orderByType = request.optString("orderBy", Constants.NA);
		if (!orderByType.equalsIgnoreCase(Constants.NA)) {
			query += " order by " + ColumnsNFields.UserLocationColumn.UPDATE_DATETIME.value;
			if (orderByType.equalsIgnoreCase("DESC"))
				query += " desc";
			else
				query += " asc";
		}

		int limit = request.optInt("limit", -1);
		if (limit != -1) {
			query += " limit ?";
		}

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, request.getInt("rmUserId"));

		if (!fromDatetime.equalsIgnoreCase(Constants.NA) && !toDatetime.equalsIgnoreCase(Constants.NA)) {
			preparedStatement.setString(2, fromDatetime);
			preparedStatement.setString(3, toDatetime);
			if (limit != -1) {
				preparedStatement.setInt(4, limit);
			}
		} else {
			if (limit != -1) {
				preparedStatement.setInt(2, limit);
			}
		}

		LoggerUtils.log("prepared Query: " + query);

		resultSet = preparedStatement.executeQuery();

		JSONArray locationArray = new JSONArray();

		if (null != resultSet && resultSet.first()) {
			do {
				locationArray.put(getParsedLocation(resultSet).toJson());
			} while (resultSet.next());
		}

		return locationArray;

	}

	private UserLocation getParsedLocation(ResultSet resultSet) throws SQLException {

		UserLocation location = new UserLocation();

		// location.id = resultSet.getInt(ColumnsNFields.UserLocationColumn.ID.value);
		location.userId = resultSet.getInt(ColumnsNFields.UserLocationColumn.USER_ID.value);
		location.sfUserId = resultSet.getString(ColumnsNFields.UserLocationColumn.SF_USER_ID.value);
		location.latitude = resultSet.getString(ColumnsNFields.UserLocationColumn.LATITUDE.value);
		location.longitude = resultSet.getString(ColumnsNFields.UserLocationColumn.LONGITUDE.value);
		location.deviceId = resultSet.getString(ColumnsNFields.UserLocationColumn.DEVICE_ID.value);
		location.deviceType = resultSet.getString(ColumnsNFields.UserLocationColumn.DEVICE_TYPE.value);
		location.updateDatetime = resultSet.getString(ColumnsNFields.UserLocationColumn.UPDATE_DATETIME.value);
		location.address = resultSet.getString(ColumnsNFields.UserLocationColumn.ADDRESS.value);

		return location;

	}

	// ================== START OF DASHBOARD METHOD IMPLEMENTATION
	// ================== //
	// ==============================================================================
	// //

	public AdminDashboard getLeadCreatedAndConvertedInfo(DashboardFilter filter, AdminDashboard dashboard)
			throws SQLException {

		checkConnection();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		StringBuilder sb = new StringBuilder();

		sb.append(
				"SELECT (count(lcr.id) + lcc.leadCreatedAndConverted) leadCreated,  (lco.leadConverted + lcc.leadCreatedAndConverted) leadConverted");
		sb.append(" FROM RMManagementDB.sf_main_object lcr ");
		sb.append(" left join (SELECT count(id) leadConverted FROM RMManagementDB.sf_main_object");
		sb.append(
				" where source = 'RM_PRO' and create_datetime = update_datetime and lead_stage = 'converted'  and create_datetime  between '"
						+ startDatetime + "' and '" + endDatetime + "') lco on lco.leadConverted");
		sb.append(" left join (SELECT count(id) leadCreatedAndConverted FROM RMManagementDB.sf_main_object ");
		sb.append(" where source = 'RM_PRO' and create_datetime != update_datetime  and create_datetime  between '" + startDatetime
				+ "' and '" + endDatetime + "') lcc on lcc.leadCreatedAndConverted");
		sb.append(" where source = 'RM_PRO' and lead_stage = 'created' and create_datetime  between '" + startDatetime + "' and '"
				+ endDatetime + "'");

		preparedStatement = connection.prepareStatement(sb.toString());

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {

			LeadStat createdLead = new LeadStat();
			createdLead.rmProCount = resultSet.getInt("leadCreated");
			dashboard.createdLead = createdLead;

			LeadStat convertedLead = new LeadStat();
			convertedLead.rmProCount = resultSet.getInt("leadConverted");
			dashboard.convertedLead = convertedLead;

		}

		return dashboard;

	}

	public AdminDashboard getKYCInfo(DashboardFilter filter, AdminDashboard dashboard) throws SQLException {

		checkConnection();

		StringBuilder sb = new StringBuilder();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		sb.append("SELECT count(info.id) dCount, info.document_type dType, SUBSTRING(info.datetime, 1,10) dDate");
		sb.append(" FROM RMManagementDB.kyc_document info");
		sb.append(" where datetime between '" + startDatetime + "' and '" + endDatetime + "' group by dType, dDate");

		dashboard.kycDocuments = getDocumentStats(sb.toString(), true, null);

		return dashboard;

	}

	public AdminDashboard getUtilityBillInfo(DashboardFilter filter, AdminDashboard dashboard) throws SQLException {

		checkConnection();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT count(info.id) dCount, info.bill_type dType, SUBSTRING(info.datetime, 1,10) dDate");
		sb.append(" FROM RMManagementDB.utility_bill info");
		sb.append(" where datetime between '" + startDatetime + "' and '" + endDatetime + "' group by dType, dDate");

		dashboard.utilityBills = getDocumentStats(sb.toString(), true, null);

		return dashboard;

	}

	public AdminDashboard getIncomeDocumentInfo(DashboardFilter filter, AdminDashboard dashboard) throws SQLException {

		checkConnection();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(info.id) dCount, SUBSTRING(info.datetime, 1,10) dDate");
		sb.append(" FROM RMManagementDB.epf_detail info");
		sb.append(" where datetime between '" + startDatetime + "' and '" + endDatetime + "' group by dDate");

		ArrayList<KYCStats> epfStats = getDocumentStats(sb.toString(), false, "EPF");

		sb = new StringBuilder();
		sb.append("SELECT count(info.id) dCount, SUBSTRING(info.datetime, 1,10) dDate");
		sb.append(" FROM RMManagementDB.itrv_document info");
		sb.append(" where datetime between '" + startDatetime + "' and '" + endDatetime + "' group by dDate");

		ArrayList<KYCStats> itrStats = getDocumentStats(sb.toString(), false, "ITR");

		ArrayList<KYCStats> finalStats = new ArrayList<>();

		if (epfStats.size() > 0 || itrStats.size() > 0) {
			if (Math.max(epfStats.size(), itrStats.size()) == epfStats.size()) {

				for (KYCStats epfItem : epfStats) {
					finalStats.add(epfItem);
					for (KYCStats itrItem : itrStats) {
						if (itrItem.date.equals(epfItem.date)) {
							finalStats.get(finalStats.indexOf(epfItem)).kycList.addAll(itrItem.kycList);
							break;
						}
					}
				}

				ArrayList<KYCStats> leftItems = new ArrayList<>();
				for (KYCStats itrItem : itrStats) {
					for (KYCStats fItem : finalStats) {
						if (itrItem.date.equals(fItem.date))
							break;
						if (fItem == finalStats.get(finalStats.size() - 1))
							leftItems.add(itrItem);
					}
				}
				if (leftItems.size() > 0)
					finalStats.addAll(leftItems);

			} else {

				for (KYCStats itrItem : itrStats) {
					finalStats.add(itrItem);
					for (KYCStats epfItem : epfStats) {
						if (epfItem.date.equals(itrItem.date)) {
							finalStats.get(finalStats.indexOf(itrItem)).kycList.addAll(epfItem.kycList);
							break;
						}
					}
				}

				ArrayList<KYCStats> leftItems = new ArrayList<>();
				for (KYCStats epfItem : epfStats) {
					for (KYCStats fItem : finalStats) {
						if (epfItem.date.equals(fItem.date))
							break;
						if (fItem == finalStats.get(finalStats.size() - 1))
							leftItems.add(epfItem);
					}
				}
				if (leftItems.size() > 0)
					finalStats.addAll(leftItems);

			}
		}

		Collections.sort(finalStats);
		dashboard.incomeDocuments = finalStats;

		return dashboard;

	}

	public AdminDashboard getOtherDocumentInfo(DashboardFilter filter, AdminDashboard dashboard) throws SQLException {

		checkConnection();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(info.id) dCount, SUBSTRING(info.datetime, 1,10) dDate");
		sb.append(" FROM RMManagementDB.vehicle_rc_info info");
		sb.append(" where datetime between '" + startDatetime + "' and '" + endDatetime + "' group by dDate");

		ArrayList<KYCStats> vehicleRCStats = getDocumentStats(sb.toString(), false, "VehicleRC");

		sb = new StringBuilder();
		sb.append("SELECT count(info.id) dCount, SUBSTRING(info.datetime, 1,10) dDate");
		sb.append(" FROM RMManagementDB.gstin_detail info");
		sb.append(" where datetime between '" + startDatetime + "' and '" + endDatetime + "' group by dDate");

		ArrayList<KYCStats> gstinStats = getDocumentStats(sb.toString(), false, "GSTIN");

		ArrayList<KYCStats> finalStats = new ArrayList<>();

		if (vehicleRCStats.size() > 0 || gstinStats.size() > 0) {
			if (Math.max(vehicleRCStats.size(), gstinStats.size()) == vehicleRCStats.size()) {

				for (KYCStats vrcItem : vehicleRCStats) {
					finalStats.add(vrcItem);
					for (KYCStats gstItem : gstinStats) {
						if (gstItem.date.equals(vrcItem.date)) {
							finalStats.get(finalStats.indexOf(vrcItem)).kycList.addAll(gstItem.kycList);
							break;
						}
					}
				}

				ArrayList<KYCStats> leftItems = new ArrayList<>();
				for (KYCStats gstItem : gstinStats) {
					for (KYCStats fItem : finalStats) {
						if (gstItem.date.equals(fItem.date))
							break;
						if (fItem == finalStats.get(finalStats.size() - 1))
							leftItems.add(gstItem);
					}
				}
				if (leftItems.size() > 0)
					finalStats.addAll(leftItems);

			} else {

				for (KYCStats gstItem : gstinStats) {
					finalStats.add(gstItem);
					for (KYCStats vrcItem : vehicleRCStats) {
						if (vrcItem.date.equals(gstItem.date)) {
							finalStats.get(finalStats.indexOf(gstItem)).kycList.addAll(vrcItem.kycList);
							break;
						}
					}
				}

				ArrayList<KYCStats> leftItems = new ArrayList<>();
				for (KYCStats vrcItem : vehicleRCStats) {
					for (KYCStats fItem : finalStats) {
						if (vrcItem.date.equals(fItem.date))
							break;
						if (fItem == finalStats.get(finalStats.size() - 1))
							leftItems.add(vrcItem);
					}
				}
				if (leftItems.size() > 0)
					finalStats.addAll(leftItems);

			}
		}

		Collections.sort(finalStats);
		dashboard.otherDocuments = finalStats;

		return dashboard;

	}

	public AdminDashboard getPaymentInfo(DashboardFilter filter, AdminDashboard dashboard) throws SQLException {

		checkConnection();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		StringBuilder sb = new StringBuilder();

		sb.append(
				"SELECT count(info.transaction_id) pCount, count(distinct(user_id)) uCount, SUBSTRING(info.completion_datetime, 1,10) pDate");
		sb.append(" FROM RMManagementDB.payment_info info");
		sb.append(" where payment_status = 'success' and completion_datetime between '" + startDatetime + "' and '"
				+ endDatetime + "' group by pDate");

		preparedStatement = connection.prepareStatement(sb.toString());
		resultSet = preparedStatement.executeQuery();

		ArrayList<KYCStats> paymentStats = new ArrayList<>();

		if (null != resultSet && resultSet.first()) {

			do {

				KYCDocStats paymentCountStat = new KYCDocStats();
				paymentCountStat.count = resultSet.getInt("pCount");
				paymentCountStat.type = "paymentCount";

				KYCDocStats paymentUserStat = new KYCDocStats();
				paymentUserStat.count = resultSet.getInt("uCount");
				paymentUserStat.type = "paymentUser";

				KYCStats paymentStat = new KYCStats();
				paymentStat.date = resultSet.getString("pDate");

				paymentStat.kycList.add(paymentCountStat);
				paymentStat.kycList.add(paymentUserStat);

				paymentStats.add(paymentStat);

			} while (resultSet.next());

		}

		dashboard.paymentList = paymentStats;

		sb = new StringBuilder();

		sb.append("SELECT sum(amount) totalPaymentAmount FROM RMManagementDB.payment_info");
		sb.append(" where payment_status = 'success' and completion_datetime between '" + startDatetime + "' and '"
				+ endDatetime + "'");

		preparedStatement = connection.prepareStatement(sb.toString());
		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			dashboard.totalPaymentAmount = resultSet.getDouble("totalPaymentAmount");

		return dashboard;

	}

	private ArrayList<KYCStats> getDocumentStats(String query, boolean shouldGetType, String defaultType)
			throws SQLException {

		preparedStatement = connection.prepareStatement(query);

		resultSet = preparedStatement.executeQuery();

		ArrayList<KYCStats> kycStats = new ArrayList<>();
		String lastProcessDate = "";
		int currentPosition = -1;

		if (null != resultSet && resultSet.first()) {

			do {

				String kycDate = resultSet.getString("dDate");

				KYCDocStats kycDocStat = new KYCDocStats();
				kycDocStat.count = resultSet.getInt("dCount");
				if (shouldGetType)
					kycDocStat.type = resultSet.getString("dType");
				else
					kycDocStat.type = defaultType;

				if (!lastProcessDate.equals(kycDate)) {

					KYCStats kycStat = new KYCStats();
					lastProcessDate = kycDate;
					kycStat.date = lastProcessDate;
					kycStat.kycList.add(kycDocStat);

					kycStats.add(kycStat);
					currentPosition++;

				} else {

					kycStats.get(currentPosition).kycList.add(kycDocStat);

				}

			} while (resultSet.next());

		}

		return kycStats;

	}

	/*
	 * private String getEndDateTime() { return
	 * DateTimeUtils.getDateTime(DateTimeFormat.yyyy_MM_dd, DateTimeZone.IST) +
	 * " 23:59:59"; }
	 * 
	 * private String getStartDateTime(DashboardFilter filter) { return
	 * DateTimeUtils.getDateTime(filter.dateCode, DateTimeFormat.yyyy_MM_dd,
	 * DateTimeZone.IST) + " 00:00:01"; }
	 */

	public ArrayList<String> getAllRMUsers() throws SQLException {

		checkConnection();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		StringBuilder sb = new StringBuilder();

		//(SELECT email_id FROM RMManagementDB.branch_map)
		
		sb.append("SELECT sf_user_id FROM RMManagementDB.user "
				+ "		where email in (SELECT user_email_id FROM RMManagementDB.region_map) "
				+ "		and last_login_datetime between '" + startDatetime + "' and '" + endDatetime + "'"
				+ "		and email not in ("
				+ "				'gaurav.khetia@homefirstindia.com','ayush.maurya@homefirstindia.com','ranan.rodrigues@homefirstindia.com',"
				+ "				'dharam.joshi@homefirstindia.com','gaurav.mohta@homefirstindia.com','manoj.viswanathan@homefirstindia.com',"
				+ "				'mahesh.saggurthi@homefirstindia.com','ajay.khetan@homefirstindia.com','erica.gonsalves@homefirstindia.com',"
				+ "				'ajinkya.chandorkar@homefirstindia.com','vilasini.subramaniam@homefirstindia.com','rohit.shetty@homefirstindia.com',"
				+ "				'rohitshetty@salesforce.com','rajarshi.mitra@homefirstindia.com'" + "		)");

		preparedStatement = connection.prepareStatement(sb.toString());
		resultSet = preparedStatement.executeQuery();

		ArrayList<String> sfUserIds = new ArrayList<>();

		if (null != resultSet && resultSet.first()) {

			do {
				sfUserIds.add(resultSet.getString("sf_user_id"));
			} while (resultSet.next());

		}

		return sfUserIds;

	}

	public ArrayList<ActiveUser> getActiveUsers() throws SQLException {

		checkConnection();

		ArrayList<ActiveUser> users = new ArrayList<>();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		StringBuilder sb = new StringBuilder();

		sb.append(
				"SELECT u.id, u.display_name name, u.email emailId, u.last_login_datetime lastLoginDatetime, li.updateDatetime, li.latitude, li.longitude");
		sb.append(" FROM RMManagementDB.user u");
		sb.append(
				" left join (select user_id, max(update_datetime) updateDatetime, latitude, longitude from RMManagementDB.user_location");
		sb.append(" where update_datetime between '" + startDatetime + "' and '" + endDatetime
				+ "' group by user_id ) li on li.user_id = u.id");
		sb.append(" where u.last_login_datetime between '" + startDatetime + "' and '" + endDatetime
				+ "' order by u.last_login_datetime desc");

		preparedStatement = connection.prepareStatement(sb.toString());

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {

			do {

				ActiveUser aUser = new ActiveUser();

				aUser.user.id = resultSet.getInt("id");
				aUser.user.displayName = resultSet.getString("name");
				aUser.user.email = resultSet.getString("emailId");
				aUser.user.lastLoginDatetime = resultSet.getString("lastLoginDatetime");
				aUser.location.updateDatetime = resultSet.getString("updateDatetime");
				aUser.location.latitude = resultSet.getString("latitude");
				aUser.location.longitude = resultSet.getString("longitude");

				users.add(aUser);

			} while (resultSet.next());

		}

		return users;

	}

	public ActiveBranchUserInfo getUserAndBranchInfo() throws SQLException {

		checkConnection();

		String startDatetime = DateTimeUtils.getStartDate();
		String endDatetime = DateTimeUtils.getEndDate();

		StringBuilder sb = new StringBuilder();

		sb.append(
				" select distinct(bm.branch) branchName, count(bm.branch) totalRMCount, TRUNCATE(((count(u.id) / count(bm.branch)) * 100), 0) appAdoption,");
		sb.append(
				" count(u.id) totalActiveUser, count(ku.id) rmWithKYC, TRUNCATE(((count(ku.id) / count(bm.branch)) * 100), 0) kycAdoption,");
		sb.append(
				" count(pu.id) rmWithPayment,  TRUNCATE(((count(pu.id) / count(bm.branch)) * 100), 0) paymentAdoption");
		sb.append(" from RMManagementDB.branch_map bm");

		sb.append(" left join (SELECT id, email FROM RMManagementDB.user where last_login_datetime between '"
				+ startDatetime + "' and '" + endDatetime + "') u on u.email = bm.email_id");

		sb.append(" left join (SELECT id, email FROM RMManagementDB.user");
		sb.append(" where id in (SELECT user_id FROM RMManagementDB.kyc_document where datetime between '"
				+ startDatetime + "' and '" + endDatetime + "')) ku on ku.email = bm.email_id");
		sb.append(" left join (SELECT id, email FROM RMManagementDB.user");
		sb.append(" where id in (SELECT user_id FROM RMManagementDB.payment_info where completion_datetime between '"
				+ startDatetime + "' and '" + endDatetime + "'");
		sb.append(" and payment_status = 'success')) pu on pu.email = bm.email_id");
		sb.append(" group by BranchName order by TotalRMCount desc;");

		preparedStatement = connection.prepareStatement(sb.toString());

		resultSet = preparedStatement.executeQuery();

		ActiveBranchUserInfo userInfo = new ActiveBranchUserInfo();

		if (null != resultSet && resultSet.first()) {

			ArrayList<ActiveBranchUser> users = new ArrayList<>();

			do {

				ActiveBranchUser user = new ActiveBranchUser();

				user.branchName = resultSet.getString("branchName");
				user.totalRMCount = resultSet.getInt("totalRMCount");
				user.totalActiveUser = resultSet.getInt("totalActiveUser");
				user.appAdoption = resultSet.getInt("appAdoption");
				user.rmWithKYC = resultSet.getInt("rmWithKYC");
				user.kycAdoption = resultSet.getInt("kycAdoption");
				user.rmWithPayment = resultSet.getInt("rmWithPayment");
				user.paymentAdoption = resultSet.getInt("paymentAdoption");

				users.add(user);

				userInfo.totalUsers += user.totalRMCount;
				userInfo.totalActiveUsers += user.totalActiveUser;
				userInfo.totalUsersWithKYC += user.rmWithKYC;
				userInfo.totalUsersWithPayment += user.rmWithPayment;

			} while (resultSet.next());

			userInfo.activeBranchUsers = users;

		}

		return userInfo;

	}

	// **************** END OF DASHBOARD IMPLEMENTATION ******************** //
	// ********************************************************************* //

	private AdminUser getAdminUserFromRS(ResultSet rs) throws SQLException {

		AdminUser user = new AdminUser();

		user.id = rs.getInt(ColumnsNFields.COMMON_KEY_ID);
		user.name = rs.getString(ColumnsNFields.AdminUserColumn.NAME.stringValue);
		user.email = rs.getString(ColumnsNFields.AdminUserColumn.EMAIL.stringValue);
		user.imageUrl = rs.getString(ColumnsNFields.AdminUserColumn.IMAGE_URL.stringValue);
		user.countryCode = rs.getString(ColumnsNFields.AdminUserColumn.COUNTRY_CODE.stringValue);
		user.mobileNumber = rs.getString(ColumnsNFields.AdminUserColumn.MOBILE_NUMBER.stringValue);
		user.sfUserId = rs.getString(ColumnsNFields.AdminUserColumn.SF_USER_ID.stringValue);
		user.passcode = rs.getString(ColumnsNFields.AdminUserColumn.PASSCODE.stringValue);
		user.password = rs.getString(ColumnsNFields.AdminUserColumn.PASSWORD.stringValue);
		user.registeredDatetime = rs.getString(ColumnsNFields.AdminUserColumn.REGISTRATION_DATETIME.stringValue);
		user.allowedNotification = rs.getInt(ColumnsNFields.AdminUserColumn.NOTIFICATOIN_ALLOWED.stringValue) == 1;
		user.role = rs.getInt(ColumnsNFields.AdminUserColumn.ROLE.stringValue);

		return user;

	}

	public AdminUser getAdminUserByEmailId(String emailId) throws Exception {

		checkConnection();

		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(ColumnsNFields.ADMIN_USER_TABLE);
		sb.append(" where ");
		sb.append(ColumnsNFields.AdminUserColumn.EMAIL.stringValue);
		sb.append("=?");

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setString(1, emailId);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return getAdminUserFromRS(resultSet);

		return null;

	}

	private String getPasscodeHash(AdminUser user) throws NoSuchAlgorithmException {
		Random random = new Random();
		double randomNumber = random.nextInt(99999);
		return (new BasicUtils()).getMD5Hash(user.mobileNumber + user.sfUserId + (randomNumber));
	}

	public String updateAdminPasscode(AdminUser user) throws SQLException, NoSuchAlgorithmException {

		checkConnection();

		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(ColumnsNFields.ADMIN_USER_TABLE);
		sb.append(" set ");
		sb.append(ColumnsNFields.AdminUserColumn.PASSCODE.stringValue);
		sb.append("=?");
		sb.append(" where ");
		sb.append(ColumnsNFields.COMMON_KEY_ID);
		sb.append("=?");

		String newPasscode = getPasscodeHash(user);

		preparedStatement = connection.prepareStatement(sb.toString());
		preparedStatement.setString(1, newPasscode);
		preparedStatement.setInt(2, user.id);

		boolean success = preparedStatement.executeUpdate() == 1;

		if (success)
			return newPasscode;

		return null;

	}

	public boolean createSecondaryInfo(int userId) throws SQLException {

		checkConnection();

		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(ColumnsNFields.ADMIN_SECONDARY_TABLE);
		sb.append(" where ");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append("=?");

		preparedStatement = connection.prepareStatement(sb.toString());
		preparedStatement.setInt(1, userId);
		resultSet = preparedStatement.executeQuery();

		if (null == resultSet || !resultSet.first()) {

			sb = new StringBuilder();
			sb.append("insert into ");
			sb.append(ColumnsNFields.ADMIN_SECONDARY_TABLE);
			sb.append(" (");
			sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
			sb.append(")");
			sb.append(" values (?)");

			preparedStatement = connection.prepareStatement(sb.toString());
			preparedStatement.setInt(1, userId);

			return preparedStatement.executeUpdate() == 1;

		}

		return true;

	}

	public boolean addAdminLoginInfo(int userId, JSONObject requestObject, String ipAddress) throws SQLException {

		checkConnection();

		String query = "INSERT INTO " + ColumnsNFields.ADMIN_LOGIN_INFO_TABLE + " ("
				+ ColumnsNFields.AdminLoginInfoColumn.USER_ID.value + ","
				+ ColumnsNFields.AdminLoginInfoColumn.LOGIN_DATETIME.value + ","
				+ ColumnsNFields.AdminLoginInfoColumn.IP_ADDRESS.value + ","
				+ ColumnsNFields.AdminLoginInfoColumn.DEVICE_ID.value + ","
				+ ColumnsNFields.AdminLoginInfoColumn.DEVICE_TYPE.value + ","
				+ ColumnsNFields.AdminLoginInfoColumn.DEVICE_MODEL.value + ","
				+ ColumnsNFields.AdminLoginInfoColumn.APP_VERSION.value + ","
				+ ColumnsNFields.AdminLoginInfoColumn.OS_VERSION.value + ") VALUES(?,?,?,?,?,?,?,?)";

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

	private PaymentInfo getPaymentInfoResultSet(ResultSet rs) throws SQLException {
		PaymentInfo paymentInfo = new PaymentInfo();

		paymentInfo.userId = rs.getInt(ColumnsNFields.COMMON_KEY_USER_ID);
		paymentInfo.transactionId = rs.getString(ColumnsNFields.PaymentInfoColumn.TRANSACTION_ID.value);
		paymentInfo.sfOpportunityId = rs.getString(ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_ID.value);
		paymentInfo.sfOpportunityNumber = rs.getString(ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_NUMBER.value);
		paymentInfo.sfOpportunityName = rs.getString(ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_NAME.value);
		paymentInfo.sfLoanAccountNumber = rs.getString(ColumnsNFields.PaymentInfoColumn.LOAN_ACCOUNT_NUMBER.value);
		paymentInfo.currency = rs.getString(ColumnsNFields.PaymentInfoColumn.CURRENCY.value);
		paymentInfo.amount = rs.getInt(ColumnsNFields.PaymentInfoColumn.AMOUNT.value);
		paymentInfo.initialDatetime = rs.getString(ColumnsNFields.PaymentInfoColumn.INITIAL_DATETIME.value);
		paymentInfo.completionDatetime = rs.getString(ColumnsNFields.PaymentInfoColumn.COMPLETION_DATETIME.value);
		paymentInfo.pgOrderId = rs.getString(ColumnsNFields.PaymentInfoColumn.ORDER_ID.value);
		paymentInfo.pgPaymentId = rs.getString(ColumnsNFields.PaymentInfoColumn.PAYMENT_ID.value);
		paymentInfo.paymentStatus = rs.getString(ColumnsNFields.PaymentInfoColumn.PAYMENT_STATUS.value);
		paymentInfo.receiptStatus = rs.getString(ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value);
		paymentInfo.statusMessage = rs.getString(ColumnsNFields.PaymentInfoColumn.STATUS_MESSAGE.value);
		paymentInfo.sfReceiptId = rs.getString(ColumnsNFields.PaymentInfoColumn.RECEIPT_ID.value);
		paymentInfo.sfReceiptNumber = rs.getString(ColumnsNFields.PaymentInfoColumn.RECEIPT_NUMBER.value);
		paymentInfo.pgPaymentData = rs.getString(ColumnsNFields.PaymentInfoColumn.RECEIPT_DATA.value);
		paymentInfo.sfPaymentNature = rs.getString(ColumnsNFields.PaymentInfoColumn.SF_PAYMENT_TYPE.value);
		paymentInfo.sfPaymentSubType = rs.getString(ColumnsNFields.PaymentInfoColumn.SF_PAYMENT_SUB_TYPE.value);
		paymentInfo.sfClosureReason = rs.getString(ColumnsNFields.PaymentInfoColumn.SF_CLOSURE_REASON.value);
		paymentInfo.sfTransferredHFCName = rs.getString(ColumnsNFields.PaymentInfoColumn.SF_TRANSFERRED_HFC_NAME.value);
		paymentInfo.paymentMethod = rs.getString(ColumnsNFields.PaymentInfoColumn.PAYMENT_METHOD.value);
		paymentInfo.deviceId = rs.getString(ColumnsNFields.PaymentInfoColumn.DEVICE_ID.value);
		paymentInfo.deviceType = rs.getString(ColumnsNFields.PaymentInfoColumn.DEVICE_TYPE.value);

		return paymentInfo;

	}

	public PaymentInfo getPaymentInfoDetails(String transactionId, String opportunityNumber) throws SQLException {

		checkConnection();

		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(ColumnsNFields.PAYMENT_INFO_TABLE);
		sb.append(" where ");
		sb.append(ColumnsNFields.PaymentInfoColumn.TRANSACTION_ID.value);
		sb.append("=? and ");
		sb.append(ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_NUMBER.value);
		sb.append("=?");

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setString(1, transactionId);
		preparedStatement.setString(2, opportunityNumber);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return getPaymentInfoResultSet(resultSet);
		return null;

	}

	public PaymentInfo updatePaymentInfo(PaymentInfo payment) throws Exception {

		checkConnection();

		StringBuilder sb = new StringBuilder();

		sb.append(" UPDATE " + ColumnsNFields.PAYMENT_INFO_TABLE + " SET ");
		sb.append(ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value + "=?,");
		sb.append(ColumnsNFields.PaymentInfoColumn.PAYMENT_STATUS.value + "=?,");
		sb.append(ColumnsNFields.PaymentInfoColumn.RECEIPT_DATA.value + "=?,");
		sb.append(ColumnsNFields.PaymentInfoColumn.STATUS_MESSAGE.value + " =?,");
		sb.append(ColumnsNFields.PaymentInfoColumn.PAYMENT_ID.value + "=?,");
		sb.append(ColumnsNFields.PaymentInfoColumn.COMPLETION_DATETIME.value + "=?");
		sb.append(" Where " + ColumnsNFields.PaymentInfoColumn.TRANSACTION_ID.value + " =? " + " AND ");
		sb.append(ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_NUMBER.value + " =?");

		preparedStatement = connection.prepareStatement(sb.toString());

		// calculate completion date time
		String completionDatetime = DateTimeUtils.alterMinutes(2, payment.initialDatetime, 
				DateTimeFormat.yyyy_MM_dd_HH_mm_ss);

		// create receipt data json object
		JSONObject receiptDataJson = new JSONObject();
		receiptDataJson.put("error", "");
		receiptDataJson.put("status", "success");

		JSONObject txntJson = new JSONObject();
		txntJson.put("txnId", payment.pgPaymentId);
		txntJson.put("amount", payment.amount);

		JSONObject resultJson = new JSONObject();
		resultJson.put("txn", txntJson);

		receiptDataJson.put("result", resultJson);

		payment.completionDatetime = completionDatetime;
		payment.receiptStatus = "pending";
		payment.paymentStatus = "success";
		payment.pgPaymentData = receiptDataJson.toString();
		payment.statusMessage = "Payment: success | Receipt: pending";

		preparedStatement.setString(1, payment.receiptStatus);
		preparedStatement.setString(2, payment.paymentStatus);
		preparedStatement.setString(3, payment.pgPaymentData);
		preparedStatement.setString(4, payment.statusMessage);
		preparedStatement.setString(5, payment.pgPaymentId);
		preparedStatement.setString(6, payment.completionDatetime);
		preparedStatement.setString(7, payment.transactionId);
		preparedStatement.setString(8, payment.sfOpportunityNumber);

		int status = preparedStatement.executeUpdate();

		if (status == 1) {
			LoggerUtils.log("==> Information has been successfully updated in DB");
			return payment;
		} else {
			LoggerUtils.log("==> Failed to update information in DB");
			return null;
		}

	}

	public boolean addAdminLog(AdminLog adminLog) throws SQLException {
		
		checkConnection();


		checkConnection();
		
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO " + ColumnsNFields.ADMIN_LOG_TABLE + " (");
		sb.append(ColumnsNFields.AdminLogInfoColumn.USER_ID.value + ",");
		sb.append(ColumnsNFields.AdminLogInfoColumn.RECORD_TYPE.value + ",");
		sb.append(ColumnsNFields.AdminLogInfoColumn.RECORD_ID.value + ",");
		sb.append(ColumnsNFields.AdminLogInfoColumn.ACTION.value + ",");
		sb.append(ColumnsNFields.AdminLogInfoColumn.DESCRIPTION.value + ",");
		sb.append(ColumnsNFields.AdminLogInfoColumn.DATETIME.value + ") ");
		sb.append(" VALUES (?,?,?,?,?,?)");

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setInt(1, adminLog.userId);
		preparedStatement.setString(2, adminLog.recordType);
		preparedStatement.setString(3, adminLog.recordId);
		preparedStatement.setString(4, adminLog.action);
		preparedStatement.setString(5, adminLog.description);
		preparedStatement.setString(6, DateTimeUtils.getCurrentDateTimeInIST());

		boolean status = preparedStatement.executeUpdate() == 1;
		
		if (status) LoggerUtils.log("Admin log has been added successfully");
		else LoggerUtils.log("Failed to add admin log");
		
		return status;

	}

}