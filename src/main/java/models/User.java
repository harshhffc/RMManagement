package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.json.JSONObject;

import utils.Constants;

@Entity
@Table(name = "`user`")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public int id;

	@Column(name = "sf_user_id", nullable = false)
    public String sfUserId;
	
	@Column(name = "org_id")
    public String orgId;
	
	@Column(name = "first_name")
    public String firstName;
	
	@Column(name = "last_name")
    public String lastName;
	
	@Column(name = "display_name")
    public String displayName; 
	
    public String email;
    public String username;
    
    @Column(name = "mobile_number")
    public String mobileNumber;
    
    @Column(name = "id_url")
    public String idUrl;
    
    @Column(name = "device_id")
    public String deviceId;
    
    @Column(name = "device_type")
    public String deviceType;
    
    @Column(name = "register_datetime", columnDefinition = "DATETIME", updatable = false, nullable = false)
    public String registerDatetime;
    
    @Column(name = "last_login_datetime", columnDefinition = "DATETIME")
    public String lastLoginDatetime;
    
    @Column(name = "update_datetime", columnDefinition = "DATETIME")
    public String updateDatetime;
    
    @Column(name = "session_passcode", nullable = false)
    public String sessionPasscode;
    
    @Transient
    public String profileImageUrl;
	
	public User() {}
	
	public User(JSONObject json) {
		id = json.optInt("id", -1);
	    sfUserId = json.optString("sfUserId", Constants.NA);
	    orgId = json.optString("orgId", Constants.NA);
	    firstName = json.optString("firstName", Constants.NA);
	    lastName = json.optString("lastName", Constants.NA);
	    displayName = json.optString("displayName", Constants.NA);
	    email = json.optString("email", Constants.NA);
	    username = json.optString("username", Constants.NA);
	    mobileNumber = json.optString("mobileNumber",Constants.NA);
	    profileImageUrl = json.optString("profileImageUrl", Constants.NA);
	    idUrl = json.optString("idUrl", Constants.NA);
	    deviceId = json.optString("deviceId", Constants.NA);
	    deviceType = json.optString("deviceType", Constants.NA);
	    registerDatetime = json.optString("registerDatetime", Constants.NA);
	    lastLoginDatetime = json.optString("lastLoginDatetime", Constants.NA);
	    sessionPasscode = json.optString("sessionPasscode", Constants.NA);
	}
	
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("id", id);
	    json.put("sfUserId", sfUserId);
	    json.put("orgId", orgId);
	    json.put("firstName", firstName);
	    json.put("lastName", lastName);
	    json.put("mobileNumber", mobileNumber);
	    json.put("displayName", displayName);
	    json.put("email", email);
	    json.put("username", username);
	    json.put("profileImageUrl", profileImageUrl);
	    json.put("idUrl", idUrl);
	    json.put("deviceId", deviceId);
	    json.put("deviceType", deviceType);
	    json.put("registerDatetime", registerDatetime);
	    json.put("lastLoginDatetime", lastLoginDatetime);
	    json.put("sessionPasscode", sessionPasscode);	    
		return json;
	}
	
	public JSONObject jsonForSearch() {
		JSONObject json = new JSONObject();
		json.put("id", id);
	    json.put("sfUserId", sfUserId);	    
	    json.put("displayName", displayName);
	    json.put("email", email);
	    json.put("username", username);
	    json.put("idUrl", idUrl);
	    json.put("registerDatetime", registerDatetime);
	    json.put("lastLoginDatetime", lastLoginDatetime);	    
		return json;
	}

}
