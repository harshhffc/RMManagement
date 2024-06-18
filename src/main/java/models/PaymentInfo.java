package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.json.JSONObject;

import manager.PaymentManager.PaymentStatus;
import utils.BasicUtils;
import utils.Constants;
import utils.Constants.PaymentType;
import utils.DateTimeUtils;

@Entity
@Table(name = "`payment_info`")
public class PaymentInfo {
	
	@Id
	@Column(name = "transaction_id", nullable = false)
	public String transactionId;
	
	@Column(name = "user_id", nullable = false)
	public int userId;
	
	@Column(name = "opportunity_id")
	public String sfOpportunityId;
	
	@Column(name = "opportunity_number")
	public String sfOpportunityNumber;
	
	@Column(name = "opportunity_name")
	public String sfOpportunityName;
	
	@Column(name = "loan_account_number")
	public String sfLoanAccountNumber;
	
	@ColumnDefault("INR")
	public String currency = "INR";
	
	public double amount = -1.0;
	
	@Column(name = "initial_datetime", columnDefinition = "DATETIME", updatable = false, nullable = false)
	public String initialDatetime;
	
	@Column(name = "completion_datetime", columnDefinition = "DATETIME")
	public String completionDatetime;
	
	@Column(name = "order_id")
	public String pgOrderId;
	
	@Column(name = "payment_id")
	public String pgPaymentId;
	
	@Column(name = "payment_status")
	public String paymentStatus;
	
	@Column(name = "pg_status")
	public String pgStatus;
	
	@Column(name = "receipt_status")
	public String receiptStatus;
	
	@Column(name = "status_message")
	public String statusMessage;
	
	@Column(name = "receipt_id")
	public String sfReceiptId;
	
	@Column(name = "receipt_number")
	public String sfReceiptNumber;
	
	@Column(name = "receipt_data")
	public String pgPaymentData;
	
	@Column(name = "sf_payment_type")
	public String sfPaymentNature;
	
	@Column(name = "sf_payment_sub_type")
	public String sfPaymentSubType;
	
	@Column(name = "sf_part_pre_payment_type")
	public String sfPartPrePaymentType;
	
	@Column(name = "sf_closure_reason")
	public String sfClosureReason;
	
	@Column(name = "sf_transferred_hfc_name")
	public String sfTransferredHFCName;
	
	@Column(name = "payment_method")
	public String paymentMethod;
	
	@Column(name = "device_type")
	public String deviceType;
	
	@Column(name = "device_id")
	public String deviceId;
	
	@Column(name = "x_sell_product_id")
	public String sfXSellProductId;
	
	@Column(name = "loan_product_id")
	public String sfLoanProductId;
	
	@Column(name = "loan_sub_type")
	public String sfLoanSubType;
	
	@Column(name = "customer_name")
	public String customerName;
	
	@Column(name = "customer_mobile_number")
	public String customerMobileNumber;
	
	@Column(name = "customer_email_id")
	public String customerEmailId;
	
	@Column(name = "hfo_payment_id")
	public String hfoPaymentId;
	
	@Column(name = "payment_link")
	public String paymentLink;
	
	public String linkType;
	
	@ColumnDefault("0")
	public boolean isNotified = false;
	
	@Transient
	public String upiAddress;
	
	@Transient
	public String source;
	
	@Transient
	public String sourceId;
	
	public PaymentInfo() {}
	
	public PaymentInfo(JSONObject data) {
		
		transactionId = data.optString("transactionId", Constants.NA);
		userId = data.optInt("userId", -1);
		upiAddress = data.optString("upiAddress", Constants.NA);
		sfOpportunityId = data.optString("opportunityId", Constants.NA);
		sfOpportunityNumber = data.optString("opportunityNumber", Constants.NA);
		sfOpportunityName = data.optString("opportunityName", Constants.NA);
		sfLoanAccountNumber = data.optString("loanAccountNumber", Constants.NA);
		currency = data.optString("currency", Constants.NA);
		amount = data.optDouble("amount", -1.0);
		initialDatetime = data.optString("initialDatetime", Constants.NA);
		completionDatetime = data.optString("completionDatetime", Constants.NA);
		pgOrderId = data.optString("orderId", Constants.NA);
		pgPaymentId = data.optString("paymentId", Constants.NA);
		paymentStatus = data.optString("paymentStatus", Constants.NA);
		receiptStatus = data.optString("receiptStatus", Constants.NA);
		statusMessage = data.optString("statusMessage", Constants.NA);
		sfReceiptId = data.optString("receiptId", Constants.NA);
		sfReceiptNumber = data.optString("receiptNumber", Constants.NA);
		pgPaymentData = data.optString("receiptData", Constants.NA);
		sfPaymentNature = data.optString("sfPaymentType", Constants.NA);
		sfPaymentSubType = data.optString("sfPaymentSubType", Constants.NA);
		sfPartPrePaymentType = data.optString("sfPartPrePaymentType", Constants.NA);
		sfClosureReason = data.optString("sfClosureReason", Constants.NA);
		sfTransferredHFCName = data.optString("sfTransferredHFCName", Constants.NA);			
		paymentMethod = data.optString("paymentMethod", Constants.NA);
		deviceType = data.optString("deviceType", Constants.NA);
		deviceId = data.optString("deviceId", Constants.NA);
		sfXSellProductId = data.optString("xSellProductId", Constants.NA);
		sfLoanProductId = data.optString("loanProductType", Constants.NA);
		sfLoanSubType = data.optString("subProductType", Constants.NA);
		
	}
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("transactionId", transactionId);
		json.put("userId", userId);
		json.put("opportunityId", sfOpportunityId);
		json.put("opportunityNumber", sfOpportunityNumber);
		json.put("opportunityName", sfOpportunityName);
		json.put("loanAccountNumber", sfLoanAccountNumber);
		json.put("currency", currency);
		json.put("amount", amount);
		json.put("initialDatetime", initialDatetime);
		json.put("completionDatetime", completionDatetime);
		json.put("orderId", pgOrderId);
		json.put("paymentId", pgPaymentId);
		json.put("paymentStatus", paymentStatus);
		json.put("receiptStatus", receiptStatus);
		json.put("statusMessage", statusMessage);
		json.put("receiptId", sfReceiptId);		
		json.put("receiptNumber", sfReceiptNumber);
		json.put("receiptData", pgPaymentData);
		json.put("sfPaymentType", sfPaymentNature);
		json.put("sfPaymentSubType", sfPaymentSubType);
		json.put("sfPartPrePaymentType", sfPartPrePaymentType);
		json.put("sfClosureReason", sfClosureReason);
		json.put("sfTransferredHFCName", sfTransferredHFCName);
		json.put("paymentMethod", paymentMethod);
		json.put("deviceType", deviceType);
		json.put("deviceId", deviceId);
		json.put("xSellProductId", sfXSellProductId);
	    json.put("loanProductType", sfLoanProductId);
	    json.put("subProductType", sfLoanSubType);

		
		return json;
	}
	
	public void fillValuesForRemotePayment(User user) {
		paymentStatus = PaymentStatus.CREATED.value;
		var transactionIdValue = "rm_TXN" + System.currentTimeMillis();
		transactionId = transactionIdValue;
		source = Constants.REMOTE_PAYMENT_SOURCE;
		sourceId = transactionIdValue;
		initialDatetime = DateTimeUtils.getCurrentDateTimeInIST();
		paymentMethod = Constants.REMOTE_PAYMENT_METHOD;
		userId = user.id;
	}
	
	public boolean isValidPaymentParameter() {
		
		if (!BasicUtils.isNotNullOrNA(paymentMethod)) 
			return false;
		
		if (paymentMethod == Constants.CASH_PAYMENT_METHOD) {
			if(sfPaymentNature == PaymentType.EMI.value 
					|| sfPaymentNature == PaymentType.PRE_EMI_INTEREST.value
					|| sfPaymentNature == PaymentType.NPA_CLOSURE_RELATED.value) {
				return true;
			} else {
				return false;
			}
		}
		
		return true;
		
	}

}
