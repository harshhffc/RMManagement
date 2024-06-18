package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import utils.Constants;

@Entity
@Table(name = "`user_secondary_info`")
public class SecondaryInfo {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int id;
	
	@Column(name="user_id")
    public int userId;
	
	@Column(name="device_type")
    public String deviceType;
	
	@Column(name="device_id")
    public String deviceId;
	
	@Column(name="apns_key", columnDefinition = "JSON")
    public String apnsKey = Constants.NA;
	
	@Column(name="fcm_key", columnDefinition = "JSON")
    public String fcmKey = Constants.NA;
	
	
	@Column(name="app_version")
    public String appVersion;
	
	public SecondaryInfo() {}

}
