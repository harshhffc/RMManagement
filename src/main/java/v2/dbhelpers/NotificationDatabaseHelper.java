package v2.dbhelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import dao.DataProvider;
import databasehelper.ColumnsNFields;
import models.DefaultResponse;
import models.User;
import models.admin.AdminUser;
import models.notification.RmNotification;
import models.notification.RegistrationKey;
import models.notification.UserNotificationKey;
import utils.Constants;
import utils.DateTimeUtils;
import utils.LoggerUtils;
import utils.NotificationUtils;
import utils.NotificationUtils.NotificationFetchType;
import utils.NotificationUtils.Platform;
import utils.NotificationUtils.ScheduleType;

public class NotificationDatabaseHelper {
	
	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;
	
	public NotificationDatabaseHelper() {}

	public void close() {
		if (resultSet != null) {
			try {
				resultSet.close();
			}catch (SQLException se) {
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
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
			connection = null;
		}
	}
	
	private void checkAndConnect() throws SQLException {
		if (null == connection || !connection.isValid(10))
			connection = DataProvider.getDataSource().getConnection();
	}
	
	public ArrayList<RegistrationKey> getRegistrationKeyByUserId(int userId) throws SQLException {
		
		checkAndConnect();
		
		String query = "SELECT " + ColumnsNFields.SecondaryInfoColumn.FCM_KEY.value
				+ " FROM " + ColumnsNFields.USER_SECONDARY_INFO_TABLE				
				+ " WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + "=?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userId);			
		
		resultSet =  preparedStatement.executeQuery();
		
		ArrayList<RegistrationKey> keys = new ArrayList<>();
		
		if (null != resultSet && resultSet.first()) {
			
			String tokenJsonString = resultSet.getString(ColumnsNFields.SecondaryInfoColumn.FCM_KEY.value);
			
			if (null != tokenJsonString) {
				JSONArray tokenJsonArray = new JSONArray(tokenJsonString);
				for (int i = 0; i < tokenJsonArray.length(); i++) {
					keys.add(new RegistrationKey(userId, tokenJsonArray.getJSONObject(i)));
				}
			}
			
		}			
		
		return keys;
		
	}
	
	public ArrayList<RegistrationKey> getAllRegistrationKeys() throws SQLException {
		
		checkAndConnect();
		
		String query = "SELECT " + ColumnsNFields.SecondaryInfoColumn.FCM_KEY.value
				+ "," + ColumnsNFields.COMMON_KEY_USER_ID
				+ " FROM " + ColumnsNFields.USER_SECONDARY_INFO_TABLE;
		preparedStatement = connection.prepareStatement(query);			
		
		resultSet =  preparedStatement.executeQuery();
		
		ArrayList<RegistrationKey> keys = new ArrayList<>();
		
		if (null != resultSet && resultSet.first()) {
			
			do {
				
				int userId = resultSet.getInt(ColumnsNFields.COMMON_KEY_USER_ID);
				String tokenJsonString = resultSet.getString(ColumnsNFields.SecondaryInfoColumn.FCM_KEY.value);
				
				if (null != tokenJsonString) {
					JSONArray tokenJsonArray = new JSONArray(tokenJsonString);
					for (int i = 0; i < tokenJsonArray.length(); i++) {
						keys.add(new RegistrationKey(userId, tokenJsonArray.getJSONObject(i)));
					}
				}
				
			} while (resultSet.next());					
			
		}			
		
		return keys;
		
	}
//	
//	public ArrayList<UserNotificationKey> getUserNotificationKeysByMobile(String mobileNumbers) throws SQLException {
//
//		checkAndConnect();
//
//		String query = "select u.user_id, u.name, u.mobile_number, si.fcm_key "
//				+ " from HomeFirstCustomerPortal.user u "
//				+ " left join (select user_id, fcm_key from HomeFirstCustomerPortal.secondary_info) si on si.user_id = u.user_id "
//				+ " where mobile_number in ('" + mobileNumbers + "')";
//
//		preparedStatement = connection.prepareStatement(query);
//		// preparedStatement.setString(1, mobileNumbers);
//
//		resultSet = preparedStatement.executeQuery();
//
//		ArrayList<UserNotificationKey> userNotificationKeys = new ArrayList<>();
//
//		if (null != resultSet && resultSet.first()) {
//			do {
//
//				UserNotificationKey unKey = new UserNotificationKey();
//
//				String fcmKeyArrayString = resultSet.getString(ColumnsNFields.SecondaryInfoColumn.FCM_KEY.value);
//				if (null != fcmKeyArrayString && fcmKeyArrayString.startsWith("[")) {
//
//					User user = new User();
//
//					user.id = resultSet.getInt(ColumnsNFields.UserColumn.ID.stringValue);
//					user.firstName = resultSet.getString(ColumnsNFields.UserColumn.NAME.stringValue);
//					user = resultSet.getString(ColumnsNFields.UserColumn.MOBILE_NUMBER.stringValue);
//
//					unKey.user = user;
//
//					ArrayList<RegistrationKey> keys = new ArrayList<>();
//
//					JSONArray tokenJsonArray = new JSONArray(fcmKeyArrayString);
//					for (int i = 0; i < tokenJsonArray.length(); i++) {
//						keys.add(new RegistrationKey(user.userId, tokenJsonArray.getJSONObject(i)));
//					}
//
//					unKey.registerationKeys = keys;
//
//					userNotificationKeys.add(unKey);
//
//				}
//
//			} while (resultSet.next());
//		}
//
//		return userNotificationKeys;
//
//	}
	
	public int addNewNotification(AdminUser aUser, RmNotification cpNotification) throws SQLException {

		checkAndConnect();

		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(ColumnsNFields.NOTIFICATION_TABLE);
		sb.append(" (");
		sb.append(ColumnsNFields.NotificationColumn.TITLE.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.MESSAGE.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.BIG_MESSAGE.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.IMAGE_URL.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.WEB_URL.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.DATA.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.AUDIENCE_TYPE.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.ADIENCE_GROUP.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.PRIORITY.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.PLATFORM.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.KIND.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.ON_CLICK_ACTION.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.SCREEN_TO_OPEN.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.SCHEDULE_TYPE.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.CREATE_DATETIME.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.IS_SCHEDULED.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.SCHEDULER_ID.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.SCHEDULER_NAME.stringValue + ",");
		sb.append(ColumnsNFields.NotificationColumn.SCHEDULE_DATETIME.stringValue);
		sb.append(")");
		sb.append(" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		preparedStatement = connection.prepareStatement(sb.toString(), new String[] { ColumnsNFields.COMMON_KEY_ID });

		preparedStatement.setString(1, cpNotification.title);
		preparedStatement.setString(2, cpNotification.message);
		preparedStatement.setString(3, cpNotification.bigMessage);
		preparedStatement.setString(4, cpNotification.imageUrl);
		preparedStatement.setString(5, cpNotification.webUrl);
		preparedStatement.setString(6, cpNotification.data.toString());
		preparedStatement.setString(7, cpNotification.audienceType);
		preparedStatement.setString(8, cpNotification.audienceGroup.toString());
		preparedStatement.setString(9, cpNotification.priority);
		preparedStatement.setString(10, Platform.ALL.value);
		preparedStatement.setString(11, cpNotification.kind);
		preparedStatement.setString(12, cpNotification.onClickAction);
		preparedStatement.setString(13, cpNotification.screenToOpen);
		preparedStatement.setString(14,
				cpNotification.shouldSchedule ? ScheduleType.LATER.value : ScheduleType.NOW.value);
		preparedStatement.setString(15, DateTimeUtils.getCurrentDateTimeInIST());
		preparedStatement.setInt(16, 0);
		preparedStatement.setInt(17, aUser.id);
		preparedStatement.setString(18, aUser.name);
		preparedStatement.setString(19,
				cpNotification.shouldSchedule ? cpNotification.datetime : DateTimeUtils.getCurrentDateTimeInIST());

		if (preparedStatement.executeUpdate() == 1) {

			resultSet = preparedStatement.getGeneratedKeys();
			if (null != resultSet && resultSet.first())
				return resultSet.getInt(1);

		}

		return -1;

	}

	public boolean updateNotification(RmNotification cpNotification, int totalCount, int successCount, int failedCount)
			throws SQLException {

		checkAndConnect();

		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(ColumnsNFields.NOTIFICATION_TABLE);
		sb.append(" set ");
		sb.append(ColumnsNFields.NotificationColumn.IS_SCHEDULED.stringValue + "=?,");
		sb.append(ColumnsNFields.NotificationColumn.TOTAL_COUNT.stringValue + "=?,");
		sb.append(ColumnsNFields.NotificationColumn.SUCCESS_COUNT.stringValue + "=?,");
		sb.append(ColumnsNFields.NotificationColumn.FAILURE_COUNT.stringValue + "=?,");
		sb.append(ColumnsNFields.NotificationColumn.SENT_DATETIME.stringValue + "=?"); 
		sb.append(" where ");
		sb.append(ColumnsNFields.COMMON_KEY_ID + "=?");

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setInt(1, 1);
		preparedStatement.setInt(2, totalCount);
		preparedStatement.setInt(3, successCount);
		preparedStatement.setInt(4, failedCount);
		preparedStatement.setString(5, DateTimeUtils.getCurrentDateTimeInIST());
		preparedStatement.setInt(6, cpNotification.id);

		return preparedStatement.executeUpdate() == 1;

	}

	public DefaultResponse insertNewUserNotifcationEntries(RmNotification cpNotification,
			ArrayList<RegistrationKey> registrationKeys) throws SQLException {		

		String message = null;
		if (cpNotification.audienceType.equals(NotificationUtils.AudienceType.PERSONALIZED.value)) {
			message = cpNotification.message;
			if (!cpNotification.bigMessage.equalsIgnoreCase(Constants.NA))
				message = cpNotification.bigMessage;
		}

		StringBuilder sb = new StringBuilder();

		sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(ColumnsNFields.USDR_NOTIFICATION_INFO_TABLE);
		sb.append(" (");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.NOTIFICATION_ID.value + ",");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID + ",");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.HAS_READ.value + ",");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.DEVICE_ID.value + ",");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.DEVICE_TYPE.value + ",");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.DYNAMIC_MESSAGE.value);
		sb.append(") values");

		for (int i = 0; i < registrationKeys.size(); i++) {

			RegistrationKey rKey = registrationKeys.get(i);
			
			sb.append("(");

			sb.append(cpNotification.id);
			sb.append(",");

			sb.append(rKey.userId);
			sb.append(",");

			sb.append(0);
			sb.append(",");

			sb.append("'");
			sb.append(rKey.deviceId);
			sb.append("'");
			sb.append(",");
			
			sb.append("'");
			sb.append(rKey.deviceType);
			sb.append("'");
			sb.append(",");

			
			sb.append(null == message ? null : "'" + message + "'");			
			

			sb.append(")");

			if (i < registrationKeys.size() - 1) sb.append(",");

		}
		
		LoggerUtils.log("+++++ insertNewUserNotifcationEntries query: " + sb.toString());
		
		checkAndConnect();
		preparedStatement = connection.prepareStatement(sb.toString());
		int insertCount = preparedStatement.executeUpdate();	

		DefaultResponse lResponse = new DefaultResponse()
				.setStatus(insertCount > 0)
				.setMessage("Total user notification in current batch: " + registrationKeys.size() + " | inserted: " + insertCount);

		return lResponse;

	}

	public boolean updateUserNotificationStatus(int userId, JSONObject requestObject, boolean shouldIncludeDeviceId)
			throws SQLException {

		checkAndConnect();

		String deviceId = requestObject.optString("deviceId", Constants.NA);

		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(ColumnsNFields.USDR_NOTIFICATION_INFO_TABLE);
		sb.append(" set ");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.HAS_READ.value + "=?,");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.READ_DATETIME.value + "=?,");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.DEVICE_TYPE.value + "=?,");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.DEVICE_MODEL.value + "=?,");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.APP_VERSION.value + "=?,");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.OS_VERSION.value + "=?");
		sb.append(" where ");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID + "=?");
		sb.append(" and ");
		sb.append(ColumnsNFields.UserNotificationInfoColumn.NOTIFICATION_ID.value + "=?");

		if (shouldIncludeDeviceId) {
			sb.append(" and ");
			sb.append(ColumnsNFields.UserNotificationInfoColumn.DEVICE_ID.value + "=?");
		}

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setInt(1, requestObject.optInt("hasRead", 0));
		preparedStatement.setString(2, DateTimeUtils.getCurrentDateTimeInIST());
		preparedStatement.setString(3, requestObject.optString("deviceType", Constants.NA));

		String modelName = requestObject.optString("deviceModel", Constants.NA);
		if (modelName.length() > 128)
			modelName = modelName.substring(0, 128);
		preparedStatement.setString(4, modelName);

		preparedStatement.setString(5, requestObject.optString("appVersion", Constants.NA));
		preparedStatement.setString(6, requestObject.optString("osVersion", Constants.NA));
		preparedStatement.setInt(7, userId);
		preparedStatement.setInt(8, requestObject.optInt("notificationId", -1));

		if (shouldIncludeDeviceId)
			preparedStatement.setString(9, deviceId);
		
		
		LoggerUtils.log("NotificationUpdated"+preparedStatement);

		return preparedStatement.executeUpdate() > 0;

	}
	
	
	public ArrayList<RmNotification> getUserNotifications(int userId, NotificationFetchType fetchType, String datetime)
			throws SQLException {

		checkAndConnect();

		StringBuilder sb = new StringBuilder();

		sb.append(
				" SELECT distinct(un.notification_id), max(un.read_datetime) read_datetime, un.user_id, un.dynamic_title, un.dynamic_message, n.title, n.message, n.big_message,");
		sb.append(" n.image_url, n.web_url, n.data, n.audience_type, n.audience_group, n.priority, n.platform,");
		sb.append(" n.kind, n.on_click_action,  n.screen_to_open, n.schedule_type, n.schedule_datetime");
		sb.append(" FROM RMManagementDB.user_notification_info un");
		sb.append(" left join (SELECT * FROM RMManagementDB.notification where is_scheduled = 1");
		sb.append(" and schedule_datetime ");
		sb.append(fetchType == NotificationFetchType.TOP ? ">" : "<");
		sb.append(" ?) n on n.id = un.notification_id");

		sb.append(" where user_id = ?");
		sb.append(" and n.schedule_datetime ");
		sb.append(fetchType == NotificationFetchType.TOP ? ">" : "<");
		sb.append(" ? and n.is_valid = true group by notification_id order by n.schedule_datetime desc limit 25");

		preparedStatement = connection.prepareStatement(sb.toString());
		preparedStatement.setString(1, datetime);
		preparedStatement.setInt(2, userId);
		preparedStatement.setString(3, datetime);

		ArrayList<RmNotification> notifications = new ArrayList<>();

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {
			do {

				RmNotification notification = new RmNotification();

				notification.id = resultSet.getInt("notification_id");
				notification.title = resultSet.getString("title");
				notification.message = resultSet.getString("message");
				notification.bigMessage = resultSet.getString("big_message");

				String dynamicMessage = resultSet.getString("dynamic_message");
				if (null != dynamicMessage) {
					notification.message = dynamicMessage;
					notification.bigMessage = dynamicMessage;
				}
				
				String dynamicTitle = resultSet.getString("dynamic_title");
				if (null != dynamicTitle)
					notification.title = dynamicTitle;

				notification.imageUrl = resultSet.getString("image_url");
				notification.datetime = resultSet.getString("schedule_datetime");
				notification.webUrl = resultSet.getString("web_url");

				String scheduleType = resultSet.getString("schedule_type");
				if (null != scheduleType)
					notification.shouldSchedule = !scheduleType.equalsIgnoreCase("now");

				notification.audienceType = resultSet.getString("audience_type");

				notification.audienceGroup = resultSet.getString("audience_group");

				notification.onClickAction = resultSet.getString("on_click_action");
				notification.screenToOpen = resultSet.getString("screen_to_open");

				notification.data = resultSet.getString("data");

				notification.priority = resultSet.getString("priority");
				notification.kind = resultSet.getString("kind");

				String readDatetime = resultSet.getString("read_datetime");
				notification.hasRead = null != readDatetime && !readDatetime.equals(Constants.NA);

				notifications.add(notification);

			} while (resultSet.next());
		}

		return notifications;

	}


//	public int getUserNotificationCount(int userId) throws SQLException {
//
//		checkAndConnect();
//
//		StringBuilder sb = new StringBuilder();
//
//		sb.append("Select count(*) notificationCount from ");
//		sb.append(ColumnsNFields.USDR_NOTIFICATION_INFO_TABLE);
//		sb.append(" where ");
//		sb.append(ColumnsNFields.UserNotificationInfoColumn.USER_ID);
//		sb.append(" = " + userId);
//		sb.append(" and !has_read");
//
//		preparedStatement = connection.prepareStatement(sb.toString());
//		LoggerUtils.log("NotificationQuery"+preparedStatement);
//
//	
//
//		resultSet = preparedStatement.executeQuery();
//
//		if (null != resultSet && resultSet.first()) {
//					return resultSet.getInt("notificationCount");
//
//		}
//		return 0;
//	}

	public int getUserNotificationCount(int userId) throws SQLException {

		checkAndConnect();

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT distinct(un.notification_id), max(un.read_datetime) read_datetime, un.user_id, n.id");
		sb.append(" FROM RMManagementDB.user_notification_info un");
		sb.append(
				" left join (SELECT id, is_valid FROM RMManagementDB.notification where is_scheduled = 1) n on n.id = un.notification_id");
		sb.append(" where user_id = ? and n.is_valid = true group by notification_id");

		preparedStatement = connection.prepareStatement(sb.toString());
		preparedStatement.setInt(1, userId);

		resultSet = preparedStatement.executeQuery();
		
		int unreadCount = 0;

		if (null != resultSet && resultSet.first()) {
			do {

				String readDatetime = resultSet.getString("read_datetime");
				if (null == readDatetime) unreadCount++; 


			} while (resultSet.next());
		}

		return unreadCount;

	}

	public ArrayList<UserNotificationKey> getUserNotificationKeysByEmail(String emails) throws SQLException {

		checkAndConnect();

		String query = "select u.id, u.first_name, si.fcm_key "
				+ " from RMManagementDB.user u "
				+ " left join (select user_id, fcm_key from RMManagementDB.user_secondary_info) si on si.user_id = u.id "
				+ " where email in ('" + emails + "')";

		preparedStatement = connection.prepareStatement(query);
		LoggerUtils.log("getUserbyEmailQuery" + query);

		resultSet = preparedStatement.executeQuery();

		ArrayList<UserNotificationKey> userNotificationKeys = new ArrayList<>();

		if (null != resultSet && resultSet.first()) {
			do {

				UserNotificationKey unKey = new UserNotificationKey();

				String fcmKeyArrayString = resultSet.getString(ColumnsNFields.SecondaryInfoColumn.FCM_KEY.value);
				if (null != fcmKeyArrayString && fcmKeyArrayString.startsWith("[")) {

					User user = new User();

					user.id = resultSet.getInt(ColumnsNFields.UserColumn.ID.value);
					user.firstName = resultSet.getString(ColumnsNFields.UserColumn.FIRST_NAME.value);
					//user.email = resultSet.getString(ColumnsNFields.UserColumn.EMAIL.value);

					unKey.user = user;

					ArrayList<RegistrationKey> keys = new ArrayList<>();

					JSONArray tokenJsonArray = new JSONArray(fcmKeyArrayString);
					for (int i = 0; i < tokenJsonArray.length(); i++) {
						keys.add(new RegistrationKey(user.id, tokenJsonArray.getJSONObject(i)));
					}

					unKey.registerationKeys = keys;

					userNotificationKeys.add(unKey);

				}

			} while (resultSet.next());
		}

		return userNotificationKeys;

	}


}