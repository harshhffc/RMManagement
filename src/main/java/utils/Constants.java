package utils;

public class Constants {

	public static final boolean IS_PRODUCTION = true; //TODO: true for production
	public static final boolean IS_API_IN_PRODUCTION = true; //TODO: true for production
	public static final boolean IS_DB_IN_PRODUCTION = true; //TODO: true for production
	public static final boolean IS_SF_IN_PRODUCTION = true; //TODO: true for production
	public static final boolean IS_STRICT_PROD_PROCESS_ACTIVE = true;  //TODO: true for production
	public static final boolean IS_NOTIFICATION_LIVE = true; // TODO: TRUE for production
	public static final boolean IS_SF_PREPROD = true;  //TODO: false for production | true is preprod - false is fullcopy
	public static final boolean IS_STAGING = false; // TODO: FALSE for production
	
	public static final String UPLOAD_FILE_SERVER = "/var/www/images/document_picture/"; // TODO For Production
	public static final String UPLOAD_FILE_LOCAL_SERVER = "/var/www/images/document_picture/"; // TODO: FOR STAGING	
//	public static final String UPLOAD_FILE_SERVER = "/Users/sanjay/var/www/images/document_picture/"; //Local
//	public static final String UPLOAD_FILE_LOCAL_SERVER = "/Users/sanjay/var/www/images/document_picture/"; //Local
	
	public static final int HITBERNATE_BATCH_SIZE = 50;
	
	public static final String NA = "NA";
	public static final String RESET = "RESET";
	public static final String NONE = "NONE";
	public static final String STATUS = "status";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";
	public static final String MESSAGE = "message";
	public static final String ERROR = "error";
	public static final String ACTION = "action";
	public static final String FAILURE_JSON_STRING = "{\"status\": \"failure\"}";
	public static final String SUCCESS_JSON_STRING = "{\"status\": \"success\"}";
	public static final String SESSION_PASSCODE = "sessionPasscode";
	public static final String SOURCE_PASSCODE = "sourcePasscode";
	public static final String CROWN_PASSCODE = "crownPasscode";
	public static final String KYC_AUTH_TOKEN = "kycAuthToken";
	public static final String USER_SOURCE_ID = "userSourceId";
	public static final String TRANSACTION_ID = "transactionId";
	public static final String OPPORTUNITY_NUMBER= "opportunityNumber";
	public static final String EMAIL_ID = "emailId";
	public static final String PASSWORD = "password";
	public static final String ADMIN_USER = "adminUser";
	public static final String ADMIN_ROLE = "adminRole";
	public static final String PAYMENT_INFO = "paymentInfo";
	public static final String SF_TASK = "sfTask";
	public static final String ID = "id";
	public static final String USER_ID = "userId";
	public static final String PAYMENT_ID = "paymentId";
	public static final String RECEIPTSTATUS = "receiptStatus";
	public static final String PAYMENTSTATUS = "paymentStatus";
	public static final String STATUSMESSAGE = "statusMessage";
	public static final String COMPLETIONDATETIME = "completionDatetime";

	public static final String ADMIN = "ADMIN";
	public static final String KYC_USER = "KYC_USER";
	public static final String CROWN_USER = "CROWN_USER";
	
	public static final String DEFAULT_ERROR_MESSAGE = "Something went wrong. Please try again!";
	public static final String PAYMENT_ERROR_MESSAGE = "We've received your payment,"
			+ " but there seem to be a problem while generating your payment receipt."
			+ " Please retry, or contact our customer support regarding this payment.";
	public static final String LOAN_TYPE_CLOSED = "Closed - Obligations met";
	public static final String DEVICE_TYPE = "deviceType";
	public static final String ANDROID = "android";
	public static final String iOS = "iOS";
	public static final String APP_VERISON = "appVersion";
	
	public static final String LAST_NOTIFICATION_DATE_TIME = "lastNotificationDatetime";
	public static final String NOTIFICATIONS = "notifications";
	public static final String UNREAD_COUNT = "unreadCount";
	public static final String NOTIFICATION_FETCH_TYPE = "notificationFetchType";
	public static final String TOP_NOTIFICATION_DATE_TIME = "topNotificationDatetime";
	public static final String BOTTOM_NOTIFICATION_DATE_TIME = "bottomNotificationDatetime";
	
	public static final String SCHEDULE_DATETIME = "scheduleDateTime";
	public static final String SCHEDULE_TIME = "scheduleTime";
	public static final String DEFAULT = "default";
	public static final String BIRTHDAY_NOTIFICATION_KEY = "birthday_wish";
	
	public static final String ADMIN_SERVICES = "ADMIN_SERVICES";
	
	//public static final String EXTERNAL_PARTNER = "externalPartner";
	public static final String AUTHORIZATION = "Authorization";
	public static final String ORG_ID = "orgId";
	public static final String ROLE_ALL = "ALL";
	public static final String UTF_8 = "UTF-8";
	public static final String VALID_UPTO = "validUpto";
	
	public static final String SITE_PHOTO_SOURCE = "RM Pro";
	
	public static final String REMOTE_PAYMENT_SOURCE = "RM_PRO";
	public static final String REMOTE_PAYMENT_METHOD = "remotePayment";
	public static final String CASH_PAYMENT_METHOD = "cash";
	public static final String MY_PAYMENTS = "my_payments";
	
	public static final String LOAN_DETAILS = "loanDetails";
	public static final String PARTNER_HOMEFIRST_ONE_SPRING = "HomefirstOneSpring";

	public static final String PARTNER_AMAZON = "AWS-RABIT"; 
	public static final String SOURCE_RM_PRO = "RM_PRO";
	
	public enum TaskStatus {
	    OPEN("Open"),
	    COMPLETED("Completed");
		
		public final String value;
		TaskStatus(String value) {
			this.value = value;
		}
	}
	
	public enum CredType {
		PRODUCTION("PRODUCTION"), UAT("UAT");

		public final String value;

		CredType(String value) {
			this.value = value;
		}
	}
	
	public enum ApplicantType {
		PRIMARY("Primary applicant"), 
		CO_APPLICANT_ONE("Co-Applicant One"), 
		CO_APPLICANT_TWO("Co-Applicant Two"),
		GUARANTOR("Guarantor");
		
		public final String value;

		ApplicantType(String value) {
			this.value = value;
		}

		public static ApplicantType get(String type) throws Exception {
			for (ApplicantType requestType : ApplicantType.values()) {
				if (requestType.value.equals(type))
					return requestType;
			}
			return null;
		}
	}
	
	public enum ApplicantSource {
		RM_PRO("RM_PRO"), 
		DIRECT_SF("DIRECT_SF");
		
		public final String value;
		ApplicantSource(String value) {
			this.value = value;
		}

		public static ApplicantSource get(String type) throws Exception {
			for (ApplicantSource requestType : ApplicantSource.values()) {
				if (requestType.value.equals(type))
					return requestType;
			}
			return null;
		}
	}
	
	public enum ActionType {
		
		DO_LOGIN("DO_LOGIN"),
		DO_REGISTERATION("DO_REGISTERATION"),
		CONTINUE("CONTINUE"),
		RESET("RESET"),
		RETRY("RETRY"),
		
		AUTHENTICATE_AGAIN("AUTHENTICATE_AGAIN"),
		CONTACT_ADMIN("CONTACT_ADMIN");
		
		public final String stringValue;

		ActionType(String stringValue) {
			this.stringValue = stringValue;
		}
	}
	
	public enum Actions {
		AUTHENTICATE_AGAIN("AUTHENTICATE_AGAIN"), RETRY("RETRY"), FIX_RETRY("FIX_RETRY"), CANCEL("CANCEL"),
		CONTACT_ADMIN("CONTACT_ADMIN"), DO_REGISTRATION("DO_REGISTRATION"), DO_VERIFICATION("DO_VERIFICATION"),
		GO_BACK("GO_BACK"), DO_LOGIN("DO_LOGIN"), CONTINUE("CONTINUE");

		public final String value;

		Actions(String value) {
			this.value = value;
		}

	}
	
	public enum Errors {
		
		INVALID_PASSWORD("INVALID_PASSWORD"),
		UNKNOWN("UNKNOWN"),
		
		FAILED("FAILED"),	
    	INVALID_CREDENTIALS("INVALID_CREDENTIALS"),
    	RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
    	ACCESS_DENIED("ACCESS_DENIED"),
    	UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS"),
    	DUPLICATE_RECORD("DUPLICATE_RECORD"),
    	STRING_TOO_LONG("STRING_TOO_LONG"),
    	JSON_PARSER_ERROR("JSON_PARSER_ERROR"),
    	OPERATION_FAILED("OPERATION_FAILED"),
    	INVALID_DATA("INVALID_DATA");
		
		public final String value;

		Errors(String stringValue) {
			this.value = stringValue;
		}
	}
	
	public enum PaymentType {
	    EMI("EMI"),
	    LOGIN_FEES("Login Fees"),
	    PART_PRE_PAYMENT("Partial Pre-Payment"),
	    FULL_PRE_PAYMENT("Full Pre-Payment (Closure)"),
	    PRE_EMI_INTEREST("Pre-EMI Interest"),
	    PROJECT_APPRAISAL_CHARGES("Project Appraisal Charges"),
	    REPRICING_FEES("Repricing Fees"),
	    LEGAL_FEES("Legal Fees"),
	    TECHNICAL_FEES("Technical Fees"),
	    SERVICE_REQUEST("Service Request"),
	    OTHER_CHARGES("Other Charges"),
	    NPA_CLOSURE_RELATED("NPA Closure Related");

		public final String value;

		PaymentType(String stringValue) {
			this.value = stringValue;
		}
	}

}
