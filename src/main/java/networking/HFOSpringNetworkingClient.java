package networking;

import org.json.JSONObject;

import models.Creds;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import utils.BasicUtils;
import utils.Constants;
import utils.Constants.CredType;
import utils.LocalHTTPResponse;
import utils.LoggerUtils;
import v1.repository.CommonRepository;

public class HFOSpringNetworkingClient {
	
	private static Creds _creds = null;
	private final OkHttpClient client;
	private int retryCount = 0;
	private static String SESSION_PASSCODE = null;
	
	public enum Endpoints {

		AUTHENTICATE("/ep/v1/authenticate"),
		GET_LOAN_DETAILS("/ep/v1/Loan.getDetails");

		public final String value;

		Endpoints(String value) {
			this.value = value;
		}
		
		public String getFullUrl() throws Exception {
			return hfoSpringCreds().apiUrl + this.value;
		}

	}
	
	private static void log(String value) {
		LoggerUtils.log("HFOSpringNetworkingClient." + value);
	}
	
	public HFOSpringNetworkingClient() throws Exception {	
		hfoSpringCreds();
		client = new OkHttpClient().newBuilder().build();				
	}
	
	private static Creds hfoSpringCreds() throws Exception {
		
		if (null == _creds || null == SESSION_PASSCODE) {
			_creds = new CommonRepository().findCredsByPartnerName(Constants.PARTNER_HOMEFIRST_ONE_SPRING,
					Constants.IS_PRODUCTION ? CredType.PRODUCTION : CredType.UAT);

			if (null == _creds) {
				log("lmsCreds - failed to get HomefirstOne Creds from DB.");
				throw new Exception("failed to get Homefirst Creds from DB.");
			}

		}
		return _creds;
		
	}
	
	private void authenticate() throws Exception {
		
		hfoSpringCreds();
		
		final var request = new Request.Builder()
				.url(Endpoints.AUTHENTICATE.getFullUrl())
				.method("GET", null)
				.addHeader("Authorization", "Basic " + BasicUtils.getBase64(hfoSpringCreds().username + ":" + hfoSpringCreds().password))
				.addHeader("orgId", hfoSpringCreds().memberPasscode)
				.build();
		okhttp3.Response lsResponse = client.newCall(request).execute();

		final var responseString = lsResponse.body().string().toString();
		log("authenticate - Response string: " + responseString);
		final var lsJsonResponse = new JSONObject(responseString);
		final var lsResponseCode = lsResponse.code();
		
		lsResponse.body().close();
		lsResponse.close();
		
		log("authenticate - Response code: " + lsResponseCode + " Body: " + lsJsonResponse.toString());

		if (lsResponseCode == 200) {

			log("authenticate - Client authorized successfully.");

			SESSION_PASSCODE = lsJsonResponse.getString("sessionPasscode");
			retryCount = 0;

		} else if (lsResponseCode == 401) {
	
			log("authenticate - Unauthorized access.");
			throw new Exception("Unauthorized access while authenticate.");

		} else {
			
			if (retryCount < 3) {
				
				retryCount++;
				
				log("authenticate - Error. retrying...");
				authenticate();
				
			} else {
			
				retryCount = 0;
				log("authenticate - Error.");
				throw new Exception("Error while authenticate.");
				
			}

		}
		
		
	}
	
	private void reAuthenticate() throws Exception {
		_creds = null;
		SESSION_PASSCODE = null;
		authenticate();
	}
	
	public LocalHTTPResponse GET(String url) throws Exception {

		authenticate();
		
		final var request = new Request.Builder()
				.url(url)
				.method("GET", null)
				.addHeader("Authorization", "Basic " + BasicUtils.getBase64(hfoSpringCreds().username + ":" + hfoSpringCreds().password))
				.addHeader("orgId", hfoSpringCreds().memberPasscode)
				.addHeader("sessionPasscode", SESSION_PASSCODE).build();

		final var response = client.newCall(request).execute();
		final var responseString = response.body().string().toString();
		final var responseCode = response.code();
		
		response.body().close();
		response.close();
		
		var responseLog = "GET - response code: " + responseCode;
		
		if (responseCode != 200) {
			responseLog = responseLog + " body: " + responseString;
		}

		final var localHTTPResponse = new LocalHTTPResponse();
		localHTTPResponse.statusCode = responseCode;
		localHTTPResponse.stringEntity = responseString;
		localHTTPResponse.isSuccess = responseCode == 200 ? true : false;
		
		if (responseCode == 200 
				|| responseCode == 201) {
			
			retryCount = 0;
			
			final var jsonResponse = new JSONObject(responseString);
			
			if (jsonResponse.has(Constants.MESSAGE))
				localHTTPResponse.message = jsonResponse.optString(Constants.MESSAGE);

		} else if (responseCode == 401) {

			log("GET - Unauthorized access while GET.");

			if (retryCount < 3) {

				retryCount++;

				reAuthenticate();
				return GET(url);

			} else {

				retryCount = 0;
				localHTTPResponse.message = "Unauthorized access.";

			}
			
		} else {

			retryCount = 0;
			localHTTPResponse.message = Constants.DEFAULT_ERROR_MESSAGE;			
			
		}
		
		return localHTTPResponse;

	}
	
	public LocalHTTPResponse POST(String url, JSONObject requestJson) throws Exception {

		authenticate();
		
		final var mediaType = MediaType.parse("application/json");
		final var body = RequestBody.create(mediaType, requestJson.toString());
		final var request = new Request.Builder()
				.url(url)
				.method("POST", body)
				.addHeader("Authorization", "Basic " + BasicUtils.getBase64(hfoSpringCreds().username + ":" + hfoSpringCreds().password))
				.addHeader("orgId", hfoSpringCreds().memberPasscode)
				.addHeader("sessionPasscode", SESSION_PASSCODE)
				.addHeader("Content-Type", "application/json").build();
		
		final var response = client.newCall(request).execute();
		final var responseString = response.body().string().toString();
		final var responseCode = response.code();
		
		response.body().close();
		response.close();
		
		log("POST - response code: " + responseCode + " body: " + responseString);

		LocalHTTPResponse localHTTPResponse = new LocalHTTPResponse();
		localHTTPResponse.statusCode = responseCode;
		localHTTPResponse.stringEntity = responseString;
		localHTTPResponse.isSuccess = responseCode == 200 ? true : false;

		if (responseCode == 200 
				|| responseCode == 201) {
			
			retryCount = 0;

			final var jsonResponse = new JSONObject(responseString);
			
			if (jsonResponse.has(Constants.MESSAGE))
				localHTTPResponse.message = jsonResponse.optString(Constants.MESSAGE);

		} else if (responseCode == 401) {

			log("POST - Unauthorized access while POST.");

			if (retryCount < 3) {

				retryCount++;

				reAuthenticate();
				return POST(url, requestJson);

			} else {

				retryCount = 0;
				localHTTPResponse.message = "Unauthorized access.";

			}
			
		} else {

			retryCount = 0;
			localHTTPResponse.message = Constants.DEFAULT_ERROR_MESSAGE;			
			
		}
		
		return localHTTPResponse;

	}
	
	public LocalHTTPResponse POST_FORM_DATA(String url, RequestBody requestBody) throws Exception {

		authenticate();
		
		final var request = new Request.Builder()
				.url(url)
				.addHeader("Content-Type", "multipart/form-data")
				.addHeader("Authorization", "Basic " + BasicUtils.getBase64(hfoSpringCreds().username + ":" + hfoSpringCreds().password))
				.addHeader("orgId", hfoSpringCreds().memberPasscode)
				.addHeader("sessionPasscode", SESSION_PASSCODE)
				.post(requestBody)
				.build();
		
		final var response = client.newCall(request).execute();
		final var responseString = response.body().string().toString();
		final var responseCode = response.code();
		
		response.body().close();
		response.close();
		
		log("POST_FORM_DATA - response code: " + responseCode + " body: " + responseString);

		LocalHTTPResponse localHTTPResponse = new LocalHTTPResponse();
		localHTTPResponse.statusCode = responseCode;
		localHTTPResponse.stringEntity = responseString;
		localHTTPResponse.isSuccess = responseCode == 200 ? true : false;
		

		if (responseCode == 200 
				|| responseCode == 201) {
			
			retryCount = 0;

			final var jsonResponse = new JSONObject(responseString);
			
			if (jsonResponse.has(Constants.MESSAGE))
				localHTTPResponse.message = jsonResponse.optString(Constants.MESSAGE);

		} else if (responseCode == 401) {

			log("POST_FORM_DATA - Unauthorized access.");

			if (retryCount < 3) {

				retryCount++;

				reAuthenticate();
				return POST_FORM_DATA(url, requestBody);

			} else {

				retryCount = 0;
				localHTTPResponse.message = "Unauthorized access.";

			}
			
		} else {

			retryCount = 0;
			localHTTPResponse.message = Constants.DEFAULT_ERROR_MESSAGE;			
			
		}
		
		return localHTTPResponse;

	}

}
