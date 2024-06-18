package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import models.admin.AdminUser;
import models.notification.RmNotification;

public class NotificationUtils {

	public static final int MAX_NOTIFICATION_THRESHOLD = 100;
	public static final String DV_FIRST_NAME = "$$FIRST_NAME$$";

	public void initFirebase() throws IOException {

		boolean hasBeenInitialized = false;
		List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
		for (FirebaseApp app : firebaseApps) {
			if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
				hasBeenInitialized = true;
			}
		}

		if (!hasBeenInitialized) {

			FileInputStream serviceAccount = new FileInputStream(
					new File(getClass().getClassLoader().getResource("hffc-rm-pro-firebase.json").getFile()));

			FirebaseOptions options = new FirebaseOptions.Builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://hffc-rm-pro.firebaseio.com").build();

			FirebaseApp.initializeApp(options);
		}

	}

	public enum OnClickAction {
		IN_APP("inApp"), WEB("web");

		public final String value;

		OnClickAction(String value) {
			this.value = value;
		}

	}

	public enum ScheduleType {
		NOW("now"), LATER("later");

		public final String value;

		ScheduleType(String value) {
			this.value = value;
		}

	}

	public enum NotificationPriority {
		HIGH("H"), MEDIUM("M"), LOW("L");

		public final String value;

		NotificationPriority(String value) {
			this.value = value;
		}
	}

	public enum NotificationKind {
		PROMOTIONAL("promotional"), TRANSACTIONAL("transactional");

		public final String value;

		NotificationKind(String value) {
			this.value = value;
		}
	}

	public enum AudienceType {
		UNIVERSAL("universal"), PERSONALIZED("personalized"), SINGLE_USER("singleUser");

		public final String value;

		AudienceType(String value) {
			this.value = value;
		}
	}

	public enum Platform {
		ANDROID("android"), iOS("iOS"), APP("app"), WEB("web"), ALL("all");

		public final String value;

		Platform(String value) {
			this.value = value;
		}
	}

	public enum NotificationFetchType {
		TOP("TOP"), BOTTOM("BOTTOM"), FIRST("FIRST");

		public final String value;

		NotificationFetchType(String value) {
			this.value = value;
		}

		public static NotificationFetchType get(String value) {
			for (NotificationFetchType nft : NotificationFetchType.values()) {
				if (nft.value.equals(value))
					return nft;
			}
			return null;
		}

	}

	public static boolean shouldDryRun() {
		return !Constants.IS_NOTIFICATION_LIVE;
	}

	public static String[] notificationCsvHeader = new String[] {
			"rmSfId",		
			"title",
			"message",
			"webUrl"			
	};

	public enum NotificationEndpoint {
		CONNECTOR_APP("CONNECTOR_APP"), RM_PRO("RM_PRO");

		public final String value;

		NotificationEndpoint(String value) {
			this.value = value;
		}

		public static NotificationEndpoint get(String value) {
			for (var item : NotificationEndpoint.values()) {
				if (item.value.equals(value))
					return item;
			}
			return null;
		}

	}

	public enum AppScreens {
		DEFAULT("default", "Default", "Simply open the App and send user to Default home screen (Dashboard)."),
		BIRTHDAY("birthday_wish", "Birthday", "Birthday wish notification"),
		OPPORTUNITIES("my_opportunities", "My Opportunities", "Opens the opportunities screen"),
		TASK_LIST("my_tasks", "My Tasks", "Opens the tasks screen"),
		LEADERBOARD("leaderboard", "Leaderboard", "Opens the Leaderboard screen"),
		PROFILE("profile", "Profile", "Opens the Profile screen"),
		CONNECTOR("my_connectors", "My Connectors", "Opens the Connectors screen"),
		COLLECTION("my_collections", "My Collections", "Opens the Collections screen"),
		LEADS("my_leads", "My Leads", "Opens the lead list screen"),
		APP_UPDATE("app_update", "New Feature is waiting", "This will open app store for updating the app");

		public final String key, displayString, screenDescription;

		AppScreens(String key, String displayString, String screenDescription) {
			this.key = key;
			this.displayString = displayString;
			this.screenDescription = screenDescription;
		}

	}
	
	public enum NotificationEvent {
		LEAD_CREATED("LEAD_CREATED", AppScreens.LEADS.key);
		
		public final String name, appScreen;
		NotificationEvent(String name, String appScreen) {
			this.name = name;
			this.appScreen = appScreen;
		}
		
		public static NotificationEvent get(String name) {
			for (var event : NotificationEvent.values()) {
				if (event.name.equals(name)) return event;
			}
			return null;
		}
		
	}

	public void sendNotificationConfirmationEmail(RmNotification rmNotification, int totalCount, int successCount,
			int failedCount, AdminUser aUser) {

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 10);
		Date time = calendar.getTime();
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			int count = 0;

			@Override
			public void run() {

				if (count < 3) {

					try {

						if (!Constants.IS_STRICT_PROD_PROCESS_ACTIVE) {
							timer.cancel();
							return;
						}

						StringBuilder sb = new StringBuilder();

						sb.append("Voila! All notifications have been successfully pushed.");

						sb.append("\n\n\n============== Notification Info ================");
						sb.append("\n\n ID: " + rmNotification.id);
						sb.append("\n Title: " + rmNotification.title);
						sb.append("\n Message : " + rmNotification.message);
						sb.append("\n Big message : " + rmNotification.bigMessage);
						sb.append("\n Create datetime : " + rmNotification.createDatetime);
						sb.append("\n Schedule datetime : " + rmNotification.datetime);
						sb.append("\n Sent datetime : " + rmNotification.sentDatetime);
						sb.append("\n Total count: " + totalCount);
						sb.append("\n Success count: " + successCount);
						sb.append("\n Failure count: " + failedCount);

						sb.append("\n\n\n============== Admin Info ================");
						sb.append("\n\n Name: " + aUser.name);
						sb.append("\n Email: " + aUser.email);

						sb.append("\n\n\nThis is an automatic email generated by HomeFirst RM Pro.");
						sb.append("\nPlease do not reply to this email.");

						MailUtils.getInstance().sendDefaultMail("Notification Scheduled | RM Pro", sb.toString(), aUser.email);

						LoggerUtils.log("Notifcation email has been sent successfully.");
						timer.cancel();

					} catch (Exception e) {
						LoggerUtils.log("Error while while notification schedule email : " + e.getMessage());
						e.printStackTrace();
						// Toolkit.getDefaultToolkit().beep();
						count++;
						LoggerUtils.log("Notifcatin email task rescheduled, Iteration : " + count);
					}

				} else {
					LoggerUtils.log("Retry count exhausted while sending notifcatin email.");
					// Toolkit.getDefaultToolkit().beep();
					timer.cancel();
				}

			}
		}, time, 10000);

	}

	public void sendNotificationSchedulingInitiationMail(String source, RmNotification rmNotification,
			AdminUser aUser) {

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 5);
		Date time = calendar.getTime();
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {

			int count = 0;

			@Override
			public void run() {

				if (count < 3) {

					try {

						if (!Constants.IS_STRICT_PROD_PROCESS_ACTIVE) {
							timer.cancel();
							return;
						}

						StringBuilder sb = new StringBuilder();

						sb.append(
								"Notification scheduling process has been initiated. You'll receive another Email once all notifications have been pushed.");

						sb.append("\n\n\n============== Notification Info ================");
						sb.append("\n\n ID: " + rmNotification.id);
						sb.append("\n Title: " + rmNotification.title);
						sb.append("\n Message : " + rmNotification.message);
						sb.append("\n Big message : " + rmNotification.bigMessage);
						sb.append("\n Create datetime : " + rmNotification.createDatetime);
						sb.append("\n Schedule datetime : " + rmNotification.datetime);
						sb.append("\n Initiation Source: " + source);

						sb.append("\n\n\n============== Admin Info ================");
						sb.append("\n\n Name: " + aUser.name);
						sb.append("\n Email: " + aUser.email);

						sb.append("\n\n\nThis is an automatic email generated by HomeFirst RM Pro.");
						sb.append("\nPlease do not reply to this email.");

						MailUtils.getInstance().sendDefaultMail("Notification Scheduling Initiated | RM Pro",
								sb.toString(), aUser.email);

						LoggerUtils.log("Notification initiation email has been sent successfully.");
						timer.cancel();

					} catch (Exception e) {
						LoggerUtils.log("Error while while Notification initiation email : " + e.getMessage());
						e.printStackTrace();
						count++;
						LoggerUtils.log("Notification initiation email task rescheduled, Iteration : " + count);
					}

				} else {
					LoggerUtils.log("Retry count exhausted while sending Notification initiation email.");
					timer.cancel();
				}

			}
		}, time, 5000);

	}

}
