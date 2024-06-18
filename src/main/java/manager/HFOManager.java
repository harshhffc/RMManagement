package manager;

import org.json.JSONObject;

import networking.HFONetworkClient;
import networking.HFONetworkClient.Endpoints;
import utils.Constants;
import utils.Constants.Actions;
import utils.Constants.Errors;
import utils.LocalHTTPResponse;
import utils.LocalResponse;
import utils.LoggerUtils;

public class HFOManager {

	private HFONetworkClient _networkClient = null;

	private HFONetworkClient getHFOClient() throws Exception {
		if (null == _networkClient)
			_networkClient = new HFONetworkClient();
		return _networkClient;
	}

	private HFONetworkClient getHFOClient(int timeout) throws Exception {
		if (null == _networkClient)
			_networkClient = new HFONetworkClient(timeout);
		return _networkClient;
	}

	private void log(String value) {
		LoggerUtils.log(HFOManager.class.getSimpleName() + "." + value);
	}

	public LocalResponse addSitePhotograph(JSONObject requestJson) throws Exception {

		LocalHTTPResponse lhResponse = getHFOClient(240)
				.POST(HFONetworkClient.Endpoints.ADD_SITE_PHOTOGRAPH.getFullUrl(), requestJson);
		LocalResponse lResponse = new LocalResponse();

		if (lhResponse.isSuccess) {

			lResponse = new LocalResponse().setStatus(true).setMessage(lhResponse.stringEntity);
			log("addSitePhotograph - Success.");

		} else {

			if (lhResponse.stringEntity.startsWith("{"))
				lResponse = new LocalResponse(new JSONObject(lhResponse.stringEntity));
			else {
				lResponse.action = Actions.RETRY.value;
				lResponse.error = Errors.OPERATION_FAILED.value;
			}

			log("addSitePhotograph - Error: " + lResponse.message);

			return lResponse;

		}

		return lResponse;

	}

	public LocalResponse getSitePhotographList(JSONObject requestJson) throws Exception {

		LocalHTTPResponse lhResponse = getHFOClient()
				.POST(HFONetworkClient.Endpoints.GET_SITE_PHOTOGRAPH_LIST.getFullUrl(), requestJson);
		LocalResponse lResponse = new LocalResponse();

		if (lhResponse.isSuccess) {

			lResponse = new LocalResponse().setStatus(true).setMessage(lhResponse.stringEntity);
			log("getSitePhotographList - Success.");

		} else {

			if (lhResponse.stringEntity.startsWith("{"))
				lResponse = new LocalResponse(new JSONObject(lhResponse.stringEntity));
			else {
				lResponse.action = Actions.RETRY.value;
				lResponse.error = Errors.OPERATION_FAILED.value;
			}

			log("getSitePhotographList - Error: " + lResponse.message);

			return lResponse;

		}

		return lResponse;

	}

	public LocalResponse sendPaymentLink(JSONObject requestJson) throws Exception {

		LocalHTTPResponse lhResponse = getHFOClient().POST(HFONetworkClient.Endpoints.SEND_PAYMENT_LINK.getFullUrl(),
				requestJson);
		LocalResponse lResponse = new LocalResponse();

		if (lhResponse.isSuccess) {

			lResponse = new LocalResponse().setStatus(true).setMessage(lhResponse.stringEntity);
			log("sendPaymentLink - Success.");

		} else {

			if (lhResponse.stringEntity.startsWith("{"))
				lResponse = new LocalResponse(new JSONObject(lhResponse.stringEntity));
			else {
				lResponse.action = Actions.RETRY.value;
				lResponse.error = Errors.OPERATION_FAILED.value;
			}

			log("sendPaymentLink - Error: " + lResponse.message);

			return lResponse;

		}

		return lResponse;

	}

	public LocalResponse generateFailedReceipt(String hfoId) throws Exception {

		var receiptJson = new JSONObject();
		receiptJson.put(Constants.ID, hfoId);

		LocalHTTPResponse lhResponse = getHFOClient()
				.POST(HFONetworkClient.Endpoints.GENERATE_FAILED_RECEIPT.getFullUrl(), receiptJson);
		LocalResponse lResponse = new LocalResponse();

		if (lhResponse.isSuccess) {

			lResponse = new LocalResponse().setStatus(true).setMessage(lhResponse.stringEntity);

		} else {

			if (lhResponse.stringEntity.startsWith("{"))
				lResponse = new LocalResponse(new JSONObject(lhResponse.stringEntity));
			else {
				lResponse.action = Actions.RETRY.value;
				lResponse.error = Errors.OPERATION_FAILED.value;
			}

			log("generateFailedReceipt - Error: " + lResponse.message);

			return lResponse;

		}

		return lResponse;

	}
	
	public LocalResponse getLoanDetails(String loanAccountNumber) throws Exception {

		var receiptJson = new JSONObject();
		receiptJson.put(Constants.ID, loanAccountNumber);
		
		var loanUrl = Endpoints.LOAN_DETAILS.getFullUrl().concat("/").concat(loanAccountNumber);
		
		LocalHTTPResponse lhResponse = getHFOClient().GET(loanUrl);
				
		LocalResponse lResponse = new LocalResponse();

		if (lhResponse.isSuccess) {

			lResponse = new LocalResponse().setStatus(true).setMessage(lhResponse.stringEntity);
	
		} else {

			if (lhResponse.stringEntity.startsWith("{"))
				lResponse = new LocalResponse(new JSONObject(lhResponse.stringEntity));
			else {
				lResponse.action = Actions.RETRY.value;
				lResponse.error = Errors.OPERATION_FAILED.value;
			}

			log("getLoanDetails - Error: " + lResponse.message);

			return lResponse;

		}

		return lResponse;

	}

	public LocalResponse clickToCall(JSONObject callRequest) throws Exception {
		
		LocalHTTPResponse lhResponse = getHFOClient().POST(HFONetworkClient.Endpoints.VOICE_CALL.getFullUrl(),
				callRequest);

		LocalResponse lResponse = new LocalResponse();

		if (lhResponse.isSuccess) {

			lResponse = new LocalResponse().setStatus(true).setMessage(lhResponse.stringEntity);
			log("clickToCall - Success: " + lResponse.message);

		} else {

			if (lhResponse.stringEntity.startsWith("{"))
				lResponse = new LocalResponse(new JSONObject(lhResponse.stringEntity));
			else {
				lResponse.action = Actions.RETRY.value;
				lResponse.error = Errors.OPERATION_FAILED.value;
			}

			log("clickToCall - Error: " + lResponse.message);

			return lResponse;

		}

		return lResponse;

	}

}
