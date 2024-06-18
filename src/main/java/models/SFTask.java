package models;

import org.json.JSONObject;

import utils.BasicUtils;
import utils.Constants;

public class SFTask {

	public int id = -1;
    public String sfId = Constants.NA;
    public String objectId = Constants.NA;
    public String objectType = Constants.NA;
    public String objectName = Constants.NA;
    public String subject = Constants.NA;
    public String activityType = Constants.NA;
    public String activityResult = Constants.NA;
    public String activityDescription = Constants.NA;
    public JSONObject recordData = new JSONObject();
    public String taskStatus = Constants.NA;
    public String taskPriority = Constants.NA;
    public String taskDatetime = Constants.NA;
    public boolean isReminderOn = true;
    public String reminderDatetime = Constants.NA;
    public boolean hasFollowUpTask = false;
    public String followUpDateTime = Constants.NA;

    public double latitude = 0.0;
    public double longitude = 0.0;
    public UserAddress address = new UserAddress();

    public String ownerId = Constants.NA;
    public String ownerName = Constants.NA;
    public String createdById = Constants.NA;
    public String createdByName = Constants.NA;
    public String createdDatetime = Constants.NA;
    public String completedDateTime = Constants.NA;
	
    public SFTask() {}
	
	public SFTask(JSONObject json) {
		
		if (null == json) return;
		
		json = BasicUtils.fetchJson(json);
		
		id = json.optInt("id", id);
	    sfId = json.optString("sfId", sfId);
	    objectId = json.optString("objectId", objectId);
	    objectType = json.optString("objectType", objectType);
	    objectName = json.optString("objectName", objectName);
	    subject = json.optString("subject", subject);
	    activityType = json.optString("activityType", activityType);
	    activityResult = json.optString("activityResult", activityResult);
	    activityDescription = json.optString("activityDescription", activityDescription);
	    
	    recordData = json.optJSONObject("recordData");
	    if (null == recordData) recordData = new JSONObject();
	    else recordData = BasicUtils.fetchJson(recordData);
	    
	    taskStatus = json.optString("taskStatus", taskStatus);
	    taskPriority = json.optString("taskPriority", taskPriority);
	    taskDatetime = json.optString("taskDatetime", taskDatetime);
	    isReminderOn = json.optBoolean("isReminderOn", isReminderOn);
	    reminderDatetime = json.optString("reminderDatetime", reminderDatetime);
	    hasFollowUpTask = json.optBoolean("hasFollowUpTask", hasFollowUpTask);
	    followUpDateTime = json.optString("followUpDateTime", followUpDateTime);

	    latitude = json.optDouble("latitude", latitude);
	    longitude = json.optDouble("longitude", longitude);
	    address = new UserAddress(json.optJSONObject("address"));

	    ownerId = json.optString("ownerId", ownerId);
	    ownerName = json.optString("ownerName", ownerName);
	    createdById = json.optString("createdById", createdById);
	    createdByName = json.optString("createdByName", createdByName);
	    createdDatetime = json.optString("createdDatetime", createdDatetime);
	    completedDateTime = json.optString("completedDateTime", completedDateTime);
		
	}
	
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("id", id);
	    json.put("sfId", sfId);
	    json.put("objectId", objectId);
	    json.put("objectType", objectType);
	    json.put("objectName", objectName);
	    json.put("subject", subject);
	    json.put("activityType", activityType);
	    json.put("activityResult", activityResult);
	    json.put("activityDescription", activityDescription);
	    json.put("recordData", recordData);
	    json.put("taskStatus", taskStatus);
	    json.put("taskPriority", taskPriority);
	    json.put("taskDatetime", taskDatetime);
	    json.put("isReminderOn", isReminderOn);
	    json.put("reminderDatetime", reminderDatetime);
	    json.put("hasFollowUpTask", hasFollowUpTask);
	    json.put("followUpDateTime", followUpDateTime);
	    json.put("latitude", latitude);
	    json.put("longitude", longitude);
	    json.put("address", address.toJson());
	    json.put("ownerId", ownerId);
	    json.put("ownerName", ownerName);
	    json.put("createdById", createdById);
	    json.put("createdByName", createdByName);
	    json.put("createdDatetime", createdDatetime);
	    json.put("completedDateTime", completedDateTime);
		
		return json;
		
	}
	
}
