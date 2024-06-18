package v2.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import models.SFLead;
import utils.Constants;
import utils.LoggerUtils;
import utils.ProptertyUtils;

public class SalesForceManager {

	private static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
	private static String REST_ENDPOINT = "/services/data";
	private static String API_VERSION = "/v47.0";
	private static String baseUri = null;
	private static String instanceUri = null;
	private static Header oauthHeader = null;
	private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
	private boolean isToolingApiCall = false;
	private static String BANK_PICKER_LIST_OBJECT_ID = "0Nt900000008OQJCA2";

	private int retryCount = 0;

	public SalesForceManager() {
		isToolingApiCall = false;
	}

	public SalesForceManager(boolean isToolingApiCall) {
		this.isToolingApiCall = isToolingApiCall;
	}
	// ================== START OF COMMON CODES ====================== //
	// =============================================================== //

	private void checkAndAuthenticate() throws Exception {

		if (null == baseUri || null == oauthHeader)
			authenticate();

	}

	public void authenticate() throws Exception {

		HttpClient httpclient = HttpClientBuilder.create().build();

		String loginUrl = Constants.NA, clientId = Constants.NA, clientSecret = Constants.NA, username = Constants.NA,
				password = Constants.NA;

		if (Constants.IS_SF_IN_PRODUCTION) {
			loginUrl = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PROD_LOGIN_URL);
			clientId = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PROD_CLIENT_ID);
			clientSecret = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PROD_CLIENT_SECRET);
			username = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PROD_USERNAME);
			password = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PROD_PASSWORD);
		} else {
			if (Constants.IS_SF_PREPROD) {
				loginUrl = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PREPROD_LOGIN_URL);
				clientId = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PREPROD_CLIENT_ID);
				clientSecret = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PREPROD_CLIENT_SECRET);
				username = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PREPROD_USERNAME);
				password = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_PREPROD_PASSWORD);
			} else {
				loginUrl = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_TEST_LOGIN_URL);
				clientId = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_TEST_CLIENT_ID);
				clientSecret = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_TEST_CLIENT_SECRET);
				username = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_TEST_USERNAME);
				password = ProptertyUtils.getValurForKey(ProptertyUtils.Keys.SF_TEST_PASSWORD);
			}
		}

		// Assemble the login request URL
		String loginURL = loginUrl + GRANTSERVICE + "&client_id=" + clientId + "&client_secret=" + clientSecret
				+ "&username=" + username + "&password=" + password;

		// LoggerUtils.log(loginURL);

		// Login requests must be POSTs
		HttpPost httpPost = new HttpPost(loginURL);
		HttpResponse response = null;

		try {
			// Execute the login POST request
			response = httpclient.execute(httpPost);
		} catch (ClientProtocolException cpException) {
			cpException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		// verify response is HTTP OK
		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			LoggerUtils
					.log("Error authenticating to Force.com: " + statusCode + " | error: " + response.getStatusLine());
			// Error is in EntityUtils.toString(response.getEntity())
			return;
		}

		String getResult = null;
		try {
			getResult = EntityUtils.toString(response.getEntity());
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		JSONObject jsonObject = null;
		String loginAccessToken = null;
		String loginInstanceUrl = null;
		try {
			jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();

			LoggerUtils.log(" received data: " + jsonObject.toString());

			loginAccessToken = jsonObject.getString("access_token");
			loginInstanceUrl = jsonObject.getString("instance_url");
		} catch (JSONException jsonException) {
			jsonException.printStackTrace();
		}

		instanceUri = loginInstanceUrl;
		baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION;
		oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken);

		System.out.println(response.getStatusLine());
		System.out.println("Successful login");
		System.out.println("  instance URL: " + loginInstanceUrl);
		System.out.println("  access token/session ID: " + loginAccessToken);
		System.out.println("baseUri: " + baseUri);

		// release connection
		httpPost.releaseConnection();

	}

	private JSONObject getSalesforceData(String query) {

		try {

			System.out.println("----> SalesForce Query: " + query);

			// Set up the HTTP objects needed to make the request.
			HttpClient httpClient = HttpClientBuilder.create().build();

			String uri = "";
			if (isToolingApiCall) {
				uri = baseUri + query;
			} else {
				uri = baseUri + "/query?q=" + query;
			}

			System.out.println("Query URL: " + uri);
			HttpGet httpGet = new HttpGet(uri);
			// System.out.println("oauthHeader2: " + oauthHeader);
			httpGet.addHeader(oauthHeader);
			httpGet.addHeader(prettyPrintHeader);

			// Make the request.
			HttpResponse response = httpClient.execute(httpGet);

			// Process the result
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				retryCount = 0;
				String response_string = EntityUtils.toString(response.getEntity());
				try {

					JSONObject json = new JSONObject(response_string);
					System.out.println("JSON result of Query:\n" + json.toString());
					return json;

				} catch (JSONException je) {
					je.printStackTrace();
					return null;
				}
			} else if (statusCode == 401 && retryCount < 3) {
				System.out.println("Query was unsuccessful. Access token was expired: " + statusCode);
				baseUri = null;
				oauthHeader = null;
				retryCount++;
				authenticate();
				return getSalesforceData(query);
			} else {
				retryCount = 0;
				System.out.println("Query was unsuccessful. Status code returned is " + statusCode);
				System.out.println("An error has occured. Http status: " + response.getStatusLine().getStatusCode());
				System.out.println(getBody(response.getEntity().getContent()));
				// System.exit(-1);
				return null;
			}

		} catch (Exception e) {
			LoggerUtils.log("error while getting data from salesforce: " + e.toString());
			e.printStackTrace();
			return null;
		}

	}

	private JSONObject getSalesforceMoreData(String nextRecordsUrl) {
		try {

			// Set up the HTTP objects needed to make the request.
			HttpClient httpClient = HttpClientBuilder.create().build();

			// String uri = baseUri +
			// nextRecordsUrl.substring(nextRecordsUrl.indexOf("/query/"),
			// nextRecordsUrl.length());
			String uri = instanceUri + nextRecordsUrl;

			// System.out.println("Query URL: " + uri);
			HttpGet httpGet = new HttpGet(uri);
			// System.out.println("oauthHeader2: " + oauthHeader);
			httpGet.addHeader(oauthHeader);
			httpGet.addHeader(prettyPrintHeader);

			// Make the request.
			HttpResponse response = httpClient.execute(httpGet);
			// Process the result
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				retryCount = 0;
				String response_string = EntityUtils.toString(response.getEntity());
				try {

					JSONObject json = new JSONObject(response_string);
					System.out.println("JSON result of More Query:\n" + json.toString());
					return json;

				} catch (JSONException je) {
					je.printStackTrace();
					return null;
				}
			} else if (statusCode == 401 && retryCount < 3) {
				System.out.println("More Query was unsuccessful. Access token was expired: " + statusCode);
				baseUri = null;
				oauthHeader = null;
				retryCount++;
				authenticate();
				return getSalesforceMoreData(nextRecordsUrl);
			} else {
				retryCount = 0;
				System.out.println("More Query was unsuccessful. Status code returned is " + statusCode);
				System.out.println("An error has occured. Http status: " + response.getStatusLine().getStatusCode());
				System.out.println(getBody(response.getEntity().getContent()));
				// System.exit(-1);
				return null;
			}

		} catch (Exception e) {
			LoggerUtils.log("error while getting More data from salesforce: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	private String getModifiedQuery(String query) throws UnsupportedEncodingException {
		// return query.replace(" ", "+");
		return URLEncoder.encode(query, "UTF-8");
	}

	private static String getBody(InputStream inputStream) {
		String result = "";
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				result += inputLine;
				result += "\n";
			}
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return result;
	}

	// ******************* END OF COMMON CODES **************************** //
	// ******************************************************************** //

	public JSONObject getUserLeadCount(String ownerId, String date) throws Exception {

		checkAndAuthenticate();

		JSONObject responseJson = new JSONObject();

		String fromDate = date.concat("T00:01:00.000Z");
		String toDate = date.concat("T23:59:00.000Z");
		int convertedCount = 0;
		int createdCount = 0;
		String query = "SELECT Id , IsConverted FROM Lead " + "where OwnerId = '" + ownerId + "' and CreatedDate > "
				+ fromDate + " and CreatedDate < " + toDate;

		JSONObject countJson = getSalesforceData(getModifiedQuery(query));

		if (null == countJson)
			return null;

		if (countJson.getInt("totalSize") > 0) {
			JSONArray records = countJson.getJSONArray("records");
			for (int i = 0; i < records.length(); i++) {
				JSONObject current = records.getJSONObject(i);
				Boolean isConverted = current.optBoolean("IsConverted", false);
				if (isConverted) {
					convertedCount = convertedCount + 1;
				} else {
					createdCount = createdCount + 1;

				}

			}

		}

		responseJson.put("sfLeadsConverted", convertedCount);
		responseJson.put("sfLeadsCreated", createdCount);

		return responseJson;
	}

	// ====================== ADMIN PRO IMPLEMENTATION ====================== //
	// ====================================================================== //

	public ArrayList<SFLead> getLeadStatForDashboard(String datetime) throws Exception {

		checkAndAuthenticate();

		String query = "SELECT Id,IsConverted,CreatedById FROM Lead WHERE CreatedDate >= " + datetime
				+ " AND LeadSource not in ('Youtube','TransUnion','Credit Mantri','Strategic Alliances',"
				+ "'Robo Silicon','Credit Sudhaar','Paisa Bazar','Affiliate','Chat Bot','Facebook',"
				+ "'Organic','Google','Digital Referal Partner','Website','GDN Ads','Social Media',"
				+ "'Customer Referral') and CreatedById not in ('00590000001Bc4PAAS','00590000002IVW8AAO',"
				+ "'0059000000UuFAEAA3','00590000005SE67AAG','0059000000UuI7XAAV','0059000000TnwQW','00590000006VHPK')";

		JSONObject leadJson = getSalesforceData(getModifiedQuery(query));

		if (null == leadJson)
			return null;

		ArrayList<SFLead> leads = new ArrayList<>();

		if (leadJson.getInt("totalSize") > 0) {

			JSONArray leadArray = leadJson.getJSONArray("records");

			for (int i = 0; i < leadArray.length(); i++) {

				JSONObject current = leadArray.getJSONObject(i);
				SFLead lead = new SFLead();
				lead.sfId = current.getString("Id");
				lead.isConverted = current.getBoolean("IsConverted");
				lead.sfCreatedById = current.getString("CreatedById");

				leads.add(lead);

			}

		}

		if (!leadJson.getBoolean("done")) {
			getMoreLeads(leads, leadJson.getString("nextRecordsUrl"));
		}

		return leads;

	}

	private void getMoreLeads(ArrayList<SFLead> leads, String nextRecordsUrl) {

		JSONObject leadJson = getSalesforceMoreData(nextRecordsUrl);

		if (leadJson.getInt("totalSize") > 0) {

			JSONArray leadArray = leadJson.getJSONArray("records");

			for (int i = 0; i < leadArray.length(); i++) {

				JSONObject current = leadArray.getJSONObject(i);
				SFLead lead = new SFLead();
				lead.sfId = current.getString("Id");
				lead.isConverted = current.getBoolean("IsConverted");
				lead.sfCreatedById = current.getString("CreatedById");

				leads.add(lead);

			}

		}

		if (!leadJson.getBoolean("done")) {
			getMoreLeads(leads, leadJson.getString("nextRecordsUrl"));
		}

	}

	// ******************* END OF ADMIN PRO IMPLEMENTATION ****************** //
	// ********************************************************************** //

	public JSONArray getAllSfUsers() throws Exception {

		checkAndAuthenticate();

		String userSfDataQuery = "Select Name " + ",Official_Email_ID__c " + ",Mobile_Number__c "+ ", Current_Location__r.Name "
				+ ", Designation__c, Current_Location__r.Region__r.Name " + "from Employee__c "
				+ "where Official_Email_ID__c != '' and Official_Email_ID__c != null and"
				+ " (Employee_Status__c = 'Active' " + "OR Employee_Status__c = 'Notice Period' "
				+ "OR Employee_Status__c =  'Leave Without Pay')";

		JSONObject regionMapData = getSalesforceData(getModifiedQuery(userSfDataQuery));

		if (null != regionMapData && regionMapData.getInt("totalSize") > 0) {
			return regionMapData.getJSONArray("records");
		}

		return new JSONArray();

	}

	public JSONArray getUsersWithBirthday(String day, String month) throws Exception {

		checkAndAuthenticate();

		String query = "Select Name ,Official_Email_ID__c ,Date_of_Birth__c,Designation__c from Employee__c where "
				+ "Official_Email_ID__c != ''" + "and Official_Email_ID__c != null "
				+ "and CALENDAR_MONTH(Date_of_Birth__c) = " + month + "and  DAY_IN_MONTH(Date_of_Birth__c) = " + day
				+ "and (Employee_Status__c = 'Active' OR Employee_Status__c = 'Notice Period' OR Employee_Status__c = 'Leave Without Pay')";

		JSONObject sfResponseJson = getSalesforceData(getModifiedQuery(query));

		if (null != sfResponseJson && sfResponseJson.getInt("totalSize") > 0) {
			return sfResponseJson.getJSONArray("records");
		}

		return new JSONArray();

	}

	public JSONObject getBankPickList() throws Exception {

		checkAndAuthenticate();

		String query = "/tooling/sobjects/GlobalValueSet/" + BANK_PICKER_LIST_OBJECT_ID; 

		return getSalesforceData(query);

	}

	
	public JSONObject customerLookUp(JSONObject requestObject) throws Exception {

		checkAndAuthenticate();

		String searchString = requestObject.optString("searchString", Constants.NA);
		
		String query =  "SELECT " +
				"Name," +
                "Opportunity__r.Name," +
                "Opportunity__r.Primary_Contact__r.Email," +
                "Opportunity__r.Primary_Contact__r.MobilePhone," +
                "Opportunity__r.Primary_Contact__r.MailingCity," +
                "Opportunity__r.Primary_Contact__r.Gender__c," +
                "Opportunity__r.Id," +
                "Opportunity__r.AccountId," +
                "Opportunity__r.Primary_Contact__c," +
                "Opportunity__r.CRM_Account_No__c," +
                "Opportunity__r.OwnerId," +
                "Opportunity__r.Opportunity_No__c," +
                "Opportunity__r.StageName," +
                "Opportunity__r.Opportunity_Branch_New__r.Name," +
                "Opportunity__r.CL_Contract_No_LAI__c," +
                "loan__Loan_Product_Name__c,"+
                "loan__Loan_Status__c,"+
                "Sub_Product_Type__c,"+
                "X_Sell_Products__c "+
                " FROM loan__Loan_Account__c " +
                " WHERE Opportunity__r.Name LIKE '%"+ searchString+"%'" +
                " OR Name LIKE '%"+ searchString+"%'" +
                " OR Opportunity__r.Primary_Contact__r.MobilePhone = '"+ searchString+"'" +
                " ORDER BY Opportunity__r.CreatedDate DESC";

		
		return getSalesforceData(getModifiedQuery(query));

	}

	public JSONObject customerOpportunityLookUp(JSONObject requestObject) throws Exception {

		checkAndAuthenticate();

		String searchString = requestObject.optString("searchString", Constants.NA);

		String hlQuery = "SELECT " +
                "Name," +
                "Primary_Contact__r.Email," +
                "Primary_Contact__r.MobilePhone," +
                "Primary_Contact__r.MailingCity," +
                "Primary_Contact__r.Gender__c," +
                "Id," +
                "AccountId," +
                "Primary_Contact__c," +
                "CRM_Account_No__c," +
                "OwnerId," +
                "Opportunity_No__c," +
                "StageName," +
                "Opportunity_Branch_New__r.Name," +
                "CL_Contract_No_LAI__c" +
                " FROM Opportunity" +
                " WHERE StageName != '0 - Declined' AND (" + 
                "Name LIKE '%"+ searchString+"%'" +
                " OR CL_Contract_No_LAI__c LIKE '%"+ searchString+"%'" +
                " OR Opportunity_No__c LIKE '%"+searchString+"%'"+
                " OR Primary_Contact__r.MobilePhone = '"+ searchString+"')" +
                " ORDER BY CreatedDate DESC";
		
		
		return getSalesforceData(getModifiedQuery(hlQuery));

	}
	
	public JSONArray getUserDetails(String userIds) throws Exception {

		checkAndAuthenticate();

		String query = "Select Id, Name, MobilePhone, Phone from User where Id " + "IN (" + userIds + ")";

		JSONObject sfResponseJson = getSalesforceData(getModifiedQuery(query));

		if (null != sfResponseJson && sfResponseJson.getInt("totalSize") > 0) {
			return sfResponseJson.getJSONArray("records");
		}

		return new JSONArray();

	}
	
	public String getUserDetail(String sfUserId) throws Exception {

		checkAndAuthenticate();

		String mobileNumer = Constants.NA;

		String query = "Select Id, Name, MobilePhone, Phone from User where Id = '" + sfUserId + "'";

		JSONObject sfResponseJson = getSalesforceData(getModifiedQuery(query));

		if (null != sfResponseJson && sfResponseJson.getInt("totalSize") > 0) {
			JSONObject record = sfResponseJson.getJSONArray("records").getJSONObject(0);
			return mobileNumer = record.optString("MobilePhone");

		}

		return mobileNumer;

	}

}
