package manager;

import org.json.JSONArray;
import org.json.JSONObject;

import databasehelper.RMDatabaseHelper;
import models.DefaultResponse;
import salesforce.SalesForceManager;
import utils.BasicUtils;
import utils.Constants;
import utils.DateTimeUtils;
import utils.LoggerUtils;
import utils.MailUtils;

public class AdvancedTaskManager {

	public AdvancedTaskManager() {}
	
	public class UpdateBankInfoTask implements Runnable {
		
		private JSONObject response = BasicUtils.getFailureTemplateObject();

		@Override
		public void run() {		
			try {
				response = updateBankInfo();
			} catch (Exception e) {
				LoggerUtils.log("Error while updating bank information: " + e.getMessage());
				e.printStackTrace();
				response = BasicUtils.getFailureTemplateObject();
			}
		}
		
		public JSONObject getResponse() {
			return response;
		}
		
	}
	
	private JSONObject updateBankInfo() throws Exception {	
		
		SalesForceManager sfManger = new SalesForceManager();		
		JSONArray perfiosInfo = sfManger.getPerfiosInfo();
		
		RMDatabaseHelper dbHelper = new RMDatabaseHelper();
		
		try {
			
			dbHelper.addBankInfo(perfiosInfo);
			dbHelper.close();
			
			if (Constants.IS_STRICT_PROD_PROCESS_ACTIVE) {
				MailUtils.getInstance().sendDefaultMail(
						"Bank information data update", 
						"Bank information has been successfully updated at "+DateTimeUtils.getCurrentDateTimeInIST(),
						"sanjay.jaiswar@homefirstindia.com",
						"ranan.rodrigues@homefirstindia.com"
				);
			}
			
			DefaultResponse response = new DefaultResponse();
			response.isSuccess = true;
			response.message = "Bank information has been successfully updated.";			
			
			return response.toJson();
			
		} catch (Exception e) {		
			
			dbHelper.close();
			LoggerUtils.log("Error while updating bank info: " + e.getMessage());
			e.printStackTrace();
			
			DefaultResponse response = new DefaultResponse();
			response.isSuccess = false;
			response.message = "Failed to update bank inforamation. Error: " + e.getMessage();
			
			return response.toJson();
		}
		
	}
	
}
