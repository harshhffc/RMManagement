package models.admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.ColumnDefault;
import org.json.JSONArray;

import databasehelper.ColumnsNFields;
import utils.BasicUtils;
import utils.Constants;
import utils.DateTimeUtils;
import utils.LoggerUtils;
import utils.ProptertyUtils;
import v1.repository.PartnerRepository;
import utils.DateTimeUtils.DateTimeFormat;
import utils.DateTimeUtils.DateTimeZone;

@Entity
@Table(name = "`Partner`")
public class HFPartner {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false, nullable = false)
	public int id = -1;
	public String orgName = Constants.NA;
	public String orgId = Constants.NA;
	public String destination = Constants.NA;
	public String leadSource = Constants.NA;
	public String leadOwner = Constants.NA;
	public String branch = Constants.NA;
	public String clientId = Constants.NA;
	public String clientSecret = Constants.NA;
	
	@ColumnDefault("0")
	public boolean isInternal = false;
	
	@Column(columnDefinition = "JSON", name = "servicesAllowed")
	public String services = Constants.NA;
	
	@ColumnDefault("0")
	public boolean isEnabled = false;
	
	@ColumnDefault("0")
	public boolean ipRestricted = false;
	
	@ColumnDefault("1")
	public boolean sessionEnabled = true;
	
	public String sessionPasscode = Constants.NA;

	@Column(columnDefinition = "DATETIME")
	public String sessionValidDatetime = Constants.NA;

	@Column(columnDefinition = "DATETIME")
	public String sessionUpdateDatetime = Constants.NA;
	
	@Column(columnDefinition = "DATETIME", updatable = false, nullable = false)
	public String createDatetime = DateTimeUtils.getCurrentDateTimeInIST();

	@Column(columnDefinition = "DATETIME")
	public String updateDatetime = DateTimeUtils.getCurrentDateTimeInIST();
	
	@Transient
	public ArrayList<String> servicesAllowed = new ArrayList<String>();
	
	public HFPartner() {}
	
	public HFPartner(ResultSet rs) throws SQLException {
		
		id = rs.getInt(ColumnsNFields.COMMON_KEY_ID);
		orgName = rs.getString(ColumnsNFields.PartnerColumn.ORG_NAME.value);
		orgId = rs.getString(ColumnsNFields.COMMON_KEY_ORG_ID);
		destination = rs.getString(ColumnsNFields.PartnerColumn.DESTINATION.value);
		leadSource = rs.getString(ColumnsNFields.PartnerColumn.LEAD_SOURCE.value);
		leadOwner = rs.getString(ColumnsNFields.PartnerColumn.LEAD_OWNER.value);
		clientId = rs.getString(ColumnsNFields.PartnerColumn.CLIENT_ID.value);
		clientSecret = rs.getString(ColumnsNFields.PartnerColumn.CLIENT_SECRET.value);
		isInternal = rs.getBoolean(ColumnsNFields.PartnerColumn.IS_INTERNAL.value);
		
		services = rs.getString(ColumnsNFields.PartnerColumn.SERVICES_ALLOWED.value);
		if (BasicUtils.isNotNullOrNA(services) && services.startsWith("[")) {
			JSONArray serviceArray = new JSONArray(services);
			for (int i = 0; i < serviceArray.length(); i++)
				servicesAllowed.add(serviceArray.getString(i));
		}
		
		isEnabled = rs.getBoolean(ColumnsNFields.COMMON_KEY_IS_ENABLED);
		ipRestricted = rs.getBoolean(ColumnsNFields.PartnerColumn.IP_RESTRICTED.value);
		sessionEnabled = rs.getBoolean(ColumnsNFields.PartnerColumn.SESSION_ENABLED.value);
		sessionPasscode = rs.getString(ColumnsNFields.COMMON_KEY_SESSION_PASSCODE);
		sessionValidDatetime = rs.getString(ColumnsNFields.COMMON_KEY_SESSION_VALID_DATETIME);
		sessionUpdateDatetime = rs.getString(ColumnsNFields.COMMON_KEY_SESSION_UPDATE_DATETIME);
		createDatetime = rs.getString(ColumnsNFields.COMMON_KEY_CREATEDATETIME);
		updateDatetime = rs.getString(ColumnsNFields.COMMON_KEY_UPDATE_DATETIME);
		
	}
	
	public HFPartner updateServicesAllowed() {
		
		if (BasicUtils.isNotNullOrNA(services) && services.startsWith("[")) {
			JSONArray serviceArray = new JSONArray(services);
			for (int i = 0; i < serviceArray.length(); i++)
				servicesAllowed.add(serviceArray.getString(i));
		}
		
		return this;
		
	}
	
	public boolean isSessionValid() throws ParseException {
		
		if (
				BasicUtils.isNotNullOrNA(sessionPasscode)
				&& BasicUtils.isNotNullOrNA(sessionValidDatetime)
		) {
	
			Date currentDatetime = DateTimeUtils.getDateFromDateTimeString(
					DateTimeUtils.getCurrentDateTimeInIST(), 
					DateTimeFormat.yyyy_MM_dd_HH_mm_ss
				);
			
			Date sessionValidDate = DateTimeUtils.getDateFromDateTimeString(
					sessionValidDatetime, 
					DateTimeFormat.yyyy_MM_dd_HH_mm_ss
				);		
			
			return currentDatetime.before(sessionValidDate);
			
		} else return false;
		
	}
	
	public boolean updateSession(boolean shouldCreateNew) {
		
		try {			
			
			if (shouldCreateNew) {
				
				if (!isSessionValid()) {
					
					sessionPasscode = ProptertyUtils.getKeyBearer()
							.encrypt(orgId + BasicUtils.getRandomKey() + id);
					
				}

			} else {
				
				int minutesLeft = DateTimeUtils.getDateDifferenceInMinutes(sessionValidDatetime);			
				
				if (minutesLeft > 15) {
					LoggerUtils.log("HFPartner - minutes left in session validity : " + minutesLeft + " | Continuing with the same session.");
					return true;
				}
				
			}
			
			sessionUpdateDatetime = DateTimeUtils.getCurrentDateTimeInIST();
			sessionValidDatetime = DateTimeUtils.getDateTimeAddingHours(1, DateTimeFormat.yyyy_MM_dd_HH_mm_ss,
					DateTimeZone.IST);			
			
			boolean status = new PartnerRepository().savePartner(this);
			
			if (status) LoggerUtils.log("Session updated successfully.");
			else LoggerUtils.log("Failed to update session.");
			
			return status;
			
		} catch (Exception e) {		
			LoggerUtils.log("Error while updating partner's session: " + e.getMessage());
			e.printStackTrace();
			return false;
			
		}		
		
	}

}
