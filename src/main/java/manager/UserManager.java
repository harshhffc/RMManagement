package manager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import databasehelper.RMDatabaseHelper;
import models.LeaderBoardItem;
import models.LeaderboardFilter;
import models.SFApplicant;
import models.SFCoApplicant;
import models.ScoreWeightage;
import models.User;
import models.UserLocation;
import totalkyc.DocumentKYCHelper;
import utils.BasicUtils;
import utils.Constants;
import utils.Constants.Actions;
import utils.Constants.Errors;
import utils.DatabaseHelper;
import utils.DateTimeUtils;
import utils.DateTimeUtils.DateTimeFormat;
import utils.DateTimeUtils.DateTimeZone;
import utils.DateTimeUtils.Time;
import utils.LeaderboardUtils;
import utils.LocalResponse;
import utils.LoggerUtils;
import utils.OneResponse;
import utils.ProptertyUtils;
import utils.ProptertyUtils.Keys;

public class UserManager {

	private static final String UPLOAD_FILE_SERVER = "/var/www/images/profile_picture/";
	private static final String UPLOAD_FILE_LOCAL_SERVER = "/Users/appledeveloper/var/www/images/profile_picture/";
	private final HFOManager hfoManager;

	public UserManager() {
		hfoManager = new HFOManager();
	}

	public boolean verifySource(String sourceCode) {

		try {

			if (BasicUtils.getTheKey(sourceCode).equals(ProptertyUtils.getValurForKey(Keys.KEY_TO_THE_SOUCE)))
				return true;

		} catch (Exception e) {
			System.out.println("Error while verifying source code: " + e.toString());
			e.printStackTrace();
		}

		return false;
	}

	public boolean verifyMyCrown(String sourceCode) {

		try {

			if (BasicUtils.getTheKey(sourceCode).equals(ProptertyUtils.getValurForKey(Keys.KEY_TO_THE_CRON)))
				return true;

		} catch (Exception e) {
			System.out.println("Error while verifying source code: " + e.toString());
			e.printStackTrace();
		}

		return false;
	}

	public boolean verifyUser(int userId, String passcode) {

		if (!passcode.equals(Constants.NA)) {
			DatabaseHelper dbHelper = new DatabaseHelper(userId);

			String dPasscode = "";
			try {
				dPasscode = dbHelper.getUserSessionPasscode(userId);
				dbHelper.close();
			} catch (Exception e) {
				System.out.println("Error while verifying user: " + e.toString());
				e.printStackTrace();
				dbHelper.close();
			}
			if (dPasscode.equals(passcode))
				return true;
		}

		return false;
	}

	public JSONObject addUserLocationInfo(String body) throws Exception {

		UserLocation userLocation = new UserLocation(new JSONObject(body));

		DatabaseHelper dbHelper = new DatabaseHelper();
		try {
			boolean status = dbHelper.insertUserLocationData(userLocation);
			dbHelper.close();
			if (status)
				return BasicUtils.getSuccessTemplateObject();
			else
				return BasicUtils.getFailureTemplateObject();
		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject addUserInfo(String body, String ipAddress) throws Exception {

		JSONObject requestJson = new JSONObject(body);
		User user = new User(requestJson);

		DatabaseHelper dbHelper = new DatabaseHelper();

		try {

			boolean status = dbHelper.insertOrUpdateUserInfo(user);

			if (status) {

				user = dbHelper.getUserBySFUserId(user.sfUserId);

				if (null != user) {

					dbHelper.addUpdateUserSecondaryInfo(user,
							requestJson.optString(Constants.APP_VERISON, Constants.NA));
					dbHelper.addLoginInfo(user.id, requestJson, ipAddress);
					dbHelper.close();

					JSONObject responseJson = BasicUtils.getSuccessTemplateObject();
					responseJson.put("user", user.toJson());
					return responseJson;

				}

				dbHelper.close();
				return BasicUtils.getFailureTemplateObject();

			} else {

				dbHelper.close();
				return BasicUtils.getFailureTemplateObject();

			}

		} catch (Exception e) {

			dbHelper.close();
			throw e;

		}

	}

	public JSONObject getAvailableDocuments(String body) throws Exception {

		DatabaseHelper dbHelper = new DatabaseHelper();
		try {
			JSONObject requestJson = new JSONObject(body);
			JSONObject responseJson = dbHelper
					.getAvailableDocuments(requestJson.optString(DocumentKYCHelper.KEY_MOBILE_NUMBER, Constants.NA));
			dbHelper.close();
			if (null != responseJson) {
				JSONObject finalJson = BasicUtils.getSuccessTemplateObject();
				finalJson.put(DocumentKYCHelper.KEY_RESPONSE, finalJson);
				return finalJson;
			} else
				return BasicUtils.getFailureTemplateObject();
		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	@Deprecated
	public class LeaderBoardTask implements Runnable {

		private String body;
		private JSONObject response = BasicUtils.getFailureTemplateObject();

		public LeaderBoardTask(String body) {
			this.body = body;
		}

		@Override
		public void run() {
			try {
				response = getLeaderBoard(body);
			} catch (Exception e) {
				LoggerUtils.log("Error while getting leaderboard: " + e.getMessage());
				e.printStackTrace();
				response = BasicUtils.getFailureTemplateObject();
			}
		}

		public JSONObject getResponse() {
			return response;
		}

	}

	private JSONObject getLeaderBoard(String body) throws Exception {

		RMDatabaseHelper dbHelper = new RMDatabaseHelper();

		try {
			JSONObject requestJson = new JSONObject(body);
			LeaderboardFilter filter = null;

			JSONObject filterObject = requestJson.optJSONObject("filter");
			if (null != filterObject && filterObject.has("nameValuePairs"))
				filterObject = filterObject.optJSONObject("nameValuePairs");

			if (null != filterObject)
				filter = new LeaderboardFilter(filterObject);

			int limit = requestJson.optInt("limit", 10);

			ArrayList<LeaderBoardItem> leaders = getLeaders(dbHelper, filter);
			ScoreWeightage weightage = dbHelper.getScoreWeigtage(filter);
			dbHelper.close();

			if (null != leaders) {

				JSONObject finalJson = BasicUtils.getSuccessTemplateObject();
				JSONArray leaderArray = new JSONArray();

				if (leaders.size() > 0) {

					Collections.sort(leaders);

					double max = 250.0;

					if (null != filter && LeaderboardUtils.isTimeFilterApplicable(filter.time)) {
						if (null != weightage)
							max = (double) weightage.target;
					} else {
						max = (double) leaders.get(0).points;
					}

					int totalLeadersRequired = limit;

					if (totalLeadersRequired > leaders.size() || totalLeadersRequired < 1)
						totalLeadersRequired = leaders.size();

					for (int i = 0; i < totalLeadersRequired; i++) {

						LeaderBoardItem leader = leaders.get(i);

						leader.calculateRating(max);
						leaderArray.put(leader.toJson());

					}
				}

				finalJson.put("leaders", leaderArray);
				if (null != weightage)
					finalJson.put("scoreWeightage", weightage.toJson());
				return finalJson;
			} else
				return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	private class HashedLeaders {

		public ArrayList<LeaderBoardItem> leaders = new ArrayList<>();
		public String time = DateTimeUtils.getDateTime(DateTimeFormat.yyyy_MM, DateTimeZone.IST);

		HashedLeaders(ArrayList<LeaderBoardItem> leaders, String time) {
			this.leaders = leaders;
			this.time = time;
		}

	}

	private ArrayList<LeaderBoardItem> getLeaders(RMDatabaseHelper dbHelper, LeaderboardFilter filter)
			throws Exception {

		ArrayList<LeaderBoardItem> leaders = new ArrayList<>();

		if (null != filter && !LeaderboardUtils.isTimeFilterApplicable(filter.time)) {

			String currentYear = DateTimeUtils.getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST);
			String currentMonth = DateTimeUtils.getDateTime(DateTimeFormat.MM, DateTimeZone.IST);

			ArrayList<Time> timeList = Time.getYearMonthList(currentMonth);
			ArrayList<HashedLeaders> hashedLeaderList = new ArrayList<>();

			for (Time item : timeList) {

				String timeString = item.getYearMonthFormat(currentYear);
				ArrayList<LeaderBoardItem> tempLeaders = dbHelper.getLeaders(filter.region, timeString);
				hashedLeaderList.add(new HashedLeaders(tempLeaders, timeString));

			}

			if (hashedLeaderList.size() > 0) {

				int totalLeaderCount = hashedLeaderList.get(0).leaders.size();

				for (int i = 0; i < totalLeaderCount; i++) {

					LeaderBoardItem finalLeader = new LeaderBoardItem();

					for (HashedLeaders hLeaders : hashedLeaderList) {
						if (i < hLeaders.leaders.size())
							finalLeader.addValues(hLeaders.leaders.get(i));
					}

					leaders.add(finalLeader);

				}

			}

		} else {

			String currentYear = DateTimeUtils.getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST);

			Time currentMonth = Time.getTimeByName(filter.time);

			leaders = dbHelper.getLeaders(filter.region, currentMonth.getYearMonthFormat(currentYear));

		}

		return leaders;

	}

	@Deprecated
	public class LeaderProfileTask implements Runnable {

		private JSONObject response = BasicUtils.getFailureTemplateObject();
		private int userId;
		private String body;

		public LeaderProfileTask(int userId, String body) {
			this.userId = userId;
			this.body = body;
		}

		@Override
		public void run() {
			try {
				response = getProfileForUser(userId, body);
			} catch (Exception e) {
				LoggerUtils.log("Error while getting leader profile: " + e.getMessage());
				e.printStackTrace();
				response = BasicUtils.getFailureTemplateObject();
			}
		}

		public JSONObject getResponse() {
			return response;
		}

	}

	private JSONObject getProfileForUser(int userId, String body) throws Exception {

		RMDatabaseHelper dbHelper = new RMDatabaseHelper();
		try {

			JSONObject requestJson = new JSONObject(body);

			LeaderboardFilter filter = null;
			JSONObject filterObject = requestJson.optJSONObject("filter");
			if (null != filterObject && filterObject.has("nameValuePairs"))
				filterObject = filterObject.optJSONObject("nameValuePairs");
			if (null != filterObject)
				filter = new LeaderboardFilter(filterObject);

			ArrayList<LeaderBoardItem> leaders = getLeaders(dbHelper, filter);
			ScoreWeightage weightage = dbHelper.getScoreWeigtage(filter);
			dbHelper.close();

			if (null != leaders && leaders.size() > 0) {

				Collections.sort(leaders);

				LeaderBoardItem userProfile = null;

				for (int i = 0; i < leaders.size(); i++) {
					if (leaders.get(i).rmUser.id == userId) {
						userProfile = leaders.get(i);
						userProfile.rank = i + 1;
						break;
					}
				}

				if (null != userProfile) {

					double max = 250.0;

					if (null != filter && (LeaderboardUtils.isTimeFilterApplicable(filter.time)
							|| LeaderboardUtils.isRegionFilterApplicable(filter.region))) {
						if (null != weightage)
							max = (double) weightage.target;
					} else {
						max = (double) leaders.get(0).points;
					}

					JSONObject finalJson = BasicUtils.getSuccessTemplateObject();

					userProfile.calculateRating(max);

					finalJson.put("profile", userProfile.toJson());
					if (null != weightage)
						finalJson.put("scoreWeightage", weightage.toJson());
					return finalJson;

				} else
					return BasicUtils.getFailureTemplateObject();

			} else
				return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject addUpdateApplicantInformation(int userId, String body) throws Exception {

		RMDatabaseHelper dbHelper = new RMDatabaseHelper();

		try {

			SFApplicant receivedApplicant = new SFApplicant(new JSONObject(body));

			SFApplicant fetchedApplicant = dbHelper
					.getApplicantInfoByMobileNumber(receivedApplicant.customerMobileNumber);

			boolean status = false;

			if (null == fetchedApplicant)
				status = dbHelper.insertApplicantInfo(receivedApplicant);
			else {
				receivedApplicant.id = fetchedApplicant.id;
				status = dbHelper.updateApplicantInfo(receivedApplicant);
			}

			dbHelper.close();

			if (status)
				return BasicUtils.getSuccessTemplateObject();
			return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject addUpdateCoApInformation(int userId, String body) throws Exception {

		RMDatabaseHelper dbHelper = new RMDatabaseHelper();

		try {

			SFCoApplicant receivedCoApplicant = new SFCoApplicant(new JSONObject(body));

			SFCoApplicant fetchedCoApplicant = dbHelper
					.getCoApInfoByMobileNumber(receivedCoApplicant.customerMobileNumber);

			boolean status = dbHelper.addUpdateCoApInfo(receivedCoApplicant, null == fetchedCoApplicant);

			dbHelper.close();

			if (status)
				return BasicUtils.getSuccessTemplateObject();
			return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	@Deprecated
	public JSONObject setProfileImage(int userId, String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);
		String fileName = bodyObject.getString("fileName");
		String base64String = bodyObject.getString("fileData");

		InputStream inputStream = new ByteArrayInputStream(Base64.decodeBase64(base64String.getBytes()));

		OutputStream outputStream = null;
		String qualifiedUploadFileName = userId + "-" + fileName;
		String qualifiedUploadFilePath = (Constants.IS_DB_IN_PRODUCTION ? UPLOAD_FILE_SERVER : UPLOAD_FILE_LOCAL_SERVER)
				+ qualifiedUploadFileName;

		boolean status = false;

		try {
			outputStream = new FileOutputStream(new File(qualifiedUploadFilePath));
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();

			status = true;

			LoggerUtils.log("=========== file saved successfully at: " + qualifiedUploadFilePath);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			outputStream.close();
		}

		if (status) {
			JSONObject json = new JSONObject();
			RMDatabaseHelper dbHelper = new RMDatabaseHelper();
			try {

				boolean success = dbHelper.addOrUpdateUserProfilePicture(userId, qualifiedUploadFileName);
				dbHelper.close();

				if (success) {
					json.put(Constants.STATUS, Constants.SUCCESS);
					json.put("imageUrl", qualifiedUploadFileName);
				} else {
					json.put(Constants.STATUS, Constants.FAILURE);
					json.put(Constants.MESSAGE, "Failed to add your profile picture. Please try again.");
					json.put("imageUrl", qualifiedUploadFileName);
				}

			} catch (Exception e) {
				dbHelper.close();
				LoggerUtils.log("Error: " + e.toString());
				e.printStackTrace();
				throw e;
			}

			return json;

		} else
			return BasicUtils.getFailureTemplateObject();

	}

	public Response addSitePhotograph(int userId, JSONObject bodyObject) throws Exception {

		DatabaseHelper dbHelper = new DatabaseHelper();
		User fetchedUser = dbHelper.getUserByUserId(userId);
		dbHelper.close();

		if (null == fetchedUser)
			return new OneResponse().getFailureResponse(new LocalResponse().setMessage("No user found for this id")
					.setAction(Actions.RETRY.value).toJson());

		LocalResponse spResponse = hfoManager.addSitePhotograph(bodyObject);

		if (!spResponse.isSuccess) {

			LoggerUtils.log("addSitePhotograph - Failed to add Site Photograph for userId " + userId);
			return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
					.setAction(Actions.RETRY.value).toJson());

		}

		LoggerUtils.log("addSitePhotograph - Successfully to added Site Photograph for userId " + userId);

		JSONObject hfoJson = new JSONObject(spResponse.message);

		return new OneResponse().getSuccessResponse(hfoJson);

	}

	public Response getSitePhotographList(int userId) throws Exception {

		DatabaseHelper dbHelper = new DatabaseHelper();
		User fetchedUser = dbHelper.getUserByUserId(userId);
		dbHelper.close();

		if (null == fetchedUser)
			return new OneResponse().getFailureResponse(new LocalResponse().setMessage("No user found for this id")
					.setAction(Actions.RETRY.value).toJson());

		JSONObject requestObject = new JSONObject();
		requestObject.put(Constants.USER_ID, fetchedUser.id);
		requestObject.put("source", Constants.SITE_PHOTO_SOURCE);
		LocalResponse spResponse = hfoManager.getSitePhotographList(requestObject);

		if (!spResponse.isSuccess) {

			LoggerUtils.log("getSitePhotographList - Failed to get Site Photograph for userId " + userId);
			return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
					.setAction(Actions.RETRY.value).toJson());

		}

		LoggerUtils.log("getSitePhotographList - Success for get Site Photograph for userId " + userId);

		JSONObject hfoJson = new JSONObject(spResponse.message);

		return new OneResponse().getSuccessResponse(hfoJson);

	}

}

/*
 * private JSONObject getLeaderBoard(String body) throws Exception {
 * 
 * RMDatabaseHelper dbHelper = new RMDatabaseHelper(); try { JSONObject
 * requestJson = new JSONObject(body); LeaderboardFilter filter = null;
 * 
 * JSONObject filterObject = requestJson.optJSONObject("filter"); if (null !=
 * filterObject && filterObject.has("nameValuePairs")) filterObject =
 * filterObject.optJSONObject("nameValuePairs");
 * 
 * if (null != filterObject) filter = new LeaderboardFilter(filterObject);
 * 
 * ScoreWeightage weightage = dbHelper.getScoreWeigtage(filter);
 * 
 * int limit = requestJson.optInt("limit", 10);
 * 
 * ArrayList<LeaderBoardItem> leaders = dbHelper.getLeaderBoard(filter,
 * weightage, -1); dbHelper.close();
 * 
 * if (null != leaders) {
 * 
 * JSONObject finalJson = BasicUtils.getSuccessTemplateObject(); JSONArray
 * leaderArray = new JSONArray();
 * 
 * if (leaders.size() > 0) {
 * 
 * Collections.sort(leaders);
 * 
 * double max = 250.0;
 * 
 * if (null != filter && (LeaderboardUtils.isTimeFilterApplicable(filter.time)
 * || LeaderboardUtils.isRegionFilterApplicable(filter.region))) { if(null !=
 * weightage) max = (double) weightage.target; } else { max = (double)
 * leaders.get(0).points; }
 * 
 * int totalLeadersRequired = limit;
 * 
 * if (totalLeadersRequired > leaders.size() || totalLeadersRequired < 1)
 * totalLeadersRequired = leaders.size();
 * 
 * for (int i = 0; i < totalLeadersRequired; i++) {
 * 
 * LeaderBoardItem leader = leaders.get(i);
 * 
 * leader.calculateRating(max); leaderArray.put(leader.toJson());
 * 
 * } }
 * 
 * finalJson.put("leaders", leaderArray); if (null != weightage)
 * finalJson.put("scoreWeightage", weightage.toJson()); return finalJson; } else
 * return BasicUtils.getFailureTemplateObject();
 * 
 * } catch (Exception e) { dbHelper.close(); throw e; }
 * 
 * }
 */