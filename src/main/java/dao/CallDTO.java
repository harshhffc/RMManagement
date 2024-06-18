package dao;

import utils.BasicUtils;
import utils.Constants;
import utils.LocalResponse;
import utils.LoggerUtils;
import utils.Constants.Actions;
import utils.Constants.Errors;

public class CallDTO {

	public String caller = null;
	public String receiver = null;
	public String objectName = null;
	public String objectId = null;
	public String source = null;
	public String userId = null;
	public String userEmail = null;
	public String userName = null;

	public LocalResponse allMandatoryFieldsPresent() {

		final var lResponse = new LocalResponse();

		lResponse.action = Actions.FIX_RETRY.value;
		lResponse.error = Errors.INVALID_DATA.value;

		if (!BasicUtils.isNotNullOrNA(receiver)) {
			LoggerUtils.log("allMandatoryFieldPresent - Invalid receiver");
			lResponse.message = "Invalid receiver";
			return lResponse;
		}
		
		if (receiver.length() != 10) {
			LoggerUtils.log("allMandatoryFieldPresent - Invalid receiver mobile number");
			lResponse.message = "Invalid receiver mobile number";
			return lResponse;
		}

		if (!BasicUtils.isNotNullOrNA(objectName)) {
			LoggerUtils.log("allMandatoryFieldsPresent - Invalid object name.");
			lResponse.message = "Invalid object name.";
			return lResponse;
		}

		if (!BasicUtils.isNotNullOrNA(objectId)) {
			LoggerUtils.log("allMandatoryFieldsPresent - Invalid object Id.");
			lResponse.message = "Invalid object Id.";
			return lResponse;
		}

		lResponse.isSuccess = true;
		lResponse.message = "Request is valid";
		lResponse.action = Constants.NA;
		lResponse.error = Constants.NA;

		return lResponse;
	}

}
