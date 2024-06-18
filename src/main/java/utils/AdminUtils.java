package utils;

import java.sql.SQLException;

import databasehelper.AdminDBHelper;
import models.AdminLog;

public class AdminUtils {
	
	private final AdminDBHelper adbHelper;
	
	public AdminUtils() {
		adbHelper = new AdminDBHelper();	
	}
	
	public enum AdminAction {
		ADD("ADD"),
		UPDATE("UPDATE"),
		DELETE("DELETE");
		
		public final String value;
		AdminAction(String value) {
			this.value = value;
		}
	}
	
	public enum RecordType {
		PAYMENT("PaymentInfo");
		
		public final String value;
		RecordType(String value) {
			this.value = value;
		}
	}
	
	public boolean addAdminLog(
			int userId,
			RecordType recordType,
			String recordId,
			AdminAction action,
			String description
	) throws SQLException {
		
		if (userId == -1) {
			LoggerUtils.log("Invalid admin's id while adding admin log: " + userId);
			return false;
		}
		
		AdminLog aLog = new AdminLog();
		aLog.userId = userId;
		aLog.recordType = recordType.value;
		aLog.recordId = recordId;
		aLog.action = action.value;
		aLog.description = description;
		
		try {
			boolean status = adbHelper.addAdminLog(aLog);
			adbHelper.close();
			return status;
		} catch (SQLException e) {
			adbHelper.close();
			throw e;
		}
			
	}

}