package databasehelper;

public class ColumnsNFields {

	public static String getTableName(String tableName) {
		return "RMManagementDB." + tableName;
	}

	public static final String DOCUMENT_KYC_TABLE = "kyc_document";
	public static final String KYC_AUTH_TABLE = "complete_kyc_auth";
	public static final String UTILITY_BILL = "utility_bill";
	public static final String EPF_EST_DETAIL_TABLE = "epf_est_passbook";
	public static final String EST_DETAIL_TABLE = "est_detail";
	public static final String EPF_DETAIL_TABLE = "epf_detail";
	public static final String VEHICLE_RC_INFO_TABLE = "vehicle_rc_info";
	public static final String ITRV_DOCUMENT_TABLE = "itrv_document";
	public static final String USER_LOCATION_TABLE = "user_location";
	public static final String USER_TABLE = "user";
	public static final String GSTIN_DETAIL_TABLE = "gstin_detail";
	public static final String PAYMENT_INFO_TABLE = "payment_info";
	public static final String SF_MAIN_OBJECT_TABLE = "sf_main_object";
	public static final String SF_CO_AP_OBJECT_TABLE = "sf_co_ap_object";
	public static final String USER_SECONDARY_INFO_TABLE = "user_secondary_info";
	public static final String BANK_INFO_TABLE = "bank_account_info";
	public static final String SCORE_WEIGHTAGE_TABLE = "score_weightage";
	public static final String REGION_MAP_TABLE = "region_map";
	public static final String LEADERBOARD_HISTORY = "leaderboard_history";
	public static final String LOGIN_INFO_TABLE = "login_info";
	public static final String INSTALLED_APPS_TABLE = "installed_apps_info";
	public static final String ADMIN_USER_TABLE = "admin_user";
	public static final String ADMIN_SECONDARY_TABLE = "admin_secondary_info";
	public static final String ADMIN_LOGIN_INFO_TABLE = "admin_login_info";
	public static final String ADMIN_LOG_TABLE = "admin_log";
	public static final String TASK_N_ACTIVITY_TABLE = "task_n_activity";
	public static final String TABLE_PARTNER = "Partner";
	public static final String TABLE_WHITELISTED_IP = "whitelisted_ip";

	public static final String NOTIFICATION_TABLE = "notification";
	public static final String USDR_NOTIFICATION_INFO_TABLE = "user_notification_info";
	public static final String USER_REQUEST = "user_request";

	public static final String COMMON_KEY_USER_ID = "user_id";
	public static final String COMMON_KEY_ID = "id";
	public static final String COMMON_KEY_CREATE_DATETIME = "create_datetime";
	public static final String COMMON_KEY_UPDATED_DATETIME = "update_datetime";
	public static final String COMMON_KEY_LATITUDE = "latitude";
	public static final String COMMON_KEY_LONGITUDE = "longitude";
	public static final String COMMON_KEY_ADDRESS = "address";

	public static final String COMMON_KEY_ORG_ID = "orgId";
	public static final String COMMON_KEY_SESSION_PASSCODE = "sessionPasscode";
	public static final String COMMON_KEY_SESSION_VALID_DATETIME = "sessionValidDatetime";
	public static final String COMMON_KEY_SESSION_UPDATE_DATETIME = "sessionUpdateDatetime";
	public static final String COMMON_KEY_IS_ENABLED = "isEnabled";
	public static final String COMMON_KEY_IP_ADDRESS = "ipAddress";
	public static final String COMMON_KEY_UPDATE_DATETIME = "updateDatetime";
	public static final String COMMON_KEY_CREATEDATETIME = "createDatetime";

	public enum UserLocationColumn {
		ID("id"), USER_ID("user_id"), SF_USER_ID("sf_user_id"), LATITUDE("latitude"), LONGITUDE("longitude"),
		DEVICE_ID("device_id"), DEVICE_TYPE("device_type"), UPDATE_DATETIME("update_datetime"), ADDRESS("address");

		public final String value;

		UserLocationColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum GstinDetailColumn {
		ID("id"), MOBILE_NUMBER("mobile_number"), GSTIN("gstin"), BUISNESS_LEGAL_NAME("business_legal_name"),
		BUISNESS_CONSTITUTION("business_constitution"), BUISNESS_ADDRESS("business_address"),
		BUISNESS_CONTACT_NUMBER("business_contact_number"), BUISNESS_EMAIL("business_email"),
		BUISNESS_NATURE("business_nature"), BUISNESS_REGISTER_DATE("business_register_date"), RAW_DATA("raw_data");

		public final String value;

		GstinDetailColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum UserColumn {
		ID("id"), SF_USER_ID("sf_user_id"), ORG_ID("org_id"), FIRST_NAME("first_name"), LAST_NAME("last_name"),
		DISPLAY_NAME("display_name"), EMAIL("email"), USERNAME("username"), MOBILENUMBER("mobile_number"),
		ID_URL("id_url"), DEVICE_ID("device_id"), DEVICE_TYPE("device_type"), REGISTER_DATETIME("register_datetime"),
		LAST_LOGIN_DATETIME("last_login_datetime"), UPDATE_DATETIME("update_datetime"), 
		SESSION_PASSCODE("session_passcode"), IP_ADDRESS("ip_address");

		public final String value;

		UserColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum VehicleRCInfoColumn {
		ID("id"), MOBILE_NUMBER("mobile_number"), REGISTRATION_NUMBER("registration_number"),
		ENGINE_NUMBER("engine_number"), VEHICLE_DESCRIPTION("vehicle_description"),
		BODY_DESCRIPTION("body_description"), REGISTERED_VEHICLE_COLOR("registered_vehicle_color"),
		PRESENT_ADDRESS("present_address"), PERMANENT_ADDRESS("permanent_address"), RTO_NAME("rto_name"),
		REGISTRATION_DATE("registration_date"), INSURANCE_COMPANY("insurance_company"),
		INSURANCE_VALID_UPTO("insurance_valid_upto"), INSURANCE_POLICY_NUMBER("insurance_policy_number"),
		OWNER_SERIAL_NUMBER("owner_serial_number"), REGISTERED_OWNER_NAME("registered_owner_name"),
		REGISTERED_OWNER_FATHERS_NAME("registered_owner_fathers_name"), CHASSIS_NUMBER("chassis_number"),
		MODEL_MAKER("model_maker"), VAHAN_DB_STATUS_MESSAGE("vahan_db_status_message"),
		FUEL_DESCRIPTION("fuel_description"), RAW_DATA("raw_data");

		public final String value;

		VehicleRCInfoColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum DocumentKYCColoumn {
		ID("id"), MOBILE_NUMBER("mobile_number"), DOCUMENT_TYPE("document_type"), DOCUMENT_ID("document_id"),
		DOCUMENT_URL("document_url"), IS_VERIFIED("is_verified"), USER_NAME("user_name"), USER_GENDER("user_gender"),
		USER_DOB("user_dob"), USER_IMAGE_URL("user_image_url"), ADDRESS("address"), RAW_DATA("raw_data");

		public final String value;

		DocumentKYCColoumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum KYCAuthColoumn {
		ID("id"), ENTITY_NAME("entity_name"), AUTH_TOKEN("auth_token"), IS_VALID("is_valid"),
		TOKEN_COUNT("token_count"), CREATION_DATE("creation_date"), REFRESH_DATE("refresh_date");

		public final String value;

		KYCAuthColoumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum UtilityBillColumn {
		ID("id"), MOBILE_NUMBER("mobile_number"), BILL_TYPE("bill_type"), BILL_ID_TYPE("bill_id_type"),
		BILL_ID("bill_id"), SERVICE_PROVIDER("service_provider"), IS_VERIFIED("is_verified"),
		CUSTOMER_NAME("customer_name"), CUSTOMER_ADDRESS("customer_address"), RAW_DATA("raw_data");

		public final String value;

		UtilityBillColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum ESTDetailColumn {
		ID("id"), UAN("uan"), MEMBER_ID("member_id"), EST_NAME("est_name"), DOE_EPF("doe_epf"), OFFICE("office"),
		DOJ_EPF("doj_epf"), DOE_EPS("doe_eps");

		public final String value;

		ESTDetailColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum EPFDetailColumn {
		ID("id"), MOBILE_NUMBER("mobile_number"), UAN("uan"), EMPLOYEE_NAME("employee_name"),
		EMPLOYEE_DOB("employee_dob"), EMPLOYEE_FATHER_NAME("employee_father_name"), RAW_DATA("raw_data"),
		REQUEST_ID("request_id");

		public final String value;

		EPFDetailColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum EpfEstPassbookColumn {
		ID("id"), UAN("uan"), MEMBER_ID("member_id"), CR_PEN_BAL("cr_pen_bal"), APPROVED_ON("approved_on"),
		DB_CR_FLAT("db_cr_flag"), TR_APPROVED("tr_approved"), TR_DATE_MY("tr_date_my"), CR_EE_SHARE("cr_ee_share"),
		R_ORDER("r_order"), CR_ER_SHARE("cr_er_share"), PARTICULAR("particular"), TRRNO("trrno"),
		MONTH_YEAR("month_year");

		public final String value;

		EpfEstPassbookColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum PaymentInfoColumn {
		TRANSACTION_ID("transaction_id"), USER_ID("user_id"), OPPORTUNITY_ID("opportunity_id"),
		OPPORTUNITY_NUMBER("opportunity_number"), OPPORTUNITY_NAME("opportunity_name"),
		LOAN_ACCOUNT_NUMBER("loan_account_number"), CURRENCY("currency"), AMOUNT("amount"),
		INITIAL_DATETIME("initial_datetime"), COMPLETION_DATETIME("completion_datetime"), ORDER_ID("order_id"),
		PAYMENT_ID("payment_id"), PAYMENT_STATUS("payment_status"), RECEIPT_STATUS("receipt_status"),
		STATUS_MESSAGE("status_message"), RECEIPT_ID("receipt_id"), RECEIPT_NUMBER("receipt_number"),
		RECEIPT_DATA("receipt_data"), SF_PAYMENT_TYPE("sf_payment_type"), SF_CLOSURE_REASON("sf_closure_reason"),
		SF_TRANSFERRED_HFC_NAME("sf_transferred_hfc_name"), SF_PAYMENT_SUB_TYPE("sf_payment_sub_type"),
		PAYMENT_METHOD("payment_method"), DEVICE_TYPE("device_type"), DEVICE_ID("device_id"),
		LOAN_PRODUCT_ID("loan_product_id"), LOAN_SUB_TYPE("loan_sub_type"), X_SELL_PRODUCT("x_sell_product_id");

		public final String value;

		PaymentInfoColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum SFCoApObjectColumn {
		ID("id"), USER_ID("user_id"), CUSTOMER_NAME("customer_name"), CUSTOMER_MOBILE_NUMBER("customer_mobile_number"),
		CONTACT_ID("contact_id"), APPLICANT_OPP_ID("applicant_opportunity_id"), CREATE_DATETIME("create_datetime"),
		UPDATE_DATETIME("update_datetime"), IMAGE_URL("image_url"), SOURCE("source");

		public final String value;

		SFCoApObjectColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum SFMainObjectColumn {
		ID("id"), USER_ID("user_id"), CUSTOMER_NAME("customer_name"), CUSTOMER_MOBILE_NUMBER("customer_mobile_number"),
		LEAD_STAGE("lead_stage"), LEAD_ID("lead_id"), ACCOUNT_ID("account_id"), CONTACT_ID("contact_id"),
		OPPORTUNITY_ID("opportunity_id"), CREATE_DATETIME("create_datetime"), UPDATE_DATETIME("update_datetime"),
		IMAGE_URL("image_url"), SOURCE("source");

		public final String value;

		SFMainObjectColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum SFLeadStage {
		CREATED("created"), CONVERTED("converted");

		public final String value;

		SFLeadStage(String stringValue) {
			this.value = stringValue;
		}

		public static SFLeadStage get(String value) {
			for (SFLeadStage item : SFLeadStage.values()) {
				if (item.value.equalsIgnoreCase(value))
					return item;
			}
			return CREATED;
		}
	}

	public enum SecondaryInfoColumn {
		ID("id"), USER_ID("user_id"), APP_VERSION("app_version"), DEVICE_TYPE("device_type"), DEVICE_ID("device_id"),
		FCM_KEY("fcm_key"), APNS_KEY("apns_key"), IMAGE_URL("image_url"), BRANCH_NAME("branch_name"), BRANCH_STREET("branch_street"),
		BRANCH_CITY("branch_city"), BRANCH_STATE("branch_state"), BRANCH_PINCODE("branch_pincode"),
		ABOUT_ME("about_me");

		public final String value;

		SecondaryInfoColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum BankInfoColumn {
		SF_RECORD_ID("sf_record_id"), SF_CONTACT_ID("sf_contact_id"), BANK_NAME("bank_name");

		public final String value;

		BankInfoColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum ScoreWeightageColumn {
		CONTEST_NAME("contest_name"), START_DATETIME("start_datetime"), END_DATETIME("end_datetime"), TARGET("target"),
		LEAD_CREATED("lead_created"), LEAD_CONVERTED("lead_converted"), KYC_DOCUMENT("kyc_document"),
		UTILITY_BILL("utility_bill"), VEHICLE_RC("vehicle_rc"), GSTIN("gstin"), EPF("epf"), ITR("itr"),
		PAYMENT("payment"), BANK_STATEMENT("bank_statement"), TASK_COMPLETED("task_completed"), VISIT_COMPLETED("visit_completed"),
		TASK_MAX_POINTS("task_max_points");

		public final String value;

		ScoreWeightageColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum RegionMapColumn {
		CURRENT_LOCATION("current_location"), CLUSTER("cluster"), USER_EMAIL_ID("user_email_id"), NAME("user_name"),
		REGION("region"), DESIGNATION("designation");

		public final String value;

		RegionMapColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum LeaderBoardHistoryColumn {
		TARGET("target"), LEAD_CREATED("lead_created"), LEAD_CONVERTED("lead_converted"), KYC_DOCUMENT("kyc_document"),
		CO_APPLICANT_ADDED("co_applicant_added"), UTILITY_BILL("utility_bill"), VEHICLE_RC("vehicle_rc"),
		GSTIN("gstin"), EPF("epf"), ITR("itr"), PAYMENT("payment"), BANK_STATEMENT("bank_statement"),
		TASK_COMPLETED("task_completed"), VISIT_COMPLETED("visit_completed"), FINAL_SCORE("final_score"), RATING("rating"), RANK("u_rank"),
		MONTH_YEAR("month_year"), LAST_UPDATED_DATETIME("last_updated_datetime");

		public final String value;

		LeaderBoardHistoryColumn(String stringValue) {
			this.value = stringValue;
		}
	}

	public enum LoginInfoColumn {

		USER_ID("user_id"), LOGIN_DATETIME("login_datetime"), IP_ADDRESS("ip_address"), DEVICE_ID("device_id"),
		DEVICE_TYPE("device_type"), DEVICE_MODEL("device_model"), APP_VERSION("app_version"), OS_VERSION("os_version");

		public final String value;

		LoginInfoColumn(String value) {
			this.value = value;
		}

	}

	public enum InstalledAppsColumn {

		USER_ID("user_id"), APP_NAME("app_name"), PACKAGE_NAME("package_name"), VERSION_NAME("version_name"),
		VERSION_CODE("version_code"), DATETIME("datetime"), RAW_DATA("raw_data");

		public final String value;

		InstalledAppsColumn(String value) {
			this.value = value;
		}

	}

	public enum AdminUserColumn {

		NAME("name"), EMAIL("email"), SF_USER_ID("sf_user_id"), IMAGE_URL("image_url"), PASSWORD("password"),
		PASSCODE("passcode"), COUNTRY_CODE("country_code"), MOBILE_NUMBER("mobile_number"),
		MOBILE_VERIFIED("mobile_verified"), REGISTRATION_DATETIME("registeration_datetime"),
		NOTIFICATOIN_ALLOWED("notification_allowed"), ROLE("role");

		public final String stringValue;

		AdminUserColumn(String stringValue) {
			this.stringValue = stringValue;
		}
	}

	public enum AdminSecondaryInfoColumn {

		ID("id"), USER_ID("user_id"), LOGIN_INFO("login_info"), DEVICE_TYPE("device_type"), DEVICE_ID("device_id"),
		DEVICE_MODEL("device_model"), APP_VERSION("app_version"), OS_VERSION("os_version"), APNS_KEY("apns_key"),
		FCM_KEY("fcm_key"), PASSWORD_CHANGE_DATETIME("password_change_datetime"),
		MOBILE_NUMBER_CHANGE_DATETIME("mobile_number_change_datetime");

		public final String value;

		AdminSecondaryInfoColumn(String value) {
			this.value = value;
		}
	}

	public enum AdminLoginInfoColumn {

		USER_ID("user_id"), LOGIN_DATETIME("login_datetime"), IP_ADDRESS("ip_address"), DEVICE_ID("device_id"),
		DEVICE_TYPE("device_type"), DEVICE_MODEL("device_model"), APP_VERSION("app_version"), OS_VERSION("os_version");

		public final String value;

		AdminLoginInfoColumn(String value) {
			this.value = value;
		}

	}

	public enum AdminLogInfoColumn {

		ID("id"), USER_ID("user_id"), RECORD_TYPE("record_type"), RECORD_ID("record_id"), ACTION("action"),
		DESCRIPTION("description"), DATETIME("datetime");

		public final String value;

		AdminLogInfoColumn(String value) {
			this.value = value;
		}

	}

	public enum TaskNActivityColumn {

		SF_TASK_ID("sf_task_id"), SF_OBJECT_ID("sf_object_id"), SF_OBJECT_TYPE("sf_object_type"),
		SF_OBJECT_NAME("sf_object_name"), SUBJECT("subject"), TYPE("type"), ACTION_RESULT("action_result"),
		TASK_DESCRIPTION("task_description"), RECORD_DATA("record_data"), PRIORITY("priority"), STATUS("status"),
		IS_REMINDER_SET("is_reminder_set"), REMINDER_DATETIME("reminder_datetime"),
		IS_FOLLOW_UP_SCHEDULED("is_follow_up_scheduled"), FOLLOW_UP_DATETIME("follow_up_datetime"),
		SF_OWNER_ID("sf_owner_id"), SF_OWNER_NAME("sf_owner_name"), SF_CREATED_BY_ID("sf_created_by_id"),
		SF_CREATED_BY_NAME("sf_created_by_name");

		public final String value;

		TaskNActivityColumn(String value) {
			this.value = value;
		}

	}

	public enum WhiteListColumn {
		ORG_ID("org_id"), NAME("name"), IP_ADDRESS("ip_address"), IS_ACTIVE("is_active");

		public final String value;

		WhiteListColumn(String value) {
			this.value = value;
		}
	}

	// =============== NOTIFICATION TABLES ABD COLUMNS ================ //
	// ================================================================ //

	public enum NotificationColumn {

		TITLE("title"), MESSAGE("message"), BIG_MESSAGE("big_message"), IMAGE_URL("image_url"), WEB_URL("web_url"),
		DATA("data"), AUDIENCE_TYPE("audience_type"), ADIENCE_GROUP("audience_group"), PRIORITY("priority"),
		PLATFORM("platform"), KIND("kind"), ON_CLICK_ACTION("on_click_action"), SCREEN_TO_OPEN("screen_to_open"),
		SCHEDULE_TYPE("schedule_type"), SCHEDULE_DATETIME("schedule_datetime"), SENT_DATETIME("sent_datetime"),
		CREATE_DATETIME("create_datetime"), IS_SCHEDULED("is_scheduled"), TOTAL_COUNT("total_count"),
		SUCCESS_COUNT("success_count"), FAILURE_COUNT("failure_count"), SCHEDULER_ID("scheduler_id"),
		SCHEDULER_NAME("scheduler_name");

		public final String stringValue;

		NotificationColumn(String stringValue) {
			this.stringValue = stringValue;
		}
	}

	public enum UserNotificationInfoColumn {

		NOTIFICATION_ID("notification_id"), DYNAMIC_MESSAGE("dynamic_message"), USER_ID("user_id"),
		HAS_READ("has_read"), READ_DATETIME("read_datetime"), DEVICE_ID("device_id"), DEVICE_TYPE("device_type"),
		DEVICE_MODEL("device_model"), APP_VERSION("app_version"), OS_VERSION("os_version");

		public final String value;

		UserNotificationInfoColumn(String value) {
			this.value = value;
		}

	}

	// ************ END OF NOTIFICATION TABLES AND COLUMNS ************ //
	// **************************************************************** //

	public enum PartnerColumn {
		ORG_NAME("orgName"), CLIENT_ID("clientId"), CLIENT_SECRET("clientSecret"), IP_RESTRICTED("ipRestricted"),
		SESSION_ENABLED("sessionEnabled"), SERVICES_ALLOWED("servicesAllowed"), DESTINATION("destination"),
		LEAD_SOURCE("leadSource"), LEAD_OWNER("leadOwner"), IS_INTERNAL("isInternal");

		public final String value;

		PartnerColumn(String value) {
			this.value = value;
		}
	}

}