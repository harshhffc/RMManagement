package v2.managers;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;

import manager.AdminUserManager;
import models.PaymentInfo;
import models.admin.AdminUser;
import models.notification.RmNotification;
import models.notification.UserNotificationKey;
import models.notification.CrossNotification;
import models.notification.NotificationCSV;
import models.notification.RegistrationKey;
import utils.BasicUtils;
import utils.Constants;
import utils.DateTimeUtils;
import utils.LocalResponse;
import utils.DateTimeUtils.DateTimeFormat;

import utils.LoggerUtils;
import utils.NotificationUtils;
import utils.OneResponse;
import v1.repository.NotificationRepository;
import v1.repository.UserRepository;
import utils.Constants.Actions;
import utils.Constants.Errors;
import utils.NotificationUtils.AudienceType;
import utils.NotificationUtils.NotificationEvent;
import utils.NotificationUtils.NotificationKind;
import utils.NotificationUtils.NotificationPriority;
import utils.NotificationUtils.OnClickAction;
import utils.NotificationUtils.ScheduleType;
import v2.managers.AmazonClient.S3BucketPath;

public class NotificationHelper {

	private final SalesForceManager sfManager;
	private final AdminUserManager aManager;
	private final AdminUser aUser;
	private final Gson gson;
	private AmazonClient amazonS3Client = null;
	private NotificationRepository _notificationRepo = null;
	private UserRepository _userRepo = null;

	public NotificationHelper(int userId) throws Exception {

		new NotificationUtils().initFirebase();

		sfManager = new SalesForceManager();
		aManager = new AdminUserManager();
		aUser = aManager.getUserByUserId(userId);
		gson = new Gson();

	}

	private void log(String value) {
		LoggerUtils.log("NotificationHelper." + value);
	}

	private AmazonClient s3Client() throws Exception {
		if (null == amazonS3Client)
			amazonS3Client = new AmazonClient();
		return amazonS3Client;
	}

	private NotificationRepository notificationRepo() {
		if (null == _notificationRepo)
			_notificationRepo = new NotificationRepository();
		return _notificationRepo;
	}

	private UserRepository userRepo() {
		if (null == _userRepo)
			_userRepo = new UserRepository();
		return _userRepo;
	}

	// ----------------------------------------------------------------- //
	// -------------- START OF COMMON IMPLEMENTATION ------------------- //
	// ----------------------------------------------------------------- //

	private BatchResponse sendMulticastMessage(RmNotification rmNotification,
			ArrayList<RegistrationKey> registrationKeys) throws FirebaseMessagingException {

		List<String> registrationTokens = new ArrayList<>();

		for (RegistrationKey rKey : registrationKeys)
			registrationTokens.add(rKey.key);

		MulticastMessage message = MulticastMessage.builder().putData("title", rmNotification.title)
				.putData("message", rmNotification.message)
				.putData("rmNotification", rmNotification.toJson().toString())
				.putData("imageUrl", rmNotification.imageUrl)
				.setApnsConfig(ApnsConfig.builder().putHeader("apns-priority", "10")
						.setAps(Aps.builder()
								.setAlert(ApsAlert.builder().setTitle(rmNotification.title)
										.setBody(rmNotification.message).build())
								.setMutableContent(true)
								.setBadge(1)
								.build())
						.build())
				.addAllTokens(registrationTokens).build();

		BatchResponse bResponse = FirebaseMessaging.getInstance().sendMulticast(message,
				NotificationUtils.shouldDryRun());

//		log("sendMulticastMessage - Firebase response : " + gson.toJson(bResponse));

		try {
			if (notificationRepo().saveUserNotificationInfo(rmNotification, registrationKeys))
				log("sendMulticastMessage - Successfully insert new user notification after pushing");
			else
				log("sendMulticastMessage - Failed to insert new user notification after pushing.");
		} catch (Exception e1) {
			log("sendMulticastMessage - Error while inserting new user notifications after pushing: "
					+ e1.getMessage());
			e1.printStackTrace();
		}

		return bResponse;

	}

	public Response pushNotification(JSONObject requestObject) throws Exception {

	//	RmNotification rmNotification = 
	//	new RmNotification(requestObject.optJSONObject("notification"));
		
		final var rmNotification = gson.fromJson(requestObject.optJSONObject("notification").toString(),
				RmNotification.class);

		if (rmNotification.audienceType.equals(NotificationUtils.AudienceType.UNIVERSAL.value))
			return pushUniversalNotification(rmNotification);
		else if (rmNotification.audienceType.equals(NotificationUtils.AudienceType.PERSONALIZED.value))
			return pushPersonalizedNotification(rmNotification);
		else
			return new OneResponse()
					.getFailureResponse(new LocalResponse().setMessage("Invalid audience type.").toJson());

	}
	// ------------------------------------------------------------------ //
	// ---------- START OF UNIVERSAL NOTIFICATION IMPLMENTATION --------- //
	// ------------------------------------------------------------------ //

	public Response pushUniversalNotification(RmNotification rmNotification) throws Exception {

		ArrayList<RegistrationKey> registrationKeys = new ArrayList<>();


		String currentDateTime = DateTimeUtils.getCurrentDateTimeInIST();
	
		rmNotification.createDatetime = currentDateTime;
	
		if (!BasicUtils.isNotNullOrNA(rmNotification.datetime))
			rmNotification.datetime = currentDateTime;
		
		rmNotification.platform = NotificationUtils.Platform.ALL.value;
		rmNotification.scheduleType = rmNotification.shouldSchedule ? ScheduleType.LATER.value : ScheduleType.NOW.value;
		rmNotification.schedulerId = aUser.id;
		rmNotification.schedulerName = aUser.name;
	
		if (BasicUtils.isNotNullOrNA(rmNotification.imageFile)) {
	
			final var decoder = Base64.decodeBase64(rmNotification.imageFile);
	
			final var fileType = BasicUtils.MimeMap.mapMimetoExt(new Tika().detect(decoder));
			
			if (!BasicUtils.isNotNullOrNA(fileType)) {
				
				return new OneResponse().getFailureResponse(new LocalResponse()
						.setError(Errors.INVALID_DATA.value).setMessage("Incorrect file type!")
						.setAction(Actions.FIX_RETRY.value).toJson());
				
			}
	
			final var s3FileName = (DateTimeUtils.getCurrentDateTimeInIST() + "_" + aUser.id + "_"
					+ "Rm_Notification_Image" + fileType).replace(" ", "_");
	
			log("pushUniversalNotification - uploading image to s3...");
	
			if (!s3Client().uploadImage(s3FileName, rmNotification.imageFile, S3BucketPath.RESOURCE_NOTIFICATION)) {
				log("pushUniversalNotification - Failed to upload Image file to S3");
	
				return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
						.setAction(Actions.RETRY.value).toJson());
	
			}
	
			log("pushUniversalNotification - image uploaded to s3");
	
			rmNotification.imageUrl = AmazonClient.S3BucketPath.RESOURCE_NOTIFICATION.fullPath() + s3FileName;
		}
	
		if (BasicUtils.isNotNullOrNA(rmNotification.thumbnailFile)) {
	
			final var decoderPersonlisedThumb = Base64.decodeBase64(rmNotification.thumbnailFile);
	
			final var fileType = BasicUtils.MimeMap.mapMimetoExt(new Tika().detect(decoderPersonlisedThumb));
			
			if (!BasicUtils.isNotNullOrNA(fileType)) {
				
				return new OneResponse().getFailureResponse(new LocalResponse()
						.setError(Errors.INVALID_DATA.value).setMessage("Incorrect file type!")
						.setAction(Actions.FIX_RETRY.value).toJson());
				
			}
	
			final var s3FileNameThumb = (DateTimeUtils.getCurrentDateTimeInIST() + "_" + aUser.id + "_"
					+ "Rm_Notification_Thumbnail" + fileType).replace(" ", "_");
	
			log("pushUniversalNotification - uploading thumnail image to s3...");
			if (!s3Client().uploadImage(s3FileNameThumb, rmNotification.thumbnailFile,
					S3BucketPath.RESOURCE_NOTIFICATION)) {
				log("pushPersonalizedNotification - failed to upload thumbnail image file to s3.");
				return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
						.setAction(Actions.RETRY.value).toJson());
			}
	
			log("pushUniversalNotification - thumnail image uploaded to s3");
	
			rmNotification.thumbnailUrl = AmazonClient.S3BucketPath.RESOURCE_NOTIFICATION.fullPath()
					+ s3FileNameThumb;
		}
		
		if (!notificationRepo().saveRmNotification(rmNotification)) {
	
			log("pushUniversalNotification - Failed to insert new notification in DB.");
			return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
					.setAction(Actions.RETRY.value).toJson());
	
		}
	
		registrationKeys = notificationRepo().getAllRegistrationKeys();

		if (registrationKeys.size() == 0)
			return new OneResponse().getFailureResponse(new LocalResponse()
					.setMessage("No notification registration keys found for the given condition.").toJson());


		scheduleUniversalNotification(rmNotification, registrationKeys);
		new NotificationUtils().sendNotificationSchedulingInitiationMail("Manual", rmNotification, aUser);
		
		return new OneResponse().getSuccessResponse(
				new LocalResponse().setStatus(true).setMessage("Notifications has been schedulled successfully. "
						+ "You'll get an email once all notifications has been pushed.").toJson());

	}

	private void scheduleUniversalNotification(RmNotification rmNotification,
			ArrayList<RegistrationKey> registrationKeys) throws ParseException {

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 10);
		Date time = calendar.getTime();

		if (rmNotification.shouldSchedule) {
			LoggerUtils.log("--- time before converting: " + rmNotification.datetime);
			time = DateTimeUtils.getDateFromDateTimeString(
					DateTimeUtils.convertISTtoGMT(rmNotification.datetime, DateTimeFormat.yyyy_MM_dd_HH_mm_ss),
					DateTimeFormat.yyyy_MM_dd_HH_mm_ss);
			LoggerUtils.log("--- time after converting: " + time.toString());
		}

		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			int count = 0;

			@Override
			public void run() {

				if (count < 3) {

					try {

						rmNotification.sentDatetime = DateTimeUtils.getCurrentDateTimeInIST();
						// JSONArray messageSuccessIds = new JSONArray();
						// JSONArray messageFailedIds = new JSONArray();

						int successCount = 0;
						int failureCount = 0;

						ArrayList<RegistrationKey> selectedRegistrationTokens = new ArrayList<>();
						boolean shouldPush = false;

						for (int i = 0; i < registrationKeys.size(); i++) {

							RegistrationKey currentKey = registrationKeys.get(i);
							selectedRegistrationTokens.add(currentKey);

							if (i == registrationKeys.size() - 1 && selectedRegistrationTokens
									.size() <= NotificationUtils.MAX_NOTIFICATION_THRESHOLD) {
								shouldPush = true;
							} else if (selectedRegistrationTokens
									.size() == NotificationUtils.MAX_NOTIFICATION_THRESHOLD) {
								shouldPush = true;
							}

							if (shouldPush) {

								LoggerUtils.log("Sending notification to: " + selectedRegistrationTokens.size());

								BatchResponse response = sendMulticastMessage(rmNotification,
										selectedRegistrationTokens);

								successCount += response.getSuccessCount();
								failureCount += response.getFailureCount();

								/*
								 * for (int n = 0; n < response.getResponses().size(); n++) {
								 * 
								 * SendResponse current = response.getResponses().get(n);
								 * 
								 * if (current.isSuccessful()) messageSuccessIds.put(current.getMessageId());
								 * else messageFailedIds.put(current.getMessageId());
								 * 
								 * }
								 */

								selectedRegistrationTokens.clear();
								shouldPush = false;

							}
						}

						LoggerUtils.log("==> Total messages status -  Success: " + successCount + " | Failure: "
								+ failureCount);
						
						rmNotification.totalCount = registrationKeys.size();
						rmNotification.successCount = successCount;
						rmNotification.failureCount = failureCount;
						rmNotification.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST();
						rmNotification.isScheduled = true;

						if (notificationRepo().saveRmNotification(rmNotification))
							LoggerUtils.log("==> Notification updated in DB successfully after pushing.");
						else
							LoggerUtils.log("==> Failed to update Notification in DB after pushing.");

						LoggerUtils.log("Universal notification send successfully. Iteration : " + count);

						new NotificationUtils().sendNotificationConfirmationEmail(rmNotification,
								registrationKeys.size(), successCount, failureCount, aUser);

						timer.cancel();

					} catch (Exception e) {
						LoggerUtils.log("Error while while scheduling Universal notification : " + e.getMessage());
						e.printStackTrace();
						count++;
						LoggerUtils.log("Universal notification task rescheduled, Iteration : " + count);
					}

				} else {
					LoggerUtils.log("Retry count exhausted while scheduling Universal notification.");
					timer.cancel();
				}

			}
		}, time, 10000);

	}

	// ******************************************************************************
	// //
	// **************** END OF UNIVERSAL NOTIFICATION IMPLEMENTATION
	// **************** //
	// ******************************************************************************
	// //

	// ------------------------------------------------------------------------------
	// //
	// ----------- START OF PERSONALIZED NOTIFICATION IMPLEMENTATION
	// ---------------- //
	// ------------------------------------------------------------------------------
	// //

	public Response pushPersonalizedNotification(RmNotification rmNotification) throws Exception {

		final var currentDateTime = DateTimeUtils.getCurrentDateTimeInIST();

		if (BasicUtils.isNotNullOrNA(rmNotification.imageFile)) {

			final var decodePersonalized = Base64.decodeBase64(rmNotification.imageFile);

			final var fileType = BasicUtils.MimeMap.mapMimetoExt(new Tika().detect(decodePersonalized));
			
			if (!BasicUtils.isNotNullOrNA(fileType)) {
				
				return new OneResponse().getFailureResponse(new LocalResponse()
						.setError(Errors.INVALID_DATA.value).setMessage("Incorrect file type!")
						.setAction(Actions.FIX_RETRY.value).toJson());
				
			}

			final var s3FileName = (DateTimeUtils.getCurrentDateTimeInIST() + "_" + aUser.id + "_"
					+ "Rm_Notification_Image" + fileType).replace(" ", "_");

			log("pushPersonalizedNotification - uploading image to s3...");

			if (!s3Client().uploadImage(s3FileName, rmNotification.imageFile, S3BucketPath.RESOURCE_NOTIFICATION)) {
				log("pushPersonalizedNotification - failed to upload image file to s3.");
				return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
						.setAction(Actions.RETRY.value).toJson());
			}

			log("pushPersonalizedNotification - image uploaded to s3");

			rmNotification.imageUrl = AmazonClient.S3BucketPath.RESOURCE_NOTIFICATION.fullPath() + s3FileName;
		}

		if (BasicUtils.isNotNullOrNA(rmNotification.thumbnailFile)) {

			final var decoderPersonlisedThumb = Base64.decodeBase64(rmNotification.thumbnailFile);

			final var fileType = BasicUtils.MimeMap.mapMimetoExt(new Tika().detect(decoderPersonlisedThumb));
			
			if (!BasicUtils.isNotNullOrNA(fileType)) {
				
				return new OneResponse().getFailureResponse(new LocalResponse()
						.setError(Errors.INVALID_DATA.value).setMessage("Incorrect file type!")
						.setAction(Actions.FIX_RETRY.value).toJson());
				
			}

			final var s3FileNameThumb = (DateTimeUtils.getCurrentDateTimeInIST() + "_" + aUser.id + "_"
					+ "Rm_Notification_Thumbnail" + fileType).replace(" ", "_");

			log("pushPersonalizedNotification - thumnail image uploaded to s3");

			if (!s3Client().uploadImage(s3FileNameThumb, rmNotification.thumbnailFile,
					S3BucketPath.RESOURCE_NOTIFICATION)) {
				log("pushPersonalizedNotification - failed to upload thumb file to s3.");
				return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
						.setAction(Actions.RETRY.value).toJson());
			}

			log("pushPersonalizedNotification - thumnail image uploaded to s3");

			rmNotification.thumbnailUrl = AmazonClient.S3BucketPath.RESOURCE_NOTIFICATION.fullPath() + s3FileNameThumb;
		}
		/*
		 * Get the file csv file base64 from request, Store it in local and in S3 bucket
		 */

		final var decoder = Base64.decodeBase64(rmNotification.file);

		final var S3FileName = (DateTimeUtils.getCurrentDateTimeInIST() + "_" + aUser.id + "_" + "Notification.csv")
				.replace(" ", "_");

		final var tempFileName = Constants.UPLOAD_FILE_LOCAL_SERVER + S3FileName;
		final var file = new File(tempFileName);
		FileUtils.writeByteArrayToFile(file, decoder);

		final var reader = Files.newBufferedReader(Paths.get(tempFileName));

		/*
		 * Check whether all the header of the CSV file are in order
		 */

		final var csvReader = new CSVReader(reader);
		final var nextRecord = csvReader.readNext();

		if (!Arrays.equals(nextRecord, NotificationUtils.notificationCsvHeader)) {
			file.delete();
			csvReader.close();
			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage("Invalid CSV format").setError(Errors.INVALID_DATA.value).toJson());
		}

		if (Constants.IS_STRICT_PROD_PROCESS_ACTIVE) {

			if (!s3Client().uploadImage(S3FileName, rmNotification.file, S3BucketPath.NOTIFICATION)) {

				log("pushPersonalizedNotification - Failed to upload Notificaiton file to S3");
				file.delete();
				csvReader.close();
				return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
						.setAction(Actions.RETRY.value).toJson());

			}

		}

		/*
		 * Get all the personalized notification from the CSV file
		 */

		final var csvNotifications = new CsvToBeanBuilder<NotificationCSV>(reader).withType(NotificationCSV.class)
				.withIgnoreLeadingWhiteSpace(true).withSkipLines(0).build();

		final var csvNotificationIterator = csvNotifications.iterator();

		final var dynamicNotification = new ArrayList<NotificationCSV>();

		while (csvNotificationIterator.hasNext()) {
			dynamicNotification.add(csvNotificationIterator.next());
		}

		csvReader.close();
		file.delete();

		if (dynamicNotification.isEmpty()) {
			log("pushPersonalizedNotification - Notification list in the csv is empty.");
			return new OneResponse()
					.getFailureResponse(new LocalResponse().setStatus(false).setMessage("No data found in the CSV")
							.setError(Errors.INVALID_DATA.value).setAction(Actions.FIX_RETRY.value).toJson());

		}

		/*
		 * Create a list of rmSfId of rm and fetch notification keys for
		 * these rms only
		 */

		ArrayList<String> rmSfIds = new ArrayList<>();
		for (var dn : dynamicNotification)
			rmSfIds.add(dn.rmSfId);

		final var userNotificationKeys = notificationRepo()
				.getUserNotificationKeysBySfId("'" + String.join("','", rmSfIds) + "'");

		if (userNotificationKeys.size() == 0)
			return new OneResponse()
					.getFailureResponse(new LocalResponse().setMessage("No registration keys found.").toJson());

		/*
		 * Insert new notification in the DB
		 */

		rmNotification.createDatetime = currentDateTime;

		if (!BasicUtils.isNotNullOrNA(rmNotification.datetime))
			rmNotification.datetime = currentDateTime;

		rmNotification.platform = NotificationUtils.Platform.ALL.value;
		rmNotification.scheduleType = rmNotification.shouldSchedule ? ScheduleType.LATER.value : ScheduleType.NOW.value;
		rmNotification.schedulerId = aUser.id;
		rmNotification.schedulerName = aUser.name;

		if (!notificationRepo().saveRmNotification(rmNotification)) {

			log("pushPersonalizedNotification - Failed to insert new notification in DB.");
			return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
					.setAction(Actions.RETRY.value).toJson());

		}

		if (rmNotification.hasDynamicContent) {

			/*
			 * Notification has dynamic content Each rm will have personalized
			 * message
			 */

			for (var unk : userNotificationKeys) {

				final var dno = dynamicNotification.stream()
						.filter(n -> n.rmSfId.equals(unk.user.sfUserId)).findFirst();

				/*
				 * Add the custom notification received in the CSV and add them for each item in
				 * userNotificationKeys
				 */

				if (dno.isPresent())
					unk.customNotification = dno.get();

			}

		}

		schedulePersonalizedNotification(rmNotification, userNotificationKeys, true);

		new NotificationUtils().sendNotificationSchedulingInitiationMail("Manual", rmNotification, aUser);

		return new OneResponse().getSuccessResponse(
				new LocalResponse().setStatus(true).setMessage("Notifications has been schedulled successfully. "
						+ "You'll get an email once all notifications has been pushed.").toJson());

	}
	
	
	public JSONObject scheduleBirthdayNotification(JSONObject requestJson) throws Exception {

		try {

			String currentDateTime = DateTimeUtils.getCurrentDateTimeInIST();

			String currentDate = DateTimeUtils.getStringFromDateTimeString(currentDateTime,
					DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeFormat.dd);

			String currentMonth = DateTimeUtils.getStringFromDateTimeString(currentDateTime,
					DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeFormat.MM);

			JSONArray contactsWithBD = sfManager.getUsersWithBirthday(currentDate, currentMonth);

			ArrayList<String> emailIdsWithBD = new ArrayList<>();
			
			for (int i = 0; i < contactsWithBD.length(); i++) {
				emailIdsWithBD.add(contactsWithBD.getJSONObject(i).optString("Official_Email_ID__c", Constants.NA));
				
			}
			
			final var userNotificationKeys = notificationRepo()
					.getUserNotificationKeysBySfId("'" + String.join("','", emailIdsWithBD) + "'");

			if (userNotificationKeys.size() == 0)
				return new LocalResponse().setMessage("No registration keys found.").toJson();
			
			RmNotification rmNotification = new RmNotification();
			rmNotification.audienceType = AudienceType.PERSONALIZED.value;

			String nTitle = "Happy Birthday!";
			String nMessage = "Hi " + NotificationUtils.DV_FIRST_NAME
					+ ", HomeFirst wishes you a day filled with happiness and a year filled with joy.";

			rmNotification.title = nTitle;
			rmNotification.message = nMessage;
			rmNotification.bigMessage = nMessage;
			rmNotification.kind = NotificationKind.TRANSACTIONAL.value;
			rmNotification.priority = NotificationPriority.MEDIUM.value;
			rmNotification.screenToOpen = Constants.BIRTHDAY_NOTIFICATION_KEY;
			rmNotification.onClickAction = OnClickAction.IN_APP.value;
			rmNotification.shouldSchedule = true;

			String scheduleTime = requestJson.optString(Constants.SCHEDULE_TIME, "09:00:00");
			rmNotification.datetime = DateTimeUtils.getStringFromDateTimeString(currentDateTime,
					DateTimeFormat.yyyy_MM_dd_HH_mm_ss, DateTimeFormat.yyyy_MM_dd) + " " + scheduleTime;

			rmNotification.createDatetime = DateTimeUtils.getCurrentDateTimeInIST();
			
			
			if (!notificationRepo().saveRmNotification(rmNotification)) {

				log("scheduleConnectorBirthdayNotification - Failed to insert new notification in DB.");
				return new LocalResponse().setError(Errors.OPERATION_FAILED.value).setAction(Actions.RETRY.value)
						.toJson();

			}

			schedulePersonalizedNotification(rmNotification, userNotificationKeys, false);
			new NotificationUtils().sendNotificationSchedulingInitiationMail("CronJob", rmNotification, aUser);
			
			return new LocalResponse().setStatus(true).setMessage("Notifications has been schedulled successfully. "
					+ "You'll get an email once all notifications has been pushed.").toJson();


		} catch (Exception e) {
			throw e;
		}

	}

	public void sendCompletedPaymentNotification(PaymentInfo paymentInfo) throws Exception {

		var rmNotification = new RmNotification();
		rmNotification.title = "Payment received";
		var message = "Payment of ₹" + BasicUtils.getCurrencyString(paymentInfo.amount)
				+ " has been successfully received." + "\nCustomer: " + paymentInfo.sfOpportunityName
				+ "\nReceipt No. : " + paymentInfo.sfReceiptNumber + "\nOpp No. : " + paymentInfo.sfOpportunityNumber;
		rmNotification.message = message;
		rmNotification.bigMessage = message;
		rmNotification.scheduleType = "now";
		rmNotification.datetime = DateTimeUtils.getCurrentDateTimeInIST();
		rmNotification.shouldSchedule = false;
		rmNotification.audienceType = AudienceType.PERSONALIZED.value;
		rmNotification.data = new JSONObject().put("paymentInfo", gson.toJson(paymentInfo)).toString();
		rmNotification.platform = "all";
		rmNotification.kind = NotificationKind.TRANSACTIONAL.value;
		rmNotification.priority = NotificationPriority.MEDIUM.value;
		rmNotification.screenToOpen = Constants.MY_PAYMENTS;
		rmNotification.onClickAction = OnClickAction.IN_APP.value;
		rmNotification.schedulerId = aUser.id;
		rmNotification.schedulerName = aUser.name;

		var eUser = userRepo().findUserById(paymentInfo.userId);

		final var userNotificationKeys = notificationRepo().getUserNotificationKeysBySfId("'" + eUser.sfUserId + "'");

		if (userNotificationKeys.size() == 0) {
			log("sendCompletedPaymentNotification - No notification keys found for RM ID : " + eUser.sfUserId);
			return;
		}

		if (!notificationRepo().saveRmNotification(rmNotification)) {
			log("sendCompletedPaymentNotification - Failed to insert new notification in DB.");
			return;
		}

		log("sendCompletedPaymentNotification - remote payment notification initiated successfully.");
		
		schedulePersonalizedNotification(rmNotification, userNotificationKeys, false);
		
	}

	public void schedulePersonalizedNotification(RmNotification rmNotification,
			ArrayList<UserNotificationKey> userNotificationKeys, boolean shouldSendMail) throws ParseException {

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 10);
		Date time = calendar.getTime();

		if (rmNotification.shouldSchedule) {
			time = DateTimeUtils.getDateFromDateTimeString(
					DateTimeUtils.convertISTtoGMT(rmNotification.datetime, DateTimeFormat.yyyy_MM_dd_HH_mm_ss),
					DateTimeFormat.yyyy_MM_dd_HH_mm_ss);
		}

		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			int count = 0;

			@Override
			public void run() {

				if (count < 3) {

					try {

						rmNotification.sentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

						int totalCount = 0;
						int successCount = 0;
						int failureCount = 0;

						for (UserNotificationKey unKey : userNotificationKeys) {

							totalCount += unKey.registerationKeys.size();

							ArrayList<RegistrationKey> selectedKeys = new ArrayList<>();
							boolean shouldPush = false;

							for (int i = 0; i < unKey.registerationKeys.size(); i++) {

								selectedKeys.add(unKey.registerationKeys.get(i));

								if (i == unKey.registerationKeys.size() - 1
										&& selectedKeys.size() <= NotificationUtils.MAX_NOTIFICATION_THRESHOLD) {
									shouldPush = true;
								} else if (selectedKeys.size() == NotificationUtils.MAX_NOTIFICATION_THRESHOLD) {
									shouldPush = true;
								}

								if (shouldPush) {

									LoggerUtils.log("Sending personalized notification to: " + selectedKeys.size());

									RmNotification cpn = rmNotification.asCopy();
									
									
									if (null != unKey.customNotification) {

										cpn.title = unKey.customNotification.title;
										cpn.message = unKey.customNotification.message;
										cpn.bigMessage = unKey.customNotification.message;

										if (BasicUtils.isNotNullOrNA(unKey.customNotification.webUrl))
											cpn.webUrl = unKey.customNotification.webUrl;

									} else if (cpn.message.contains(NotificationUtils.DV_FIRST_NAME)
											|| cpn.bigMessage.contains(NotificationUtils.DV_FIRST_NAME)) {
										String firstName = unKey.user.firstName;
										cpn.message = cpn.message.replace(NotificationUtils.DV_FIRST_NAME, firstName);
										cpn.bigMessage = cpn.bigMessage.replace(NotificationUtils.DV_FIRST_NAME,
												firstName);
									}

									// TODO: Need to think about this as it was making message size greater than 4kb
									// for my lead

									if (rmNotification.screenToOpen == "my_leads")
										rmNotification.data = "";

									BatchResponse response = sendMulticastMessage(cpn, selectedKeys);
									successCount += response.getSuccessCount();
									failureCount += response.getFailureCount();

									shouldPush = false;
									selectedKeys.clear();

								}

							}

						}

						LoggerUtils.log("==> Total messages status -  Success: " + successCount + " | Failure: "
								+ failureCount);

						rmNotification.totalCount = totalCount;
						rmNotification.successCount = successCount;
						rmNotification.failureCount = failureCount;
						rmNotification.updateDatetime = DateTimeUtils.getCurrentDateTimeInIST();
						rmNotification.isScheduled = true;

						if (notificationRepo().saveRmNotification(rmNotification))
							log("schedulePersonalizedNotification - Personalized notification updated in DB successfully after pushing.");
						else
							LoggerUtils.log(
									"schedulePersonalizedNotification - Failed to update Personalized Notification in DB after pushing.");
						
						
						
						LoggerUtils.log("schedulePersonalizedNotification - Personalized notification send successfully. Iteration : " + count);
						
						
						if (shouldSendMail) {
							new NotificationUtils().sendNotificationConfirmationEmail(rmNotification, totalCount,
									successCount, failureCount, aUser); 
						}

						timer.cancel();

					} catch (Exception e) {
						LoggerUtils.log("Error while scheduling personalized notification : " + e.getMessage());
						e.printStackTrace();
						count++;
						LoggerUtils.log("Personalized notification task rescheduled, Iteration : " + count);
					}

				} else {
					log("schedulePersonalizedNotification - Retry count exhausted while scheduling personalized notification.");
					timer.cancel();
				}

			}
		}, time, 10000);

	}

	// ***************************************************************************
	// //
	// *********** END OF PERSONALIZED NOTIFICATION IMPLEMENTATION ***************
	// //
	// ***************************************************************************
	// //

	public Response pushConnectorNotification(CrossNotification crossNotification) throws Exception {

		final var rmNotification = crossNotification.notification;

		final var nEvent = NotificationEvent.get(crossNotification.event);

		if (null == nEvent) {

			log("pushRmNotification - Invalid event passed for the notification : " + crossNotification.event);

			return new OneResponse().getFailureResponse(new LocalResponse()
					.setMessage("Invalid event passed for the notification : " + crossNotification.event)
					.setError(Errors.INVALID_DATA.value).setAction(Actions.FIX_RETRY.value).toJson());

		}

		var sfConnectorOwnerId = Constants.NA;

		if (BasicUtils.isNotNullOrNA(rmNotification.data) && rmNotification.data.startsWith("{")) {

			final var dataJson = new JSONObject(rmNotification.data);

			if (null == dataJson || dataJson.isEmpty()) {

				log("pushRmNotification - Invalid data Json : " + gson.toJson(crossNotification));

				return new OneResponse().getFailureResponse(new LocalResponse().setMessage("Invalid lead json.")
						.setError(Errors.INVALID_DATA.value).setAction(Actions.FIX_RETRY.value).toJson());

			}

			sfConnectorOwnerId = dataJson.getJSONObject("lead").optString("sfOwnerId");

		}

		if (!BasicUtils.isNotNullOrNA(sfConnectorOwnerId)) {

			log("pushRmNotification - Invalid owner id of the lead : " + gson.toJson(crossNotification));

			return new OneResponse().getFailureResponse(new LocalResponse().setMessage("Invalid owner id of the lead.")
					.setError(Errors.INVALID_DATA.value).setAction(Actions.FIX_RETRY.value).toJson());

		}

		final var userNotificationKeys = notificationRepo()
				.getUserNotificationKeysBySfId("'" + sfConnectorOwnerId + "'");

		if (userNotificationKeys.size() == 0)
			return new OneResponse().getFailureResponse(
					new LocalResponse().setMessage("No notification keys found for RM ID : " + sfConnectorOwnerId)
							.setError(Errors.INVALID_DATA.value).setAction(Actions.FIX_RETRY.value).toJson());

		rmNotification.screenToOpen = nEvent.appScreen;
		rmNotification.cnId = crossNotification.id;
		rmNotification.schedulerId = aUser.id;
		rmNotification.schedulerName = aUser.name;
		rmNotification.onClickAction = NotificationUtils.OnClickAction.IN_APP.value;

		if (!notificationRepo().saveRmNotification(rmNotification)) {

			log("pushRmNotification - Failed to insert new notification in DB.");

			return new OneResponse().getFailureResponse(new LocalResponse().setError(Errors.OPERATION_FAILED.value)
					.setAction(Actions.RETRY.value).toJson());

		}
		
		schedulePersonalizedNotification(rmNotification, userNotificationKeys, false);

		return new OneResponse().getSuccessResponse(
				new JSONObject().put(Constants.MESSAGE, "Push notification has been initiated successfully."));

	}

}
