package v2.dbhelpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import dao.DataProvider;
import databasehelper.ColumnsNFields;
import manager.PaymentManager;
import models.FetchDocument;
import models.LeaderBoardItem;
import models.LeaderboardFilter;
import models.PaymentInfo;
import models.SFApplicant;
import models.SFCoApplicant;
import models.SFTask;
import models.ScoreWeightage;
import models.SecondaryInfo;
import models.User;
import totalkyc.DocumentKYCHelper.DocumentType;
import utils.BasicUtils;
import utils.Constants;
import utils.Constants.ApplicantType;
import utils.CryptoUtils;
import utils.DateTimeUtils;
import utils.DateTimeUtils.DateTimeFormat;
import utils.DateTimeUtils.DateTimeZone;
import utils.LeaderboardUtils;
import utils.LeaderboardUtils.RegionMap;
import utils.LoggerUtils;

public class RMDatabaseHelper {

	private Connection connection = null;
	private PreparedStatement preparedStatement = null;
	private ResultSet resultSet = null;

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

	public RMDatabaseHelper() {
	}

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
					ColumnsNFields.PaymentInfoColumn.SF_TRANSFERRED_HFC_NAME.value + "," +
					ColumnsNFields.PaymentInfoColumn.X_SELL_PRODUCT.value + "," +
					ColumnsNFields.PaymentInfoColumn.LOAN_PRODUCT_ID.value + "," +
					ColumnsNFields.PaymentInfoColumn.LOAN_SUB_TYPE.value +
					") " + 
					"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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

			
			preparedStatement.setString(19, checkout.sfXSellProductId);
			preparedStatement.setString(20, checkout.sfLoanProductId);
			preparedStatement.setString(21, checkout.sfLoanSubType);

			int status = preparedStatement.executeUpdate();

			if (status == 1) {
				checkout.transactionId = transactionId; 
				checkout.initialDatetime = initialDatetime;
				return checkout;			
			} else return null;

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
			checkout.sfXSellProductId = resultSet.getString(ColumnsNFields.PaymentInfoColumn.X_SELL_PRODUCT.value);
			checkout.sfLoanProductId = resultSet.getString(ColumnsNFields.PaymentInfoColumn.LOAN_PRODUCT_ID.value);
			checkout.sfLoanSubType = resultSet.getString(ColumnsNFields.PaymentInfoColumn.LOAN_SUB_TYPE.value);

			return checkout;

		}


	// ****************** END OF PAYMENT IMPLEMENTATION ******************* //

	// ================ START OF LEADERBOARD IMPLEMENTATION =================== //

	public ArrayList<LeaderBoardItem> getLeadersLiveData(String regionFilter, String timeFilter, ScoreWeightage weightage)
			throws SQLException, NumberFormatException, ParseException {

		boolean isRegionFilterApplicable = LeaderboardUtils.isRegionFilterApplicable(regionFilter);

		String filterYear = DateTimeUtils.getDateTimeFromString(timeFilter, DateTimeFormat.yyyy_MM, DateTimeFormat.yyyy,
				DateTimeZone.IST);
		String filterMonth = DateTimeUtils.getDateTimeFromString(timeFilter, DateTimeFormat.yyyy_MM, DateTimeFormat.MM,
				DateTimeZone.IST);

		StringBuilder sb = new StringBuilder();

		sb.append("select u.id, u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,");
		sb.append("si.image_url profileImageUrl,");

		sb.append(" COALESCE(kd.count_id,0) kycDocumentCount, COALESCE(ub.count_id,0) utilityBillCount,");
		sb.append(" COALESCE(vrc.count_id,0) vehicleRCCount, COALESCE(epf.count_id,0) epfCount,");
		sb.append(" COALESCE(gstin.count_id,0) gstinCount, COALESCE(itrv.count_id,0) itrvCount,");
		sb.append(" COALESCE(pi.count_id,0) paymentCount, COALESCE(ap1.count_id,0) createdApCount,");
		sb.append(" COALESCE(ap2.count_id,0) convertedApCount, COALESCE(coap.count_id,0) coApCount,");
		sb.append(" COALESCE(bi.count_id,0) bankInfoFetchedCount, COALESCE(v.count_id,0) visitCompletedCount,");

		sb.append(" ((COALESCE(kd.count_id,0) * COALESCE(sw.kyc_document, dsw.kyc_document)) +");
		sb.append(" (COALESCE(ub.count_id,0) * COALESCE(sw.utility_bill, dsw.utility_bill)) +");
		sb.append(" (COALESCE(vrc.count_id,0) * COALESCE(sw.vehicle_rc, dsw.vehicle_rc)) +");
		sb.append(" (COALESCE(epf.count_id,0) * COALESCE(sw.epf, dsw.epf)) +");
		sb.append(" (COALESCE(gstin.count_id,0) * COALESCE(sw.gstin, dsw.gstin)) +");
		sb.append(" (COALESCE(pi.count_id,0) * COALESCE(sw.payment, dsw.payment)) +");
		sb.append(" (COALESCE(ap1.count_id,0) * COALESCE(sw.lead_created, dsw.lead_created)) +");
		sb.append(" (COALESCE(ap2.count_id,0) * COALESCE(sw.lead_converted, dsw.lead_converted)) +");
		sb.append(" (COALESCE(coap.count_id,0) * COALESCE(sw.lead_created, dsw.lead_created))  +");
		sb.append(" (COALESCE(bi.count_id,0) * COALESCE(sw.bank_statement, dsw.bank_statement)) +");
		sb.append(" (COALESCE(v.count_id,0) * COALESCE(sw.visit_completed, dsw.visit_completed)) +");
		sb.append(" (COALESCE(itrv.count_id,0) * COALESCE(sw.itr, dsw.itr))) total ");

		sb.append(" from RMManagementDB.user u ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.kyc_document");
		sb.append(" where MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) kd on kd.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.utility_bill");
		sb.append(" where MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) ub on ub.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.vehicle_rc_info");
		sb.append(" where MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) vrc on vrc.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.epf_detail");
		sb.append(" where MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) epf on epf.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.gstin_detail");
		sb.append(" where MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) gstin on gstin.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.itrv_document");
		sb.append(" where MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) itrv on itrv.user_id = u.id ");

		sb.append(
				" left join (select user_id,count(transaction_id) as count_id from RMManagementDB.payment_info where receipt_status = 'success'");
		sb.append(" and MONTH(completion_datetime) = ? AND YEAR(completion_datetime) = ? ");
		sb.append(" group by user_id) pi on pi.user_id = u.id ");

		sb.append(
				" left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'created'");
		sb.append(" and MONTH(create_datetime) = ? AND YEAR(create_datetime) = ? ");
		sb.append(" group by user_id) ap1 on ap1.user_id = u.id ");

		sb.append(
				" left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where source = 'RM_PRO' AND lead_stage = 'converted'");
		sb.append(" and MONTH(create_datetime) = ? AND YEAR(create_datetime) = ? ");
		sb.append(" group by user_id) ap2 on ap2.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.sf_co_ap_object where source = 'RM_PRO'");
		sb.append(" AND MONTH(create_datetime) = ? AND YEAR(create_datetime) = ? ");
		sb.append(" group by user_id) coap on coap.user_id = u.id ");

		sb.append(
				" left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id ");

		sb.append(
				" left join (select user_id,count(id) count_id from RMManagementDB.bank_account_info where MONTH(create_datetime) = ? AND YEAR(create_datetime) = ? ");
		sb.append(" group by user_id) bi on bi.user_id = u.id ");
		
		sb.append(
				" left join (select userId,count(id) count_id from RMManagementDB.Visit where status = 'COMPLETED' and MONTH(updateDatetime) = ? AND YEAR(updateDatetime) = ? ");
		sb.append(" group by userId) v on v.userId = u.id ");

		sb.append(
				" left join (select * from RMManagementDB.score_weightage where MONTH(start_datetime) = ? AND YEAR(start_datetime) = ?) sw on sw.start_datetime,");
		sb.append(" (select * from RMManagementDB.score_weightage where contest_name = 'default') dsw ");

		if (isRegionFilterApplicable) {
			sb.append(" where u.id in (select id from RMManagementDB.user "
					+ " where email in (SELECT user_email_id FROM RMManagementDB.region_map WHERE cluster = ?)) ");
		}

		sb.append(" order by u.id");

		checkConnection();

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setString(1, filterMonth);
		preparedStatement.setString(2, filterYear);

		preparedStatement.setString(3, filterMonth);
		preparedStatement.setString(4, filterYear);

		preparedStatement.setString(5, filterMonth);
		preparedStatement.setString(6, filterYear);

		preparedStatement.setString(7, filterMonth);
		preparedStatement.setString(8, filterYear);

		preparedStatement.setString(9, filterMonth);
		preparedStatement.setString(10, filterYear);

		preparedStatement.setString(11, filterMonth);
		preparedStatement.setString(12, filterYear);

		preparedStatement.setString(13, filterMonth);
		preparedStatement.setString(14, filterYear);

		preparedStatement.setString(15, filterMonth);
		preparedStatement.setString(16, filterYear);

		preparedStatement.setString(17, filterMonth);
		preparedStatement.setString(18, filterYear);

		preparedStatement.setString(19, filterMonth);
		preparedStatement.setString(20, filterYear);

		preparedStatement.setString(21, filterMonth);
		preparedStatement.setString(22, filterYear);

		preparedStatement.setString(23, filterMonth);
		preparedStatement.setString(24, filterYear);

		preparedStatement.setString(25, filterMonth);
		preparedStatement.setString(26, filterYear);
		
		if (isRegionFilterApplicable)
			preparedStatement.setString(27, LeaderboardUtils.Region.get(regionFilter).value);

		resultSet = preparedStatement.executeQuery();

		ArrayList<LeaderBoardItem> leaders = new ArrayList<LeaderBoardItem>();

		if (null != resultSet && resultSet.first()) {
			do {

				LeaderBoardItem newItem = getLeaderBoardItemFromRS(resultSet);
				newItem.calculatePointsWithWeightage(weightage); 
				leaders.add(newItem);

			} while (resultSet.next());
		}

		return leaders;

	}

	public ArrayList<LeaderBoardItem> getLeadersLiveDataForDashboard(int userId, String timeFilter)
			throws SQLException, ParseException {

		String filterYear = DateTimeUtils.getDateTimeFromString(timeFilter, DateTimeFormat.yyyy_MM_dd, DateTimeFormat.yyyy,
				DateTimeZone.IST);
		String filterMonth = DateTimeUtils.getDateTimeFromString(timeFilter, DateTimeFormat.yyyy_MM_dd, DateTimeFormat.MM,
				DateTimeZone.IST);
		String filterDay = DateTimeUtils.getDateTimeFromString(timeFilter, DateTimeFormat.yyyy_MM_dd, DateTimeFormat.dd,
				DateTimeZone.IST);


		StringBuilder sb = new StringBuilder();

		sb.append("select u.id, u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,");
		sb.append("si.image_url profileImageUrl,");

		sb.append(" COALESCE(kd.count_id,0) kycDocumentCount, COALESCE(ub.count_id,0) utilityBillCount,");
		sb.append(" COALESCE(vrc.count_id,0) vehicleRCCount, COALESCE(epf.count_id,0) epfCount,");
		sb.append(" COALESCE(gstin.count_id,0) gstinCount, COALESCE(itrv.count_id,0) itrvCount,");
		sb.append(" COALESCE(pi.count_id,0) paymentCount, COALESCE(ap1.count_id,0) createdApCount,");
		sb.append(" COALESCE(ap2.count_id,0) convertedApCount, COALESCE(coap.count_id,0) coApCount,");
		sb.append(" COALESCE(bi.count_id,0) bankInfoFetchedCount, COALESCE(v.count_id,0) visitCompletedCount,");
	
		sb.append(" ((COALESCE(kd.count_id,0) * COALESCE(sw.kyc_document, dsw.kyc_document)) +");
		sb.append(" (COALESCE(ub.count_id,0) * COALESCE(sw.utility_bill, dsw.utility_bill)) +");
		sb.append(" (COALESCE(vrc.count_id,0) * COALESCE(sw.vehicle_rc, dsw.vehicle_rc)) +");
		sb.append(" (COALESCE(epf.count_id,0) * COALESCE(sw.epf, dsw.epf)) +");
		sb.append(" (COALESCE(gstin.count_id,0) * COALESCE(sw.gstin, dsw.gstin)) +");
		sb.append(" (COALESCE(pi.count_id,0) * COALESCE(sw.payment, dsw.payment)) +");
		sb.append(" (COALESCE(ap1.count_id,0) * COALESCE(sw.lead_created, dsw.lead_created)) +");
		sb.append(" (COALESCE(ap2.count_id,0) * COALESCE(sw.lead_converted, dsw.lead_converted)) +");
		sb.append(" (COALESCE(coap.count_id,0) * COALESCE(sw.lead_created, dsw.lead_created))  +");
		sb.append(" (COALESCE(bi.count_id,0) * COALESCE(sw.bank_statement, dsw.bank_statement)) +");
		sb.append(" (COALESCE(v.count_id,0) * COALESCE(sw.visit_completed, dsw.visit_completed)) +");
		sb.append(" (COALESCE(itrv.count_id,0) * COALESCE(sw.itr, dsw.itr))) total ");

		sb.append(" from RMManagementDB.user u ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.kyc_document");
		sb.append(" where DAY(datetime) = ? AND MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) kd on kd.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.utility_bill");
		sb.append(" where DAY(datetime) = ? AND MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) ub on ub.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.vehicle_rc_info");
		sb.append(" where DAY(datetime) = ? AND MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) vrc on vrc.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.epf_detail");
		sb.append(" where DAY(datetime) = ? AND MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) epf on epf.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.gstin_detail");
		sb.append(" where DAY(datetime) = ? AND MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) gstin on gstin.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.itrv_document");
		sb.append(" where DAY(datetime) = ? AND MONTH(datetime) = ? AND YEAR(datetime) = ?");
		sb.append(" group by user_id) itrv on itrv.user_id = u.id ");

		sb.append(
				" left join (select user_id,count(transaction_id) as count_id from RMManagementDB.payment_info where receipt_status = 'success'");
		sb.append(" and DAY(completion_datetime) = ? AND MONTH(completion_datetime) = ? AND YEAR(completion_datetime) = ? ");
		sb.append(" group by user_id) pi on pi.user_id = u.id ");

		sb.append(
				" left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where lead_stage = 'created'");
		sb.append(" and DAY(create_datetime) = ? AND MONTH(create_datetime) = ? AND YEAR(create_datetime) = ? ");
		sb.append(" group by user_id) ap1 on ap1.user_id = u.id ");

		sb.append(
				" left join (select user_id,count(id) count_id from RMManagementDB.sf_main_object where source = 'RM_PRO' and lead_stage = 'converted'");
		sb.append(" and DAY(create_datetime) = ? AND MONTH(create_datetime) = ? AND YEAR(create_datetime) = ? ");
		sb.append(" group by user_id) ap2 on ap2.user_id = u.id ");

		sb.append(" left join (select user_id,count(id) count_id from RMManagementDB.sf_co_ap_object where source = 'RM_PRO'");
		sb.append(" and DAY(create_datetime) = ? AND MONTH(create_datetime) = ? AND YEAR(create_datetime) = ? ");
		sb.append(" group by user_id) coap on coap.user_id = u.id ");

		sb.append(
				" left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id ");

		sb.append(
				" left join (select user_id,count(id) count_id from RMManagementDB.bank_account_info where DAY(create_datetime) = ? AND MONTH(create_datetime) = ? AND YEAR(create_datetime) = ? ");
		sb.append(" group by user_id) bi on bi.user_id = u.id ");
		
		sb.append(
				" left join (select userId,count(id) count_id from RMManagementDB.Visit where status = 'COMPLETED' and DAY(updateDatetime) = ? AND MONTH(updateDatetime) = ? AND YEAR(updateDatetime) = ? ");
		sb.append(" group by userId) v on v.userId = u.id ");

		sb.append(
				" left join (select * from RMManagementDB.score_weightage where DAY(start_datetime) = ? AND MONTH(start_datetime) = ? AND YEAR(start_datetime) = ?) sw on sw.start_datetime,");
		sb.append(" (select * from RMManagementDB.score_weightage where contest_name = 'default') dsw ");

		sb.append(" where u.id=?");

		sb.append(" order by u.id");

		checkConnection();

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setString(1, filterDay);
		preparedStatement.setString(2, filterMonth);
		preparedStatement.setString(3, filterYear);

		preparedStatement.setString(4, filterDay);
		preparedStatement.setString(5, filterMonth);
		preparedStatement.setString(6, filterYear);
		
		preparedStatement.setString(7, filterDay);
		preparedStatement.setString(8, filterMonth);
		preparedStatement.setString(9, filterYear);
		
		preparedStatement.setString(10, filterDay);
		preparedStatement.setString(11, filterMonth);
		preparedStatement.setString(12, filterYear);
		
		preparedStatement.setString(13, filterDay);
		preparedStatement.setString(14, filterMonth);
		preparedStatement.setString(15, filterYear);
		
		
		preparedStatement.setString(16, filterDay);
		preparedStatement.setString(17, filterMonth);
		preparedStatement.setString(18, filterYear);
		
		preparedStatement.setString(19, filterDay);
		preparedStatement.setString(20, filterMonth);
		preparedStatement.setString(21, filterYear);
		
		preparedStatement.setString(22, filterDay);
		preparedStatement.setString(23, filterMonth);
		preparedStatement.setString(24, filterYear);
		
		preparedStatement.setString(25, filterDay);
		preparedStatement.setString(26, filterMonth);
		preparedStatement.setString(27, filterYear);
		
		
		preparedStatement.setString(28, filterDay);
		preparedStatement.setString(29, filterMonth);
		preparedStatement.setString(30, filterYear);
		
		preparedStatement.setString(31, filterDay);
		preparedStatement.setString(32, filterMonth);
		preparedStatement.setString(33, filterYear);
		
		preparedStatement.setString(34, filterDay);
		preparedStatement.setString(35, filterMonth);
		preparedStatement.setString(36, filterYear);
		
		preparedStatement.setString(37, filterDay);
		preparedStatement.setString(38, filterMonth);
		preparedStatement.setString(39, filterYear);
		
		preparedStatement.setString(40, filterDay);
		preparedStatement.setString(41, filterMonth);
		preparedStatement.setString(42, filterYear);

		preparedStatement.setInt(43, userId);
		
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
		leader.visitCompletedCount = resultSet.getInt("visitCompletedCount");
		leader.total = resultSet.getInt("total");
		leader.points = leader.total;

		try {
			leader.rank = resultSet.getInt("uRank");
			leader.rating = resultSet.getDouble("rating");
		} catch (Exception e) {
			// LoggerUtils.log("No value for rank and rating");
		}

		return leader;

	}

	public ScoreWeightage getScoreWeigtage(LeaderboardFilter filter) throws SQLException, ParseException {

		checkConnection();

		if (null == filter || !LeaderboardUtils.isTimeFilterApplicable(filter.time)) {
			return getDefaultScoreWeightage();
		} else {

			String filterYearMonth = DateTimeUtils.Month.get(filter.time).dateFormat;
			int filterYear = Integer.parseInt(DateTimeUtils.getDateTimeFromString(filterYearMonth,
					DateTimeFormat.yyyy_MM, DateTimeFormat.yyyy, DateTimeZone.IST));
			int filterMonth = Integer.parseInt(DateTimeUtils.getDateTimeFromString(filterYearMonth,
					DateTimeFormat.yyyy_MM, DateTimeFormat.MM, DateTimeZone.IST));

			String query = "select * from " + ColumnsNFields.SCORE_WEIGHTAGE_TABLE + " where MONTH("
					+ ColumnsNFields.ScoreWeightageColumn.START_DATETIME.value + ") = ?" + " and YEAR("
					+ ColumnsNFields.ScoreWeightageColumn.START_DATETIME.value + ") = ?";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, filterMonth);
			preparedStatement.setInt(2, filterYear);

			resultSet = preparedStatement.executeQuery();

			if (null != resultSet && resultSet.first())
				return getScoreWeightageFromRS(resultSet);
			return getDefaultScoreWeightage();

		}

	}

	private ScoreWeightage getDefaultScoreWeightage() throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.SCORE_WEIGHTAGE_TABLE + " where "
				+ ColumnsNFields.ScoreWeightageColumn.CONTEST_NAME.value + " = 'default'";

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
		sWeightage.visitCompleted = resultSet.getInt(ColumnsNFields.ScoreWeightageColumn.VISIT_COMPLETED.value);	

		return sWeightage;

	}

	public boolean insertLeaderboardHistory(ArrayList<LeaderBoardItem> leaders, String yearMonth, int target)
			throws SQLException {

		boolean status = false;

		for (LeaderBoardItem item : leaders) {
			status = insertSingleLeaderBoardHistory(item, target, yearMonth);
		}

		return status;
	}

	public boolean insertSingleLeaderBoardHistory(LeaderBoardItem item, int target, String yearMonth)
			throws SQLException {

		checkConnection();

		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO " + ColumnsNFields.LEADERBOARD_HISTORY + " (");
		sb.append(ColumnsNFields.COMMON_KEY_USER_ID + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.TARGET.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.LEAD_CREATED.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.LEAD_CONVERTED.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.CO_APPLICANT_ADDED.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.KYC_DOCUMENT.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.UTILITY_BILL.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.VEHICLE_RC.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.GSTIN.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.EPF.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.ITR.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.PAYMENT.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.BANK_STATEMENT.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.VISIT_COMPLETED.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.FINAL_SCORE.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.RATING.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.RANK.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.MONTH_YEAR.value + ",");
		sb.append(ColumnsNFields.LeaderBoardHistoryColumn.LAST_UPDATED_DATETIME.value + ") ");
		sb.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setInt(1, item.rmUser.id);
		preparedStatement.setInt(2, target);
		preparedStatement.setInt(3, item.createdApCount);
		preparedStatement.setInt(4, item.convertedApCount);
		preparedStatement.setInt(5, item.coApCount);
		preparedStatement.setInt(6, item.kycDocumentCount);
		preparedStatement.setInt(7, item.utilityBillCount);
		preparedStatement.setInt(8, item.vehicleRCCount);
		preparedStatement.setInt(9, item.gstinCount);
		preparedStatement.setInt(10, item.epfCount);
		preparedStatement.setInt(11, item.itrvCount);
		preparedStatement.setInt(12, item.paymentCount);
		preparedStatement.setInt(13, item.bankInfoFetchedCount);
		preparedStatement.setInt(15, item.visitCompletedCount);
		preparedStatement.setInt(16, item.points);
		preparedStatement.setDouble(17, item.rating);
		preparedStatement.setInt(18, item.rank);
		preparedStatement.setString(19, yearMonth);
		preparedStatement.setString(20, currentDatetime);

		return preparedStatement.executeUpdate() == 1;

	}

	public boolean insertOrUpdateLeaderBoardHistory(ArrayList<LeaderBoardItem> leaders, int target, String yearMonth)
			throws SQLException {

		boolean status = false;

		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		for (LeaderBoardItem item : leaders) {

			checkConnection();

			String query = "Select * from " + ColumnsNFields.LEADERBOARD_HISTORY + " where "
					+ ColumnsNFields.LeaderBoardHistoryColumn.MONTH_YEAR.value + "=?" + " and "
					+ ColumnsNFields.COMMON_KEY_USER_ID + "=?";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, yearMonth);
			preparedStatement.setInt(2, item.rmUser.id);

			resultSet = preparedStatement.executeQuery();

			if (null != resultSet && resultSet.first()) {

				StringBuilder sb = new StringBuilder();

				sb.append(" UPDATE " + ColumnsNFields.LEADERBOARD_HISTORY + " SET ");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.TARGET.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.LEAD_CREATED.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.LEAD_CONVERTED.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.CO_APPLICANT_ADDED.value + " =?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.KYC_DOCUMENT.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.UTILITY_BILL.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.VEHICLE_RC.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.GSTIN.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.EPF.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.ITR.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.PAYMENT.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.BANK_STATEMENT.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.VISIT_COMPLETED.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.FINAL_SCORE.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.RATING.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.RANK.value + "=?,");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.LAST_UPDATED_DATETIME.value + " =? ");
				sb.append(" Where " + ColumnsNFields.COMMON_KEY_USER_ID + " =? " + " AND ");
				sb.append(ColumnsNFields.LeaderBoardHistoryColumn.MONTH_YEAR.value + " =?");

				preparedStatement = connection.prepareStatement(sb.toString());
				preparedStatement.setInt(1, target);
				preparedStatement.setInt(2, item.createdApCount);
				preparedStatement.setInt(3, item.convertedApCount);
				preparedStatement.setInt(4, item.coApCount);
				preparedStatement.setInt(5, item.kycDocumentCount);
				preparedStatement.setInt(6, item.utilityBillCount);
				preparedStatement.setInt(7, item.vehicleRCCount);
				preparedStatement.setInt(8, item.gstinCount);
				preparedStatement.setInt(9, item.epfCount);
				preparedStatement.setInt(10, item.itrvCount);
				preparedStatement.setInt(11, item.paymentCount);
				preparedStatement.setInt(12, item.bankInfoFetchedCount);
				preparedStatement.setInt(13, item.visitCompletedCount);
				preparedStatement.setInt(14, item.points);
				preparedStatement.setDouble(15, item.rating);
				preparedStatement.setInt(16, item.rank);
				preparedStatement.setString(17, currentDatetime);
				preparedStatement.setInt(18, item.rmUser.id);
				preparedStatement.setString(19, yearMonth);

				status = preparedStatement.executeUpdate() == 1;
			} else {
				status = insertSingleLeaderBoardHistory(item, target, yearMonth);
			}
		}

		return status;

	}

	public ArrayList<LeaderBoardItem> getLeadersFromHistory(String regionFilter, String timeFilter,
			boolean shouldGetAll) throws SQLException {

		checkConnection();

		boolean isRegionFilterApplicable = LeaderboardUtils.isRegionFilterApplicable(regionFilter);

		StringBuilder sb = new StringBuilder();
		sb.append("select u.id,u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,");
		sb.append(" si.image_url profileImageUrl,");
		sb.append(" sum(lb.lead_created) createdApCount,");
		sb.append(" sum(lb.lead_converted) convertedApCount,");
		sb.append(" sum(lb.co_applicant_added) coApCount,");
		sb.append(" sum(lb.kyc_document) kycDocumentCount,");
		sb.append(" sum(lb.utility_bill) utilityBillCount,");
		sb.append(" sum(lb.vehicle_rc) vehicleRCCount,");
		sb.append(" sum(lb.gstin) gstinCount,");
		sb.append(" sum(lb.epf) epfCount,");
		sb.append(" sum(lb.itr) itrvCount,");
		sb.append(" sum(lb.payment) paymentCount,");
		sb.append(" sum(lb.bank_statement) bankInfoFetchedCount,");
		sb.append(" sum(lb.visit_completed) visitCompletedCount,");
		sb.append(" sum(lb.final_score) total");
		sb.append(" from RMManagementDB.user u");

		if (!shouldGetAll) {
			sb.append(" left join (select * from RMManagementDB.leaderboard_history where month_year = '" + timeFilter
					+ "') lb on lb.user_id = u.id");
		} else {
			sb.append(" left join (select * from RMManagementDB.leaderboard_history where month_year like '"
					+ timeFilter + "%') lb on lb.user_id = u.id");
		}

		sb.append(
				" left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id");

		if (isRegionFilterApplicable) {
			sb.append(" where u.id in (select id from RMManagementDB.user where email in ");
			sb.append(" (SELECT user_email_id FROM RMManagementDB.region_map WHERE cluster=?))");
		}

		sb.append(" group by u.id,profileImageUrl order by total desc");

		preparedStatement = connection.prepareStatement(sb.toString());

		if (isRegionFilterApplicable)
			preparedStatement.setString(1, LeaderboardUtils.Region.get(regionFilter).value);
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

	public ArrayList<LeaderBoardItem> getLeaderProfileFromHistory(String timeFilter, int userId) throws SQLException {

		checkConnection();

		StringBuilder sb = new StringBuilder();
		sb.append("select u.id,u.display_name displayName,u.sf_user_id sfUserId,u.username emailId,");
		sb.append(" si.image_url profileImageUrl,");
		sb.append(" sum(lb.lead_created) createdApCount,");
		sb.append(" sum(lb.lead_converted) convertedApCount,");
		sb.append(" sum(lb.co_applicant_added) coApCount,");
		sb.append(" sum(lb.kyc_document) kycDocumentCount,");
		sb.append(" sum(lb.utility_bill) utilityBillCount,");
		sb.append(" sum(lb.vehicle_rc) vehicleRCCount,");
		sb.append(" sum(lb.gstin) gstinCount,");
		sb.append(" sum(lb.epf) epfCount,");
		sb.append(" sum(lb.itr) itrvCount,");
		sb.append(" sum(lb.payment) paymentCount,");
		sb.append(" sum(lb.bank_statement) bankInfoFetchedCount,");
		sb.append(" sum(lb.visit_completed) visitCompletedCount,");
		sb.append(" sum(lb.final_score) total,");
		sb.append(" lb.u_rank uRank,");
		sb.append(" lb.rating rating");
		sb.append(" from RMManagementDB.user u");

		sb.append(" left join (select * from RMManagementDB.leaderboard_history where month_year = '" + timeFilter
				+ "') lb on lb.user_id = u.id");

		sb.append(
				" left join (select user_id,image_url from RMManagementDB.user_secondary_info) si on si.user_id = u.id");
		sb.append(" where u.id = ?");
		sb.append(" group by u.id,profileImageUrl order by total desc");

		preparedStatement = connection.prepareStatement(sb.toString());
		preparedStatement.setInt(1, userId);
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

	// ****************** END OF LEADER BOARD IMPLEMENTATION ******************* //

	// ================ START OF APPLICANT AND CO-APPLICANT IMPLEMENTATION //
	// =================== //

	public boolean insertApplicantInfo(SFApplicant apInfo, String source) throws SQLException {

		checkConnection();

		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		ColumnsNFields.SFLeadStage updateType = ColumnsNFields.SFLeadStage.get(apInfo.leadStage);

		String query;

		if (updateType == ColumnsNFields.SFLeadStage.CONVERTED) {

			query = "INSERT INTO sf_main_object " + "(user_id," + "customer_name," + "customer_mobile_number,"
					+ "lead_stage," + "lead_id," + "account_id," + "contact_id," + "opportunity_id," + "source,"
					+ "create_datetime," + "update_datetime) " + "VALUES(?,?,?,?,?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, apInfo.userId);
			preparedStatement.setString(2, apInfo.customerName);
			preparedStatement.setString(3, apInfo.customerMobileNumber);
			preparedStatement.setString(4, apInfo.leadStage);
			preparedStatement.setString(5, apInfo.leadId);
			preparedStatement.setString(6, apInfo.accountId);
			preparedStatement.setString(7, apInfo.contactId);
			preparedStatement.setString(8, apInfo.opportunityId);
			preparedStatement.setString(9, source);
			preparedStatement.setString(10, currentDatetime);
			preparedStatement.setString(11, currentDatetime);

		} else {

			query = "INSERT INTO sf_main_object " + "(user_id," + "customer_name," + "customer_mobile_number,"
					+ "lead_stage," + "lead_id," + "create_datetime) " + "VALUES(?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, apInfo.userId);
			preparedStatement.setString(2, apInfo.customerName);
			preparedStatement.setString(3, apInfo.customerMobileNumber);
			preparedStatement.setString(4, apInfo.leadStage);
			preparedStatement.setString(5, apInfo.leadId);
			preparedStatement.setString(6, currentDatetime);

		}

		boolean status = preparedStatement.executeUpdate() == 1;

		if (status)
			LoggerUtils.log("==> Applicant information has been successfully inserted in DB");
		else
			LoggerUtils.log("==> Failed to insert Applicant information in DB");

		return status;

	}

	public boolean updateApplicantInfo(SFApplicant apInfo) throws SQLException {

		checkConnection();

		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		String query = "update sf_main_object set " + "user_id=?," + "customer_name=?," + "customer_mobile_number=?,"
				+ "lead_stage=?," + "account_id=?," + "contact_id=?," + "opportunity_id=?," + "update_datetime=? "
				+ "where lead_id=?";

		preparedStatement = connection.prepareStatement(query);

		preparedStatement.setInt(1, apInfo.userId);
		preparedStatement.setString(2, apInfo.customerName);
		preparedStatement.setString(3, apInfo.customerMobileNumber);
		preparedStatement.setString(4, apInfo.leadStage);
		preparedStatement.setString(5, apInfo.accountId);
		preparedStatement.setString(6, apInfo.contactId);
		preparedStatement.setString(7, apInfo.opportunityId);
		preparedStatement.setString(8, currentDatetime);
		preparedStatement.setString(9, apInfo.leadId);

		boolean status = preparedStatement.executeUpdate() == 1;

		if (status)
			LoggerUtils.log("==> Applicant information has been successfully updated in DB");
		else
			LoggerUtils.log("==> Failed to update Applicant information in DB");

		return status;

	}

	public SFApplicant getApplicantInfoByMobileNumber(String mobileNumber) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.SF_MAIN_OBJECT_TABLE + " where "
				+ ColumnsNFields.SFMainObjectColumn.CUSTOMER_MOBILE_NUMBER.value + "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, mobileNumber);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return getApplicantObjectFromRS(resultSet);
		else
			return null;

	}

	private SFApplicant getApplicantObjectFromRS(ResultSet resultSet) throws SQLException {

		SFApplicant applicant = new SFApplicant();

		applicant.id = resultSet.getInt(ColumnsNFields.SFMainObjectColumn.ID.value);
		applicant.userId = resultSet.getInt(ColumnsNFields.SFMainObjectColumn.USER_ID.value);
		applicant.customerName = resultSet.getString(ColumnsNFields.SFMainObjectColumn.CUSTOMER_NAME.value);
		applicant.customerMobileNumber = resultSet
				.getString(ColumnsNFields.SFMainObjectColumn.CUSTOMER_MOBILE_NUMBER.value);
		applicant.leadStage = resultSet.getString(ColumnsNFields.SFMainObjectColumn.LEAD_STAGE.value);
		applicant.leadId = resultSet.getString(ColumnsNFields.SFMainObjectColumn.LEAD_ID.value);
		applicant.accountId = resultSet.getString(ColumnsNFields.SFMainObjectColumn.ACCOUNT_ID.value);
		applicant.contactId = resultSet.getString(ColumnsNFields.SFMainObjectColumn.CONTACT_ID.value);
		applicant.opportunityId = resultSet.getString(ColumnsNFields.SFMainObjectColumn.OPPORTUNITY_ID.value);
		applicant.createDatetime = resultSet.getString(ColumnsNFields.SFMainObjectColumn.CREATE_DATETIME.value);
		applicant.updateDatetime = resultSet.getString(ColumnsNFields.SFMainObjectColumn.UPDATE_DATETIME.value);
		applicant.imageUrl = resultSet.getString(ColumnsNFields.SFMainObjectColumn.IMAGE_URL.value);

		return applicant;

	}

	public boolean addUpdateCoApInfo(SFCoApplicant apInfo, boolean shouldInsert, String source) throws SQLException {

		checkConnection();

		String currentDatetime = DateTimeUtils.getCurrentDateTimeInIST();

		String query;

		if (shouldInsert) {

			query = "INSERT INTO sf_co_ap_object " + "(user_id," + "customer_name," + "customer_mobile_number,"
					+ "contact_id," + "applicant_opportunity_id," + "source," + "create_datetime) "
					+ "VALUES(?,?,?,?,?,?,?)";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, apInfo.userId);
			preparedStatement.setString(2, apInfo.customerName);
			preparedStatement.setString(3, apInfo.customerMobileNumber);
			preparedStatement.setString(4, apInfo.contactId);
			preparedStatement.setString(5, apInfo.applicantOpportunityId);
			preparedStatement.setString(6, source);
			preparedStatement.setString(7, currentDatetime);

		} else {

			query = "UPDATE sf_co_ap_object SET " + "user_id=?," + "customer_name=?," + "applicant_opportunity_id=?,"
					+ "update_datetime=? " + "where contact_id=? and customer_mobile_number=?";

			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, apInfo.userId);
			preparedStatement.setString(2, apInfo.customerName);
			preparedStatement.setString(3, apInfo.applicantOpportunityId);
			preparedStatement.setString(4, currentDatetime);
			preparedStatement.setString(5, apInfo.contactId);
			preparedStatement.setString(6, apInfo.customerMobileNumber);

		}

		boolean status = preparedStatement.executeUpdate() == 1;

		if (status)
			LoggerUtils.log("==> Co-Applicant information has been successfully inserted/updated in DB");
		else
			LoggerUtils.log("==> Failed to insert Co-Applicant information in DB");

		return status;

	}

	public SFCoApplicant getCoApInfoByMobileNumber(String mobileNumber) throws SQLException {

		if (null == connection)
			connection = DataProvider.getDataSource().getConnection();

		String query = "select * from " + ColumnsNFields.SF_CO_AP_OBJECT_TABLE + " where "
				+ ColumnsNFields.SFCoApObjectColumn.CUSTOMER_MOBILE_NUMBER.value + "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, mobileNumber);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return getCoApObjectFromRS(resultSet);
		else
			return null;

	}

	private SFCoApplicant getCoApObjectFromRS(ResultSet resultSet) throws SQLException {

		SFCoApplicant applicant = new SFCoApplicant();

		applicant.id = resultSet.getInt(ColumnsNFields.SFCoApObjectColumn.ID.value);
		applicant.userId = resultSet.getInt(ColumnsNFields.SFCoApObjectColumn.USER_ID.value);
		applicant.customerName = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.CUSTOMER_NAME.value);
		applicant.customerMobileNumber = resultSet
				.getString(ColumnsNFields.SFCoApObjectColumn.CUSTOMER_MOBILE_NUMBER.value);
		applicant.contactId = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.CONTACT_ID.value);
		applicant.applicantOpportunityId = resultSet
				.getString(ColumnsNFields.SFCoApObjectColumn.APPLICANT_OPP_ID.value);
		applicant.createDatetime = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.CREATE_DATETIME.value);
		applicant.updateDatetime = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.UPDATE_DATETIME.value);
		applicant.imageUrl = resultSet.getString(ColumnsNFields.SFCoApObjectColumn.IMAGE_URL.value);

		return applicant;

	}

	// ****************** END OF APPLICANT AND CO-APPLICANT IMPLEMENTATION
	// ******************* //

	// ================ START OF USER PROFILE IMPLEMENTATION =================== //

	public boolean addOrUpdateUserProfilePicture(int userId, String imageUrl) throws SQLException {

		if (null == connection)
			connection = DataProvider.getDataSource().getConnection();

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

		LoggerUtils
				.log("Bank info update status ==> Total: " + bankInfoSetCount + " | Inserted: " + bankInfoInsertCount);

	}

	private boolean insertNewBankInfo(int userId, JSONObject bankInfo) throws SQLException {

		checkConnection();

		String query = "INSERT INTO bank_account_info" + " (user_id," + "sf_record_id," + "sf_contact_id,"
				+ "bank_name," + ColumnsNFields.COMMON_KEY_CREATE_DATETIME + ")" + " VALUES(?,?,?,?,?);";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userId);
		preparedStatement.setString(2, bankInfo.optString("Id", Constants.NA));
		preparedStatement.setString(3, bankInfo.optString("Contact__c", Constants.NA));
		preparedStatement.setString(4, bankInfo.optString("Account_Bank__c", Constants.NA));

		String createdDatetime;

		String sfCreateDateTime = bankInfo.optString("CreatedDate", Constants.NA);

		try {

			createdDatetime = DateTimeUtils.getDate(sfCreateDateTime,
					DateTimeUtils.DateTimeFormat.yyyy_MM_dd_T_HH_mm_ss_SSSZ.value, "yyyy-MM-dd HH:mm:ss");

		} catch (ParseException e) {
			LoggerUtils.log("Error while parsing datetime fetched from SF ==> FetchedDateTime: " + sfCreateDateTime
					+ " | Error: " + e.getMessage());
			createdDatetime = DateTimeUtils.getCurrentDateTimeInIST();
		}

		preparedStatement.setString(5, createdDatetime);

		return preparedStatement.executeUpdate() == 1;
	}

	private int getUserIdFromSfId(String sfId) throws SQLException {

		checkConnection();

		String query = "select " + ColumnsNFields.COMMON_KEY_ID + " from " + ColumnsNFields.USER_TABLE + " where "
				+ ColumnsNFields.UserColumn.SF_USER_ID.value + "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, sfId);
		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return resultSet.getInt(ColumnsNFields.COMMON_KEY_ID);

		return -1;

	}

	private boolean doesBankInfoExists(int userId, String contactId) throws SQLException {

		checkConnection();

		String query = "select * from " + ColumnsNFields.BANK_INFO_TABLE + " where "
				+ ColumnsNFields.BankInfoColumn.SF_CONTACT_ID.value + "=?" + " and " + ColumnsNFields.COMMON_KEY_USER_ID
				+ "=?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, contactId);
		preparedStatement.setInt(2, userId);
		resultSet = preparedStatement.executeQuery();

		return (null != resultSet && resultSet.first());

	}

	// ****************** END OF BANK INFO IMPLEMENTATION ******************* //

	public boolean addNotificationToken(int userId, JSONObject data) throws SQLException {

		checkConnection();

		SecondaryInfo sInfo = getSecondaryInfoById(userId);

		if (null != sInfo) {

			String deviceType = data.optString(Constants.DEVICE_TYPE, Constants.NA);

			if (!deviceType.equalsIgnoreCase(Constants.NA)) {

				JSONArray niArray = new JSONArray();

				if (null != sInfo.fcmKey && !sInfo.fcmKey.equals(Constants.NA))
					niArray = new JSONArray(sInfo.fcmKey);

				String query = "update user_secondary_info set fcm_key = ? where user_id = ?";

				boolean isDataAlreadyUpdated = false;

				if (niArray.length() > 0) {
					for (int i = 0; i < niArray.length(); i++) {
						JSONObject current = niArray.getJSONObject(i);
						if (current.getString("device_id").equalsIgnoreCase(data.getString("deviceId"))) {
							current.put("key", data.getString("notificationKey"));
							current.put("device_type", deviceType);
							isDataAlreadyUpdated = true;
							break;
						}
					}
				}

				if (!isDataAlreadyUpdated)
					niArray.put(getNotificationInfoJson(data));

				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1, niArray.toString());
				preparedStatement.setInt(2, userId);
				int status = preparedStatement.executeUpdate();

				return status == 1;

			} else {
				return false;
			}

		}

		return false;

	}
	
	public boolean addApnsToken(int userId, JSONObject data) throws SQLException {

		checkConnection();

		SecondaryInfo sInfo = getSecondaryInfoById(userId);

		if (null != sInfo) {

			String deviceType = data.optString(Constants.DEVICE_TYPE, Constants.NA);

			if (!deviceType.equalsIgnoreCase(Constants.NA)) {

				JSONArray niArray = new JSONArray();

				if (null != sInfo.apnsKey && !sInfo.apnsKey.equals(Constants.NA))
					niArray = new JSONArray(sInfo.apnsKey);

				String query = "update user_secondary_info set apns_key = ? where user_id = ?";

				boolean isDataAlreadyUpdated = false;

				if (niArray.length() > 0) {
					for (int i = 0; i < niArray.length(); i++) {
						JSONObject current = niArray.getJSONObject(i);
						if (current.getString("device_id").equalsIgnoreCase(data.getString("deviceId"))) {
							current.put("key", data.getString("notificationKey"));
							current.put("device_type", deviceType);
							isDataAlreadyUpdated = true;
							break;
						}
					}
				}

				if (!isDataAlreadyUpdated)
					niArray.put(getNotificationInfoJson(data));

				preparedStatement = connection.prepareStatement(query);
				preparedStatement.setString(1, niArray.toString());
				preparedStatement.setInt(2, userId);
				int status = preparedStatement.executeUpdate();

				return status == 1;

			} else {
				return false;
			}

		}

		return false;

	}

	public SecondaryInfo getSecondaryInfoById(int userId) throws SQLException {

		checkConnection();

		String query = "select * from user_secondary_info where user_id = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userId);
		resultSet = preparedStatement.executeQuery();

		if (null == resultSet)
			return null;
		else {
			if (resultSet.first())
				return getSecondaryInfoObjectFromResultSet(resultSet);
			else
				return null;
		}

	}

	private JSONObject getNotificationInfoJson(JSONObject nData) throws JSONException {
		JSONObject niJson = new JSONObject();
		niJson.put("device_type", nData.getString(Constants.DEVICE_TYPE));
		niJson.put("device_id", nData.getString("deviceId"));
		niJson.put("key", nData.getString("notificationKey"));
		return niJson;
	}

	private SecondaryInfo getSecondaryInfoObjectFromResultSet(ResultSet resultSet) throws SQLException {

		SecondaryInfo info = new SecondaryInfo();

		info.id = resultSet.getInt(ColumnsNFields.SecondaryInfoColumn.ID.value);
		info.userId = resultSet.getInt(ColumnsNFields.SecondaryInfoColumn.USER_ID.value);
		info.deviceType = resultSet.getString(ColumnsNFields.SecondaryInfoColumn.DEVICE_TYPE.value);
		info.deviceId = resultSet.getString(ColumnsNFields.SecondaryInfoColumn.DEVICE_ID.value);
		info.fcmKey = resultSet.getString(ColumnsNFields.SecondaryInfoColumn.FCM_KEY.value);
		info.apnsKey = resultSet.getString(ColumnsNFields.SecondaryInfoColumn.APNS_KEY.value);
		
		return info;

	}

	public String getOwnerIdById(int userId) throws SQLException {

		checkConnection();

		String query = "select * from user where id = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, userId);
		resultSet = preparedStatement.executeQuery();

		// LoggerUtils.log("SFID:"+resultSet.getString("sf_user_id"));
		if (null == resultSet)
			return null;
		else {
			if (resultSet.first())
				return resultSet.getString("sf_user_id");

			else
				return Constants.NA;
		}

	}

	public int addAppsData(int userId, JSONObject appJson) throws SQLException {

		checkConnection();

		String appName = appJson.optString("appName", Constants.NA);
		String packageName = appJson.optString("packageName", Constants.NA);
		String versionName = appJson.optString("versionName", Constants.NA);
		String versionCode = appJson.optString("versionCode", Constants.NA);

		String query = "INSERT INTO " + ColumnsNFields.INSTALLED_APPS_TABLE + " ("
				+ ColumnsNFields.InstalledAppsColumn.USER_ID.value + ","
				+ ColumnsNFields.InstalledAppsColumn.APP_NAME.value + ","
				+ ColumnsNFields.InstalledAppsColumn.PACKAGE_NAME.value + ","
				+ ColumnsNFields.InstalledAppsColumn.VERSION_NAME.value + ","
				+ ColumnsNFields.InstalledAppsColumn.VERSION_CODE.value + ","
				+ ColumnsNFields.InstalledAppsColumn.DATETIME.value + ","
				+ ColumnsNFields.InstalledAppsColumn.RAW_DATA.value + ") VALUES(?,?,?,?,?,?,?)";

		preparedStatement = connection.prepareStatement(query);

		preparedStatement.setInt(1, userId);
		preparedStatement.setString(2, appName);
		preparedStatement.setString(3, packageName);
		preparedStatement.setString(4, versionName);
		preparedStatement.setString(5, versionCode);
		preparedStatement.setString(6, DateTimeUtils.getCurrentDateTimeInIST());
		preparedStatement.setString(7, appJson.toString());

		return preparedStatement.executeUpdate();

	}

	public Boolean getFlag(String flag_name) throws SQLException {

		checkConnection();

		String query = "select * from control_flags where name = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, flag_name);
		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return resultSet.getBoolean("value");
		else
			return false;

	}

	public SFTask addUpdateTaskAndActivity(int userId, SFTask sfTask) throws Exception {

		checkConnection();

		String currentDateTime = DateTimeUtils.getDateTime(DateTimeFormat.yyyy_MM_dd_HH_mm, DateTimeZone.IST);

		String query = "select " + ColumnsNFields.COMMON_KEY_ID + " from " + ColumnsNFields.TASK_N_ACTIVITY_TABLE
				+ " where " + ColumnsNFields.TaskNActivityColumn.SF_TASK_ID.value + " = ?";
		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, sfTask.sfId);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {

			int taskId = resultSet.getInt(ColumnsNFields.COMMON_KEY_ID);

			// Task already present. Update here

			StringBuilder sb = new StringBuilder();
			sb.append("update " + ColumnsNFields.TASK_N_ACTIVITY_TABLE + " set ");
			sb.append(ColumnsNFields.TaskNActivityColumn.ACTION_RESULT.value + "=?,");
			sb.append(ColumnsNFields.TaskNActivityColumn.TASK_DESCRIPTION.value + "=?,");
			sb.append(ColumnsNFields.TaskNActivityColumn.RECORD_DATA.value + "=?,");
			sb.append(ColumnsNFields.TaskNActivityColumn.STATUS.value + "=?,");
			sb.append(ColumnsNFields.TaskNActivityColumn.IS_FOLLOW_UP_SCHEDULED.value + "=?,");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_OWNER_ID.value + "=?,");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_OWNER_NAME.value + "=?,");
			sb.append(ColumnsNFields.COMMON_KEY_UPDATED_DATETIME + "=?,");
			sb.append(ColumnsNFields.COMMON_KEY_LATITUDE + "=?,");
			sb.append(ColumnsNFields.COMMON_KEY_LONGITUDE + "=?,");
			sb.append(ColumnsNFields.COMMON_KEY_ADDRESS + "=?");

			if (sfTask.hasFollowUpTask)
				sb.append("," + ColumnsNFields.TaskNActivityColumn.FOLLOW_UP_DATETIME.value + "=?");

			sb.append(" where " + ColumnsNFields.TaskNActivityColumn.SF_TASK_ID.value + "=?");

			preparedStatement = connection.prepareStatement(sb.toString());

			preparedStatement.setString(1, sfTask.activityResult);
			preparedStatement.setString(2, BasicUtils.substringToLength(sfTask.activityDescription, 512));
			preparedStatement.setString(3, sfTask.recordData.toString());
			preparedStatement.setString(4, sfTask.taskStatus);
			preparedStatement.setBoolean(5, sfTask.hasFollowUpTask);
			preparedStatement.setString(6, sfTask.ownerId);
			preparedStatement.setString(7, sfTask.ownerName);
			preparedStatement.setString(8, currentDateTime);
			preparedStatement.setDouble(9, sfTask.latitude);
			preparedStatement.setDouble(10, sfTask.longitude);
			preparedStatement.setString(11, sfTask.address.toJson().toString());

			if (sfTask.hasFollowUpTask) {

				preparedStatement.setString(12, DateTimeUtils.getSFtoDBDateTime(sfTask.followUpDateTime));
				preparedStatement.setString(13, sfTask.sfId);

			} else {
				preparedStatement.setString(12, sfTask.sfId);
			}

			if (preparedStatement.executeUpdate() > 0) {
				sfTask.id = taskId;
				return sfTask;
			}

		} else {

			// Task is not present in the DB. Insert here.

			StringBuilder sb = new StringBuilder();
			sb.append("insert into " + ColumnsNFields.TASK_N_ACTIVITY_TABLE + "(");
			sb.append(ColumnsNFields.COMMON_KEY_USER_ID + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_TASK_ID.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_OBJECT_ID.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_OBJECT_TYPE.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_OBJECT_NAME.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SUBJECT.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.TYPE.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.ACTION_RESULT.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.TASK_DESCRIPTION.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.RECORD_DATA.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.PRIORITY.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.STATUS.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.IS_REMINDER_SET.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.IS_FOLLOW_UP_SCHEDULED.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_OWNER_ID.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_OWNER_NAME.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_CREATED_BY_ID.value + ",");
			sb.append(ColumnsNFields.TaskNActivityColumn.SF_CREATED_BY_NAME.value + ",");
			sb.append(ColumnsNFields.COMMON_KEY_CREATE_DATETIME + ",");
			sb.append(ColumnsNFields.COMMON_KEY_LATITUDE + ",");
			sb.append(ColumnsNFields.COMMON_KEY_LONGITUDE + ",");
			sb.append(ColumnsNFields.COMMON_KEY_ADDRESS);

			if (sfTask.hasFollowUpTask)
				sb.append("," + ColumnsNFields.TaskNActivityColumn.FOLLOW_UP_DATETIME.value);

			if (sfTask.isReminderOn)
				sb.append("," + ColumnsNFields.TaskNActivityColumn.REMINDER_DATETIME.value);

			if (sfTask.taskStatus.equals(Constants.TaskStatus.COMPLETED.value))
				sb.append("," + ColumnsNFields.COMMON_KEY_UPDATED_DATETIME);

			sb.append(") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?");

			if (sfTask.hasFollowUpTask)
				sb.append(",?");

			if (sfTask.isReminderOn)
				sb.append(",?");

			if (sfTask.taskStatus.equals(Constants.TaskStatus.COMPLETED.value))
				sb.append(",?");

			sb.append(")");

			preparedStatement = connection.prepareStatement(sb.toString(),
					new String[] { ColumnsNFields.COMMON_KEY_ID });

			preparedStatement.setInt(1, userId);
			preparedStatement.setString(2, sfTask.sfId);
			preparedStatement.setString(3, sfTask.objectId);
			preparedStatement.setString(4, sfTask.objectType);
			preparedStatement.setString(5, BasicUtils.substringToLength(sfTask.objectName, 96));
			preparedStatement.setString(6, sfTask.subject);
			preparedStatement.setString(7, sfTask.activityType);
			preparedStatement.setString(8, sfTask.activityResult);
			preparedStatement.setString(9, BasicUtils.substringToLength(sfTask.activityDescription, 512));
			preparedStatement.setString(10, sfTask.recordData.toString());
			preparedStatement.setString(11, sfTask.taskPriority);
			preparedStatement.setString(12, sfTask.taskStatus);
			preparedStatement.setBoolean(13, sfTask.isReminderOn);
			preparedStatement.setBoolean(14, sfTask.hasFollowUpTask);
			preparedStatement.setString(15, sfTask.ownerId);
			preparedStatement.setString(16, sfTask.ownerName);
			preparedStatement.setString(17, sfTask.createdById);
			preparedStatement.setString(18, sfTask.createdByName);
			preparedStatement.setString(19, currentDateTime);
			preparedStatement.setDouble(20, sfTask.latitude);
			preparedStatement.setDouble(21, sfTask.longitude);
			preparedStatement.setString(22, sfTask.address.toJson().toString());

			if (sfTask.hasFollowUpTask) {
				preparedStatement.setString(23, DateTimeUtils.getSFtoDBDateTime(sfTask.followUpDateTime));
				if (sfTask.isReminderOn) {
					preparedStatement.setString(24, DateTimeUtils.getSFtoDBDateTime(sfTask.reminderDatetime));
					if (sfTask.taskStatus.equals(Constants.TaskStatus.COMPLETED.value)) {
						preparedStatement.setString(25, currentDateTime);
					}
				} else if (sfTask.taskStatus.equals(Constants.TaskStatus.COMPLETED.value)) {
					preparedStatement.setString(24, currentDateTime);
				}
			} else if (sfTask.isReminderOn) {
				preparedStatement.setString(23, DateTimeUtils.getSFtoDBDateTime(sfTask.reminderDatetime));
				if (sfTask.taskStatus.equals(Constants.TaskStatus.COMPLETED.value)) {
					preparedStatement.setString(24, currentDateTime);
				}
			} else if (sfTask.taskStatus.equals(Constants.TaskStatus.COMPLETED.value)) {
				preparedStatement.setString(23, currentDateTime);
			}

			/*
			 * if (sfTask.hasFollowUpTask) preparedStatement.setString(20,
			 * (sfTask.followUpDateTime.equals(Constants.NA) ? currentDateTime :
			 * DateTimeUtils.getSFtoDBDateTime(sfTask.followUpDateTime)));
			 * 
			 * if (sfTask.isReminderOn) preparedStatement.setString(21,
			 * (sfTask.reminderDatetime.equals(Constants.NA) ? currentDateTime :
			 * DateTimeUtils.getSFtoDBDateTime(sfTask.reminderDatetime)));
			 * 
			 * if (sfTask.taskStatus.equals(Constants.TaskStatus.COMPLETED.value))
			 * preparedStatement.setString(22,
			 * (sfTask.taskStatus.equals(Constants.TaskStatus.COMPLETED.value) ?
			 * currentDateTime : currentDateTime));
			 */

			boolean isSuccess = preparedStatement.executeUpdate() == 1;

			if (isSuccess) {

				resultSet = preparedStatement.getGeneratedKeys();

				if (null != resultSet && resultSet.first())
					sfTask.id = resultSet.getInt(1);

				return sfTask;

			}

		}

		return null;

	}

	public ArrayList<FetchDocument> fetchDoc(String mobileNumber, String secondaryMobileNumber) throws SQLException {

		checkConnection();

		StringBuilder sb = new StringBuilder();

		sb.append(" select document_type as type, document_id as id,user_name as customerName ");
		sb.append(" from RMManagementDB.kyc_document where mobile_number = ? ");
		sb.append(" union select bill_type as type,bill_id as id ,customer_name as customerName ");
		sb.append(" from RMManagementDB.utility_bill where mobile_number in (? , ?)");
		sb.append(
				" union select 'vehicle_rc' as type,registration_number as id,registered_owner_name as customerName ");
		sb.append(" from RMManagementDB.vehicle_rc_info where mobile_number = ? ");
		sb.append(" union select 'epf_passbook' as type,uan as id,employee_name as customerName ");
		sb.append(" from RMManagementDB.epf_detail where mobile_number = ? ");
		sb.append(" union select 'gstin' as type,gstin as id, business_legal_name as customerName ");
		sb.append(" from RMManagementDB.gstin_detail where mobile_number = ? ");
		sb.append(" union select 'itrv' as type,acknowledge_number as id,customer_name as customerName ");
		sb.append(" from RMManagementDB.itrv_document where mobile_number = ? ");

		preparedStatement = connection.prepareStatement(sb.toString());

		preparedStatement.setString(1, mobileNumber);
		preparedStatement.setString(2, mobileNumber);
		preparedStatement.setString(3, secondaryMobileNumber);
		preparedStatement.setString(4, mobileNumber);
		preparedStatement.setString(5, mobileNumber);
		preparedStatement.setString(6, mobileNumber);
		preparedStatement.setString(7, mobileNumber);

		resultSet = preparedStatement.executeQuery();

		ArrayList<FetchDocument> fetchDocuments = new ArrayList<FetchDocument>();

		if (null != resultSet && resultSet.first()) {
			do {

				FetchDocument fetchDoc = new FetchDocument(resultSet);
				if (fetchDoc.id.length() > 15) {
					if (DocumentType.get(fetchDoc.type) != null) {
						fetchDoc.id = CryptoUtils.decrypt(fetchDoc.id);
					}
				}

				fetchDocuments.add(fetchDoc);

			} while (resultSet.next());
		}

		return fetchDocuments;

	}

	public String getApplicantOrCoApplicantImageUrl(String mobileNumber, String tableName) throws SQLException {

		checkConnection();

		String query = "select image_url from " + tableName + " where customer_mobile_number = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, mobileNumber);

		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first())
			return resultSet.getString("image_url");
		else
			return null;

	}

	public boolean setApplicantProfilePicture(String mobileNumber, String imageUrl, ApplicantType applicantType)
			throws SQLException {

		checkConnection();

		String query = "update " + (applicantType == ApplicantType.PRIMARY ? ColumnsNFields.SF_MAIN_OBJECT_TABLE
				: ColumnsNFields.SF_CO_AP_OBJECT_TABLE) + " set image_url = ? where customer_mobile_number = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, imageUrl);
		preparedStatement.setString(2, mobileNumber);

		return preparedStatement.executeUpdate() > 0;

	}

	public boolean verifyMobileNumber(String mobileNumber) throws SQLException {

		checkConnection();

		String query = "update utility_bill set is_verified = 1 where mobile_number = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, mobileNumber);
		boolean status = preparedStatement.executeUpdate() == 1;

		if (status)
			LoggerUtils.log("Verify OTP successfully");
		else
			LoggerUtils.log("Failed to verify OTP");

		return status;
	}

	public boolean verifyCrownIpAddress(String ipAddress) throws SQLException {

		checkConnection();

		String query = "Select * from whitelisted_ip where ip_address = ?";

		preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, ipAddress);
		resultSet = preparedStatement.executeQuery();

		if (null != resultSet && resultSet.first()) {
			return resultSet.getBoolean("is_active");
		}

		return false;

	}

	public boolean addUpdatedRegionMapData(JSONArray sfData) throws SQLException {

		checkConnection();

		String truncateQuery = "TRUNCATE " + ColumnsNFields.REGION_MAP_TABLE;
		preparedStatement = connection.prepareStatement(truncateQuery);
		int truncateResult = preparedStatement.executeUpdate(truncateQuery);
		
		if (truncateResult != 0) return false;

		String commonQuery = "Insert into " + ColumnsNFields.REGION_MAP_TABLE + "("
				+ ColumnsNFields.RegionMapColumn.USER_EMAIL_ID.value + ","
				+ ColumnsNFields.RegionMapColumn.CURRENT_LOCATION.value + ","
				+ ColumnsNFields.RegionMapColumn.REGION.value + "," + ColumnsNFields.RegionMapColumn.NAME.value + ","
				+ ColumnsNFields.RegionMapColumn.DESIGNATION.value + "," + ColumnsNFields.RegionMapColumn.CLUSTER.value
				+ ")" + "values ";

		StringBuilder query = new StringBuilder();

		query.append(commonQuery);

		for (int m = 0; m < sfData.length(); m++) {

			JSONObject current = (JSONObject) sfData.get(m);			
			
			query.append("(");
			query.append("'");
			query.append(current.optString("Official_Email_ID__c"));
			query.append("'");
			query.append(",");

			if (null != current.optJSONObject("Current_Location__r")) {

				query.append("'");
				query.append(current.optJSONObject("Current_Location__r").optString("Name"));
				query.append("'");
				query.append(",");
				query.append("'");

				if (null != current.optJSONObject("Current_Location__r").optJSONObject("Region__r")) {
					query.append(
							current.optJSONObject("Current_Location__r").optJSONObject("Region__r").optString("Name"));
				} else {
					query.append("UnKnown");
				}
				query.append("'");
				query.append(",");

			} else {
				query.append("'nill','nill',");
			}

			query.append("'");
			query.append(current.optString("Name").replace("'", "''"));
			query.append("'");
			query.append(",");
			query.append("'");
			query.append(current.optString("Designation__c"));
			query.append("'");
			query.append(",");
			query.append("'");
			
			if (null != current.optJSONObject("Current_Location__r")
					&& null != current.optJSONObject("Current_Location__r").optJSONObject("Region__r")) {
				
				query.append(RegionMap.mapRegionToCluster(
						current.optJSONObject("Current_Location__r").optJSONObject("Region__r").optString("Name")));
				
			} else {
				query.append("UnKnown");
			}
			
			query.append("'");
			query.append(")");
			
			if (m < sfData.length() - 1) {
				query.append(",");
			}

		}

		preparedStatement = connection.prepareStatement(query.toString());

		return preparedStatement.executeUpdate() > 0;

	}
	
}
