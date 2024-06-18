package models;

import utils.Constants;

public class AdminLog {
	
	public AdminLog() {}
	
	public int id = -1;
	public int userId = -1;
	public String recordType = Constants.NA;
	public String recordId = Constants.NA;
	public String action = Constants.NA;
	public String description = Constants.NA;

}
