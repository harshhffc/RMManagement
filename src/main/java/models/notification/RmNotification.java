package models.notification;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.json.JSONObject;

import utils.Constants;
import utils.DateTimeUtils;
import utils.NotificationUtils;

@Entity
@Table(name = "`notification`")
public class RmNotification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	public int id;
	
	@Column(name = "cn_id")
	public String cnId; // cross notification id	
	public String origin;
	public String event;
	
	@Column(name = "title")
	public String title = Constants.NA;
	
	@Column(name = "message")
	public String message = Constants.NA;
	
	@Column(name = "big_message")
	public String bigMessage = Constants.NA;
	
	@Column(name = "image_url")
	public String imageUrl = Constants.NA;
	
	@Column(name = "web_url")
	public String webUrl = Constants.NA;
	
	@Column(name = "should_schedule", columnDefinition = "BOOLEAN default false")
	public boolean shouldSchedule = false;
	
	@Column(name = "audience_type")
	public String audienceType = Constants.NA;
	
	@Column(name = "has_dynamic_contect", columnDefinition = "BOOLEAN default false")
	public boolean hasDynamicContent = false;
	
	@Column(name = "audience_group", columnDefinition = "JSON")
	public String audienceGroup;
	
	@Column(name = "on_click_action")
	public String onClickAction = Constants.NA;
	
	@Column(name = "screen_to_open")
	public String screenToOpen = Constants.NA;
	
	@Column(name = "deeplink")
	public String deeplink = Constants.NA;
	
	@Column(name = "data", columnDefinition = "JSON")
	public String data;
	
	@Column(name = "priority")
	public String priority = Constants.NA;
	
	@Column(name = "kind")
	public String kind = NotificationUtils.NotificationKind.TRANSACTIONAL.value;
	
	@Transient
	public boolean hasRead = false;
	
	@Column(name = "is_scheduled", columnDefinition = "BOOLEAN default false")
	public boolean isScheduled = false;	
    
	@Column(name = "create_datetime", columnDefinition = "DATETIME", updatable = false, nullable = false)
	public String createDatetime = DateTimeUtils.getCurrentDateTimeInIST();
	
	@Column(name = "update_datetime", columnDefinition = "DATETIME")
	public String updateDatetime = DateTimeUtils.getCurrentDateTimeInIST();
	
	@Column(name = "schedule_datetime", columnDefinition = "DATETIME")
	public String datetime; // SCHEDULE DATETIME
	
	@Column(name = "sent_datetime", columnDefinition = "DATETIME")
	public String sentDatetime;
	
	@Column(name = "platform")
	public String platform;
	
	@Column(name = "schedule_type")
	public String scheduleType;
		
	@Column(name = "total_count")
	public int totalCount = 0;
	
	@Column(name = "success_count")
	public int successCount = 0;
	
	@Column(name = "failure_count")
	public int failureCount = 0;
		
	@Column(name = "scheduler_id")
	public int schedulerId = 0;
		
	@Column(name = "scheduler_name")
	public String schedulerName;
	
	@Column(name = "is_valid", columnDefinition = "BOOLEAN default true")
	public boolean isValid = true;
	
	@Transient
	public String file;
	
	@Transient
	public String imageFile;
	
	@Transient
	public String thumbnailFile;
	
	@Column(name = "thumbnail_url")
	public String thumbnailUrl = Constants.NA;
	
    public RmNotification() {}
    
    public RmNotification(JSONObject json) {
    
    	id = json.optInt("id", id);
    	title = json.optString("title", title);
        message = json.optString("message", message);
        bigMessage = json.optString("bigMessage", bigMessage);
        imageUrl = json.optString("imageUrl", imageUrl);
        datetime = json.optString("datetime", datetime);
        webUrl = json.optString("webUrl", webUrl);
        shouldSchedule = json.optBoolean("shouldSchedule", shouldSchedule);
        audienceType = json.optString("audienceType", audienceType);
        onClickAction = json.optString("onClickAction", onClickAction);
        deeplink = json.optString("deeplink", deeplink);
        screenToOpen = json.optString("screenToOpen", screenToOpen);
        thumbnailUrl = json.optString("thumbnailUrl", thumbnailUrl);
        
        data = json.optString("data");
        audienceGroup = json.optString("audienceGroup"); 

        priority = json.optString("priority", priority);
        kind = json.optString("kind", kind);
    	
    }
    
    public JSONObject toJson() {
    	
    	JSONObject json = new JSONObject();
    	
    	json.put("id", id);
    	json.put("title", title);
        json.put("message", message);
        json.put("bigMessage", bigMessage);
        json.put("imageUrl", imageUrl);
        json.put("datetime", datetime);
        json.put("webUrl", webUrl);
        json.optBoolean("shouldSchedule", shouldSchedule);
        json.put("audienceType", audienceType);
        json.put("audienceGroup", audienceGroup);
        json.put("onClickAction", onClickAction);
        json.put("screenToOpen", screenToOpen);
        json.put("deeplink", deeplink);
        json.put("data", data);
        json.put("priority", priority);
        json.put("kind", kind);
    	json.put("hasRead", hasRead);
    	json.put("thumbnailUrl", thumbnailUrl);
    	
    	return json;
    	
    }

	public RmNotification asCopy() {

		RmNotification copy = new RmNotification();

		copy.id = id;
		copy.title = title;
		copy.message = message;
		copy.bigMessage = bigMessage;
		copy.imageUrl = imageUrl;
		copy.datetime = datetime;
		copy.webUrl = webUrl;
		copy.shouldSchedule = shouldSchedule;
		copy.audienceType = audienceType;
		copy.audienceGroup = audienceGroup;
		copy.onClickAction = onClickAction;
		copy.screenToOpen = screenToOpen;
		copy.deeplink = deeplink;
		copy.data = data;
		copy.priority = priority;
		copy.kind = kind;
		copy.hasRead = hasRead;
		copy.thumbnailUrl = thumbnailUrl;

		return copy;

	}

}
