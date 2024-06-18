package totalkyc;

import org.json.JSONObject;

import models.DefaultResponse;
import utils.Constants;

public class KYCRequestValidator {

	public KYCRequestValidator() {}
	
	private static boolean hasConsent(JSONObject request) {
		return request.has(DocumentKYCHelper.KEY_CONSENT);
	}
	
	public static boolean hasMobileNumber(JSONObject request) {
		return request.has(DocumentKYCHelper.KEY_MOBILE_NUMBER);
	}
	
	public static boolean isValidMobileNumber(JSONObject request) {
		final String mobileNumber = request.optString(DocumentKYCHelper.KEY_MOBILE_NUMBER, Constants.NA); 
		return (!mobileNumber.startsWith("+91")
				&& !mobileNumber.isEmpty()
				&& !mobileNumber.equalsIgnoreCase(Constants.NA)
				&& mobileNumber.length() == 10);
	}
	
	public static DefaultResponse validatePANRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_PAN)) {				
				if (hasMobileNumber(request)) {
					if (isValidMobileNumber(request)) {
						dResponse.isSuccess = true;
						dResponse.message = "Request is valid.";
					} else {
						dResponse.isSuccess = false;
						dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
					}
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;				
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_PAN;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validatePANVerifcationRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_PAN)) {
				if (request.has(DocumentKYCHelper.KEY_NAME)) {
					if (request.has(DocumentKYCHelper.KEY_DOB)) {
						if (hasMobileNumber(request)) {
							if (isValidMobileNumber(request)) {
								dResponse.isSuccess = true;
								dResponse.message = "Request is valid.";
							} else {
								dResponse.isSuccess = false;
								dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
							}
						} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
					} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_DOB;
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_NAME;			
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_PAN;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateDLRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_DL_NUMBER)) {
				if (request.has(DocumentKYCHelper.KEY_DOB)) {
					
					
					if (hasMobileNumber(request)) {
						if (isValidMobileNumber(request)) {
							dResponse.isSuccess = true;
							dResponse.message = "Request is valid.";
						} else {
							dResponse.isSuccess = false;
							dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
						}
					} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
					
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_DOB;
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_DL_NUMBER;			
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateVoterIDRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_EPIC_NUMBER)) {				
				
				if (hasMobileNumber(request)) {
					if (isValidMobileNumber(request)) {
						dResponse.isSuccess = true;
						dResponse.message = "Request is valid.";
					} else {
						dResponse.isSuccess = false;
						dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
					}
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
				
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_EPIC_NUMBER;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validatePassportRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_NAME)) {
				if (request.has(DocumentKYCHelper.KEY_LAST_NAME)) {
					if (request.has(DocumentKYCHelper.KEY_DOB)) {
						if (request.has(DocumentKYCHelper.KEY_DOI)) {
							if (request.has(DocumentKYCHelper.KEY_GENDER)) {
								if (request.has(DocumentKYCHelper.KEY_PASSPORT_NUMBER)) {
									if (request.has(DocumentKYCHelper.KEY_TYPE)) {
										if (request.has(DocumentKYCHelper.KEY_COUNTRY)) {
											
											if (hasMobileNumber(request)) {
												if (isValidMobileNumber(request)) {
													dResponse.isSuccess = true;
													dResponse.message = "Request is valid.";
												} else {
													dResponse.isSuccess = false;
													dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
												}
											} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
											
										} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_COUNTRY;
									} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_TYPE;
								} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_PASSPORT_NUMBER;
							} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_GENDER;
						} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_DOI;
					} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_DOB;
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_LAST_NAME;			
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_NAME;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateGstAuthRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_GST_NUMBER)) {
				
				if (hasMobileNumber(request)) {
					if (isValidMobileNumber(request)) {
						dResponse.isSuccess = true;
						dResponse.message = "Request is valid.";
					} else {
						dResponse.isSuccess = false;
						dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
					}
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
				
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_GST_NUMBER;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}

	
	public static DefaultResponse validateMobileOTPGenerateRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_MOBILE)) {
				if (request.optString(DocumentKYCHelper.KEY_MOBILE, Constants.NA).startsWith("+91")) {
					dResponse.isSuccess = false;
					dResponse.message = "Please enter only 10 digits of mobile number. Do not include +91.";					
				} else {
					dResponse.isSuccess = true;
					dResponse.message = "Request is valid.";
				}
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateMobileOTPAuthRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (request.has(DocumentKYCHelper.KEY_OTP)) {
			if (request.has(DocumentKYCHelper.KEY_KYC_REQUEST_ID)) {
				if (request.has(DocumentKYCHelper.KEY_MOBILE)) {
					dResponse.isSuccess = true;
					dResponse.message = "Request is valid.";
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE;					
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_KYC_REQUEST_ID; 
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_OTP;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateElectricityBillAuthRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_CONSUMER_ID)) {
				if (request.has(DocumentKYCHelper.KEY_SERVICE_PROVIDER)) {
										
					if (hasMobileNumber(request)) {
						if (isValidMobileNumber(request)) {
							dResponse.isSuccess = true;
							dResponse.message = "Request is valid.";
						} else {
							dResponse.isSuccess = false;
							dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
						}
					} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
					
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_SERVICE_PROVIDER; 
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSUMER_ID;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateTelephoneBillAuthRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_TELEPHONE_NUMBER)) {
				
				if (hasMobileNumber(request)) {
					if (isValidMobileNumber(request)) {
						dResponse.isSuccess = true;
						dResponse.message = "Request is valid.";
					} else {
						dResponse.isSuccess = false;
						dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
					}
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
				
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_TELEPHONE_NUMBER;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateLPGIdAuthRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_LPG_ID)) {
				
				if (hasMobileNumber(request)) {
					if (isValidMobileNumber(request)) {
						dResponse.isSuccess = true;
						dResponse.message = "Request is valid.";
					} else {
						dResponse.isSuccess = false;
						dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
					}
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
				
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_LPG_ID;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateEFPUANLookupRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_MOBILE)) {
				if (request.optString(DocumentKYCHelper.KEY_MOBILE, Constants.NA).startsWith("+91")) {
					dResponse.isSuccess = false;
					dResponse.message = "Please enter only 10 digits of mobile number. Do not include +91.";					
				} else {
					dResponse.isSuccess = true;
					dResponse.message = "Request is valid.";
				}
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateEFPUANEmployerLookupRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_UAN)) {
				if (request.has(DocumentKYCHelper.KEY_MOBILE)) {
					if (request.optString(DocumentKYCHelper.KEY_MOBILE, Constants.NA).startsWith("+91")) {
						dResponse.isSuccess = false;
						dResponse.message = "Please enter only 10 digits of mobile number. Do not include +91.";					
					} else {
						dResponse.isSuccess = true;
						dResponse.message = "Request is valid.";
					}
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE;
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_UAN;			
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateITRAuthenticationRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_PAN)) {
				if (request.has(DocumentKYCHelper.KEY_ACK)) {
					dResponse.isSuccess = true;
					dResponse.message = "Request is valid.";
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_ACK;				
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_PAN;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateVehicleRCAuthenticationRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_REG_NUMBER)) {
				
				if (hasMobileNumber(request)) {
					if (isValidMobileNumber(request)) {
						dResponse.isSuccess = true;
						dResponse.message = "Request is valid.";
					} else {
						dResponse.isSuccess = false;
						dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
					}
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
				
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_REG_NUMBER;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateEPFGetOTPRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (hasConsent(request)) {
			if (request.has(DocumentKYCHelper.KEY_UAN)) {
				if (request.has(DocumentKYCHelper.KEY_MOBILE_NO)) {
					dResponse.isSuccess = true;
					dResponse.message = "Request is valid.";
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NO;				
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_UAN;
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_CONSENT;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateEPFGetPassbookRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (request.has(DocumentKYCHelper.KEY_KYC_REQUEST_ID)) {
			if (request.has(DocumentKYCHelper.KEY_OTP)) {					
					dResponse.isSuccess = true;
					dResponse.message = "Request is valid.";								
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_OTP;				
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_KYC_REQUEST_ID;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateDocumentOCRRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (request.has(DocumentKYCHelper.KEY_FRONT_IMAGE_FILE)) {
			if (request.has(DocumentKYCHelper.KEY_FRONT_IMAGE_NAME)) {					
				if (request.has(DocumentKYCHelper.KEY_DOUCMENT_TYPE)) {					
					
					if (hasMobileNumber(request)) {
						if (isValidMobileNumber(request)) {
							dResponse.isSuccess = true;
							dResponse.message = "Request is valid.";
						} else {
							dResponse.isSuccess = false;
							dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
						}
					} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
					
				} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_DOUCMENT_TYPE;								
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_FRONT_IMAGE_NAME;				
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_FRONT_IMAGE_FILE;
		
		return dResponse;
		
	}
	
	public static DefaultResponse validateITRvOCRRequest(JSONObject request) {
		
		DefaultResponse dResponse = new DefaultResponse();
		
		if (request.has(DocumentKYCHelper.KEY_FILE_NAME)) {
			if (request.has(DocumentKYCHelper.KEY_FILE_DATA)) {														
					
					if (hasMobileNumber(request)) {
						if (isValidMobileNumber(request)) {
							dResponse.isSuccess = true;
							dResponse.message = "Request is valid.";
						} else {
							dResponse.isSuccess = false;
							dResponse.message = "Please enter a valid 10 digit mobile number. Do not include +91.";						
						}
					} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_MOBILE_NUMBER;
													
			} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_FILE_DATA;				
		} else dResponse.message = "No value for key: " + DocumentKYCHelper.KEY_FILE_NAME;
		
		return dResponse;
		
	}
	
}
