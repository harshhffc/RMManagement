package v2.managers;

import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.google.gson.Gson;

import manager.PaymentManager;
import models.PaymentInfo;
import models.notification.CrossNotification;
import utils.BasicUtils;
import utils.Constants;
import utils.Constants.ActionType;
import utils.Constants.Actions;
import utils.Constants.Errors;
import utils.DateTimeUtils;
import utils.LocalResponse;
import utils.LoggerUtils;
import utils.NotificationUtils.NotificationEndpoint;
import utils.OneResponse;
import v1.repository.PartnerRepository;
import v1.repository.PaymentInfoRepository;

public class ExternalPartnerManager {

	private PartnerRepository _partnerRepos = null;
	private final Gson gson;
	private PaymentInfoRepository _paymentInfoRepos= null;

	public ExternalPartnerManager() {
		gson = new Gson();
	}

	private PartnerRepository partnerRepos() {
		if (null == _partnerRepos)
			_partnerRepos = new PartnerRepository();
		return _partnerRepos;
	}
	
	private PaymentInfoRepository paymentInfoRepos() {
		if (null == _paymentInfoRepos)
			_paymentInfoRepos = new PaymentInfoRepository();
		return _paymentInfoRepos;
	}

	private void log(String value) {
		LoggerUtils.log("ExternalPartnerManager." + value);
	}

	public Response authenticateClient(String orgId) throws Exception {

		final var hfPartner = partnerRepos().findPartnerOrgId(orgId);

		if (hfPartner.updateSession(true)) {

			log("authenticateClient - Session update successfully for orgId : " + orgId);

			JSONObject responseJson = new JSONObject();
			responseJson.put(Constants.SESSION_PASSCODE, hfPartner.sessionPasscode);
			responseJson.put(Constants.VALID_UPTO, hfPartner.sessionValidDatetime);

			return new OneResponse().getSuccessResponse(responseJson);

		} else {

			log("authenticateClient - Failed to update session for orgId : " + orgId);

			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage(Constants.DEFAULT_ERROR_MESSAGE).setError(Errors.OPERATION_FAILED.value)
					.setAction(ActionType.AUTHENTICATE_AGAIN.stringValue).toNewJson());

		}

	}
	
	public Response pushCrossNotification(String orgId, JSONObject requestJson) throws Exception {

		final var crossNotification = gson.fromJson(requestJson.toString(), CrossNotification.class);

		final var notificationOrigin = NotificationEndpoint.get(crossNotification.origin);

		if (null == notificationOrigin) {

			log("pushCrossNotification - Invalid origin of notificaton : " + crossNotification.origin);

			return new OneResponse().getFailureResponse(new LocalResponse().setMessage("Invalid notification origin")
					.setError(Errors.INVALID_DATA.value).setAction(Actions.FIX_RETRY.value).toJson());

		}
		
		switch (notificationOrigin) {
		case CONNECTOR_APP:
			return new NotificationHelper(2).pushConnectorNotification(crossNotification);
		default:
			break;
		}

		return new OneResponse().getSuccessResponse(
				new JSONObject().put(Constants.MESSAGE, "Notification routing initiated successfully."));

	}
	
	public Response updatePaymentStatus(JSONObject requestJson) throws Exception {

		var paymentInfo = gson.fromJson(requestJson.toString(), PaymentInfo.class);
		
		var ePaymentInfo = paymentInfoRepos().findPaymentInfoByTransId(paymentInfo.sourceId);
		
		if(null == ePaymentInfo) {
			
			log("updatePaymentStatus - No transaction found for : " + paymentInfo.sourceId);
			return new OneResponse().getFailureResponse(new LocalResponse().setMessage("No transaction found!")
					.setAction(Actions.RETRY.value).toJson()); 
			
		}
		
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
		
		ePaymentInfo.statusMessage = "Payment:" + ePaymentInfo.paymentStatus + " | " + "Receipt:" + ePaymentInfo.receiptStatus;
		
		if (paymentInfo.pgStatus.equalsIgnoreCase(PaymentManager.RAZOR_PAY_PAID_STATUS)) {
			
			if (!ePaymentInfo.isNotified) {
				
				log("updatePaymentStatus - sending notification for remote payment...");
				new NotificationHelper(6).sendCompletedPaymentNotification(ePaymentInfo);
				
				ePaymentInfo.isNotified = true;
				
			}
			
		}
		
		if (!paymentInfoRepos().savePaymentInfo(ePaymentInfo)) {
			log("updatePaymentStatus - Failed to update payment status in DB");
			return new OneResponse().getFailureResponse(new LocalResponse().setMessage("Failed to update payment status in DB")
					.setAction(Actions.RETRY.value).toJson());
		}
		
		log("updatePaymentStatus - Payment status updated successfully");
	
		return new OneResponse().getSuccessResponse(new LocalResponse()
				.setStatus(true)
				.setMessage("Payment status updated successfully!")
				.setAction(Actions.CONTINUE.value).toJson());

	}

}
