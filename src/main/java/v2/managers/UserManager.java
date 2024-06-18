package v2.managers;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dao.CallDTO;
import manager.HFOManager;
import manager.PaymentManager;
import manager.PaymentManager.PaymentStatus;
import models.DefaultResponse;
import models.FetchDocument;
import models.LeaderBoardItem;
import models.LeaderboardFilter;
import models.PaymentInfo;
import models.SFApplicant;
import models.SFCoApplicant;
import models.SFCustomer;
import models.SFTask;
import models.ScoreWeightage;
import models.User;
import models.UtilityBill;
import models.notification.RmNotification;
import networking.HFOSpringNetworkingClient;
import networking.HFOSpringNetworkingClient.Endpoints;
import totalkyc.DocumentKYCHelper;
import totalkyc.DocumentKYCHelper.UtilityBillType;
import utils.BasicUtils;
import utils.Constants;
import utils.Constants.Actions;
import utils.Constants.ApplicantSource;
import utils.Constants.ApplicantType;
import utils.Constants.Errors;
import utils.DatabaseHelper;
import utils.DateTimeUtils;
import utils.DateTimeUtils.DateTimeFormat;
import utils.DateTimeUtils.DateTimeZone;
import utils.DateTimeUtils.Time;
import utils.LeaderboardUtils;
import utils.LocalResponse;
import utils.LoggerUtils;
import utils.MailUtils;
import utils.NotificationUtils.NotificationFetchType;
import utils.OneResponse;
import utils.ProptertyUtils;
import utils.ProptertyUtils.Keys;
import v1.repository.PaymentInfoRepository;
import v1.repository.UserRepository;
import v2.dbhelpers.NotificationDatabaseHelper;
import v2.dbhelpers.RMDatabaseHelper;
import v2.managers.AmazonClient.S3BucketPath;

public class UserManager {

	private RMDatabaseHelper dbHelper;
	private final HFOManager hfoManager;
	private UserRepository userRepository = null;
	private PaymentInfoRepository paymentInfoRepository = null;
	private Gson gson;
	private HFOSpringNetworkingClient _hfoSpringNetworkingClient = null;

	public UserManager() {
		dbHelper = new RMDatabaseHelper();
		hfoManager = new HFOManager();
		gson = new Gson();
	}

	private UserRepository userRepo() {
		if (null == userRepository)
			userRepository = new UserRepository();
		return userRepository;
	}

	private PaymentInfoRepository paymentInfoRepo() {
		if (null == paymentInfoRepository)
			paymentInfoRepository = new PaymentInfoRepository();
		return paymentInfoRepository;
	}

	private void log(String value) {
		LoggerUtils.log(UserManager.class.getSimpleName() + "." + value);
	}

	private JSONObject closeResourceAndReturn(JSONObject json) {
		dbHelper.close();
		return json;
	}
	
	private HFOSpringNetworkingClient hfoSpringNetworkingClient() throws Exception {
		if (null == _hfoSpringNetworkingClient) {
			_hfoSpringNetworkingClient = new HFOSpringNetworkingClient();
		}
		return _hfoSpringNetworkingClient;
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

	public boolean verifyMyCrown(String sourceCode, String ipAddress) {

		try {

			if (BasicUtils.getTheKey(sourceCode).equals(ProptertyUtils.getValurForKey(Keys.KEY_TO_THE_CRON))) {

				if (dbHelper.verifyCrownIpAddress(ipAddress)) {
					dbHelper.close();
					return true;
				} else {
					dbHelper.close();
					return false;
				}

			} else
				return false;

		} catch (Exception e) {
			dbHelper.close();
			System.out.println("Error while verifying crown: " + e.toString());
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

		try {

			JSONObject requestJson = new JSONObject(body);

			LeaderboardFilter filter = null;
			JSONObject filterObject = requestJson.optJSONObject("filter");
			if (null != filterObject && filterObject.has("nameValuePairs"))
				filterObject = filterObject.optJSONObject("nameValuePairs");
			if (null != filterObject)
				filter = new LeaderboardFilter(filterObject);

			ArrayList<LeaderBoardItem> leaders = getLeaderProfile(filter, userId);
			ScoreWeightage weightage = dbHelper.getScoreWeigtage(filter);
			dbHelper.close();

			if (null != leaders && leaders.size() > 0) {

				JSONObject finalJson = BasicUtils.getSuccessTemplateObject();
				LeaderBoardItem userProfile = null;

				if (leaders.size() == 1) {

					userProfile = leaders.get(0);

				} else {

					Collections.sort(leaders);

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

						userProfile.calculateRating(max);

					} else
						return BasicUtils.getFailureTemplateObject();

				}

				finalJson.put("profile", userProfile.toJson());
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

	private ArrayList<LeaderBoardItem> getLeaderProfile(LeaderboardFilter filter, int userId) throws Exception {

		ArrayList<LeaderBoardItem> leaders = new ArrayList<>();

		if (null != filter && !LeaderboardUtils.isTimeFilterApplicable(filter.time)) {

			LoggerUtils.log("Time filter is All, getting data from LeaderboardHistory table.");
			leaders = dbHelper.getLeadersFromHistory(filter.region, filter.year, true);

		} else {

			String currentYear = filter.year;
			if (null == currentYear || currentYear.equals(Constants.NA))
				currentYear = DateTimeUtils.getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST);

			Time systemTime = Time.getTimeByCodeString(DateTimeUtils.getDateTime(DateTimeFormat.MM, DateTimeZone.IST));
			Time requestTime = Time.getTimeByName(filter.time);

			LoggerUtils.log("Time filter is Set, getting dynamic data for time: " + requestTime.value + " | system: "
					+ systemTime.value);

			leaders = dbHelper.getLeaderProfileFromHistory(requestTime.getYearMonthFormat(currentYear), userId);

		}

		return leaders;

	}

	// ============================ Leaderboard
	// Implementation===============================================

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

		try {

			JSONObject requestJson = new JSONObject(body);
			LeaderboardFilter filter = null;

			JSONObject filterObject = requestJson.optJSONObject("filter");

			if (null != filterObject && filterObject.has("nameValuePairs"))
				filterObject = filterObject.optJSONObject("nameValuePairs");

			if (null != filterObject)
				filter = new LeaderboardFilter(filterObject);

			int limit = requestJson.optInt("limit", 10);
			ArrayList<LeaderBoardItem> leaders = getLeadersList(filter);
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
						leader.rank = i + 1;
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

	/*
	 * private class HashedLeaders {
	 * 
	 * public ArrayList<LeaderBoardItem> leaders = new ArrayList<>(); public String
	 * time = DateTimeUtils.getDateTime(DateTimeFormat.yyyy_MM, DateTimeZone.IST);
	 * 
	 * HashedLeaders(ArrayList<LeaderBoardItem> leaders, String time) { this.leaders
	 * = leaders; this.time = time; }
	 * 
	 * }
	 */

	private ArrayList<LeaderBoardItem> getLeadersList(LeaderboardFilter filter) throws Exception {

		ArrayList<LeaderBoardItem> leaders = new ArrayList<>();

		if (null != filter && !LeaderboardUtils.isTimeFilterApplicable(filter.time)) {

			leaders = dbHelper.getLeadersFromHistory(filter.region, filter.year, true);

		} else {

			String currentYear = filter.year;
			if (null == currentYear || currentYear.equals(Constants.NA))
				currentYear = DateTimeUtils.getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST);

			Time requestTime = Time.getTimeByName(filter.time);
			leaders = dbHelper.getLeadersFromHistory(filter.region, requestTime.getYearMonthFormat(currentYear), false);

		}

		return leaders;

	}

	public JSONObject addUpdateApplicantInformation(int userId, String body) throws Exception {

		try {

			SFApplicant receivedApplicant = new SFApplicant(new JSONObject(body));

			SFApplicant fetchedApplicant = dbHelper
					.getApplicantInfoByMobileNumber(receivedApplicant.customerMobileNumber);

			boolean status = false;

			if (null == fetchedApplicant)
				status = dbHelper.insertApplicantInfo(receivedApplicant, ApplicantSource.RM_PRO.value);
			else
				status = dbHelper.updateApplicantInfo(receivedApplicant);

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

		try {

			SFCoApplicant receivedCoApplicant = new SFCoApplicant(new JSONObject(body));

			SFCoApplicant fetchedCoApplicant = dbHelper
					.getCoApInfoByMobileNumber(receivedCoApplicant.customerMobileNumber);

			boolean status = dbHelper.addUpdateCoApInfo(receivedCoApplicant, null == fetchedCoApplicant,
					ApplicantSource.RM_PRO.value);

			dbHelper.close();

			if (status)
				return BasicUtils.getSuccessTemplateObject();
			return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject setProfileImage(int userId, String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);
		AmazonClient amazonClient = new AmazonClient();

		String fileName = userId + "-" + bodyObject.getString("fileName");
		String fileData = bodyObject.getString("fileData");

		boolean status = amazonClient.uploadImage(fileName, fileData, S3BucketPath.PROFILE_IMAGES);

		if (status) {

			JSONObject json = new JSONObject();

			try {

				boolean success = dbHelper.addOrUpdateUserProfilePicture(userId, fileName);
				dbHelper.close();

				if (success) {
					json.put(Constants.STATUS, Constants.SUCCESS);
					json.put("imageUrl", fileName);
				} else {
					json.put(Constants.STATUS, Constants.FAILURE);
					json.put(Constants.MESSAGE, "Failed to add your profile picture. Please try again.");
					json.put("imageUrl", fileName);
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

	// ===================== New Code for leaderboard History
	// =================================

	public class LeaderBoardHistory implements Runnable {

		private JSONObject response = BasicUtils.getFailureTemplateObject();

		public LeaderBoardHistory() {

		}

		@Override
		public void run() {
			try {
				response = insertAllLeaderBoardHistoryData();
			} catch (Exception e) {
				LoggerUtils.log("Error while storing leaderboard history: " + e.getMessage());
				e.printStackTrace();
				response = BasicUtils.getFailureTemplateObject();
			}
		}

		public JSONObject getResponse() {
			return response;
		}
	}

	private JSONObject insertAllLeaderBoardHistoryData() throws Exception {

		try {

			String currentYear = DateTimeUtils.getDateTime(DateTimeFormat.yyyy, DateTimeZone.IST);
			String currentMonth = DateTimeUtils.getDateTime(DateTimeFormat.MM, DateTimeZone.IST);

			ArrayList<Time> timeList = Time.getYearMonthList(currentMonth);
			int insertedMonthCount = 0;

			for (Time item : timeList) {

				String timeString = item.getYearMonthFormat(currentYear);

				LeaderboardFilter filter = new LeaderboardFilter();
				filter.time = item.value;
				ScoreWeightage weightage = dbHelper.getScoreWeigtage(filter);
				ArrayList<LeaderBoardItem> tempLeaders = dbHelper.getLeadersLiveData(filter.region, timeString,
						weightage);

				if (null != tempLeaders && tempLeaders.size() > 0) {

					Collections.sort(tempLeaders);

					double max = 250.0;

					if (null != filter && LeaderboardUtils.isTimeFilterApplicable(filter.time)) {
						if (null != weightage)
							max = (double) weightage.target;
					} else {
						max = (double) tempLeaders.get(0).points;
					}

					for (int i = 0; i < tempLeaders.size(); i++) {
						LeaderBoardItem leader = tempLeaders.get(i);
						leader.calculateRating(max);
						leader.rank = i + 1;
					}

					try {
						boolean status = dbHelper.insertLeaderboardHistory(tempLeaders, timeString, (int) max);
						if (status)
							insertedMonthCount++;
					} catch (Exception e) {
						LoggerUtils.log("error while inserting data: " + e.getMessage());
					}

				}

			}

			dbHelper.close();

			JSONObject finalJson = BasicUtils.getSuccessTemplateObject();

			DefaultResponse oResponse = new DefaultResponse();
			oResponse.isSuccess = true;
			oResponse.message = "Leaderboard history has been successfully stored. All success month count: "
					+ insertedMonthCount;
			finalJson = oResponse.toJson();

			return finalJson;

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject insertOrUpdateLeaderBoardHistory() throws Exception {

		try {

			String currentMonth = DateTimeUtils.getDateTime(DateTimeFormat.MM, DateTimeZone.IST);
			String yearMonth = DateTimeUtils.getDateTime(DateTimeFormat.yyyy_MM, DateTimeZone.IST);

			Time thisMonthTimeObject = Time.getTimeByCodeString(currentMonth);
			LeaderboardFilter filter = new LeaderboardFilter();
			filter.time = thisMonthTimeObject.value;

			ScoreWeightage weightage = dbHelper.getScoreWeigtage(filter);
			ArrayList<LeaderBoardItem> tempLeaders = dbHelper.getLeadersLiveData(filter.region, yearMonth, weightage);

			boolean isSuccess = false;

			if (null != tempLeaders && tempLeaders.size() > 0) {

				Collections.sort(tempLeaders);

				double max = 250.0;

				if (null != filter && LeaderboardUtils.isTimeFilterApplicable(filter.time)) {
					if (null != weightage)
						max = (double) weightage.target;
				} else {
					max = (double) tempLeaders.get(0).points;
				}

				for (int i = 0; i < tempLeaders.size(); i++) {
					LeaderBoardItem leader = tempLeaders.get(i);
					leader.calculateRating(max);
					leader.rank = i + 1;
				}

				isSuccess = dbHelper.insertOrUpdateLeaderBoardHistory(tempLeaders, (int) max, yearMonth);
			}

			dbHelper.close();

			JSONObject finalJson = BasicUtils.getSuccessTemplateObject();

			if (Constants.IS_STRICT_PROD_PROCESS_ACTIVE) {
				MailUtils.getInstance().sendDefaultMail("Leaderboard history data update",
						"Leaderboard history has been successfully updated at "
								+ DateTimeUtils.getCurrentDateTimeInIST(),
						"sanjay.jaiswar@homefirstindia.com", "ranan.rodrigues@homefirstindia.com");
			}

			DefaultResponse oResponse = new DefaultResponse();
			oResponse.isSuccess = isSuccess;
			oResponse.message = (isSuccess ? "Leaderboard history has been successfully updated."
					: "Failed to update this month leaderboard history data.");
			finalJson = oResponse.toJson();

			return finalJson;

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject addNotificationToken(int userId, String body) throws Exception {

		DefaultResponse response = new DefaultResponse();

		try {

			JSONObject data = new JSONObject(body);
			boolean status = dbHelper.addNotificationToken(userId, data);
			dbHelper.close();

			if (status) {
				response.isSuccess = true;
				response.message = "Notification token updated successfully.";
			}

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

		return response.toJson();
	}
	
	public JSONObject addApnsToken(int userId, String body) throws Exception {

		RMDatabaseHelper dbHelper = new RMDatabaseHelper();
		LocalResponse response = new LocalResponse();

		try {

			JSONObject data = new JSONObject(body);
			boolean status = dbHelper.addApnsToken(userId, data);
			dbHelper.close();

			if (status) {
				response.isSuccess = true;
				response.message = "APNS token updated successfully.";
			}

		} catch (Exception e) {
			dbHelper.close();
			LoggerUtils.log("Error: " + e.toString());
			e.printStackTrace();
			throw e;
		}

		return response.toJson();
	}
	

	public class UserDashboardTask implements Runnable {

		private JSONObject response = BasicUtils.getFailureTemplateObject();
		private int userId;
		private String body;

		public UserDashboardTask(int userId, String body) {
			this.userId = userId;
			this.body = body;
		}

		@Override
		public void run() {
			try {
				response = getDashboardForUser(userId, body);
			} catch (Exception e) {
				LoggerUtils.log("Error while getting dashboard data: " + e.getMessage());
				e.printStackTrace();
				response = BasicUtils.getFailureTemplateObject();
			}
		}

		public JSONObject getResponse() {
			return response;
		}

	}

	private JSONObject getDashboardForUser(int userId, String body) throws Exception {

		SalesForceManager sfHelper = new SalesForceManager();

		try {

			String dateForBoth = DateTimeUtils.getDateTime(-1, DateTimeFormat.yyyy_MM_dd, DateTimeZone.IST);

			ArrayList<LeaderBoardItem> leaders = new ArrayList<>();
			Boolean dashboardFlag = false;
			JSONObject sfLeadCount = new JSONObject();

			try {

				dashboardFlag = dbHelper.getFlag("RmDashboard");
				if (!dashboardFlag) {
					dbHelper.close();
					return BasicUtils.getFailureTemplateObject();
				}

				leaders = dbHelper.getLeadersLiveDataForDashboard(userId, dateForBoth);
				String ownerId = dbHelper.getOwnerIdById(userId);
				sfLeadCount = sfHelper.getUserLeadCount(ownerId, dateForBoth);
				dbHelper.close();

			} catch (Exception e) {
				dbHelper.close();
			}

			LeaderBoardItem userProfile = null;

			userProfile = leaders.get(0);
			if (null != userProfile) {

				LoggerUtils.log("DB data" + userProfile.toJson().toString());
				int totalRmProLeadCount = userProfile.convertedApCount + userProfile.createdApCount;
				int totalSfLeadCount = sfLeadCount.getInt("sfLeadsCreated") + sfLeadCount.getInt("sfLeadsConverted");

				float leadAdoptionPer = 0;
				if (totalSfLeadCount > 0) {
					leadAdoptionPer = (float) (totalRmProLeadCount * 100 / totalSfLeadCount);
				}
				int totalLeadsConverted = userProfile.convertedApCount;// + sfLeadCount.getInt("sfLeadsConverted");
				int totalCoAppCount = userProfile.coApCount;

				String kycCount = userProfile.kycDocumentCount + " | " + (totalLeadsConverted + totalCoAppCount) * 2;
				String utilitiesCount = userProfile.utilityBillCount + " | "
						+ (totalLeadsConverted + totalCoAppCount) * 1;
				String paymentCount = userProfile.paymentCount + " | " + totalLeadsConverted * 1;
				String bankCount = (userProfile.itrvCount + userProfile.epfCount) + " | "
						+ (totalLeadsConverted + totalCoAppCount) * 1;
				String othersCount = (userProfile.gstinCount + userProfile.vehicleRCCount) + " | "
						+ (totalLeadsConverted + totalCoAppCount) * 1;

				float kycPer = 0;
				if ((totalLeadsConverted + totalCoAppCount) > 0) {
					kycPer = (userProfile.kycDocumentCount * 100 / ((totalLeadsConverted + totalCoAppCount) * 2));
				}

				float utilitiesPer = 0;
				if ((totalLeadsConverted + totalCoAppCount) > 0) {
					utilitiesPer = ((userProfile.utilityBillCount * 100)
							/ ((totalLeadsConverted + totalCoAppCount) * 1));
				}

				float paymentPer = 0;
				if (totalLeadsConverted > 0) {
					paymentPer = (userProfile.paymentCount * 100 / (totalLeadsConverted * 1));
				}

				float bankPer = 0;
				if ((totalLeadsConverted + totalCoAppCount) > 0) {
					bankPer = ((userProfile.itrvCount + userProfile.epfCount) * 100
							/ ((totalLeadsConverted + totalCoAppCount) * 1));
				}

				float othersPer = 0;
				if ((totalLeadsConverted + totalCoAppCount) > 0) {
					othersPer = ((userProfile.gstinCount + userProfile.vehicleRCCount) * 100
							/ ((totalLeadsConverted + totalCoAppCount) * 1));
				}

				String kycTip = Constants.NA;
				int kycPerInt = (int) Math.round(kycPer);

				int kycCase = (kycPerInt == 0) ? 0
						: (1 <= kycPerInt && kycPerInt <= 50) ? 1
								: (51 <= kycPerInt && kycPerInt <= 80) ? 2 : (kycPerInt > 80) ? 3 : 4;
				switch (kycCase) {

				case 0:
					kycTip = "You have no KYC verifications done yesterday. "
							+ "You should start off by doing KYC verifications using RM Pro now!!";
					break;
				case 1:
					kycTip = "You completed only " + kycPerInt + "% KYCs. "
							+ "Your KYC verification count is very low. " + "You should increase this value from "
							+ kycPerInt + " to 100 " + "by doing 2 KYCs per primary or co applicant you add.";
					break;
				case 2:
					kycTip = "You completed " + kycPerInt + "% KYCs. " + "Your KYC count is average. "
							+ "You should increase this value from " + kycPerInt + " to 100 "
							+ "by doing 2 KYCs per primary or co applicant you add.";
					break;
				case 3:
					kycTip = "Amazing Job!! " + "Your KYC stats are above average. " + "Keep up the good work!!";
					break;

				default:
					kycTip = Constants.NA;
					break;

				}

				String utilityTip = Constants.NA;
				int utilityPerInt = Math.round(utilitiesPer);
				int utilityCase = (utilityPerInt == 0) ? 0
						: (1 <= utilityPerInt && utilityPerInt <= 50) ? 1
								: (51 <= utilityPerInt && utilityPerInt <= 80) ? 2 : (utilityPerInt > 80) ? 3 : 4;
				switch (utilityCase) {

				case 0:
					utilityTip = "You have no Utility Bill verifications done yesterday. "
							+ "You should start off by doing Utility Bil verifications using RM Pro now!!";
					break;
				case 1:
					utilityTip = "You completed only " + utilityPerInt + "% Utility Bill verifications. "
							+ "Your Utility Bill verification count is very low. "
							+ "You should increase this value from " + utilityPerInt + " to 100 "
							+ "by doing at least 1 verification per primary or co applicant you add.";
					break;
				case 2:
					utilityTip = "You completed " + utilityPerInt + "% Utility Bill verifications. "
							+ "Your Utility Bill verification count is average. "
							+ "You should increase this value from " + utilityPerInt + " to 100 "
							+ "by doing at least 1 verification per primary or co applicant you add.";
					break;
				case 3:
					utilityTip = "Amazing Job!! " + "Your Utility Bill verification stats are above average. "
							+ "Keep up the good work!!";
					break;
				default:
					utilityTip = Constants.NA;
					break;

				}

				String paymentTip = Constants.NA;
				int paymentPerInt = Math.round(paymentPer);
				int paymentCase = (paymentPerInt == 0) ? 0
						: (1 <= paymentPerInt && paymentPerInt <= 50) ? 1
								: (51 <= paymentPerInt && paymentPerInt <= 80) ? 2 : (paymentPerInt >= 80) ? 3 : 4;
				switch (paymentCase) {

				case 0:
					paymentTip = "You have no payments accepted yesterday. "
							+ "You should start off by accepting payments using RM Pro now!!";
					break;
				case 1:
					paymentTip = "You completed only " + paymentPerInt + "% payments. "
							+ "Your payment count is very low. " + "You should increase this value from "
							+ paymentPerInt + " to 100 " + "by accepting at least 1 payment per applicant you add.";
					break;
				case 2:
					paymentTip = "You completed " + paymentPerInt + "% payments. " + "Your payment count is average. "
							+ "You should increase this value from " + paymentPerInt + " to 100 "
							+ "by accepting at least 1 payment per applicant you add.";
					break;
				case 3:
					paymentTip = "Amazing Job!! " + "Your payment stats are above average. "
							+ "Keep up the good work!!";
					break;
				default:
					paymentTip = Constants.NA;
					break;

				}

				String bankTip = Constants.NA;
				int bankPerInt = Math.round(bankPer);

				int bankCase = (bankPerInt == 0) ? 0
						: (1 <= bankPerInt && bankPerInt <= 50) ? 1
								: (51 <= bankPerInt && bankPerInt <= 80) ? 2 : (bankPerInt >= 80) ? 3 : 4;
				switch (bankCase) {

				case 0:
					bankTip = "You have no Bank verifications yesterday. "
							+ "You should start off by doing ITR or EPF verifications using RM Pro now!!";
					break;
				case 1:
					bankTip = "You completed only " + bankPerInt + "% banking verifications. "
							+ "Your banking verification count is very low. " + "You should increase this value from "
							+ bankPerInt + " to 100 "
							+ "by verifying at least 1 banking detail (ITR or EPF) per applicant you add.";
					break;
				case 2:
					bankTip = "You completed " + bankPerInt + "% banking verifications. "
							+ "Your banking verification count is average. " + "You should increase this value from "
							+ bankPerInt + " to 100 "
							+ "by verifying at least 1 banking detail (ITR or EPF) per applicant you add.";
					break;
				case 3:
					bankTip = "Amazing Job!! " + "Your banking verification stats are above average. "
							+ "Keep up the good work!!";
					break;
				default:
					bankTip = Constants.NA;
					break;

				}

				String otherTip = Constants.NA;
				int othersPerInt = Math.round(othersPer);
				int othersCase = (othersPerInt == 0) ? 0
						: (1 <= othersPerInt && othersPerInt <= 50) ? 1
								: (51 <= othersPerInt && othersPerInt <= 80) ? 2 : (othersPerInt >= 80) ? 3 : 4;
				switch (othersCase) {

				case 0:
					otherTip = "You have no Other verifications yesterday. "
							+ "You should start off by doing verifications like GST, Vehical RC, etc using RM Pro now!!";
					break;
				case 1:
					otherTip = "You completed only " + othersPerInt + "% other verifications. "
							+ "Your verification count is very low. " + "You should increase this value from "
							+ othersPerInt + " to 100 "
							+ "by verifying at least 1 other detail(GST , Vehical RC, etc) per applicant you add.";
					break;
				case 2:
					otherTip = "You completed " + othersPerInt + "% verifications. "
							+ "Your verification count is average. " + "You should increase this value from "
							+ othersPerInt + " to 100 "
							+ "by verifying at least 1 other detail(GST, Vehical RC, etc) per applicant you add.";
					break;
				case 3:
					otherTip = "Amazing Job!! " + "Your other verification stats are above average. "
							+ "Keep up the good work!!";
					break;
				default:
					otherTip = Constants.NA;
					break;

				}

				JSONObject dashboardStats = new JSONObject();
				JSONObject finalJson = BasicUtils.getSuccessTemplateObject();
				finalJson.put("profile", userProfile.toJson());
				finalJson.put("sfLeadCounts", sfLeadCount);

				dashboardStats.put("totalRmProLeadCount", totalRmProLeadCount);
				dashboardStats.put("totalSfLeadCount", totalSfLeadCount);
				dashboardStats.put("leadAdoptionPer", leadAdoptionPer);
				dashboardStats.put("totalLeadsConverted", totalLeadsConverted);
				dashboardStats.put("totalCoAppCount", totalCoAppCount);

				dashboardStats.put("kycCount", kycCount);
				dashboardStats.put("utilitiesCount", utilitiesCount);
				dashboardStats.put("paymentCount", paymentCount);
				dashboardStats.put("bankCount", bankCount);
				dashboardStats.put("othersCount", othersCount);

				dashboardStats.put("kycPer", kycPer);
				dashboardStats.put("utilitiesPer", utilitiesPer);
				dashboardStats.put("paymentPer", paymentPer);
				dashboardStats.put("bankPer", bankPer);
				dashboardStats.put("othersPer", othersPer);

				dashboardStats.put("kycTip", kycTip);
				dashboardStats.put("utilitiesTip", utilityTip);
				dashboardStats.put("paymentTip", paymentTip);
				dashboardStats.put("bankTip", bankTip);
				dashboardStats.put("othersTip", otherTip);
				dashboardStats.put("dbFlag", dashboardFlag);

				finalJson.put("dashboardStats", dashboardStats);

				return finalJson;

			} else
				return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject addInstalledAppInfo(int userId, JSONObject requestObject) throws Exception {

		JSONArray appsArray = new JSONArray(requestObject.optString("data", "[]"));

		try {

			int storedAppCount = 0;

			for (int i = 0; i < appsArray.length(); i++) {

				JSONObject currentApp = appsArray.getJSONObject(i);

				try {
					storedAppCount += dbHelper.addAppsData(userId, currentApp);
				} catch (SQLException sqle) {
				}

			}

			dbHelper.close();

			LoggerUtils.log("Total Apps Added: " + storedAppCount);

			return BasicUtils.getSuccessTemplateObject();

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject addUpdateTaskAndActivity(int userId, String body) throws Exception {

		JSONObject requestBody = new JSONObject(body);

		SFTask sfTask = new SFTask(requestBody.optJSONObject(Constants.SF_TASK));

		try {
			sfTask = dbHelper.addUpdateTaskAndActivity(userId, sfTask);
			dbHelper.close();
		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

		JSONObject responseJson = BasicUtils.getFailureTemplateObject();

		if (null != sfTask) {
			responseJson = BasicUtils.getSuccessTemplateObject();
			responseJson.put(Constants.SF_TASK, sfTask.toJson());
		}

		return responseJson;

	}

	public JSONObject updateApplicantAndFetchVerifiedDetails(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		ApplicantType applicantType = ApplicantType.get(bodyObject.optString("applicantType", Constants.NA));
		if (null == applicantType)
			return closeResourceAndReturn(new DefaultResponse().setError("Invalid applicant type.").toJson());

		ApplicantSource applicantSource = ApplicantSource
				.get(bodyObject.optString("applicantSource", ApplicantSource.DIRECT_SF.value));
		if (null == applicantSource)
			return closeResourceAndReturn(new DefaultResponse().setError("Invalid applicant source.").toJson());

		JSONObject responseJson = new JSONObject();

		String applicantImageUrl = Constants.NA;
		ArrayList<FetchDocument> fetchDocuments = new ArrayList<FetchDocument>();

		try {

			if (applicantType == ApplicantType.PRIMARY) {

				SFApplicant receivedApplicant = new SFApplicant(bodyObject);
				SFApplicant existingApplicant = dbHelper
						.getApplicantInfoByMobileNumber(receivedApplicant.customerMobileNumber);

				if (null == existingApplicant) {

					if (dbHelper.insertApplicantInfo(receivedApplicant, applicantSource.value))
						LoggerUtils.log("Applicant details have been inserted successfully while fetching document.");
					else
						closeResourceAndReturn(new DefaultResponse().setError("Failed to insert applicant.").toJson());

				} else
					applicantImageUrl = existingApplicant.imageUrl;

				fetchDocuments = dbHelper.fetchDoc(receivedApplicant.customerMobileNumber,
						receivedApplicant.customerSecondaryMobileNumber);

			} else {

				SFCoApplicant receivedCoApplicant = new SFCoApplicant(bodyObject);
				SFCoApplicant existingCoApplicant = dbHelper
						.getCoApInfoByMobileNumber(receivedCoApplicant.customerMobileNumber);

				if (null == existingCoApplicant) {

					if (dbHelper.addUpdateCoApInfo(receivedCoApplicant, true, applicantSource.value))
						LoggerUtils
								.log("Co-Applicant details have been inserted successfully while fetching document.");
					else
						closeResourceAndReturn(
								new DefaultResponse().setError("Failed to insert co-applicant.").toJson());

				} else
					applicantImageUrl = existingCoApplicant.imageUrl;

				fetchDocuments = dbHelper.fetchDoc(receivedCoApplicant.customerMobileNumber,
						receivedCoApplicant.customerSecondaryMobileNumber);

			}

			JSONArray documentArray = new JSONArray();

			for (FetchDocument fetchDocument : fetchDocuments) {
				documentArray.put(fetchDocument.toJson());
			}

			responseJson = BasicUtils.getSuccessTemplateObject();
			responseJson.put("imageUrl",
					new AmazonClient().getFullUrl(applicantImageUrl, S3BucketPath.APPLICANT_IMAGES));
			responseJson.put("documents", documentArray);

			return closeResourceAndReturn(responseJson);

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject setApplicantProfilePicture(String body) throws Exception {

		JSONObject jsonObject = new JSONObject(body);

		ApplicantType applicantType = ApplicantType.get(jsonObject.optString("applicantType", Constants.NA));
		if (null == applicantType)
			return closeResourceAndReturn(new DefaultResponse().setError("Invalid applicant type.").toJson());

		JSONObject responseJson = new JSONObject();

		String mobileNumber = jsonObject.optString("mobileNumber", Constants.NA);
		String fileName = jsonObject.optString("fileName", Constants.NA);
		String fileData = jsonObject.optString("fileData", Constants.NA);

		try {

			if (mobileNumber.equals(Constants.NA))
				return closeResourceAndReturn(new DefaultResponse().setMessage("Invalid mobile number.").toJson());

			if (fileName.equals(Constants.NA))
				return closeResourceAndReturn(new DefaultResponse().setMessage("Invalid image name.").toJson());

			if (fileData.equals(Constants.NA))
				return closeResourceAndReturn(new DefaultResponse().setMessage("Invalid image data.").toJson());

			if (applicantType == ApplicantType.PRIMARY) {

				SFApplicant existingApplicant = dbHelper.getApplicantInfoByMobileNumber(mobileNumber);

				if (null == existingApplicant) {
					return closeResourceAndReturn(new DefaultResponse()
							.setMessage("Unable to udpate profile picture. No applicant found for given mobile number.")
							.toJson());
				}

			} else {

				SFCoApplicant existingCoApplicant = dbHelper.getCoApInfoByMobileNumber(mobileNumber);

				if (null == existingCoApplicant) {

					return closeResourceAndReturn(new DefaultResponse()
							.setMessage(
									"Unable to udpate profile picture. No co-applicant found for given mobile number.")
							.toJson());
				}

			}

			AmazonClient amazonClient = new AmazonClient();

			if (!amazonClient.uploadImage(fileName, fileData, S3BucketPath.APPLICANT_IMAGES)) {

				return closeResourceAndReturn(new DefaultResponse()
						.setMessage("Failed to add your profile picture. Please try again.").toJson());

			}

			if (!dbHelper.setApplicantProfilePicture(mobileNumber, fileName, applicantType)) {
				return closeResourceAndReturn(new DefaultResponse()
						.setMessage("Something went wrong while udpate profile picture. Please try again.")
						.setError("Failed to set co/app profile picture in DB.").toJson());
			}

			responseJson = BasicUtils.getSuccessTemplateObject();
			responseJson.put("imageUrl", amazonClient.getFullUrl(fileName, S3BucketPath.APPLICANT_IMAGES));

			return closeResourceAndReturn(responseJson);

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject generateOTP(String body) throws Exception {

		JSONObject bodyOject = new JSONObject(body);
		String mobileNumber = bodyOject.optString("mobileNumber", Constants.NA);
		String countryCode = bodyOject.optString("countryCode", "+91");
		JSONObject responseObject = new JSONObject();

		try {

			ContactManager cManager = new ContactManager();
			JSONObject optJson = cManager.sendOTP(mobileNumber, countryCode);

			if (null != optJson) {

				responseObject = optJson;
				responseObject.put(Constants.STATUS, Constants.SUCCESS);
				responseObject.put(Constants.MESSAGE, "Send otp successfully");

			} else {
				responseObject = new DefaultResponse().toJson();
			}

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

		return responseObject;
	}

	public JSONObject verifyMobileNumber(int userId, String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);

		String OTP = bodyObject.getString("OTP");
		String mobileNumber = bodyObject.getString("mobileNumber");
		String countryCode = bodyObject.optString("countryCode", "+91");
		String customerName = bodyObject.optString("customerName", Constants.NA);
		ContactManager cManager = new ContactManager();
		JSONObject otpJson = cManager.verifyOTP(mobileNumber, countryCode, OTP);

		if (otpJson.getString(Constants.STATUS).equals(Constants.SUCCESS)) {

			try {

				UtilityBill bill = new UtilityBill();

				bill.mobileNumber = mobileNumber;
				bill.billType = UtilityBillType.MOBILE.value;
				bill.billIdType = DocumentKYCHelper.KEY_MOBILE;
				bill.billId = mobileNumber;
				bill.customerName = customerName;
				bill.isVerified = true;
				bill.rawData = new JsonObject().toString();

				DatabaseHelper dbHelper = new DatabaseHelper(userId);

				boolean status = dbHelper.insertUtilityBill(bill);
				dbHelper.close();

				if (status) {
					otpJson = new JSONObject();
					otpJson.put(Constants.STATUS, Constants.SUCCESS);
					otpJson.put(Constants.MESSAGE, "Verify OTP Successfully");
				} else {
					otpJson = new JSONObject();
					otpJson.put(Constants.STATUS, Constants.FAILURE);
					otpJson.put(Constants.MESSAGE, Constants.DEFAULT_ERROR_MESSAGE);
				}

			} catch (Exception e) {
				dbHelper.close();
				throw e;
			}
		}

		return otpJson;

	}

	public JSONObject resendOTP(String body) throws Exception {

		JSONObject bodyObject = new JSONObject(body);
		String mobileNumber = bodyObject.optString("mobileNumber", Constants.NA);
		String countryCode = bodyObject.optString("countryCode", "+91");

		ContactManager contactManager = new ContactManager();
		JSONObject responseObject = new JSONObject();
		JSONObject optJson = contactManager.resendOTP(mobileNumber, countryCode);

		if (null != optJson) {

			responseObject = optJson;
			responseObject.put(Constants.STATUS, Constants.SUCCESS);
			responseObject.put(Constants.MESSAGE, "Resend otp successfully");

		} else {
			responseObject = new DefaultResponse().toJson();
		}

		return responseObject;

	}

	public JSONObject insertOrUpdateRegionMap() throws Exception {

		try {

			SalesForceManager sfHelper = new SalesForceManager();
			boolean isSuccess = false;

			JSONObject finalJson = new JSONObject();
			JSONArray sfUserData = sfHelper.getAllSfUsers();

			if (null != sfUserData) {

				isSuccess = dbHelper.addUpdatedRegionMapData(sfUserData);
				dbHelper.close();

				finalJson = BasicUtils.getSuccessTemplateObject();

				if (Constants.IS_STRICT_PROD_PROCESS_ACTIVE) {
					MailUtils.getInstance().sendDefaultMail("Regions Updated | RM Pro",
							"===> Region data has been successfully updated at "
									+ DateTimeUtils.getCurrentDateTimeInIST() + " <===",
							"sanjay.jaiswar@homefirstindia.com", "ranan.rodrigues@homefirstindia.com");
				}

				DefaultResponse oResponse = new DefaultResponse();
				oResponse.isSuccess = isSuccess;
				oResponse.message = (isSuccess ? "Region Map has been successfully updated."
						: "Failed to update Region Map data.");
				finalJson = oResponse.toJson();

			} else {
				finalJson = BasicUtils.getFailureTemplateObject();
			}

			return finalJson;

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	// ===================== Notification implementation methods
	// =========================

	public JSONObject getNotifications(int userId, JSONObject requestObject) throws Exception {

		// User user = getUserById(userId);
		NotificationDatabaseHelper ndbHelper = new NotificationDatabaseHelper();

		try {

			NotificationFetchType fetchType = NotificationFetchType
					.get(requestObject.optString(Constants.NOTIFICATION_FETCH_TYPE, NotificationFetchType.FIRST.value));

			String currentDateTime = DateTimeUtils.getCurrentDateTimeInIST();

			String topNotificationDatetime = requestObject.optString(Constants.TOP_NOTIFICATION_DATE_TIME,
					currentDateTime);

			String bottomNotificationDatetime = requestObject.optString(Constants.BOTTOM_NOTIFICATION_DATE_TIME,
					currentDateTime);

			if (fetchType != NotificationFetchType.FIRST) {
				if (topNotificationDatetime.equals(Constants.NA))
					return new DefaultResponse().setError("Invalid topNotificationDatetime.").toJson();
				if (bottomNotificationDatetime.equals(Constants.NA))
					return new DefaultResponse().setError("Invalid bottomNotificationDatetime.").toJson();
			}

			ArrayList<RmNotification> notifications = ndbHelper.getUserNotifications(userId, fetchType,
					(fetchType == NotificationFetchType.TOP ? topNotificationDatetime
							: (fetchType == NotificationFetchType.BOTTOM ? bottomNotificationDatetime
									: currentDateTime)));

			ndbHelper.close();

			int unreadNotificationCount = 0;
			JSONArray notificationArray = new JSONArray();

			for (RmNotification item : notifications) {
				notificationArray.put(item.toJson());
				if (!item.hasRead)
					unreadNotificationCount++;
			}

			JSONObject response = BasicUtils.getSuccessTemplateObject();
			response.put(Constants.NOTIFICATIONS, notificationArray);
			response.put(Constants.UNREAD_COUNT, unreadNotificationCount);

			return response;

		} catch (Exception e) {
			ndbHelper.close();
			throw e;
		}

	}

	public JSONObject getUnreadNotificationCount(int userId) throws Exception {

		NotificationDatabaseHelper dbHelper = new NotificationDatabaseHelper();

		try {
			int unreadNotificationCount = dbHelper.getUserNotificationCount(userId);
			dbHelper.close();

			JSONObject response = BasicUtils.getSuccessTemplateObject();
			response.put(Constants.UNREAD_COUNT, unreadNotificationCount);

			return response;

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject updateNotificationStatus(int userId, JSONObject requestObject) throws Exception {

		NotificationDatabaseHelper dbHelper = new NotificationDatabaseHelper();

		try {

			boolean status = dbHelper.updateUserNotificationStatus(userId, requestObject, true);
			if (!status)
				status = dbHelper.updateUserNotificationStatus(userId, requestObject, false);
			dbHelper.close();

			DefaultResponse lResponse = new DefaultResponse();

			if (status) {
				lResponse.isSuccess = true;
				lResponse.message = "Successfully updated user notification status";
				LoggerUtils.log("Successfully updated user notification status");
			} else
				LoggerUtils.log("Failed to update user notification status");

			return lResponse.toJson();

		} catch (Exception e) {
			dbHelper.close();
			throw e;
		}

	}

	public JSONObject getBankPickList() throws Exception {

		SalesForceManager sfHelper = new SalesForceManager(true);

		try {
			JSONObject bankData = sfHelper.getBankPickList();

			if (null != bankData) {
				JSONObject response = BasicUtils.getSuccessTemplateObject();
				response.put("result", bankData);
				return response;
			}

			return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			throw e;
		}

	}

	public JSONObject customerLookUp(JSONObject requestObject) throws Exception {

		SalesForceManager sfHelper = new SalesForceManager();

		try {
			JSONObject customerData = sfHelper.customerLookUp(requestObject);

			if (null != customerData) {
				JSONObject response = BasicUtils.getSuccessTemplateObject();
				response.put("result", customerData);
				return response;
			}

			return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			throw e;
		}

	}

	public JSONObject customerLookUpGlobal(JSONObject requestObject) throws Exception {

		SalesForceManager sfHelper = new SalesForceManager();

		try {

			boolean isHomeLoan = requestObject.optBoolean("isHomeLoan", true);
			JSONObject customerData = new JSONObject();

			if (isHomeLoan)
				customerData = sfHelper.customerOpportunityLookUp(requestObject);
			else
				customerData = sfHelper.customerLookUp(requestObject);

			if (null != customerData) {

				JSONArray customersArray = new JSONArray();

				if (customerData.getInt("totalSize") > 0) {

					JSONArray customerArray = customerData.getJSONArray("records");

					for (int i = 0; i < customerArray.length(); i++) {
						JSONObject current = customerArray.getJSONObject(i);
						SFCustomer customer = new SFCustomer(current, isHomeLoan);
						customersArray.put(customer.toJson());
					}

				}
				JSONObject response = BasicUtils.getSuccessTemplateObject();
				response.put("result", customersArray);
				return response;
			}

			return BasicUtils.getFailureTemplateObject();

		} catch (Exception e) {
			throw e;
		}

	}

	public Response sendPaymentLink(int userId, JSONObject requestJson) throws Exception {

		var eUser = userRepo().findUserById(userId);

		if (null == eUser) {
			log("sendPaymentLink - No user found for userId: " + userId);
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No user found!").setAction(Actions.RETRY.value).toJson());
		}

		var paymentInfo = gson.fromJson(requestJson.toString(), PaymentInfo.class);

		paymentInfo.fillValuesForRemotePayment(eUser);

		if (!paymentInfoRepo().savePaymentInfo(paymentInfo)) {
			log("sendPaymentLink - Failed to add new paymentInfo in DB");
			return new OneResponse().getFailureResponse(new LocalResponse()
					.setMessage("Failed to add new paymentInfo in DB").setAction(Actions.RETRY.value).toJson());
		}

		LocalResponse spResponse = hfoManager.sendPaymentLink(new JSONObject(gson.toJson(paymentInfo)));

		if (!spResponse.isSuccess) {

			LoggerUtils.log("sendPaymentLink - Failed to send payment link from HFO");
			return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
					.setAction(Actions.RETRY.value).toJson());

		}

		log("sendPaymentLink - Successfully sent payment link");

		var spResponseJson = new JSONObject(spResponse.message);

		paymentInfo.hfoPaymentId = spResponseJson.optString("id", Constants.NA);
		paymentInfo.paymentStatus = PaymentStatus.INITIATED.value;
		paymentInfo.pgStatus = PaymentStatus.CREATED.value;
		paymentInfo.paymentLink = spResponseJson.optString("paymentLink", Constants.NA);

		if (!paymentInfoRepo().savePaymentInfo(paymentInfo)) {
			log("sendPaymentLink - Failed to update paymentInfo in DB after sending payment link through HFO");
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("Failed to update paymentInfo in DB after sending payment link")
							.setAction(Actions.RETRY.value).toJson());
		}

		log("sendPaymentLink - Successfully update payment info in DB");

		var responseJson = new LocalResponse().setStatus(true)
				.setMessage("Payment link sent successfully to email id: " + paymentInfo.customerEmailId
						+ " and mobile number: " + paymentInfo.customerMobileNumber)
				.setAction(Actions.CONTINUE.value).toJson();

		responseJson.put("paymentInfo", new JSONObject(gson.toJson(paymentInfo)));
		return new OneResponse().getSuccessResponse(responseJson);

	}

	public Response getPayments(int userId, JSONObject requestJson) throws Exception {

		var eUser = userRepo().findUserById(userId);

		if (null == eUser) {
			log("getPayments - No user found for userId: " + userId);
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No user found!").setAction(Actions.RETRY.value).toJson());
		}

		var offset = requestJson.optInt("offset", 0);
		var limit = requestJson.optInt("limit", 0);

		final var payments = paymentInfoRepo().getAllPayments(limit, offset, eUser.id);

		var responseJson = new LocalResponse().setStatus(true).setMessage("Payments fetched successfully.").toJson();
		responseJson.put("payments", new JSONArray(gson.toJson(payments)));

		return new OneResponse().getSuccessResponse(responseJson);

	}

	public Response searchPayment(int userId, JSONObject requestJson) throws Exception {

		var eUser = userRepo().findUserById(userId);

		if (null == eUser) {
			log("searchPayment - No user found for userId: " + userId);
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No user found!").setAction(Actions.RETRY.value).toJson());
		}

		var searchKey = requestJson.optString("searchKey", Constants.NA);

		if (!BasicUtils.isNotNullOrNA(searchKey)) {
			log("searchPayment - search key is invalid: " + searchKey);
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("Search key is invalid!").setAction(Actions.RETRY.value).toJson());
		}

		final var payments = paymentInfoRepo().searchPayment(eUser.id, searchKey);

		var responseJson = new LocalResponse().setStatus(true).setMessage("Payments fetched successfully.").toJson();
		responseJson.put("payments", new JSONArray(gson.toJson(payments)));

		return new OneResponse().getSuccessResponse(responseJson);

	}

	public Response generateFailedReceipt(int userId, JSONObject requestJson) throws Exception {

		var eUser = userRepo().findUserById(userId);

		if (null == eUser) {
			log("generateFailedReceipt - No user found for userId: " + userId);
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No user found!").setAction(Actions.RETRY.value).toJson());
		}

		var transactionId = requestJson.optString(Constants.TRANSACTION_ID, Constants.NA);

		if (!BasicUtils.isNotNullOrNA(transactionId)) {
			log("generateFailedReceipt - Invalid transaction Id");
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("Invalid transaction Id").setAction(Actions.RETRY.value).toJson());
		}

		var ePaymentInfo = paymentInfoRepo().findPaymentInfoByTransId(transactionId);

		if (null == ePaymentInfo) {
			log("generateFailedReceipt - No payment info found for transaction Id: " + transactionId);
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No payment info found").setAction(Actions.RETRY.value).toJson());
		}

		LocalResponse spResponse = hfoManager.generateFailedReceipt(ePaymentInfo.hfoPaymentId);

		if (!spResponse.isSuccess) {

			LoggerUtils.log("generateFailedReceipt - Failed to generate receipt on HFO");

			return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
					.setAction(Actions.RETRY.value).setMessage(spResponse.message).toJson());

		}

		var hfoJson = new JSONObject(spResponse.message);

		var paymentInfo = gson.fromJson(hfoJson.optString("paymentInfo"), PaymentInfo.class);

		ePaymentInfo.pgOrderId = paymentInfo.pgOrderId;
		ePaymentInfo.pgPaymentId = paymentInfo.pgPaymentId;
		ePaymentInfo.sfReceiptId = paymentInfo.sfReceiptId;
		ePaymentInfo.sfReceiptNumber = paymentInfo.sfReceiptNumber;
		ePaymentInfo.pgPaymentData = paymentInfo.pgPaymentData;
		ePaymentInfo.pgStatus = paymentInfo.pgStatus;
		ePaymentInfo.receiptStatus = Constants.NONE.toLowerCase();

		if (paymentInfo.pgStatus.equalsIgnoreCase(PaymentManager.RAZOR_PAY_PAID_STATUS)) {
			ePaymentInfo.paymentStatus = Constants.SUCCESS;
			ePaymentInfo.completionDatetime = DateTimeUtils.getCurrentDateTimeInIST();
		}

		if (BasicUtils.isNotNullOrNA(paymentInfo.sfReceiptId)) {
			ePaymentInfo.receiptStatus = Constants.SUCCESS;
		}

		ePaymentInfo.statusMessage = "Payment:" + ePaymentInfo.paymentStatus + " | " + "Receipt:"
				+ ePaymentInfo.receiptStatus;

		if (!paymentInfoRepo().savePaymentInfo(ePaymentInfo)) {
			log("generateFailedReceipt - Failed to update payment status in DB");
			return new OneResponse().getFailureResponse(new LocalResponse()
					.setMessage("Failed to update payment status in DB").setAction(Actions.RETRY.value).toJson());
		}

		log("generateFailedReceipt - Receipt created successfully");

		var responseJson = new LocalResponse().setStatus(true).setMessage("Receipt created successfully!")
				.setAction(Actions.CONTINUE.value).toJson();

		responseJson.put("paymentInfo", new JSONObject(gson.toJson(paymentInfo)));
		return new OneResponse().getSuccessResponse(responseJson);

	}

	public Response logout(int userId) throws Exception {

		var eUser = userRepo().findUserById(userId);

		if (null == eUser) {
			log("logout - No user found for userId: " + userId);
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No user found!").setAction(Actions.RETRY.value).toJson());
		}

		if (null != eUser.sessionPasscode && eUser.sessionPasscode.length() > 5) {

			eUser.sessionPasscode = eUser.sessionPasscode.concat(eUser.sessionPasscode
					.substring(eUser.sessionPasscode.length() - 5, eUser.sessionPasscode.length()));

			if (!userRepo().saveUser(eUser)) {
				log("logout - fail to update session passcode to null");
			}

		}

		JSONObject responseJson = new LocalResponse().setStatus(true).setMessage("user logged out successfully")
				.toJson();

		return new OneResponse().getSuccessResponse(responseJson);

	}

	public Response syncSfUserDetail() throws Exception {

		var allUser = userRepo().getAllUsers();

		if (allUser.isEmpty()) {
			log("syncSfUserDetail - No users found on local DB");
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No user found!").setAction(Actions.RETRY.value).toJson());
		}

		AtomicInteger counter = new AtomicInteger();

		final Collection<List<User>> groupUserList = allUser.stream()
				.collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 200)).values();

		groupUserList.forEach(uList -> {

			var sfIds = String.join(",",
					uList.stream().map(u -> ("'" + u.sfUserId + "'")).collect(Collectors.toList()));

			try {

				var sfUserData = new SalesForceManager().getUserDetails(sfIds);

				sfUserData.forEach(sfu -> {

					JSONObject currentSfUserJson = (JSONObject) sfu;

					var sfUserId = currentSfUserJson.get("Id");

					if (null != sfUserId) {

						var user = uList.stream().filter(item -> item.sfUserId.equals(sfUserId))
								.collect(Collectors.toList()).get(0);

						user.mobileNumber = currentSfUserJson.optString("MobilePhone", Constants.NA);

						if (BasicUtils.isNotNullOrNA(user.mobileNumber)) {

							
							user.mobileNumber = BasicUtils.getTruncatedDataFromEnd(user.mobileNumber.replaceAll("\\s", ""), 10);

							if (BasicUtils.isNotNullOrNA(user.mobileNumber) && user.mobileNumber.length() == 10) {
								userRepo().saveUser(user);
							}

						}

					}

				});

			} catch (Exception e) {
				log("Error while getting user details from sf: " + e.toString());
				e.printStackTrace();
			}

		});

		var responseJson = new LocalResponse().setStatus(true).setMessage("User details sysnced successfully.")
				.toJson();

		return new OneResponse().getSuccessResponse(responseJson);

	}

	public Response initiateCall(int userId, String body) throws Exception {

		var callRequest = gson.fromJson(body.toString(), CallDTO.class);

		callRequest.receiver = BasicUtils.getTruncatedDataFromEnd(callRequest.receiver, 10);

		final var vResponse = callRequest.allMandatoryFieldsPresent();

		if (!vResponse.isSuccess) {

			log("initiateCall - " + vResponse.message);
			return new OneResponse().getFailureResponse(vResponse.toJson());

		}

		var eUser = userRepo().findUserById(userId);

		if (null == eUser) {
			log("initiateCall - No user found for userId: " + userId);
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No user found!").setAction(Actions.RETRY.value).toJson());
		}
		
		if (!BasicUtils.isNotNullOrNA(eUser.mobileNumber)) {

			var mobileNumber = new SalesForceManager().getUserDetail(eUser.sfUserId);
			
			if (BasicUtils.isNotNullOrNA(mobileNumber)) {
				eUser.mobileNumber = BasicUtils.getTruncatedDataFromEnd(mobileNumber.replaceAll("\\s", ""), 10);
				userRepo().saveUser(eUser);
			}
			
		}

		if (!BasicUtils.isNotNullOrNA(eUser.mobileNumber) || eUser.mobileNumber.length() != 10) {
			return new OneResponse().getFailureResponse(new LocalResponse()
					.setMessage("No valid mobile number found to initiate a call, "
							+ "please make sure your mobile number is updated on salesforce or contact admin.")
					.setAction(Actions.RETRY.value).toJson());
		}

		callRequest.caller = eUser.mobileNumber;
		callRequest.source = Constants.SOURCE_RM_PRO;
		callRequest.userId = eUser.sfUserId;
		callRequest.userEmail = eUser.email;
		callRequest.userName = eUser.displayName;

		var spResponse = hfoManager.clickToCall(new JSONObject(gson.toJson(callRequest)));

		if (!spResponse.isSuccess) {

			LoggerUtils.log("initiateCall - Failed to initiate call: " + spResponse.message);

			return new OneResponse().getFailureResponse(new LocalResponse().setMessage(spResponse.message)
					.setError(Errors.OPERATION_FAILED.value).setAction(Actions.RETRY.value).toJson());

		}

		return new OneResponse().getSuccessResponse(new JSONObject(spResponse.message));

	}
	
	public Response getLoanDetails(String loanAccountNumber) throws Exception {
		
		if (!BasicUtils.isNotNullOrNA(loanAccountNumber)) {

			log("getLoanDetails - Invalid loan account number : " + loanAccountNumber);

			return new OneResponse().getFailureResponse(new LocalResponse().setMessage("Invalid loan account number.")
					.setError(Errors.INVALID_DATA.value).setAction(Actions.FIX_RETRY.value).toJson());

		}
		
		var loanUrl = Endpoints.GET_LOAN_DETAILS.getFullUrl().concat("/").concat(loanAccountNumber);
		
		final var hfoResponse = hfoSpringNetworkingClient().GET(loanUrl);
		
		if (!hfoResponse.isSuccess) {

			log("getLoanDetails - Failed to get loan details from HFO Spring");
			
			return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
					.setAction(Actions.RETRY.value)
					.setMessage(hfoResponse.message).toJson());

		}

		var responseJson = new JSONObject(hfoResponse.stringEntity);
		responseJson.put(Constants.MESSAGE, "Loan details fetched successfully.");

		return new OneResponse().getSuccessResponse(responseJson);
		
	}

}
