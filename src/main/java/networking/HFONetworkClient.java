package networking;

import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.BasicUtils;
import utils.Constants;
import utils.LocalHTTPResponse;
import utils.LoggerUtils;
import utils.ProptertyUtils;
import utils.ProptertyUtils.Keys;

public class HFONetworkClient {
	
	private static String CLIENT_ID = null;
	private static String CLIENT_SECRET = null;
	private static String CLIENT_ORG_ID = null;
	private static String SESSION_PASSCODE = null;

	private final static int TIMEOUT = 60;
	private int retryCount = 0;

	public static final String BASE_URL_PROD = "https://one.homefirstindia.com:8443/HomefirstOne"; // TODO: Uncomment for production
	public static final String BASE_URL_UAT = "http://localhost:8080/HomefirstOne";
	
	public OkHttpClient client;

	public enum Endpoints {
		AUTHENTICATE_CLIENT("/V1/client/authenticateClient"),
		ADD_SITE_PHOTOGRAPH("/V1/cs/addSitePhotograph"),
		GET_SITE_PHOTOGRAPH_LIST("/V1/cs/getSitePhotographList"),
		SEND_PAYMENT_LINK("/V1/ds/Payment.sendLink"),
		GENERATE_FAILED_RECEIPT("/V1/ds/Payment.generateFailedReceipt"),
		LOAN_DETAILS("/V1/cs/getLoanDetails"),
		VOICE_CALL("/V1/com/Voice.requestCall");
		
		public final String value;

		Endpoints(String value) {
			this.value = value;
		}
		
		public String getFullUrl() {
			return (Constants.IS_API_IN_PRODUCTION ? BASE_URL_PROD : BASE_URL_UAT) + this.value;
		}

	}
	
	public HFONetworkClient() throws Exception {
		client = new OkHttpClient.Builder().connectTimeout(TIMEOUT, TimeUnit.SECONDS)
				.writeTimeout(TIMEOUT, TimeUnit.SECONDS).readTimeout(TIMEOUT, TimeUnit.SECONDS).build();
		authenticateRMClient();
	}
	
	public HFONetworkClient(int timeout) throws Exception {
		client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS)
				.writeTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout, TimeUnit.SECONDS).build();
		authenticateRMClient();
	}
	
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++ //
	// ++++++++++++ STAR TO COMMON MEHTODS +++++++++++++++++++ //
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++ //

	
	public LocalHTTPResponse GET(String url) throws Exception {

		Request request = new Request.Builder()
				.url(url)
				.method("GET", null)
				.addHeader("Authorization", "Basic " + BasicUtils.getBase64(CLIENT_ID + ":" + CLIENT_SECRET))
				.addHeader("orgId", CLIENT_ORG_ID)
				.addHeader("sessionPasscode", SESSION_PASSCODE).build();

		Response response = client.newCall(request).execute();

		String responseString = response.body().string().toString();
		int responseCode = response.code();
		response.body().close();
		response.close();
		LoggerUtils.log("RMM - HFO GET response code: " + responseCode + " body: " + responseString);

		LocalHTTPResponse localHTTPResponse = new LocalHTTPResponse();
		localHTTPResponse.statusCode = responseCode;
		localHTTPResponse.stringEntity = responseString;

		if (responseCode == 200) {
			
			retryCount = 0;
			localHTTPResponse.isSuccess = true;


		} else if (responseCode == 401) {

			LoggerUtils.log("Unauthorized access while GET.");

			if (retryCount < 3) {

				retryCount++;

				reAuthenticateClient();
				return GET(url);

			} else {

				retryCount = 0;
				localHTTPResponse.isSuccess = false;
				localHTTPResponse.message = "Unauthorized access.";

			}
			
		} else {

			retryCount = 0;
			localHTTPResponse.isSuccess = false;
			localHTTPResponse.message = Constants.DEFAULT_ERROR_MESSAGE;			
			
		}
		
		return localHTTPResponse;

	}
	
	public LocalHTTPResponse POST(String url, JSONObject requestJson) throws Exception {

		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, requestJson.toString());
		Request request = new Request.Builder()
				.url(url)
				.method("POST", body)
				.addHeader("Authorization", "Basic " + BasicUtils.getBase64(CLIENT_ID + ":" + CLIENT_SECRET))
				.addHeader("orgId", CLIENT_ORG_ID)
				.addHeader("sessionPasscode", SESSION_PASSCODE)
				.addHeader("Content-Type", "application/json").build();
		
		Response response = client.newCall(request).execute();

		String responseString = response.body().string().toString();
		int responseCode = response.code();
		response.body().close();
		response.close();
		LoggerUtils.log("RMM - HFO POST response code: " + responseCode + " body: " + responseString);

		LocalHTTPResponse localHTTPResponse = new LocalHTTPResponse();
		localHTTPResponse.statusCode = responseCode;
		localHTTPResponse.stringEntity = responseString;

		if (responseCode == 200) {
			
			retryCount = 0;
			localHTTPResponse.isSuccess = true;

		} else if (responseCode == 401) {

			LoggerUtils.log("Unauthorized access while POST. Retry Count: " + retryCount);

			if (retryCount < 3) {

				retryCount++;

				reAuthenticateClient();
				return POST(url, requestJson);

			} else {

				retryCount = 0;
				localHTTPResponse.isSuccess = false;
				localHTTPResponse.message = "Unauthorized access.";

			}
			
		} else {

			retryCount = 0;
			localHTTPResponse.isSuccess = false;
			localHTTPResponse.message = Constants.DEFAULT_ERROR_MESSAGE;			
			
		}
		
		return localHTTPResponse;

	}
	
	private void reAuthenticateClient() throws Exception {

		SESSION_PASSCODE = null;
		authenticateRMClient();

	}
	
	// ------------------------------------------------------- //
	// ---------------- END OF COMMON METHODS ---------------- //
	// ------------------------------------------------------- //

	private void authenticateRMClient() throws Exception {

		try {

			if (null == CLIENT_ID || null == CLIENT_SECRET || null == CLIENT_ORG_ID || null == SESSION_PASSCODE) {

				if (Constants.IS_API_IN_PRODUCTION) {

					CLIENT_ID = ProptertyUtils.getValurForKey(Keys.HFO_CLIENT_ID_PROD);
					CLIENT_SECRET = ProptertyUtils.getValurForKey(Keys.HFO_CLIENT_SECRET_PROD);
					CLIENT_ORG_ID = ProptertyUtils.getValurForKey(Keys.HFO_ORG_ID_PROD);

				} else {

					CLIENT_ID = ProptertyUtils.getValurForKey(Keys.HFO_CLIENT_ID_TEST);
					CLIENT_SECRET = ProptertyUtils.getValurForKey(Keys.HFO_CLIENT_SECRET_TEST);
					CLIENT_ORG_ID = ProptertyUtils.getValurForKey(Keys.HFO_ORG_ID_TEST);

				}


			} else
				return;

			OkHttpClient client = new OkHttpClient().newBuilder().build();
			Request request = new Request.Builder().url(Endpoints.AUTHENTICATE_CLIENT.getFullUrl())
					.method("GET", null)
					.addHeader("Authorization", "Basic " + BasicUtils.getBase64(CLIENT_ID + ":" + CLIENT_SECRET))
					.addHeader("orgId", CLIENT_ORG_ID)
					.build();
			okhttp3.Response lsResponse = client.newCall(request).execute();
			
			String responseEntity = lsResponse.body().string().toString();

			LoggerUtils.log("authenticateRMClient - Response body: " + responseEntity);

			JSONObject lsJsonResponse = new JSONObject(responseEntity);
			int lsResponseCode = lsResponse.code();
			lsResponse.body().close();
			lsResponse.close();
			LoggerUtils.log("HFO response code: " + lsResponseCode + " body: " + lsJsonResponse.toString());

			if (lsResponseCode == 200) {

				LoggerUtils.log("HFO Client authorized successfully.");

				SESSION_PASSCODE = lsJsonResponse.getString("sessionPasscode");

			} else if (lsResponseCode == 401) {

				LoggerUtils.log("Unauthorized access while authenticateRMClient.");
				throw new Exception("Unauthorized access while authenticateRMClient.");

			} else {

				LoggerUtils.log("Error while authenticateRMClient.");
				throw new Exception("Error while authenticateRMClient.");

			}

		} catch (Exception e) {
			LoggerUtils.log("Error while authenticateRMClient: " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

	}

}
