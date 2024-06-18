package v2.managers;

import javax.ws.rs.core.Response;

import models.admin.ClientRequest;
import utils.BasicAuthCreds;
import utils.BasicUtils;
import utils.Constants;
import utils.LocalResponse;
import utils.LoggerUtils;
import utils.OneResponse;
import utils.ProptertyUtils;
import v1.repository.PartnerRepository;
import utils.Constants.ActionType;
import utils.Constants.Errors;


public class SecurityManager {
	
	private PartnerRepository _partnerRepo = null;
	
	private PartnerRepository partnerRepo() {
		if (null == _partnerRepo)
			_partnerRepo = new PartnerRepository();
		return _partnerRepo;
	}

	public Response authenticateRequest(ClientRequest cRequest) throws Exception {

		if (!BasicUtils.isNotNullOrNA(cRequest.authorization)) {

			LoggerUtils.log("Invalid authorization header value received while authenticateRequest: "
					+ cRequest.authorization);

			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage("Invalid auth headers.").setError(Errors.INVALID_CREDENTIALS.value).toJson());

		}

		if (!BasicUtils.isNotNullOrNA(cRequest.orgId)) {

			LoggerUtils.log("Invalid Orgranization ID received while authenticateRequest: " + cRequest.orgId);

			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage("Invalid Orgranization ID.").setError(Errors.INVALID_CREDENTIALS.value).toJson());

		}

		BasicAuthCreds clientCreds = new BasicUtils().getClientCreds(cRequest.authorization);

		//HFPartner hfPartner = hfpDbHelper.getPartnerInfo(cRequest.orgId);
		
		final var hfPartner = partnerRepo().findPartnerOrgId(cRequest.orgId);

		if (null == hfPartner) {

			LoggerUtils.log("No hf partner found while authenticateRequest.");

			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage("No organization info found.").setError(Errors.RESOURCE_NOT_FOUND.value).toJson());

		}

		if (!hfPartner.isEnabled) {

			LoggerUtils.log("HF Partner is not enabled: " + cRequest.orgId);

			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage("Orgranization ID is disabled. Please contact admin.")
					.setError(Errors.ACCESS_DENIED.value).toJson());

		}

		if (!hfPartner.servicesAllowed.contains(Constants.ROLE_ALL)
				&& !hfPartner.servicesAllowed.contains(cRequest.methodName)) {

			LoggerUtils.log(cRequest.methodName + " service is not enabled for orgId : " + cRequest.orgId);

			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage("This service is not enabled for your Org ID. Please contact admin.")
					.setError(Errors.ACCESS_DENIED.value).setAction(ActionType.CONTACT_ADMIN.stringValue).toNewJson());

		}

		if (hfPartner.ipRestricted) {

			if (!partnerRepo().isPartnerIPAllowed(cRequest.orgId, cRequest.ipAddress)) {

				LoggerUtils.log("HF Partner's IP Address is blocked for orgId : " + cRequest.orgId);

				return new OneResponse().getFailureResponse(new LocalResponse()
						.setStatus(false).setMessage("IP Address is blocked. Please contact admin.")
						.setError(Errors.ACCESS_DENIED.value).toJson());

			}

		}

		if (ProptertyUtils.getKeyBearer().decrypt(hfPartner.clientId).equals(clientCreds.clientId)
				&& ProptertyUtils.getKeyBearer().decrypt(hfPartner.clientSecret).equals(clientCreds.clientSecret)) {

			LoggerUtils
					.log("Client successfully authorized while authenticateRequest for orgId : " + cRequest.orgId);

			return new OneResponse()
					.getSuccessResponse(new LocalResponse().setStatus(true).setMessage("Authorized.").toJson());

		} else {

			LoggerUtils.log(
					"Invalid cliendID and clientSecrete while authenticateRequest for orgId : " + cRequest.orgId);

			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage("Invalid client credentials.").setError(Errors.INVALID_CREDENTIALS.value).toJson());

		}

	}

	public Response verifyExternalPartnerSession(ClientRequest cRequest) throws Exception {

		if (!BasicUtils.isNotNullOrNA(cRequest.sessionPasscode)) {

			LoggerUtils.log("Invalid sessionPasscode while authenticateRequest: " + cRequest.sessionPasscode);

			return new OneResponse().getFailureResponse(new LocalResponse().setStatus(false)
					.setMessage("Invalid session passcode.").setError(Errors.INVALID_CREDENTIALS.value).toJson());

		}

		Response authenticateResponse = authenticateRequest(cRequest);

		if (authenticateResponse.getStatus() != 200) {

			LoggerUtils.log("authenticateRequest failed while verifySession.");
			return authenticateResponse;

		}

		final var hfPartner = partnerRepo().findPartnerOrgId(cRequest.orgId);

		if (hfPartner.isSessionValid() && cRequest.sessionPasscode.equals(hfPartner.sessionPasscode)) {

			hfPartner.updateSession(false); // Increase session validity by 1 hour

			return 
					new OneResponse().getSuccessResponse(BasicUtils.getSuccessTemplateObject());

		}

		return new OneResponse().getAccessDeniedResponse();

	}
}
