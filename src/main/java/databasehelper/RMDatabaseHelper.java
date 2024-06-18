package databasehelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import dao.DataProvider;
import manager.PaymentManager;
import manager.PaymentManager.PaymentUpdateType;
import models.LeaderBoardItem;
import models.LeaderboardFilter;
import models.PaymentInfo;
import models.SFApplicant;
import models.SFCoApplicant;
import models.ScoreWeightage;
import models.User;
import utils.Constants;
import utils.Constants.ApplicantSource;
import utils.DateTimeUtils;
import utils.LeaderboardUtils;
import utils.LoggerUtils;

public class RMDatabaseHelper {

	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

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

	public RMDatabaseHelper() {}

	private void checkConnection() throws SQLException {
		if (null == connection || !connection.isValid(10))
			connection = DataProvider.getDataSource().getConnection();
	}

	// ================ PAYMENT IMPLEMENTATION =================== //

	public PaymentInfo initalizePayment(PaymentInfo checkout) throws SQLException {

		checkConnection();

		String query = "INSERT INTO " + ColumnsNFields.PAYMENT_INFO_TABLE + 
				"(" + 
				ColumnsNFields.PaymentInfoColumn.TRANSACTION_ID.value + "," +
				ColumnsNFields.PaymentInfoColumn.USER_ID.value + "," +
				ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_ID.value + "," +
				ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_NUMBER.value + "," +			
				ColumnsNFields.PaymentInfoColumn.LOAN_ACCOUNT_NUMBER.value + "," +
				ColumnsNFields.PaymentInfoColumn.CURRENCY.value + "," +
				ColumnsNFields.PaymentInfoColumn.AMOUNT.value + "," +
				ColumnsNFields.PaymentInfoColumn.INITIAL_DATETIME.value + "," +				
				ColumnsNFields.PaymentInfoColumn.PAYMENT_STATUS.value + "," +
				ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value + "," +
				ColumnsNFields.PaymentInfoColumn.SF_PAYMENT_TYPE.value + "," +
				ColumnsNFields.PaymentInfoColumn.SF_PAYMENT_SUB_TYPE.value + "," +								
				ColumnsNFields.PaymentInfoColumn.PAYMENT_METHOD.value + "," +
				ColumnsNFields.PaymentInfoColumn.DEVICE_TYPE.value + "," +
				ColumnsNFields.PaymentInfoColumn.DEVICE_ID.value + "," +
				ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_NAME.value + "," +
				ColumnsNFields.PaymentInfoColumn.SF_CLOSURE_REASON.value + "," +
				ColumnsNFields.PaymentInfoColumn.SF_TRANSFERRED_HFC_NAME.value +
				") " + 
				"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		preparedStatement = connection.prepareStatement(query);

		String transactionId = "rm_TXN" + System.currentTimeMillis();
		String initialDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		preparedStatement.setString(1, transactionId);
		preparedStatement.setInt(2, checkout.userId);
		preparedStatement.setString(3, checkout.sfOpportunityId);
		preparedStatement.setString(4, checkout.sfOpportunityNumber);
		preparedStatement.setString(5, checkout.sfLoanAccountNumber);
		preparedStatement.setString(6, checkout.currency);
		preparedStatement.setDouble(7, checkout.amount);
		preparedStatement.setString(8, initialDatetime);
		preparedStatement.setString(9, checkout.paymentStatus);
		preparedStatement.setString(10, checkout.receiptStatus);
		preparedStatement.setString(11, checkout.sfPaymentNature);

		String paymentSubType;		
		if (checkout.sfPaymentNature.equalsIgnoreCase(PaymentManager.PARTIAL_PRE_PAYMENT))
			paymentSubType = checkout.sfPartPrePaymentType;
		else 
			paymentSubType = checkout.sfPaymentSubType;

		if (paymentSubType.length() > 256)
			paymentSubType = paymentSubType.substring(0,  256);

		preparedStatement.setString(12, paymentSubType);				

		preparedStatement.setString(13, checkout.paymentMethod);
		preparedStatement.setString(14, checkout.deviceType);
		preparedStatement.setString(15, checkout.deviceId);

		String opportunityName = checkout.sfOpportunityName;
		if (opportunityName.length() > 64) 
			opportunityName = opportunityName.substring(0, 64);

		preparedStatement.setString(16, opportunityName);
		preparedStatement.setString(17, checkout.sfClosureReason);
		preparedStatement.setString(18, checkout.sfTransferredHFCName);

		int status = preparedStatement.executeUpdate();

		if (status == 1) {
			checkout.transactionId = transactionId; 
			checkout.initialDatetime = initialDatetime;
			return checkout;			
		} else return null;

	}

	public PaymentInfo updatePaymentInfo(PaymentUpdateType updateType, PaymentInfo checkout) throws SQLException {

		checkConnection();


		String query = "UPDATE " + ColumnsNFields.PAYMENT_INFO_TABLE + 
				" SET " + 							
				ColumnsNFields.PaymentInfoColumn.PAYMENT_STATUS.value + "=?," +
				ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value + "=?," +				
				ColumnsNFields.PaymentInfoColumn.PAYMENT_ID.value + "=?," +
				ColumnsNFields.PaymentInfoColumn.RECEIPT_DATA.value + "=?," +				
				ColumnsNFields.PaymentInfoColumn.STATUS_MESSAGE.value + "=?," +
				ColumnsNFields.PaymentInfoColumn.COMPLETION_DATETIME.value + "=?" +
				" WHERE " + ColumnsNFields.PaymentInfoColumn.TRANSACTION_ID.value + "=?";

		preparedStatement = connection.prepareStatement(query);

		String completionDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		preparedStatement.setString(1, checkout.paymentStatus);
		preparedStatement.setString(2, checkout.receiptStatus);
		preparedStatement.setString(3, checkout.pgPaymentId);

		String receiptData = checkout.pgPaymentData;
		if (receiptData.length() > 2000)
			receiptData = receiptData.substring(0,  2000);

		preparedStatement.setString(4, receiptData);

		String statusMessage = "Payment: " + checkout.paymentStatus + " | Receipt: " + checkout.receiptStatus;
		preparedStatement.setString(5, statusMessage);

		preparedStatement.setString(6, completionDatetime);
		preparedStatement.setString(7, checkout.transactionId);		

		int status = preparedStatement.executeUpdate();

		if (status == 1) {
			checkout.completionDatetime = completionDatetime;
			return checkout;			
		} else return null;

	}

	public PaymentInfo updateReceiptInfo(PaymentUpdateType updateType, PaymentInfo checkout) throws SQLException {

		checkConnection();


		String query = "UPDATE " + ColumnsNFields.PAYMENT_INFO_TABLE + 
				" SET " + 							
				ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value + "=?," +
				ColumnsNFields.PaymentInfoColumn.RECEIPT_ID.value + "=?," +
				ColumnsNFields.PaymentInfoColumn.RECEIPT_NUMBER.value + "=?," +
				ColumnsNFields.PaymentInfoColumn.STATUS_MESSAGE.value + "=?," +
				ColumnsNFields.PaymentInfoColumn.COMPLETION_DATETIME.value + "=?" +
				" WHERE " + ColumnsNFields.PaymentInfoColumn.TRANSACTION_ID.value + "=?";

		preparedStatement = connection.prepareStatement(query);

		String statusMessage = "Payment: " + checkout.paymentStatus + " | Receipt: " + checkout.receiptStatus;

		preparedStatement.setString(1, checkout.receiptStatus);
		preparedStatement.setString(2, checkout.sfReceiptId);
		preparedStatement.setString(3, checkout.sfReceiptNumber);
		preparedStatement.setString(4, statusMessage);
		preparedStatement.setString(5, checkout.completionDatetime);
		preparedStatement.setString(6, checkout.transactionId);		

		int status = preparedStatement.executeUpdate();

		if (status == 1) return checkout;			
		else return null;

	}

	public ArrayList<PaymentInfo> getPendingPayments(int userId) throws SQLException {

		checkConnection();


		String query = "select * from " + ColumnsNFields.PAYMENT_INFO_TABLE + 
				" where " + ColumnsNFields.PaymentInfoColumn.USER_ID.value + "=?" +
				" and " + ColumnsNFields.PaymentInfoColumn.PAYMENT_STATUS.value + "='" + PaymentManager.PaymentStatus.SUCCESS.value + "'" +
				" and (" + ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value + "='" + PaymentManager.PaymentStatus.PENDING.value + "'" +
				" or " + ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value + "='" + PaymentManager.PaymentStatus.FAILED.value + "')";

		preparedStatement = connection.prepareStatement(query);		
		preparedStatement.setInt(1, userId);

		resultSet = preparedStatement.executeQuery();

		ArrayList<PaymentInfo> payments = new ArrayList<PaymentInfo>();

		if (null != resultSet && resultSet.first()) {
			do {
				payments.add(getCheckoutDataFromResultSet(resultSet));
			} while (resultSet.next());
		}

		return payments;

	}

	public PaymentInfo getPaymentInfoById(String transactionId) {

		return new PaymentInfo();

	}

	private PaymentInfo getCheckoutDataFromResultSet(ResultSet resultSet) throws SQLException {

		PaymentInfo checkout = new PaymentInfo();

		checkout.transactionId = resultSet.getString(ColumnsNFields.PaymentInfoColumn.TRANSACTION_ID.value);
		checkout.userId = resultSet.getInt(ColumnsNFields.PaymentInfoColumn.USER_ID.value);
		checkout.sfOpportunityId = resultSet.getString(ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_ID.value);
		checkout.sfOpportunityNumber = resultSet.getString(ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_NUMBER.value);
		checkout.sfOpportunityName = resultSet.getString(ColumnsNFields.PaymentInfoColumn.OPPORTUNITY_NAME.value);
		checkout.sfLoanAccountNumber = resultSet.getString(ColumnsNFields.PaymentInfoColumn.LOAN_ACCOUNT_NUMBER.value);
		checkout.currency = resultSet.getString(ColumnsNFields.PaymentInfoColumn.CURRENCY.value);
		checkout.amount = resultSet.getDouble(ColumnsNFields.PaymentInfoColumn.AMOUNT.value);
		checkout.initialDatetime = resultSet.getString(ColumnsNFields.PaymentInfoColumn.INITIAL_DATETIME.value);
		checkout.completionDatetime = resultSet.getString(ColumnsNFields.PaymentInfoColumn.COMPLETION_DATETIME.value);
		checkout.pgOrderId = resultSet.getString(ColumnsNFields.PaymentInfoColumn.ORDER_ID.value);
		checkout.pgPaymentId = resultSet.getString(ColumnsNFields.PaymentInfoColumn.PAYMENT_ID.value);
		checkout.paymentStatus = resultSet.getString(ColumnsNFields.PaymentInfoColumn.PAYMENT_STATUS.value);
		checkout.receiptStatus = resultSet.getString(ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value);
		checkout.statusMessage = resultSet.getString(ColumnsNFields.PaymentInfoColumn.STATUS_MESSAGE.value);
		checkout.sfReceiptId = resultSet.getString(ColumnsNFields.PaymentInfoColumn.RECEIPT_ID.value);
		checkout.sfReceiptNumber = resultSet.getString(ColumnsNFields.PaymentInfoColumn.RECEIPT_NUMBER.value);
		checkout.pgPaymentData = resultSet.getString(ColumnsNFields.PaymentInfoColumn.RECEIPT_DATA.value);
		checkout.sfPaymentNature = resultSet.getString(ColumnsNFields.PaymentInfoColumn.SF_PAYMENT_TYPE.value);
		checkout.sfPaymentSubType = resultSet.getString(ColumnsNFields.PaymentInfoColumn.SF_PAYMENT_SUB_TYPE.value);	
		checkout.sfPartPrePaymentType = resultSet.getString(ColumnsNFields.PaymentInfoColumn.SF_PAYMENT_SUB_TYPE.value);	
		checkout.sfClosureReason = resultSet.getString(ColumnsNFields.PaymentInfoColumn.SF_CLOSURE_REASON.value);
		checkout.sfTransferredHFCName = resultSet.getString(ColumnsNFields.PaymentInfoColumn.SF_TRANSFERRED_HFC_NAME.value);		
		checkout.paymentMethod = resultSet.getString(ColumnsNFields.PaymentInfoColumn.PAYMENT_METHOD.value);
		checkout.deviceType = resultSet.getString(ColumnsNFields.PaymentInfoColumn.DEVICE_TYPE.value);
		checkout.deviceId = resultSet.getString(ColumnsNFields.PaymentInfoColumn.DEVICE_ID.value);

		return checkout;

	}

	// ****************** END OF PAYMENT IMPLEMENTATION ******************* //

	// ================ START OF LEADERBOARD IMPLEMENTATION =================== //

	public ArrayList<LeaderBoardItem> getLeaders(String regionFilter, String timeFilter) throws SQLException {						

		boolean isRegionFilterApplicable = LeaderboardUtils.isRegionFilterApplicable(regionFilter);

		StringBuilder sb = new StringBuilder();

		sb.append("select u.id, u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,");
		sb.append("si.image_url profileImageUrl,");

		sb.append("COALESCE(kd.count_id,0) kycDocumentCount, COALESCE(ub.count_id,0) utilityBillCount,"); 
		sb.append("COALESCE(vrc.count_id,0) vehicleRCCount, COALESCE(epf.count_id,0) epfCount,");
		sb.append("COALESCE(gstin.count_id,0) gstinCount, COALESCE(itrv.count_id,0) itrvCount,");				
		sb.append("COALESCE(pi.count_id,0) paymentCount, COALESCE(ap1.count_id,0) createdApCount,");
		sb.append("COALESCE(ap2.count_id,0) convertedApCount, COALESCE(coap.count_id,0) coApCount,");
		sb.append("COALESCE(bi.count_id,0) bankInfoFetchedCount,");

		sb.append("((COALESCE(kd.count_id,0) * COALESCE(sw.kyc_document, dsw.kyc_document)) +"); 
		sb.append("(COALESCE(ub.count_id,0) * COALESCE(sw.utility_bill, dsw.utility_bill)) +"); 
		sb.append("(COALESCE(vrc.count_id,0) * COALESCE(sw.vehicle_rc, dsw.vehicle_rc)) +");
		sb.append("(COALESCE(epf.count_id,0) * COALESCE(sw.epf, dsw.epf)) +");
		sb.append("(COALESCE(gstin.count_id,0) * COALESCE(sw.gstin, dsw.gstin)) +");
		sb.append("(COALESCE(pi.count_id,0) * COALESCE(sw.payment, dsw.payment)) +");
		sb.append("(COALESCE(ap1.count_id,0) * COALESCE(sw.lead_created, dsw.lead_created)) +"); 
		sb.append("(COALESCE(ap2.count_id,0) * COALESCE(sw.lead_converted, dsw.lead_converted)) +");
		sb.append("(COALESCE(coap.count_id,0) * COALESCE(sw.lead_created, dsw.lead_created))  +");
		sb.append("(COALESCE(bi.count_id,0) * COALESCE(sw.bank_statement, dsw.bank_statement)) +"); 
		sb.append("(COALESCE(itrv.count_id,0) * COALESCE(sw.itr, dsw.itr))) total ");

		sb.append("from RMManagementDB.user u ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.kyc_document");
		sb.append(" where datetime like ?");					
		sb.append(" group by user_id) kd on kd.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.utility_bill");
		sb.append(" where datetime like ?");					
		sb.append(" group by user_id) ub on ub.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.vehicle_rc_info");
		sb.append(" where datetime like ?");					
		sb.append(" group by user_id) vrc on vrc.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.epf_detail");
		sb.append(" where datetime like ?");					
		sb.append(" group by user_id) epf on epf.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.gstin_detail");
		sb.append(" where datetime like ?");					
		sb.append(" group by user_id) gstin on gstin.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.itrv_document");
		sb.append(" where datetime like ?");					
		sb.append(" group by user_id) itrv on itrv.user_id = u.id ");

		sb.append("left join (select user_id,count(transaction_id) as count_id from RMManagementDB.payment_info where receipt_status = 'success'");
		sb.append(" and completion_datetime like ? ");
		sb.append(" group by user_id) pi on pi.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'created'");
		sb.append(" and create_datetime like ? ");
		sb.append(" group by user_id) ap1 on ap1.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'converted'");
		sb.append(" and create_datetime like ? ");
		sb.append(" group by user_id) ap2 on ap2.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_co_ap_object");
		sb.append(" where create_datetime like ? ");
		sb.append(" group by user_id) coap on coap.user_id = u.id ");

		sb.append("left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.bank_account_info where create_datetime");
		sb.append(" like ? ");
		//else sb.append(" > '2019-07-01 00:00:01' ");
		sb.append(" group by user_id) bi on bi.user_id = u.id ");

		sb.append("left join (select * from RMManagementDB.score_weightage where start_datetime like ?) sw on sw.start_datetime,");
		sb.append("(select * from RMManagementDB.score_weightage where contest_name = 'default') dsw ");

		if (isRegionFilterApplicable) {
			sb.append(" where u.id in (select id from RMManagementDB.user " + 
					"where email in (SELECT user_email_id FROM RMManagementDB.region_map WHERE cluster = ?)) ");	
		}		

		sb.append(" order by u.id");		

		checkConnection();

		preparedStatement = connection.prepareStatement(sb.toString());		

		preparedStatement.setString(1, timeFilter + "%");
		preparedStatement.setString(2, timeFilter + "%");			
		preparedStatement.setString(3, timeFilter + "%");			
		preparedStatement.setString(4, timeFilter + "%");			
		preparedStatement.setString(5, timeFilter + "%");			
		preparedStatement.setString(6, timeFilter + "%");		
		preparedStatement.setString(7, timeFilter + "%");			
		preparedStatement.setString(8, timeFilter + "%");			
		preparedStatement.setString(9, timeFilter + "%");			
		preparedStatement.setString(10, timeFilter + "%");
		preparedStatement.setString(11, timeFilter + "%");
		preparedStatement.setString(12, timeFilter + "%");

		if (isRegionFilterApplicable) 
			preparedStatement.setString(13, LeaderboardUtils.Region.get(regionFilter).value);

		resultSet = preparedStatement.executeQuery();

		ArrayList<LeaderBoardItem> leaders = new ArrayList<LeaderBoardItem>();

		if (null != resultSet && resultSet.first()) {
			do {

				LeaderBoardItem newItem = getLeaderBoardItemFromRS(resultSet);								
				leaders.add(newItem);

			} while (resultSet.next());
		}

		return leaders;

	}		

	private LeaderBoardItem getLeaderBoardItemFromRS(ResultSet resultSet) throws SQLException {

		LeaderBoardItem leader = new LeaderBoardItem();

		User user = new User();
		user.id = resultSet.getInt("id");
		user.displayName = resultSet.getString("displayName");
		user.sfUserId = resultSet.getString("sfUserId");
		user.email = resultSet.getString("emailId");
		user.profileImageUrl = resultSet.getString("profileImageUrl");

		leader.rmUser = user;
		leader.kycDocumentCount = resultSet.getInt("kycDocumentCount");
		leader.utilityBillCount = resultSet.getInt("utilityBillCount");
		leader.epfCount = resultSet.getInt("epfCount");
		leader.vehicleRCCount = resultSet.getInt("vehicleRCCount");
		leader.gstinCount = resultSet.getInt("gstinCount");
		leader.itrvCount = resultSet.getInt("itrvCount");
		leader.paymentCount = resultSet.getInt("paymentCount");
		leader.createdApCount = resultSet.getInt("createdApCount");
		leader.convertedApCount = resultSet.getInt("convertedApCount");
		leader.coApCount = resultSet.getInt("coApCount");
		leader.bankInfoFetchedCount = resultSet.getInt("bankInfoFetchedCount");
		leader.total = resultSet.getInt("total");		
		leader.points = leader.total;

		return leader;

	}

	public ScoreWeightage getScoreWeigtage(LeaderboardFilter filter) throws SQLException {

		checkConnection();


		if (null == filter || !LeaderboardUtils.isTimeFilterApplicable(filter.time)) {
			return getDefaultScoreWeightage();
		} else {

			String query = "select * from " + ColumnsNFields.SCORE_WEIGHTAGE_TABLE + 
					" where " + ColumnsNFields.ScoreWeightageColumn.START_DATETIME.value + " like ?" +
					" order by " + ColumnsNFields.COMMON_KEY_ID + " desc";

			preparedStatement = connection.prepareStatement(query);			
			preparedStatement.setString(1, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			

			resultSet = preparedStatement.executeQuery();			

			if (null != resultSet && resultSet.first())				
				return getScoreWeightageFromRS(resultSet);			
			return getDefaultScoreWeightage();

		}		

	}

	private ScoreWeightage getDefaultScoreWeightage() throws SQLException {

		checkConnection();


		String query = "select * from " + ColumnsNFields.SCORE_WEIGHTAGE_TABLE + 
				" where " + ColumnsNFields.ScoreWeightageColumn.CONTEST_NAME.value + " = 'default'";

		preparedStatement = connection.prepareStatement(query);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) 
			return getScoreWeightageFromRS(resultSet);

		return null;

	}

	private ScoreWeightage getScoreWeightageFromRS(ResultSet resultSet) throws SQLException {

		ScoreWeightage sWeightage = new ScoreWeightage();

		sWeightage.id = resultSet.getInt(ColumnsNFields.COMMON_KEY_ID);		
		sWeightage.contestName = resultSet.getString(ColumnsNFields.ScoreWeightageColumn.CONTEST_NAME.value);
		sWeightage.startDatetime = resultSet.getString(ColumnsNFields.ScoreWeightageColumn.START_DATETIME.value);
		sWeightage.endDatetime = resultSet.getString(ColumnsNFields.ScoreWeightageColumn.END_DATETIME.value);		
		sWeightage.target = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.TARGET.value);
		sWeightage.leadCreated = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.LEAD_CREATED.value);
		sWeightage.leadConverted = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.LEAD_CONVERTED.value);
		sWeightage.kycDocument = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.KYC_DOCUMENT.value);
		sWeightage.utilityBill = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.UTILITY_BILL.value);
		sWeightage.vehicleRC = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.VEHICLE_RC.value);
		sWeightage.gstin = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.GSTIN.value);
		sWeightage.epf = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.EPF.value);
		sWeightage.itr = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.ITR.value);
		sWeightage.payment = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.PAYMENT.value);
		sWeightage.bankStatement = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.BANK_STATEMENT.value);

		return sWeightage;

	}

	// ****************** END OF LEADER BOARD IMPLEMENTATION ******************* //

	// ================ START OF APPLICANT AND CO-APPLICANT IMPLEMENTATION =================== //

	public boolean insertApplicantInfo(SFApplicant apInfo) throws SQLException {

		checkConnection();


		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		ColumnsNFields.SFLeadStage updateType = ColumnsNFields.SFLeadStage.get(apInfo.leadStage);

		String query;

		if (updateType == ColumnsNFields.SFLeadStage.CONVERTED) {

			query = "INSERT INTO sf_main_object " + 
					"(user_id," + 
					"customer_name," + 
					"customer_mobile_number," + 
					"lead_stage," + 
					"lead_id," + 
					"account_id," + 
					"contact_id," + 
					"opportunity_id," + 
					"source," + 
					"create_datetime," +
					"update_datetime) " + 
					"VALUES(?,?,?,?,?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);		
			preparedStatement.setInt(1, apInfo.userId);
			preparedStatement.setString(2, apInfo.customerName);
			preparedStatement.setString(3, apInfo.customerMobileNumber);
			preparedStatement.setString(4, apInfo.leadStage);
			preparedStatement.setString(5, apInfo.leadId);
			preparedStatement.setString(6, apInfo.accountId);
			preparedStatement.setString(7, apInfo.contactId);
			preparedStatement.setString(8, apInfo.opportunityId);
			preparedStatement.setString(9, ApplicantSource.RM_PRO.value);
			preparedStatement.setString(10, currentDatetime);
			preparedStatement.setString(11, currentDatetime);

		} else {

			query = "INSERT INTO sf_main_object " + 
					"(user_id," + 
					"customer_name," + 
					"customer_mobile_number," + 
					"lead_stage," + 
					"lead_id," + 
					"create_datetime) " + 
					"VALUES(?,?,?,?,?,?)"; 

			preparedStatement = connection.prepareStatement(query);		
			preparedStatement.setInt(1, apInfo.userId);
			preparedStatement.setString(2, apInfo.customerName);
			preparedStatement.setString(3, apInfo.customerMobileNumber);
			preparedStatement.setString(4, apInfo.leadStage);
			preparedStatement.setString(5, apInfo.leadId);
			preparedStatement.setString(6, currentDatetime);

		}

		boolean status = preparedStatement.executeUpdate() == 1;

		if (status) LoggerUtils.log("==> Applicant information has been successfully inserted in DB");
		else LoggerUtils.log("==> Failed to insert Applicant information in DB");

		return status;

	}

	public boolean updateApplicantInfo(SFApplicant apInfo) throws SQLException {

		checkConnection();


		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		String query = "update sf_main_object set " + 
				"user_id=?," + 
				"customer_name=?," + 
				"customer_mobile_number=?," + 
				"lead_stage=?," +  
				"account_id=?," + 
				"contact_id=?," + 
				"opportunity_id=?," + 
				"update_datetime=? " + 
				"where id=?";

		preparedStatement = connection.prepareStatement(query);	

		preparedStatement.setInt(1, apInfo.userId);
		preparedStatement.setString(2, apInfo.customerName);
		preparedStatement.setString(3, apInfo.customerMobileNumber);
		preparedStatement.setString(4, apInfo.leadStage);
		preparedStatement.setString(5, apInfo.accountId);
		preparedStatement.setString(6, apInfo.contactId);
		preparedStatement.setString(7, apInfo.opportunityId);
		preparedStatement.setString(8, currentDatetime);
		preparedStatement.setInt(9, apInfo.id);

		boolean status = preparedStatement.executeUpdate() == 1;

		if (status) LoggerUtils.log("==> Applicant information has been successfully updated in DB");
		else LoggerUtils.log("==> Failed to update Applicant information in DB");

		return status;

	}

	public SFApplicant getApplicantInfoByMobileNumber(String mobileNumber) throws SQLException {

		checkConnection();


		String query = "select * from " + ColumnsNFields.SF_MAIN_OBJECT_TABLE +
				" where " + ColumnsNFields.SFMainObjectColumn.CUSTOMER_MOBILE_NUMBER.value + "=?";

		preparedStatement = connection.prepareStatement(query);		
		preparedStatement.setString(1, mobileNumber);	

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) return getApplicantObjectFromRS(resultSet);			
		else return null;

	}

	private SFApplicant getApplicantObjectFromRS(ResultSet resultSet) throws SQLException {

		SFApplicant applicant = new SFApplicant();

		applicant.id = resultSet.getInt(ColumnsNFields.SFMainObjectColumn.ID.value);
		applicant.userId = resultSet.getInt(ColumnsNFields.SFMainObjectColumn.USER_ID.value);
		applicant.customerName = resultSet.getString(ColumnsNFields.SFMainObjectColumn.CUSTOMER_NAME.value);
		applicant.customerMobileNumber = resultSet.getString(ColumnsNFields.SFMainObjectColumn.CUSTOMER_MOBILE_NUMBER.value);
		applicant.leadStage = resultSet.getString(ColumnsNFields.SFMainObjectColumn.LEAD_STAGE.value);
		applicant.leadId = resultSet.getString(ColumnsNFields.SFMainObjectColumn.LEAD_ID.value);
		applicant.accountId = resultSet.getString(ColumnsNFields.SFMainObjectColumn.ACCOUNT_ID.value);
		applicant.contactId = resultSet.getString(ColumnsNFields.SFMainObjectColumn.CONTACT_ID.value);
		applicant.opportunityId = resultSet.getString(ColumnsNFields.SFMainObjectColumn.OPPORTUNITY_ID.value);
		applicant.createDatetime = resultSet.getString(ColumnsNFields.SFMainObjectColumn.CREATE_DATETIME.value);
		applicant.updateDatetime = resultSet.getString(ColumnsNFields.SFMainObjectColumn.UPDATE_DATETIME.value);

		return applicant;

	}

	public boolean addUpdateCoApInfo(SFCoApplicant apInfo, boolean shouldInsert) throws SQLException {

		checkConnection();


		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		String query;

		if (shouldInsert) {

			query = "INSERT INTO sf_co_ap_object " + 
					"(user_id," + 
					"customer_name," + 
					"customer_mobile_number," + 
					"contact_id," + 
					"applicant_opportunity_id," + 
					"create_datetime) " + 
					"VALUES(?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);		
			preparedStatement.setInt(1, apInfo.userId);
			preparedStatement.setString(2, apInfo.customerName);
			preparedStatement.setString(3, apInfo.customerMobileNumber);
			preparedStatement.setString(4, apInfo.contactId);
			preparedStatement.setString(5, apInfo.applicantOpportunityId);
			preparedStatement.setString(6, currentDatetime);

		} else {

			query = "UPDATE sf_co_ap_object SET " + 
					"user_id=?," +   
					"applicant_opportunity_id=?," + 
					"update_datetime=? " + 
					"where contact_id=? and customer_mobile_number=?"; 

			preparedStatement = connection.prepareStatement(query);		
			preparedStatement.setInt(1, apInfo.userId);
			preparedStatement.setString(2, apInfo.applicantOpportunityId);
			preparedStatement.setString(3, currentDatetime);
			preparedStatement.setString(4, apInfo.contactId);
			preparedStatement.setString(5, apInfo.customerMobileNumber);			

		}

		boolean status = preparedStatement.executeUpdate() == 1;

		if (status) LoggerUtils.log("==> Co-Applicant information has been successfully inserted/updated in DB");
		else LoggerUtils.log("==> Failed to insert Co-Applicant information in DB");

		return status;

	}

	public SFCoApplicant getCoApInfoByMobileNumber(String mobileNumber) throws SQLException {

		checkConnection();


		String query = "select * from " + ColumnsNFields.SF_CO_AP_OBJECT_TABLE +
				" where " + ColumnsNFields.SFCoApObjectColumn.CUSTOMER_MOBILE_NUMBER.value + "=?";

		preparedStatement = connection.prepareStatement(query);		
		preparedStatement.setString(1, mobileNumber);	

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) return getCoApObjectFromRS(resultSet);			
		else return null;

	}

	private SFCoApplicant getCoApObjectFromRS(ResultSet resultSet) throws SQLException {

		SFCoApplicant applicant = new SFCoApplicant();

		applicant.id = resultSet.getInt(ColumnsNFields.SFCoApObjectColumn.ID.value);
		applicant.userId = resultSet.getInt(ColumnsNFields.SFCoApObjectColumn.USER_ID.value);
		applicant.customerName = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.CUSTOMER_NAME.value);
		applicant.customerMobileNumber = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.CUSTOMER_MOBILE_NUMBER.value);
		applicant.contactId = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.CONTACT_ID.value);
		applicant.applicantOpportunityId = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.APPLICANT_OPP_ID.value);
		applicant.createDatetime = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.CREATE_DATETIME.value);
		applicant.updateDatetime = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.UPDATE_DATETIME.value);

		return applicant;

	}

	// ****************** END OF APPLICANT AND CO-APPLICANT IMPLEMENTATION ******************* //

	// ================ START OF USER PROFILE IMPLEMENTATION =================== //

	public boolean addOrUpdateUserProfilePicture(int userId, String imageUrl) throws SQLException {

		checkConnection();


		String query = "update user_secondary_info set image_url = ? where user_id = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, imageUrl);
		preparedStatement.setInt(2, userId);			
		int status = preparedStatement.executeUpdate();

		return status == 1;

	}

	// ****************** END OF USER PROFILE IMPLEMENTATION ******************* //

	// ================ START OF BANK INFO IMPLEMENTATION =================== //

	public void addBankInfo(JSONArray bankInfoArray) throws SQLException {

		checkConnection();


		final int bankInfoSetCount = bankInfoArray.length();
		int bankInfoInsertCount = 0;

		for (int i = 0; i < bankInfoSetCount; i++) {

			System.out.println(i + ": loading...");

			JSONObject currentBankInfo = bankInfoArray.getJSONObject(i);

			int userId = getUserIdFromSfId(currentBankInfo.optString("CreatedById", Constants.NA));			

			if (userId != -1) {						

				String contactId = currentBankInfo.optString("Contact__c", Constants.NA);

				if (!doesBankInfoExists(userId, contactId)) {

					insertNewBankInfo(userId, currentBankInfo);
					bankInfoInsertCount++;					

				}

			}

		}

		LoggerUtils.log("Bank info update status ==> Total: " + bankInfoSetCount + " | Inserted: " + bankInfoInsertCount);

	}

	private boolean insertNewBankInfo(int userId, JSONObject bankInfo) throws SQLException {

		checkConnection();


		String query = "INSERT INTO bank_account_info" + 
				" (user_id," + 
				"sf_record_id," + 
				"sf_contact_id," + 
				"bank_name," +
				ColumnsNFields.COMMON_KEY_CREATE_DATETIME + ")" + 
				" VALUES(?,?,?,?,?);";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userId);
		preparedStatement.setString(2, bankInfo.optString("Id", Constants.NA));
		preparedStatement.setString(3, bankInfo.optString("Contact__c", Constants.NA));
		preparedStatement.setString(4, bankInfo.optString("Account_Bank__c", Constants.NA));

		String createdDatetime;

		String sfCreateDateTime = bankInfo.optString("CreatedDate", Constants.NA);

		try {

			createdDatetime = DateTimeUtils.getDate(
					sfCreateDateTime, 
					DateTimeUtils.DateTimeFormat.yyyy_MM_dd_T_HH_mm_ss_SSSZ.value, 
					"yyyy-MM-dd HH:mm:ss"
					);

		} catch (ParseException e) {
			LoggerUtils.log("Error while parsing datetime fetched from SF ==> FetchedDateTime: " 
					+ sfCreateDateTime + " | Error: " + e.getMessage());
			createdDatetime = DateTimeUtils.getCurrentDateTimeInIST();
		}

		preparedStatement.setString(5, createdDatetime);

		return preparedStatement.executeUpdate() == 1;
	}

	private int getUserIdFromSfId(String sfId) throws SQLException {

		checkConnection();


		String query = "select " + ColumnsNFields.COMMON_KEY_ID + 
				" from " + ColumnsNFields.USER_TABLE + 
				" where " + ColumnsNFields.UserColumn.SF_USER_ID.value + "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, sfId);			
		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return resultSet.getInt(ColumnsNFields.COMMON_KEY_ID);

		return -1;

	}

	private boolean doesBankInfoExists(int userId, String contactId) throws SQLException {

		checkConnection();


		String query = "select * from " + ColumnsNFields.BANK_INFO_TABLE + 
				" where " + ColumnsNFields.BankInfoColumn.SF_CONTACT_ID.value + "=?" +
				" and " + ColumnsNFields.COMMON_KEY_USER_ID + "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, contactId);
		preparedStatement.setInt(2, userId);
		resultSet = preparedStatement.executeQuery();

		return (null != resultSet && resultSet.first());

	}

	// ****************** END OF BANK INFO IMPLEMENTATION ******************* //

}

// ----------------- START OF UDPATED IMPLEMENTATION ------------------------ //

/*
public ArrayList<LeaderBoardItem> getLeaderBoard(LeaderboardFilter filter, ScoreWeightage weightage, int limit) throws SQLException {

	checkConnection();

	ArrayList<LeaderBoardItem> leaders = new ArrayList<LeaderBoardItem>();

	if (null == filter 
			|| (!LeaderboardUtils.isTimeFilterApplicable(filter.time)
			&& !LeaderboardUtils.isRegionFilterApplicable(filter.region))) 
		leaders = prepareLeaderboardDefaultQuery(limit, weightage); 		
	else prepareLeaderboardQueryWithFilters(filter, limit);							

	return leaders;

}

private ArrayList<LeaderBoardItem> prepareLeaderboardDefaultQuery(int limit, ScoreWeightage weightage) throws SQLException {

	checkConnection();

	ArrayList<LeaderBoardItem> leaders = new ArrayList<LeaderBoardItem>();

	StringBuilder sb = new StringBuilder();

	sb.append("select ");
	sb.append(ColumnsNFields.UserColumn.ID.value + ",");
	sb.append(ColumnsNFields.UserColumn.DISPLAY_NAME.value + ",");
	sb.append(ColumnsNFields.UserColumn.SF_USER_ID.value + ","); 
	sb.append(ColumnsNFields.UserColumn.USERNAME.value);
	sb.append(" from " + ColumnsNFields.USER_TABLE);

	preparedStatement = connection.prepareStatement(sb.toString());
	ResultSet userRs =  preparedStatement.executeQuery();

	if (null != userRs && userRs.first()) {
		do {
			LeaderBoardItem leader = new LeaderBoardItem();

			User user = new User();
			user.id = userRs.getInt(ColumnsNFields.UserColumn.ID.value);
			user.displayName = userRs.getString(ColumnsNFields.UserColumn.DISPLAY_NAME.value);
			user.sfUserId = userRs.getString(ColumnsNFields.UserColumn.SF_USER_ID.value);
			user.email = userRs.getString(ColumnsNFields.UserColumn.USERNAME.value);			

			leader.rmUser = user;

			leaders.add(leader);

		} while (userRs.next());
	}

	if (leaders.size() > 0) {

		ArrayList<Integer> userIds = new ArrayList<Integer>();
		StringBuilder userIdSb = new StringBuilder("");

		int leadersSize = leaders.size(); 

		for (int i = 0; i < leadersSize; i++) {
			LeaderBoardItem item = leaders.get(i);
			userIds.add(item.rmUser.id);

			userIdSb.append("" + item.rmUser.id + "");
			if (leadersSize > 1 && i < leadersSize - 1) userIdSb.append(","); 
		}

		LoggerUtils.log("User id string: " + userIdSb.toString());

		sb = new StringBuilder("SELECT ");		
		sb.append(ColumnsNFields.SecondaryInfoColumn.IMAGE_URL.value + ",");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.USER_SECONDARY_INFO_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");						
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet siRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.DOCUMENT_KYC_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet kdRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.UTILITY_BILL);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet ubRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.VEHICLE_RC_INFO_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet vrcRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.EPF_DETAIL_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet epfRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.GSTIN_DETAIL_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet gstRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.ITRV_DOCUMENT_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet itrRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.PaymentInfoColumn.TRANSACTION_ID.value + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.PAYMENT_INFO_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" AND " + ColumnsNFields.PaymentInfoColumn.RECEIPT_STATUS.value + "='success'");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet pRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.SF_MAIN_OBJECT_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" AND " + ColumnsNFields.SFMainObjectColumn.LEAD_STAGE.value + "='created'");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet createdLeadRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.SF_MAIN_OBJECT_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" AND " + ColumnsNFields.SFMainObjectColumn.LEAD_STAGE.value + "='converted'");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet convertedLeadRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.SF_CO_AP_OBJECT_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet coapRs =  preparedStatement.executeQuery();

		sb = new StringBuilder("SELECT COUNT(" + ColumnsNFields.COMMON_KEY_ID + ") count_id,");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID);
		sb.append(" FROM " + ColumnsNFields.BANK_INFO_TABLE);
		sb.append(" WHERE " + ColumnsNFields.COMMON_KEY_USER_ID + " in (" + userIdSb.toString() + ")");
		sb.append(" AND " + ColumnsNFields.COMMON_KEY_CREATE_DATETIME + " > '2019-07-01 00:00:01'");
		sb.append(" GROUP BY " + ColumnsNFields.COMMON_KEY_USER_ID);			
		checkConnection();			
		preparedStatement = connection.prepareStatement(sb.toString());
		ResultSet bankRs =  preparedStatement.executeQuery();

		ArrayList<IdTextPair> imageInfo = new ArrayList<>();			
		if (null != siRs && siRs.first()) {
			do {

				IdTextPair pair = new IdTextPair();
				pair.id = siRs.getInt(ColumnsNFields.COMMON_KEY_USER_ID);
				pair.text = siRs.getString(ColumnsNFields.SecondaryInfoColumn.IMAGE_URL.value);
				imageInfo.add(pair);

			} while (siRs.next());
		}

		ArrayList<IdCountPair> kycInfo = getIDCountFromRS(kdRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> ubInfo = getIDCountFromRS(ubRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> vrcInfo = getIDCountFromRS(vrcRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> epfInfo = getIDCountFromRS(epfRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> gstInfo = getIDCountFromRS(gstRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> itrInfo = getIDCountFromRS(itrRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> paymentInfo = getIDCountFromRS(pRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> createdLeads = getIDCountFromRS(createdLeadRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> convertedLeads = getIDCountFromRS(convertedLeadRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> coapInfo = getIDCountFromRS(coapRs, ColumnsNFields.COMMON_KEY_USER_ID);
		ArrayList<IdCountPair> bankInfo = getIDCountFromRS(bankRs, ColumnsNFields.COMMON_KEY_USER_ID);

		for (LeaderBoardItem leader: leaders) {

			Predicate<IdTextPair> imagePredicate = idTextP -> idTextP.id == leader.rmUser.id;
			Predicate<IdCountPair> idCountPredicate = idCountP -> idCountP.id == leader.rmUser.id;

			IdTextPair userImage = imageInfo.stream().findFirst().filter(imagePredicate).get();				
			leader.rmUser.profileImageUrl = userImage.text;

			IdCountPair kyc = kycInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = kyc.count;

			IdCountPair ub = ubInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = ub.count;

			IdCountPair vrc = vrcInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = vrc.count;

			IdCountPair epf = epfInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = epf.count;

			IdCountPair gst = gstInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = gst.count;

			IdCountPair itr = itrInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = itr.count;

			IdCountPair payments = paymentInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = payments.count;

			IdCountPair created = createdLeads.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = created.count;

			IdCountPair converted = convertedLeads.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = converted.count;

			IdCountPair coap = coapInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = coap.count;

			IdCountPair bank = bankInfo.stream().findFirst().filter(idCountPredicate).get();
			leader.kycDocumentCount = bank.count;											

			if (null != weightage) leader.calculatePointsWithWeightage(weightage);
			else leader.calculateDefaultPoints();

		}			

	}

	return leaders;

}

private ArrayList<IdCountPair> getIDCountFromRS(ResultSet rs, String ID) throws SQLException {

	ArrayList<IdCountPair> pairs = new ArrayList<>();

	if (null != rs && rs.first()) {
		do {
			IdCountPair pair = new IdCountPair();
			pair.id = rs.getInt(ID);
			pair.count = rs.getInt("count_id");
			pairs.add(pair);

		} while (rs.next());
	}

	return pairs;

}
 */

// ----------------- END OF UPDATED IMPLEMENTATION -------------------------- //

/*
public ArrayList<LeaderBoardItem> getLeaderBoard(LeaderboardFilter filter, ScoreWeightage weightage, int limit) throws SQLException {

	checkConnection();		

	if (null == filter 
			|| (!LeaderboardUtils.isTimeFilterApplicable(filter.time)
			&& !LeaderboardUtils.isRegionFilterApplicable(filter.region))) 
		prepareLeaderboardDefaultQuery(limit); 		
	else prepareLeaderboardQueryWithFilters(filter, limit);

	resultSet = preparedStatement.executeQuery();

	ArrayList<LeaderBoardItem> leaders = new ArrayList<LeaderBoardItem>();

	if (null != resultSet && resultSet.first()) {
		do {
			LeaderBoardItem newItem = getLeaderBoardItemFromRS(resultSet);

			if (null != weightage) newItem.calculatePointsWithWeightage(weightage);
			else newItem.calculateDefaultPoints();

			leaders.add(newItem);
		} while (resultSet.next());
	}

	return leaders;

}

private void prepareLeaderboardDefaultQuery(int limit) throws SQLException {

	String query = "select u.id,u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,si.image_url profileImageUrl," + 
			"COALESCE(kd.count_id,0) kycDocumentCount, COALESCE(ub.count_id,0) utilityBillCount," + 
			"COALESCE(vrc.count_id,0) vehicleRCCount, COALESCE(epf.count_id,0) epfCount," + 
			"COALESCE(gstin.count_id,0) gstinCount, COALESCE(itrv.count_id,0) itrvCount," + 
			"COALESCE(pi.count_id,0) paymentCount, COALESCE(ap1.count_id,0) createdApCount," + 
			"COALESCE(ap2.count_id,0) convertedApCount, COALESCE(coap.count_id,0) coApCount," + 
			"COALESCE(bi.count_id,0) bankInfoFetchedCount," + 
			"(COALESCE(kd.count_id,0) + COALESCE(ub.count_id,0) + COALESCE(vrc.count_id,0) +" + 
			"COALESCE(epf.count_id,0) + COALESCE(gstin.count_id,0) +" + 
			"COALESCE(pi.count_id,0) + COALESCE(ap1.count_id,0) + COALESCE(ap2.count_id,0) +" + 
			"COALESCE(coap.count_id,0)  + COALESCE(bi.count_id,0) + COALESCE(itrv.count_id,0)) total " + 
			"from RMManagementDB.user u " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.kyc_document group by user_id) kd on kd.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.utility_bill group by user_id) ub  on ub.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.vehicle_rc_info group by user_id) vrc  on vrc.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.epf_detail group by user_id) epf  on epf.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.gstin_detail group by user_id) gstin on gstin.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.itrv_document group by user_id) itrv on itrv.user_id = u.id " + 
			"left join (select user_id,count(transaction_id) as count_id from RMManagementDB.payment_info where receipt_status = 'success' group by user_id) pi on pi.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'created' group by user_id) ap1 on ap1.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'converted' group by user_id) ap2 on ap2.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.sf_co_ap_object group by user_id) coap on coap.user_id = u.id " + 
			"left join (select user_id,count(id) count_id from RMManagementDB.bank_account_info where create_datetime > '2019-07-01 00:00:01' group by user_id) bi on bi.user_id = u.id " +
			"left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id " +
			"order by total desc";		

	if (limit != -1) query += " limit ?";

	preparedStatement = connection.prepareStatement(query);
	if (limit != -1) preparedStatement.setInt(1, limit);

}

private void prepareLeaderboardQueryWithFilters(LeaderboardFilter filter, int limit) throws SQLException {

	boolean isTimeFilterApplicable = LeaderboardUtils.isTimeFilterApplicable(filter.time);
	boolean isRegionFilterApplicable = LeaderboardUtils.isRegionFilterApplicable(filter.region);

	StringBuilder sb = new StringBuilder();

	sb.append("select u.id, u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,");
	sb.append("si.image_url profileImageUrl,");

	sb.append("COALESCE(kd.count_id,0) kycDocumentCount, COALESCE(ub.count_id,0) utilityBillCount,"); 
	sb.append("COALESCE(vrc.count_id,0) vehicleRCCount, COALESCE(epf.count_id,0) epfCount,");
	sb.append("COALESCE(gstin.count_id,0) gstinCount, COALESCE(itrv.count_id,0) itrvCount,");				
	sb.append("COALESCE(pi.count_id,0) paymentCount, COALESCE(ap1.count_id,0) createdApCount,");
	sb.append("COALESCE(ap2.count_id,0) convertedApCount, COALESCE(coap.count_id,0) coApCount,");
	sb.append("COALESCE(bi.count_id,0) bankInfoFetchedCount,");

	sb.append("(COALESCE(kd.count_id,0) + COALESCE(ub.count_id,0) + COALESCE(vrc.count_id,0) +");
	sb.append("COALESCE(epf.count_id,0) + COALESCE(gstin.count_id,0) +");				
	sb.append("COALESCE(pi.count_id,0) + COALESCE(ap1.count_id,0) + COALESCE(ap2.count_id,0) +");
	sb.append("COALESCE(coap.count_id,0)  + COALESCE(bi.count_id,0) + COALESCE(itrv.count_id,0)) total ");

	sb.append("from RMManagementDB.user u ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.kyc_document");
	if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
	sb.append(" group by user_id) kd on kd.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.utility_bill");
	if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
	sb.append(" group by user_id) ub on ub.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.vehicle_rc_info");
	if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
	sb.append(" group by user_id) vrc on vrc.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.epf_detail");
	if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
	sb.append(" group by user_id) epf on epf.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.gstin_detail");
	if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
	sb.append(" group by user_id) gstin on gstin.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.itrv_document");
	if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
	sb.append(" group by user_id) itrv on itrv.user_id = u.id ");

	sb.append("left join (select user_id,count(transaction_id) as count_id from RMManagementDB.payment_info where receipt_status = 'success'");
	if (isTimeFilterApplicable) sb.append(" and completion_datetime like ? ");
	sb.append(" group by user_id) pi on pi.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'created'");
	if (isTimeFilterApplicable) sb.append(" and create_datetime like ? ");
	sb.append(" group by user_id) ap1 on ap1.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'converted'");
	if (isTimeFilterApplicable) sb.append(" and create_datetime like ? ");
	sb.append(" group by user_id) ap2 on ap2.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_co_ap_object");
	if (isTimeFilterApplicable) sb.append(" where create_datetime like ? ");
	sb.append(" group by user_id) coap on coap.user_id = u.id ");

	sb.append("left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id ");

	sb.append("left join (select user_id,count(id) count_id from RMManagementDB.bank_account_info where create_datetime");
	if (isTimeFilterApplicable) sb.append(" like ? ");
	else sb.append(" > '2019-07-01 00:00:01' ");
	sb.append(" group by user_id) bi on bi.user_id = u.id ");

	if (isRegionFilterApplicable) {
		sb.append(" where u.id in (select id from RMManagementDB.user " + 
				"where email in (SELECT user_email_id FROM RMManagementDB.region_map WHERE cluster = ?)) ");	
	}		

	sb.append(" order by total desc");

	if (limit != -1) sb.append(" limit ?");

	preparedStatement = connection.prepareStatement(sb.toString());

	if (isTimeFilterApplicable) {

		preparedStatement.setString(1, DateTimeUtils.Month.get(filter.time).dateFormat + "%");
		preparedStatement.setString(2, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
		preparedStatement.setString(3, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
		preparedStatement.setString(4, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
		preparedStatement.setString(5, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
		preparedStatement.setString(6, DateTimeUtils.Month.get(filter.time).dateFormat + "%");		
		preparedStatement.setString(7, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
		preparedStatement.setString(8, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
		preparedStatement.setString(9, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
		preparedStatement.setString(10, DateTimeUtils.Month.get(filter.time).dateFormat + "%");
		preparedStatement.setString(11, DateTimeUtils.Month.get(filter.time).dateFormat + "%");

		if (isRegionFilterApplicable) {
			preparedStatement.setString(12, LeaderboardUtils.Region.get(filter.region).value);			
			if (limit != -1) preparedStatement.setInt(13, limit);
		} else {
			if (limit != -1) preparedStatement.setInt(12, limit);
		}			

	} else {
		if (isRegionFilterApplicable) {
			preparedStatement.setString(1, LeaderboardUtils.Region.get(filter.region).value);
			if (limit != -1) preparedStatement.setInt(2, limit);
		} else {
			if (limit != -1) preparedStatement.setInt(1, limit);	
		}
	}		

}
 */

/*
public LeaderBoardItem getLeaderBoardForUser(int userId, LeaderboardFilter filter, ScoreWeightage weightage) throws SQLException {

	if (null == connection)
		connection = DataProvider.getDataSource().getConnection();

	String query;

	if (null == filter || !LeaderboardUtils.isTimeFilterApplicable(filter.time)) {

		query = "select u.id,u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,si.image_url profileImageUrl," + 
				"COALESCE(kd.count_id,0) kycDocumentCount, COALESCE(ub.count_id,0) utilityBillCount," + 
				"COALESCE(vrc.count_id,0) vehicleRCCount, COALESCE(epf.count_id,0) epfCount," + 
				"COALESCE(gstin.count_id,0) gstinCount, COALESCE(itrv.count_id,0) itrvCount," + 
				"COALESCE(pi.count_id,0) paymentCount, COALESCE(ap1.count_id,0) createdApCount," + 
				"COALESCE(ap2.count_id,0) convertedApCount, COALESCE(coap.count_id,0) coApCount," + 
				"COALESCE(bi.count_id,0) bankInfoFetchedCount," + 
				"(COALESCE(kd.count_id,0) + COALESCE(ub.count_id,0) + COALESCE(vrc.count_id,0) +" + 
				"COALESCE(epf.count_id,0) + COALESCE(gstin.count_id,0) +" + 
				"COALESCE(pi.count_id,0) + COALESCE(ap1.count_id,0) + COALESCE(ap2.count_id,0) +" + 
				"COALESCE(coap.count_id,0)  + COALESCE(bi.count_id,0) + COALESCE(itrv.count_id,0)) total " + 
				"from RMManagementDB.user u " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.kyc_document group by user_id) kd on kd.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.utility_bill group by user_id) ub  on ub.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.vehicle_rc_info group by user_id) vrc  on vrc.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.epf_detail group by user_id) epf  on epf.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.gstin_detail group by user_id) gstin on gstin.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.itrv_document group by user_id) itrv on itrv.user_id = u.id " + 
				"left join (select user_id,count(transaction_id) as count_id from RMManagementDB.payment_info where receipt_status = 'success' group by user_id) pi on pi.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'created' group by user_id) ap1 on ap1.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'converted' group by user_id) ap2 on ap2.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.sf_co_ap_object group by user_id) coap on coap.user_id = u.id " + 
				"left join (select user_id,count(id) count_id from RMManagementDB.bank_account_info where create_datetime > '2019-07-01 00:00:01' group by user_id) bi on bi.user_id = u.id " +
				"left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id " +
				"where u.id = ? order by total desc";

		preparedStatement = connection.prepareStatement(query);

		preparedStatement.setInt(1, userId);

	} else {

		boolean isTimeFilterApplicable = LeaderboardUtils.isTimeFilterApplicable(filter.time);

		StringBuilder sb = new StringBuilder();

		sb.append("select u.id, u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,");
		sb.append("si.image_url profileImageUrl,");

		sb.append("COALESCE(kd.count_id,0) kycDocumentCount, COALESCE(ub.count_id,0) utilityBillCount,"); 
		sb.append("COALESCE(vrc.count_id,0) vehicleRCCount, COALESCE(epf.count_id,0) epfCount,");
		sb.append("COALESCE(gstin.count_id,0) gstinCount, COALESCE(itrv.count_id,0) itrvCount,");				
		sb.append("COALESCE(pi.count_id,0) paymentCount, COALESCE(ap1.count_id,0) createdApCount,");
		sb.append("COALESCE(ap2.count_id,0) convertedApCount, COALESCE(coap.count_id,0) coApCount,");
		sb.append("COALESCE(bi.count_id,0) bankInfoFetchedCount,");

		sb.append("(COALESCE(kd.count_id,0) + COALESCE(ub.count_id,0) + COALESCE(vrc.count_id,0) +");
		sb.append("COALESCE(epf.count_id,0) + COALESCE(gstin.count_id,0) +");				
		sb.append("COALESCE(pi.count_id,0) + COALESCE(ap1.count_id,0) + COALESCE(ap2.count_id,0) +");
		sb.append("COALESCE(coap.count_id,0)  + COALESCE(bi.count_id,0) + COALESCE(itrv.count_id,0)) total ");

		sb.append("from RMManagementDB.user u ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.kyc_document");
		if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
		sb.append(" group by user_id) kd on kd.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.utility_bill");
		if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
		sb.append(" group by user_id) ub on ub.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.vehicle_rc_info");
		if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
		sb.append(" group by user_id) vrc on vrc.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.epf_detail");
		if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
		sb.append(" group by user_id) epf on epf.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.gstin_detail");
		if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
		sb.append(" group by user_id) gstin on gstin.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.itrv_document");
		if (isTimeFilterApplicable) sb.append(" where datetime like ?");					
		sb.append(" group by user_id) itrv on itrv.user_id = u.id ");

		sb.append("left join (select user_id,count(transaction_id) as count_id from RMManagementDB.payment_info where receipt_status = 'success'");
		if (isTimeFilterApplicable) sb.append(" and completion_datetime like ? ");
		sb.append(" group by user_id) pi on pi.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'created'");
		if (isTimeFilterApplicable) sb.append(" and create_datetime like ? ");
		sb.append(" group by user_id) ap1 on ap1.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'converted'");
		if (isTimeFilterApplicable) sb.append(" and create_datetime like ? ");
		sb.append(" group by user_id) ap2 on ap2.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.sf_co_ap_object");
		if (isTimeFilterApplicable) sb.append(" where create_datetime like ? ");
		sb.append(" group by user_id) coap on coap.user_id = u.id ");

		sb.append("left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id ");

		sb.append("left join (select user_id,count(id) count_id from RMManagementDB.bank_account_info where create_datetime");
		if (isTimeFilterApplicable) sb.append(" like ? ");
		else sb.append(" > '2019-07-01 00:00:01' ");
		sb.append(" group by user_id) bi on bi.user_id = u.id ");

		sb.append("where u.id = ?");

		preparedStatement = connection.prepareStatement(sb.toString());

		if (isTimeFilterApplicable) {

			preparedStatement.setString(1, DateTimeUtils.Month.get(filter.time).dateFormat + "%");
			preparedStatement.setString(2, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
			preparedStatement.setString(3, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
			preparedStatement.setString(4, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
			preparedStatement.setString(5, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
			preparedStatement.setString(6, DateTimeUtils.Month.get(filter.time).dateFormat + "%");		
			preparedStatement.setString(7, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
			preparedStatement.setString(8, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
			preparedStatement.setString(9, DateTimeUtils.Month.get(filter.time).dateFormat + "%");			
			preparedStatement.setString(10, DateTimeUtils.Month.get(filter.time).dateFormat + "%");
			preparedStatement.setString(11, DateTimeUtils.Month.get(filter.time).dateFormat + "%");
			preparedStatement.setInt(12, userId);

		} else preparedStatement.setInt(1, userId);

	}

	resultSet = preparedStatement.executeQuery();

	if (null != resultSet && resultSet.first()) {

		LeaderBoardItem leader = getLeaderBoardItemFromRS(resultSet);

		if (null != weightage) leader.calculatePointsWithWeightage(weightage);
		else leader.calculateDefaultPoints();

		return leader;

	}

	return null;

}
 */
